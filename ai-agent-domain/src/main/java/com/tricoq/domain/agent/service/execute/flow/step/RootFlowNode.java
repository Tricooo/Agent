package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RootFlowNode extends AbstractExecuteSupport {

    private final IAgentRepository agentRepository;

    private final Step1McpToolsAnalysisNode step1McpToolsAnalysisNode;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("=== 流程执行开始 ====");
        log.info("用户输入: {}", requestParam.getUserInput());
        log.info("最大执行步数: {}", requestParam.getMaxSteps());
        log.info("会话ID: {}", requestParam.getSessionId());

        Map<String, AiAgentClientFlowConfigVO> configMap = agentRepository
                .queryAiAgentFlowConfigByAgentId(requestParam.getAgentId());
        if (configMap.isEmpty()) {
            throw new IllegalArgumentException("agent未配置");
        }
        dynamicContext.getConfigMap().putAll(configMap);
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step1McpToolsAnalysisNode;
    }
}
