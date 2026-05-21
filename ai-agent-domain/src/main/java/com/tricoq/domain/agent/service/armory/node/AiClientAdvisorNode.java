package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.enums.AiClientAdvisorTypeEnumVO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.agent.service.rag.rerank.DocumentReranker;
import com.tricoq.domain.agent.service.rag.rerank.factory.DocumentRerankerFactory;
import com.tricoq.domain.agent.service.rag.rewrite.QueryRewriter;
import com.tricoq.domain.agent.service.rag.rewrite.enums.RewritePolicy;
import com.tricoq.domain.agent.service.rag.rewrite.factory.QueryRewriterFactory;
import com.tricoq.domain.agent.service.rag.rewrite.model.RewriteContext;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author trico qiang
 * @date 10/28/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiClientAdvisorNode extends AbstractArmorySupport {

    private final AiClientNode clientNode;

    private final VectorStore vectorStore;

    protected final DocumentRerankerFactory documentRerankerFactory;

    protected final QueryRewriterFactory queryRewriterFactory;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，Advisor 顾问角色{}", JSON.toJSONString(requestParam));

        List<AiClientAdvisorDTO> advisors = dynamicContext.getAdvisorConfigs();

        if (CollectionUtils.isEmpty(advisors)) {
            log.warn("没有需要被初始化的 ai client advisor");
            return router(requestParam, dynamicContext);
        }

        for (AiClientAdvisorDTO advisorVO : advisors) {
            Advisor advisor = createClientAdvisor(advisorVO, dynamicContext);
            registerBean(beanName(advisorVO.getAdvisorId()), Advisor.class, advisor);
        }
        return router(requestParam, dynamicContext);
    }

    private Advisor createClientAdvisor(AiClientAdvisorDTO advisorVO,
                                        DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        AiClientAdvisorTypeEnumVO vo = AiClientAdvisorTypeEnumVO.getByCode(advisorVO.getAdvisorType());
        AiClientAdvisorDTO.RagAnswer ragAnswer = advisorVO.getRagAnswer();
        if (vo != AiClientAdvisorTypeEnumVO.RAG_ANSWER || ragAnswer == null) {
            return vo.createAdvisor(advisorVO, vectorStore);
        }

        DocumentReranker reranker = documentRerankerFactory.getDocumentReranker(
                ragAnswer.getRerankPolicy()
        );

        RewriteContext.RewriteContextBuilder rewriteContextBuilder = RewriteContext.builder();
        if (RewritePolicy.LLM_MULTI_QUERY.getPolicyName().equals(ragAnswer.getRewritePolicy())) {
            String queryRewriterClientId = ragAnswer.getQueryRewriterClientId();
            if (StringUtils.isBlank(queryRewriterClientId)) {
                throw new IllegalArgumentException("Query rewriter client id is empty");
            }
            AiClientDTO queryRewriterClient = dynamicContext.getClientMap().get(queryRewriterClientId);
            if (queryRewriterClient == null) {
                throw new IllegalArgumentException("Query rewriter client is not loaded: " + queryRewriterClientId);
            }
            rewriteContextBuilder.extraClientId(queryRewriterClientId);
            rewriteContextBuilder.queryRewriteDomainHints(ragAnswer.getQueryRewriteDomainHints());
        }

        QueryRewriter queryRewriter = queryRewriterFactory.getQueryRewriter(
                ragAnswer.getRewritePolicy(), rewriteContextBuilder.build()
        );
        return vo.createAdvisor(advisorVO, vectorStore, reranker, queryRewriter);
    }


    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return clientNode;
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName();
    }
}
