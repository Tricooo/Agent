package com.tricoq.domain.agent.service.execute;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.service.execute.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RootExecuteNode extends AbstractExecuteSupport {

    private final IAgentRepository agentRepository;

    private final Step1AnalyzeNode step1AnalyzeNode;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        log.info("=== 动态多轮执行测试开始 ====");
        log.info("用户输入: {}", requestParam.getUserInput());
        log.info("最大执行步数: {}", requestParam.getMaxSteps());
        log.info("会话ID: {}", requestParam.getSessionId());

        Map<String, AiAgentClientFlowConfigVO> configs = agentRepository.queryAiAgentFlowConfigByAgentId(requestParam.getAgentId());
        dynamicContext = DefaultExecuteStrategyFactory.ExecuteContext.builder()
                .originalUserInput(requestParam.getUserInput())
                .currentTask(requestParam.getUserInput())
                .flowConfigMap(configs)
                .maxStep(requestParam.getMaxSteps())
                .executionHistory(new StringBuilder())
                .build();
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> get(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        return step1AnalyzeNode;
    }
}
