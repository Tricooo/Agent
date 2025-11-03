package com.tricoq.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.enums.AiClientAdvisorTypeEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientAdvisorVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        List<AiClientAdvisorVO> advisors = dynamicContext.getValue(dataName());

        if (CollectionUtils.isEmpty(advisors)) {
            log.warn("没有需要被初始化的 ai client advisor");
            return router(requestParam, dynamicContext);
        }

        for (AiClientAdvisorVO advisorVO : advisors) {
            Advisor advisor = createClientAdvisor(advisorVO);
            registerBean(beanName(advisorVO.getAdvisorId()), Advisor.class, advisor);
        }
        return router(requestParam, dynamicContext);
    }

    private Advisor createClientAdvisor(AiClientAdvisorVO advisorVO) {
        String advisorType = advisorVO.getAdvisorType();
        AiClientAdvisorTypeEnumVO vo = AiClientAdvisorTypeEnumVO.getByCode(advisorType);
        return vo.createAdvisor(advisorVO, vectorStore);
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
