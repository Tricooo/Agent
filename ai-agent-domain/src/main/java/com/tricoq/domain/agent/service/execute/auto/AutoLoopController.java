package com.tricoq.domain.agent.service.execute.auto;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.LoopResultDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.auto.context.AutoExecuteContext;
import com.tricoq.domain.agent.service.execute.auto.context.AutoLoopState;
import com.tricoq.domain.agent.service.execute.auto.context.AutoTerminationReason;
import com.tricoq.domain.agent.service.execute.auto.step.Step1AnalyzeNode;
import com.tricoq.domain.agent.service.execute.auto.step.Step2ExecuteNode;
import com.tricoq.domain.agent.service.execute.auto.step.Step3QualitySupervisorNode;
import com.tricoq.domain.agent.service.execute.auto.step.Step4LogExecutionSummaryNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/20/26
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AutoLoopController {

    private final IAgentRepository agentRepository;

    private final Step1AnalyzeNode step1AnalyzeNode;
    private final Step2ExecuteNode step2ExecuteNode;
    private final Step3QualitySupervisorNode step3QualitySupervisorNode;
    private final Step4LogExecutionSummaryNode step4LogExecutionSummaryNode;


    public LoopResultDTO run(ExecuteCommandEntity requestParam, AutoExecuteContext dynamicContext) {
        log.info("=== 动态多轮执行测试开始 ====");
        log.info("用户输入: {}", requestParam.getUserInput());
        log.info("最大执行步数: {}", requestParam.getMaxSteps());
        log.info("会话ID: {}", requestParam.getSessionId());

        loadFlowConfig(requestParam, dynamicContext);

        AutoLoopState state = AutoLoopState.ANALYZE;
        //轻量状态机，将隐式流程显式化,容易直观的看出来控制流设计
        while (state != AutoLoopState.TERMINATED) {
            switch (state) {
                case ANALYZE -> {
                    if (shouldTerminateByStepPolicy(dynamicContext)) {
                        state = AutoLoopState.SUMMARY;
                        break;
                    }

                    step1AnalyzeNode.apply(requestParam, dynamicContext);
                    state = nextAfterAnalyze(dynamicContext);
                }
                case EXECUTE -> {
                    step2ExecuteNode.apply(requestParam, dynamicContext);
                    state = AutoLoopState.SUPERVISE;
                }
                case SUPERVISE -> {
                    step3QualitySupervisorNode.apply(requestParam, dynamicContext);
                    state = nextAfterSupervise(dynamicContext);
                }
                case SUMMARY -> {
                    step4LogExecutionSummaryNode.apply(requestParam, dynamicContext);
                    state = AutoLoopState.TERMINATED;
                }
            }
        }
        return LoopResultDTO.builder().completed(dynamicContext.isCompleted())
                .terminationReason(dynamicContext.getTerminationReason())
                .actualSteps(dynamicContext.getExecutionHistoryBuffer().size())
                .build();
    }

    private AutoLoopState nextAfterAnalyze(AutoExecuteContext dynamicContext) {
        return shouldTerminateAfterAnalyze(dynamicContext)
                ? AutoLoopState.SUMMARY
                : AutoLoopState.EXECUTE;
    }

    private AutoLoopState nextAfterSupervise(AutoExecuteContext dynamicContext) {
        return shouldTerminateAfterSupervise(dynamicContext)
                ? AutoLoopState.SUMMARY
                : AutoLoopState.ANALYZE;
    }

    private boolean shouldTerminateAfterAnalyze(AutoExecuteContext dynamicContext) {
        return AutoTerminationReason.COMPLETED_BY_ANALYZE.equals(dynamicContext.getTerminationReason());
    }

    private boolean shouldTerminateAfterSupervise(AutoExecuteContext dynamicContext) {
        return AutoTerminationReason.COMPLETED_BY_SUPERVISION.equals(dynamicContext.getTerminationReason());
    }

    private boolean shouldTerminateByStepPolicy(AutoExecuteContext dynamicContext) {
        if (dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            dynamicContext.markStopped(AutoTerminationReason.STOPPED_BY_MAX_STEP);
            return true;
        }
        return false;
    }

    private void loadFlowConfig(ExecuteCommandEntity requestParam,
                                AutoExecuteContext dynamicContext) {
        Map<String, AiAgentClientFlowConfigDTO> configs =
                agentRepository.queryAiAgentFlowConfigMapByAgentId(requestParam.getAgentId());
        dynamicContext.getFlowConfigMap().putAll(configs);
    }
}
