package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AutoAnalyzeResultDTO;
import com.tricoq.domain.agent.model.dto.AutoAnalyzeResultDTO.TaskStatus;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.service.execute.auto.context.AutoExecuteContext;
import com.tricoq.domain.agent.service.execute.auto.step.context.ExecutionHistoryBuffer;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 任务状态分析节点。
 * 使用 Spring AI Structured Output 直接输出 {@link AutoAnalyzeResultDTO}，
 * 替代原来的字符串 contains("COMPLETED") 魔法字符串检测。
 *
 * @author trico qiang
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Step1AnalyzeNode extends AbstractExecuteSupport {

    private final LlmInvocationFacade facade;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             AutoExecuteContext dynamicContext) {
        Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = dynamicContext.getFlowConfigMap();
        if (MapUtils.isEmpty(flowConfigMap)) {
            throw new RuntimeException("flowConfig is invalid");
        }
        AiAgentClientFlowConfigDTO flowConfig = Optional
                .ofNullable(flowConfigMap.get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("没有此 client"));

        Integer step = dynamicContext.getStep();
        log.info("\n=== 执行第 {} 步 ===", step);
        log.info("阶段1: 任务状态分析");

        ExecutionHistoryBuffer buffer = dynamicContext.getExecutionHistoryBuffer();

        String analysisPrompt = buildAnalysisPrompt(flowConfig, requestParam.getUserInput(), step,
                dynamicContext.getMaxStep(), buffer.renderForAnalyzer(),
                dynamicContext.getCurrentTask());

        AutoAnalyzeResultDTO analyzeResult = facade.invokeStructured(StructuredInvocationRequest
                .<AutoAnalyzeResultDTO>builder()
                .operationName("auto.step1.analyze")
                .clientId(flowConfig.getClientId())
                .prompt(analysisPrompt)
                .sessionId(requestParam.getSessionId())
                .roleSuffix(ANALYZER_MEMORY_SUFFIX)
                .responseType(AutoAnalyzeResultDTO.class)
                .retrieveSize(1024)
                .validate(AutoAnalyzeResultDTO::validate)
                .maxAttempts(2)
                .timeoutMillis(30000L)
                .build()
        );

        log.info("分析完成: status={}, percent={}%, nextStrategy={}",
                analyzeResult.getTaskStatus(), analyzeResult.getCompletionPercent(),
                analyzeResult.getNextStrategy());

        pushAnalysisToSse(dynamicContext, analyzeResult, requestParam.getSessionId());
        dynamicContext.setAnalyzeResultDTO(analyzeResult);

        //只保留单个指标驱动控制流
        if (analyzeResult.getTaskStatus() == TaskStatus.COMPLETED) {
            dynamicContext.setCompleted(true);
            log.info("任务分析显示已完成");
        }
        return "step1 analyze completed";
    }

    /**
     * 构建分析阶段提示词。
     * Spring AI 会在消息末尾自动注入 JSON Schema，无需手动指定输出格式。
     */
    private String buildAnalysisPrompt(AiAgentClientFlowConfigDTO flowConfig,
                                       String userInput,
                                       int step,
                                       int maxStep,
                                       String executionHistory,
                                       String currentTask) {
        String historySection = executionHistory.isBlank() ? "[首次执行，无历史记录]" : executionHistory;
        String currentTaskValue = StringUtils.hasText(currentTask) ? currentTask : "[尚未生成当前任务]";
        String fallbackPrompt = """
                # 任务状态分析
                
                ## 用户原始目标
                %s
                
                ## 当前执行进度
                - 当前步骤：第 %d 步（共 %d 步）
                
                ## 执行历史
                %s
                
                ## 当前任务
                %s
                
                ## 分析要求
                请综合以上信息，评估当前任务执行状态：
                1. 判断任务是否已完全完成（completionPercent = 100 且 taskStatus = COMPLETED）
                2. 如果未完成，评估执行历史的质量和下一步策略
                3. nextStrategy 要具体可执行，作为下一步执行节点的行动指南
                """;
        return resolveStepPrompt(flowConfig.getStepPrompt(), fallbackPrompt, true,
                userInput, step, maxStep, historySection, currentTaskValue);
    }

    /**
     * 将结构化分析结果推送到 SSE。
     * 保持与原有前端协议兼容的 subType 字段。
     */
    private void pushAnalysisToSse(AutoExecuteContext dynamicContext,
                                   AutoAnalyzeResultDTO result, String sessionId) {
        int step = dynamicContext.getStep();

        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createAnalysisSubResult(
                step, "analysis_task_status",
                "任务状态: " + result.getTaskStatus() + " | 完成度: " + result.getCompletionPercent() + "%",
                sessionId));

        if (result.getStatusDescription() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    step, "analysis_status", result.getStatusDescription(), sessionId));
        }

        if (result.getHistoryEvaluation() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    step, "analysis_history", result.getHistoryEvaluation(), sessionId));
        }

        if (result.getNextStrategy() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    step, "analysis_strategy", result.getNextStrategy(), sessionId));
        }

        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createAnalysisSubResult(
                step, "analysis_progress",
                "完成度评估: " + result.getCompletionPercent() + "%",
                sessionId));
    }
}
