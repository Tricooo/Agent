package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.adapter.repository.IKnowledgeBaseProfileRepository;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.enums.AiClientAdvisorTypeEnumVO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.agent.service.rag.profile.model.KnowledgeBaseProfile;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author trico qiang
 * @date 10/28/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiClientAdvisorNode extends AbstractArmorySupport {

    private static final Pattern KNOWLEDGE_FILTER_VALUE_PATTERN = Pattern.compile("'([^']+)'");

    private final AiClientNode clientNode;

    private final VectorStore vectorStore;

    protected final DocumentRerankerFactory documentRerankerFactory;

    protected final QueryRewriterFactory queryRewriterFactory;

    protected final IKnowledgeBaseProfileRepository knowledgeBaseProfileRepository;

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
            if (ragAnswer.isQueryRewriteProfileEnabled()) {
                QueryRewriteProfileContext profileContext = queryRewriteProfileContext(ragAnswer);
                rewriteContextBuilder.knowledgeBaseProfileHints(profileContext.profileHints());
                rewriteContextBuilder.profileVersion(profileContext.profileVersion());
            }
            rewriteContextBuilder.profileHintTopN(ragAnswer.getQueryRewriteProfileHintTopN());
        }

        QueryRewriter queryRewriter = queryRewriterFactory.getQueryRewriter(
                ragAnswer.getRewritePolicy(), rewriteContextBuilder.build()
        );
        return vo.createAdvisor(advisorVO, vectorStore, reranker, queryRewriter);
    }

    private QueryRewriteProfileContext queryRewriteProfileContext(AiClientAdvisorDTO.RagAnswer ragAnswer) {
        List<String> knowledgeTags = knowledgeTags(ragAnswer.getFilterExpression());
        if (CollectionUtils.isEmpty(knowledgeTags)) {
            return QueryRewriteProfileContext.empty();
        }
        List<KnowledgeBaseProfile> profiles = knowledgeBaseProfileRepository.queryByKnowledgeTags(knowledgeTags);
        if (CollectionUtils.isEmpty(profiles)) {
            log.info("RAG知识库画像未命中: knowledgeTags={}", knowledgeTags);
            return QueryRewriteProfileContext.empty();
        }
        List<ProfileHint> profileHints = profiles.stream()
                .filter(Objects::nonNull)
                .filter(profile -> !CollectionUtils.isEmpty(profile.getHints()))
                .flatMap(profile -> profile.getHints().stream())
                .filter(Objects::nonNull)
                .toList();
        String profileVersion = profiles.stream()
                .filter(Objects::nonNull)
                .map(KnowledgeBaseProfile::getProfileVersion)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        log.info("RAG知识库画像加载完成: knowledgeTags={}, profiles={}, hints={}",
                knowledgeTags, profiles.size(), profileHints.size());
        return new QueryRewriteProfileContext(profileHints, profileVersion);
    }

    private record QueryRewriteProfileContext(List<ProfileHint> profileHints, String profileVersion) {

        private static QueryRewriteProfileContext empty() {
            return new QueryRewriteProfileContext(List.of(), "");
        }
    }

    private List<String> knowledgeTags(String filterExpression) {
        if (StringUtils.isBlank(filterExpression) || !StringUtils.containsIgnoreCase(filterExpression, "knowledge")) {
            return List.of();
        }
        Set<String> knowledgeTags = new LinkedHashSet<>();
        Matcher matcher = KNOWLEDGE_FILTER_VALUE_PATTERN.matcher(filterExpression);
        while (matcher.find()) {
            String knowledgeTag = StringUtils.trimToEmpty(matcher.group(1));
            if (StringUtils.isNotBlank(knowledgeTag)) {
                knowledgeTags.add(knowledgeTag);
            }
        }
        return List.copyOf(knowledgeTags);
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
