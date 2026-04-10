package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.dto.FlowStepDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

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
        DefaultFlowAgentExecuteStrategyFactory.FlowInput input = dynamicContext.getInput();

        try {
            AiAgentClientFlowConfigDTO config = dynamicContext.getFlowConfigMap()
                    .get(AiClientTypeEnumVO.EXECUTOR_CLIENT.getCode());
            ChatClient executorChatClient = getChatClient(config.getClientId());

            List<FlowStepDTO> plannedSteps = state.getPlannedSteps();
            if (plannedSteps == null || plannedSteps.isEmpty()) {
                return "步骤列表为空，无法执行";
            }

            // 按顺序执行每个步骤
            for (FlowStepDTO step : plannedSteps) {
                executeStep(executorChatClient, step, dynamicContext);
            }

            // 发送SSE结果
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createExecutionResult(
                    state.getCurrentStep(),
                    "已完成所有规划步骤的执行",
                    request.getSessionId());
            sendSseResult(dynamicContext, result);

            sendSummaryResult(dynamicContext, request.getSessionId());
            //sendCompleteResult(dynamicContext, request.getSessionId());

            state.setCurrentStep(state.getCurrentStep() + 1);
            state.setCompleted(true);

            log.info("第四步执行完成：所有规划步骤已执行");
            return "所有规划步骤执行完成";
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
        log.info("\n--- 开始执行 第{}步: {} ---", stepNo, step.getTitle());

        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        DefaultFlowAgentExecuteStrategyFactory.FlowInput input = dynamicContext.getInput();

        try {
            String executionResult = executorChatClient.prompt()
                    .user(buildStepExecutionPrompt(step, dynamicContext))
                    .call()
                    .content();
            //assert 在生产环境默认关闭（JVM 不加 -ea），这行等于无效校验
            //assert executionResult != null;
            if (null == executionResult) {
                throw new RuntimeException("执行失败，步骤编号：" + stepNo);
            }
            log.info("步骤 {} 执行结果: {}...", stepNo,
                    executionResult.substring(0, Math.min(150, executionResult.length())));

            // 保存执行结果
            var resultDTO = DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO.builder()
                    .stepNo(stepNo)
                    .status(DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.SUCCESS)
                    .stepTitle(step.getTitle())
                    .result(executionResult)
                    .build();

            state.getStepResults().put(stepNo, resultDTO);

            // 发送步骤执行结果的SSE
            AutoAgentExecuteResultEntity stepResult = AutoAgentExecuteResultEntity.createExecutionResult(
                    stepNo,
                    "第" + stepNo + "步 执行完成: " + executionResult.substring(0, Math.min(500, executionResult.length())),
                    input.getSessionId());
            sendSseResult(dynamicContext, stepResult);

            //todo 这里的等待需要吗？
            //Thread.sleep(1000);

        } catch (Exception e) {
            //todo 这里步骤执行失败没有阻止执行 如果步骤 2 依赖步骤 1 的结果，步骤 1 失败后继续执行步骤 2 没有意义
            log.error("执行步骤 {} 时发生错误: {}", stepNo, e.getMessage());
            handleStepExecutionError(stepNo, step.getTitle(), e, dynamicContext);
        }

        log.info("--- 完成执行 第{}步 ---", stepNo);
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
        // 读取前一步的执行结果
        String prevResultSection = "";
        if (step.getStepNo() > 1) {
            //todo 如果步骤之间跨步依赖，可能需要传入累积上下文
            DefaultFlowAgentExecuteStrategyFactory.FlowStepResultDTO resultDTO = dynamicContext
                    .getState()
                    .getStepResults()
                    .get(step.getStepNo() - 1);
            if (null != resultDTO) {
                String result = resultDTO.getResult();
                if (StringUtils.isBlank(result)) {
                    throw new RuntimeException();
                }
                prevResultSection = "\n**前置步骤执行结果:**\n"
                        + result.substring(0, Math.min(500, result.length())) + "\n";
            }
        }

        return String.format("""
                        你是一个智能执行助手，需要执行以下步骤:
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
                                   String sessionId) {
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("## 执行步骤完成总结\n\n");

        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        List<FlowStepDTO> steps = state.getPlannedSteps();
        if (steps != null) {
            summaryContent.append("### 已完成的工作\n");
            for (FlowStepDTO step : steps) {
                DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus status = state.getStepResults()
                        .get(step.getStepNo()).getStatus();
                String icon = DefaultFlowAgentExecuteStrategyFactory.FlowStepStatus.FAILED.equals(status) ? "x" : "v";
                summaryContent.append(String.format("- [%s] 第%d步: %s\n",
                        icon, step.getStepNo(), step.getTitle()));
            }
            summaryContent.append("\n");
        }

        summaryContent.append("### 执行状态\n");
        summaryContent.append("所有规划步骤已执行完成\n");

        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummaryResult(
                summaryContent.toString(), sessionId);
        sendSseResult(dynamicContext, result);
        log.info("已发送总结结果");
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
}
