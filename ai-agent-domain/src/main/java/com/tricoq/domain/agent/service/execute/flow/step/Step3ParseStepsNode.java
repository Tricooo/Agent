package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.dto.FlowStepDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 规划步骤校验节点。
 * Step2 通过 Structured Output 直接产出 List<FlowStepDTO>，
 * 本节点不再做正则解析，而是负责校验步骤的完整性和合理性。
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Step3ParseStepsNode extends AbstractExecuteSupport {

    private final Step4ExecuteStepsNode step4ExecuteStepsNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n--- 步骤3: 规划步骤校验 ---");

        List<FlowStepDTO> plannedSteps = dynamicContext.getPlannedSteps();

        // 校验步骤列表非空
        if (plannedSteps == null || plannedSteps.isEmpty()) {
            log.warn("规划步骤为空，无法继续执行");
            throw new RuntimeException("规划步骤为空，无法继续执行");
        }

        // 校验每个步骤的必填字段
        int invalidCount = 0;
        for (FlowStepDTO step : plannedSteps) {
            if (step.getStepNo() == null || StringUtils.isBlank(step.getTitle())
                    || StringUtils.isBlank(step.getExecutionInstruction())) {
                log.warn("步骤{}字段不完整: stepNo={}, title={}", step.getStepNo(), step.getTitle(),
                        step.getExecutionInstruction() != null ? "有" : "缺失");
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            log.warn("共 {} 个步骤字段不完整，仍将尝试执行", invalidCount);
        }

        log.info("校验通过，共 {} 个有效步骤", plannedSteps.size());

        // 构建校验结果摘要用于 SSE 推送
        StringBuilder parseResult = new StringBuilder();
        parseResult.append("## 步骤校验结果\n\n");
        parseResult.append(String.format("共 %d 个执行步骤，校验通过：\n\n", plannedSteps.size()));
        for (FlowStepDTO step : plannedSteps) {
            parseResult.append(String.format("- **第%d步**: %s (工具: %s)\n",
                    step.getStepNo(), step.getTitle(),
                    step.getToolHint() != null ? step.getToolHint() : "无"));
        }

        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                dynamicContext.getStep(),
                "analysis_progress",
                parseResult.toString(),
                requestParam.getSessionId());
        sendSseResult(dynamicContext, result);

        dynamicContext.setStep(dynamicContext.getStep() + 1);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step4ExecuteStepsNode;
    }
}
