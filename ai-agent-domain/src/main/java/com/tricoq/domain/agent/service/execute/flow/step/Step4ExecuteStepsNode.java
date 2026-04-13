package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.FlowStepDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 步骤执行节点。
 * 按顺序执行 Step2 产出的结构化步骤列表。
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@Slf4j
public class Step4ExecuteStepsNode extends AbstractExecuteSupport {

    @Override
    public String doApply(ExecuteCommandEntity request,
                          DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("开始执行第四步：按顺序执行规划步骤");

        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();

        try {
            AiAgentClientFlowConfigDTO config = dynamicContext.getFlowConfigMap()
                    .get(AiClientTypeEnumVO.EXECUTOR_CLIENT.getCode());
            ChatClient executorChatClient = getChatClient(config.getClientId());

            List<FlowStepDTO> plannedSteps = state.getPlannedSteps();
            if (plannedSteps == null || plannedSteps.isEmpty()) {
                return "步骤列表为空，无法执行";
            }

            for (FlowStepDTO step : plannedSteps) {
                executeStep(executorChatClient, step, dynamicContext);
            }

            ExecutionOverview overview = summarizeExecution(plannedSteps, state.getStepResults());
            String executionMessage = buildExecutionMessage(overview);

            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createExecutionResult(
                    state.getCurrentStep(),
                    executionMessage,
                    request.getSessionId());
            sendSseResult(dynamicContext, result);

            sendSummaryResult(dynamicContext, request.getSessionId(), overview);
            // sendCompleteResult(dynamicContext, request.getSessionId());

            state.setCurrentStep(state.getCurrentStep() + 1);
            state.setCompleted(true);

            log.info("第四步执行结束：{}", executionMessage);
            return executionMessage;
        } catch (Exception e) {
            log.error("第四步执行失败", e);
            return "执行步骤失败: " + e.getMessage();
        }
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return getDefaultHandler();
    }

    /**
     * 执行单个步骤
     */
    private void executeStep(ChatClient executorChatClient, FlowStepDTO step,
                             DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        int stepNo = step.getStepNo();
        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        if (!canExecuteStep(step, dynamicContext)) {
            var skippedDTO = DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO.builder()
                    .stepNo(stepNo)
                    .stepTitle(step.getTitle())
                    .status(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.SKIPPED)
                    .errorMessage("前置依赖步骤未成功，跳过执行")
                    .build();
            state.getStepResults().put(stepNo, skippedDTO);
            return;
        }
        log.info("\n--- 开始执行 第{}步: {} ---", stepNo, step.getTitle());

        DefaultFlowAgentExecuteStrategyFactory.FlowInput input = dynamicContext.getInput();

        try {
            String executionResult = executeWithRetry(() -> executorChatClient.prompt()
                            .user(buildStepExecutionPrompt(step, dynamicContext))
                            .call()
                            .content(), "执行步骤" + stepNo,
                    3);
            if (executionResult == null) {
                throw new RuntimeException("执行失败，步骤编号：" + stepNo);
            }
            log.info("步骤 {} 执行结果: {}...", stepNo,
                    executionResult.substring(0, Math.min(150, executionResult.length())));

            var resultDTO = DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO.builder()
                    .stepNo(stepNo)
                    .status(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.SUCCESS)
                    .stepTitle(step.getTitle())
                    .result(executionResult)
                    .build();

            state.getStepResults().put(stepNo, resultDTO);

            AutoAgentExecuteResultEntity stepResult = AutoAgentExecuteResultEntity.createExecutionResult(
                    stepNo,
                    "第" + stepNo + "步 执行完成: " + executionResult.substring(0, Math.min(500, executionResult.length())),
                    input.getSessionId());
            sendSseResult(dynamicContext, stepResult);

        } catch (Exception e) {
            log.error("执行步骤 {} 时发生错误: {}", stepNo, e.getMessage());
            handleStepExecutionError(stepNo, step.getTitle(), e, dynamicContext);
        }

        log.info("--- 完成执行 第{}步 ---", stepNo);
    }

    /**
     * 检查所依赖的步骤是否成功执行
     */
    private boolean canExecuteStep(FlowStepDTO step,
                                   DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        List<Integer> dependsOn = step.getDependsOn();
        if (CollectionUtils.isEmpty(dependsOn)) {
            return Boolean.TRUE;
        }
        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        Map<Integer, DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO> stepResults = state.getStepResults();
        for (Integer depend : dependsOn) {
            DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO result = stepResults.get(depend);
            if (result == null ||
                    !result.getStatus().equals(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.SUCCESS)) {
                log.warn("步骤 {} 的前置依赖步骤 {} 未成功（result={}），跳过", step.getStepNo(), depend, result);
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 处理步骤执行错误
     */
    private void handleStepExecutionError(int stepNo, String stepTitle, Exception e,
                                          DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.warn("步骤 {} 执行失败，记录错误", stepNo);
        var resultDTO = DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO.builder()
                .stepNo(stepNo)
                .stepTitle(stepTitle)
                .status(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.FAILED)
                .errorMessage(e.getMessage())
                .build();
        dynamicContext.getState().getStepResults().put(stepNo, resultDTO);
        try {
            AutoAgentExecuteResultEntity errorResult = AutoAgentExecuteResultEntity.createExecutionResult(
                    stepNo,
                    "第" + stepNo + "步 执行失败: " + e.getMessage(),
                    dynamicContext.getInput().getSessionId());
            sendSseResult(dynamicContext, errorResult);
        } catch (Exception sseException) {
            log.error("发送错误SSE结果失败", sseException);
        }
    }

    /**
     * 构建步骤执行提示词。
     * 基于 FlowStepDTO 的结构化字段构建，而非原来的裸文本。
     */
    private String buildStepExecutionPrompt(FlowStepDTO step,
                                            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        StringBuilder prevResultSection = new StringBuilder();
        List<Integer> deps = step.getDependsOn();
        if (deps != null && !deps.isEmpty()) {
            prevResultSection.append("\n**前置步骤执行结果:**\n");
            for (Integer depNo : deps) {
                DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO depResult = dynamicContext.getState()
                        .getStepResults().get(depNo);
                if (depResult != null && StringUtils.isNotBlank(depResult.getResult())) {
                    String truncated = depResult.getResult()
                            .substring(0, Math.min(500, depResult.getResult().length()));
                    prevResultSection.append(String.format("- 第%d步(%s): %s\n",
                            depNo, depResult.getStepTitle(), truncated));
                }
            }
        }

        return String.format("""
                        你是一个智能执行助手，需要依赖以下步骤执行结果:
                        %s
                        **当前步骤:** 第%d步 - %s
                        
                        **步骤目标:** %s
                        
                        **执行指令:** %s
                        
                        **建议工具:** %s
                        
                        **预期产出:** %s
                        
                        **用户原始请求:** %s
                        
                        **执行要求:**
                        1. 严格按照执行指令完成任务
                        2. 如果涉及MCP工具调用，请使用建议工具中指定的工具
                        3. 提供详细的执行过程和结果
                        4. 如果遇到问题，请说明具体的错误信息
                        5. **重要**: 执行完成后，必须在回复末尾明确输出执行结果，格式如下:
                           ```
                           === 执行结果 ===
                           状态: [成功/失败]
                           结果描述: [具体的执行结果描述]
                           输出数据: [如果有具体的输出数据，请在此列出]
                           ```
                        
                        请开始执行这个步骤。""",
                prevResultSection,
                step.getStepNo(),
                step.getTitle(),
                step.getGoal(),
                step.getExecutionInstruction(),
                step.getToolHint() != null ? step.getToolHint() : "无指定工具",
                step.getExpectedOutput(),
                dynamicContext.getInput().getUserInput());
    }

    /**
     * 发送总结结果到流式输出
     */
    private void sendSummaryResult(DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext,
                                   String sessionId,
                                   ExecutionOverview overview) {
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("## 执行步骤完成总结\n\n");

        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        List<FlowStepDTO> steps = state.getPlannedSteps();
        if (steps != null) {
            summaryContent.append("### 步骤执行明细\n");
            for (FlowStepDTO step : steps) {
                DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO resultDTO =
                        state.getStepResults().get(step.getStepNo());
                DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus status =
                        resultDTO != null ? resultDTO.getStatus() : null;
                summaryContent.append(String.format("- [%s] 第%d步: %s\n",
                        formatStatusLabel(status), step.getStepNo(), step.getTitle()));
            }
            summaryContent.append("\n");
        }

        summaryContent.append("### 执行状态\n");
        summaryContent.append(buildExecutionStatusText(overview));

        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummaryResult(
                summaryContent.toString(), sessionId);
        sendSseResult(dynamicContext, result);
        log.info("已发送总结结果");
    }

    private ExecutionOverview summarizeExecution(List<FlowStepDTO> plannedSteps,
                                                 Map<Integer, DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO> stepResults) {
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        int unknownCount = 0;

        for (FlowStepDTO step : plannedSteps) {
            DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO result = stepResults.get(step.getStepNo());
            if (result == null || result.getStatus() == null) {
                unknownCount++;
                continue;
            }
            switch (result.getStatus()) {
                case SUCCESS -> successCount++;
                case FAILED -> failedCount++;
                case SKIPPED -> skippedCount++;
            }
        }

        return new ExecutionOverview(plannedSteps.size(), successCount, failedCount, skippedCount, unknownCount);
    }

    private String buildExecutionMessage(ExecutionOverview overview) {
        if (overview.isAllSucceeded()) {
            return String.format("已完成所有规划步骤的执行（共%d步，全部成功）", overview.totalSteps);
        }
        return String.format("规划步骤执行结束：成功%d步，失败%d步，跳过%d步%s",
                overview.successSteps,
                overview.failedSteps,
                overview.skippedSteps,
                overview.unknownSteps > 0 ? String.format("，未记录%d步", overview.unknownSteps) : "");
    }

    private String buildExecutionStatusText(ExecutionOverview overview) {
        StringBuilder builder = new StringBuilder();
        if (overview.isAllSucceeded()) {
            builder.append("全部规划步骤均执行成功\n");
        } else {
            builder.append("本次执行已结束，但并非所有步骤都成功完成\n");
        }
        builder.append(String.format("- 总步骤数: %d\n", overview.totalSteps));
        builder.append(String.format("- 成功: %d\n", overview.successSteps));
        builder.append(String.format("- 失败: %d\n", overview.failedSteps));
        builder.append(String.format("- 跳过: %d\n", overview.skippedSteps));
        if (overview.unknownSteps > 0) {
            builder.append(String.format("- 未记录: %d\n", overview.unknownSteps));
        }
        return builder.toString();
    }

    private String formatStatusLabel(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case SUCCESS -> "SUCCESS";
            case FAILED -> "FAILED";
            case SKIPPED -> "SKIPPED";
        };
    }

    /**
     * 发送完成标识到流式输出
     */
    private void sendCompleteResult(DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext,
                                    String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createCompleteResult(sessionId);
        sendSseResult(dynamicContext, result);
        log.info("已发送完成标识");
    }

    private static final class ExecutionOverview {
        private final int totalSteps;
        private final int successSteps;
        private final int failedSteps;
        private final int skippedSteps;
        private final int unknownSteps;

        private ExecutionOverview(int totalSteps, int successSteps, int failedSteps, int skippedSteps, int unknownSteps) {
            this.totalSteps = totalSteps;
            this.successSteps = successSteps;
            this.failedSteps = failedSteps;
            this.skippedSteps = skippedSteps;
            this.unknownSteps = unknownSteps;
        }

        private boolean isAllSucceeded() {
            return totalSteps > 0 && successSteps == totalSteps;
        }
    }
}
