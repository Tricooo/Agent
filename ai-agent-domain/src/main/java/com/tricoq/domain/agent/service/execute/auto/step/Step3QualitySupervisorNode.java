package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO.QualityStatus;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.service.execute.auto.context.AutoExecuteContext;
import com.tricoq.domain.agent.service.execute.auto.context.AutoTerminationReason;
import com.tricoq.domain.agent.service.execute.auto.step.context.ExecutionHistoryBuffer;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 质量监督节点。
 * 使用 Spring AI Structured Output 直接输出 {@link AutoSupervisionResultDTO}，
 * 替代原来的字符串 contains("是否通过: FAIL") 魔法字符串检测。
 *
 * @author trico qiang
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step3QualitySupervisorNode extends AbstractExecuteSupport {

    private final LlmInvocationFacade facade;

    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             AutoExecuteContext dynamicContext) {
        AutoExecuteResultDTO executeResult = Optional.ofNullable(dynamicContext.getExecuteResultDTO())
                .orElseThrow(() -> new RuntimeException("任务未执行"));

        String originalUserInput = dynamicContext.getOriginalUserInput();
        if (StringUtils.isBlank(originalUserInput)) {
            throw new IllegalArgumentException("用户输入异常");
        }
        Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = dynamicContext.getFlowConfigMap();
        if (MapUtils.isEmpty(flowConfigMap)) {
            throw new IllegalArgumentException("flowConfigMap 为空");
        }

        log.info("\n阶段3: 质量监督检查");

        AiAgentClientFlowConfigDTO flowConfig = Optional
                .ofNullable(flowConfigMap.get(AiClientTypeEnumVO.QUALITY_SUPERVISOR_CLIENT.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("没有此 client"));

        String supervisionPrompt = buildSupervisionPrompt(flowConfig, originalUserInput, executeResult);

        AutoSupervisionResultDTO supervisionResult = facade.invokeStructured(
                StructuredInvocationRequest.<AutoSupervisionResultDTO>builder()
                        .operationName("step3")
                        .clientId(flowConfig.getClientId())
                        .prompt(supervisionPrompt)
                        .sessionId(requestParam.getSessionId())
                        .roleSuffix(SUPERVISOR_MEMORY_SUFFIX)
                        .responseType(AutoSupervisionResultDTO.class)
                        .retrieveSize(80)
                        .validate(AutoSupervisionResultDTO::validate)
                        .maxAttempts(2)
                        .timeoutMillis(30000L)
                        .build()
        );

        log.info("监督完成: pass={}, score={}", supervisionResult.getPass(), supervisionResult.getQualityScore());

        pushSupervisionToSse(dynamicContext, supervisionResult, requestParam.getSessionId());

        // 根据监督结论更新任务和完成状态
        if (supervisionResult.getPass() == QualityStatus.FAIL) {
            log.info("质量检查未通过，需要重新执行");
            String nextTask = CollectionUtils.isNotEmpty(supervisionResult.getSuggestions())
                    ? String.join("；", supervisionResult.getSuggestions())
                    : "根据质量监督的建议重新执行任务";
            dynamicContext.setCurrentTask(nextTask);
        } else if (supervisionResult.getPass() == QualityStatus.OPTIMIZE) {
            log.info("质量检查建议优化，继续改进");
            String nextTask = CollectionUtils.isNotEmpty(supervisionResult.getSuggestions())
                    ? String.join("；", supervisionResult.getSuggestions())
                    : "根据质量监督的建议优化执行结果";
            dynamicContext.setCurrentTask(nextTask);
        } else {
            log.info("质量检查通过");
            dynamicContext.markCompleted(AutoTerminationReason.COMPLETED_BY_SUPERVISION);
        }

        // 补充监督记录到执行历史
        ExecutionHistoryBuffer buffer = dynamicContext.getExecutionHistoryBuffer();
        buffer.recordSupervision(dynamicContext.getStep(), supervisionResult);

        dynamicContext.setSupervisionResultDTO(supervisionResult);
        dynamicContext.setStep(dynamicContext.getStep() + 1);

        return "step3 superview completed";
    }

    /**
     * 构建监督阶段提示词。
     */
    private String buildSupervisionPrompt(AiAgentClientFlowConfigDTO flowConfig,
                                          String userInput,
                                          AutoExecuteResultDTO executeResult) {
        String executionSummary = String.format("""
                        执行目标: %s
                        执行过程: %s
                        执行结果: %s
                        质量检查: %s
                        """,
                executeResult.getExecutionTarget(),
                executeResult.getExecutionProcess(),
                executeResult.getExecutionResult(),
                executeResult.getQualityCheck());
        String fallbackPrompt = """
                # 执行质量监督
                
                ## 用户原始目标
                %s
                
                ## 本次执行结果
                %s
                
                ## 监督要求
                请对执行结果进行客观评估：
                1. qualityAssessment：综合评估执行结果是否满足用户原始目标
                2. issues：列出发现的具体问题（可为空列表）
                3. suggestions：列出改进建议（PASS 时可为空，FAIL/OPTIMIZE 时必须有建议）
                4. qualityScore：给出 1-10 的质量评分
                5. pass：PASS（完全满足目标）、FAIL（需要重新执行）、OPTIMIZE（基本满足但可改进）
                """;
        return resolveStepPrompt(flowConfig.getStepPrompt(), fallbackPrompt, true,
                userInput, executionSummary);
    }

    /**
     * 将结构化监督结果推送到 SSE。
     */
    private void pushSupervisionToSse(AutoExecuteContext dynamicContext,
                                      AutoSupervisionResultDTO result, String sessionId) {
        int step = dynamicContext.getStep();

        if (result.getQualityAssessment() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionSubResult(
                    step, "assessment", result.getQualityAssessment(), sessionId));
        }
        if (CollectionUtils.isNotEmpty(result.getIssues())) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionSubResult(
                    step, "issues", String.join("\n- ", result.getIssues()), sessionId));
        }
        if (CollectionUtils.isNotEmpty(result.getSuggestions())) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionSubResult(
                    step, "suggestions", String.join("\n- ", result.getSuggestions()), sessionId));
        }
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionSubResult(
                step, "score", String.valueOf(result.getQualityScore()), sessionId));
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionSubResult(
                step, "pass", result.getPass().name(), sessionId));

        // 发送完整监督摘要
        String summary = String.format("评分: %d/10 | 结论: %s\n评估: %s",
                result.getQualityScore(), result.getPass(), result.getQualityAssessment());
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSupervisionResult(
                step, summary, sessionId));
    }
}
