package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.model.request.TextInvocationRequest;
import com.tricoq.domain.agent.service.execute.auto.context.AutoExecuteContext;
import com.tricoq.domain.agent.service.execute.auto.step.context.ExecutionHistoryBuffer;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step4LogExecutionSummaryNode extends AbstractExecuteSupport {

    private final LlmInvocationFacade facade;

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, AutoExecuteContext dynamicContext) {
        log.info("\n📊 === 执行第 {} 步 ===", dynamicContext.getStep());

        // 第四阶段：执行总结
        log.info("\n📊 阶段4: 执行总结分析");

        ExecutionHistoryBuffer buffer = dynamicContext.getExecutionHistoryBuffer();

        // 记录执行总结
        logExecutionSummary(dynamicContext.getMaxStep(), buffer.size(), dynamicContext.isCompleted());

        // 生成最终总结报告（无论任务是否完成都需要生成）
        generateFinalReport(requestParameter, dynamicContext);

        log.info("\n🏁 === 动态多轮执行结束 ====");

        return "ai agent execution summary completed!";
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, AutoExecuteContext, String> get(ExecuteCommandEntity requestParameter, AutoExecuteContext dynamicContext) {
        // 总结节点是最后一个节点，返回null表示执行结束
        return getDefaultHandler();
    }

    /**
     * 记录执行总结
     */
    private void logExecutionSummary(int maxSteps, int historyStepCount, boolean isCompleted) {
        log.info("\n📊 === 动态多轮执行总结 ====");

        int actualSteps = Math.min(maxSteps, historyStepCount);
        log.info("📈 总执行步数: {} 步", actualSteps);

        if (isCompleted) {
            log.info("✅ 任务完成状态: 已完成");
        } else {
            log.info("⏸️ 任务完成状态: 未完成（达到最大步数限制）");
        }

        // 计算执行效率
        double efficiency = isCompleted ? 100.0 : (double) actualSteps / maxSteps * 100;
        log.info("📊 执行效率: {}%", efficiency);
    }

    /**
     * 生成最终总结报告
     */
    private void generateFinalReport(ExecuteCommandEntity requestParameter, AutoExecuteContext dynamicContext) {
        boolean isCompleted = dynamicContext.isCompleted();
        log.info("\n--- 生成{}任务的最终答案 ---", isCompleted ? "已完成" : "未完成");
        AiAgentClientFlowConfigDTO aiAgentClientFlowConfigVO = dynamicContext.getFlowConfigMap()
                .get(AiClientTypeEnumVO.RESPONSE_ASSISTANT.getCode());
        if (aiAgentClientFlowConfigVO == null) {
            throw new IllegalArgumentException("没有配置 RESPONSE_ASSISTANT client");
        }

        String summaryPrompt = getSummaryPrompt(aiAgentClientFlowConfigVO, dynamicContext, isCompleted);

        String summaryResult = facade.invokeText(TextInvocationRequest.builder()
                .operationName("step4")
                .clientId(aiAgentClientFlowConfigVO.getClientId())
                .prompt(summaryPrompt)
                .sessionId(requestParameter.getSessionId())
                .roleSuffix(SUMMARY_MEMORY_SUFFIX)
                .retrieveSize(50)
                .maxAttempts(2)
                .timeoutMillis(45000L)
                .fallbackResult(() -> buildSummaryFallback(dynamicContext.isCompleted()))
                .fallbackOnNonRetryable(true)
                .build());

        logFinalReport(dynamicContext, summaryResult, requestParameter.getSessionId());

        // 将总结结果保存到动态上下文中
        dynamicContext.setFinalSummary(summaryResult);

    }

    private String getSummaryPrompt(AiAgentClientFlowConfigDTO flowConfig,
                                    AutoExecuteContext dynamicContext,
                                    boolean isCompleted) {
        String fallbackPrompt;
        if (isCompleted) {
            fallbackPrompt = """
                    基于以下执行过程，请直接回答用户的原始问题，提供最终的答案和结果：
                    
                    **用户原始问题:** %s
                    
                    **执行历史和过程:**
                    %s
                    
                    **要求:**
                    1. 直接回答用户的原始问题
                    2. 基于执行过程中获得的信息和结果
                    3. 提供具体、实用的最终答案
                    4. 如果是要求制定计划、列表等，请直接给出完整的内容
                    5. 避免只描述执行过程，重点是最终答案
                    6. 以MD语法的表格形式，优化展示结果数据
                    
                    请直接给出用户问题的最终答案：
                    """;
        } else {
            fallbackPrompt = """
                    虽然任务未完全执行完成，但请基于已有的执行过程，尽力回答用户的原始问题：
                    
                    **用户原始问题:** %s
                    
                    **已执行的过程和获得的信息:**
                    %s
                    
                    **要求:**
                    1. 基于已有信息，尽力回答用户的原始问题
                    2. 如果信息不足，说明哪些部分无法完成并给出原因
                    3. 提供已能确定的部分答案
                    4. 给出完成剩余部分的具体建议
                    5. 以MD语法的表格形式，优化展示结果数据
                    
                    请基于现有信息给出用户问题的答案：
                    """;
        }
        String prompt = resolveStepPrompt(flowConfig.getStepPrompt(), fallbackPrompt, false,
                dynamicContext.getOriginalUserInput(),
                dynamicContext.getExecutionHistoryBuffer().renderForSummary());
        if (isCompleted) {
            return prompt;
        }
        return prompt + System.lineSeparator() + System.lineSeparator() + """
                补充要求：
                当前任务未完全完成，请基于已有信息尽力回答，并明确说明仍缺失的部分及后续建议。
                """;
    }

    /**
     * 输出最终总结报告
     */
    private void logFinalReport(AutoExecuteContext dynamicContext, String summaryResult, String sessionId) {
        boolean isCompleted = dynamicContext.isCompleted();
        log.info("\n📋 === {}任务最终总结报告 ===", isCompleted ? "已完成" : "未完成");

        String[] lines = summaryResult.split("\n");
        String currentSection = "summary_overview";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // 检测是否开始新的总结部分
            String newSection = detectSummarySection(line);
            if (newSection != null && !newSection.equals(currentSection)) {
                // 发送前一个部分的内容
                if (!sectionContent.isEmpty()) {
                    sendSummarySubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                }
                currentSection = newSection;
                sectionContent.setLength(0);
            }

            // 收集当前部分的内容
            if (!sectionContent.isEmpty()) {
                sectionContent.append("\n");
            }
            sectionContent.append(line);

            // 根据内容类型添加不同图标
            if (line.contains("已完成") || line.contains("完成的工作")) {
                log.info("✅ {}", line);
            } else if (line.contains("未完成") || line.contains("原因")) {
                log.info("❌ {}", line);
            } else if (line.contains("建议") || line.contains("推荐")) {
                log.info("💡 {}", line);
            } else if (line.contains("评估") || line.contains("效果")) {
                log.info("📊 {}", line);
            } else {
                log.info("📝 {}", line);
            }
        }

        // 发送最后一个部分的内容
        if (!sectionContent.isEmpty()) {
            sendSummarySubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
        }

        // 发送完整的总结结果
        sendSummaryResult(dynamicContext, summaryResult, sessionId);

        // 发送完成标识
        sendCompleteResult(dynamicContext, sessionId);
    }

    private String buildSummaryFallback(boolean isCompleted) {
        return isCompleted
                ? "任务已执行完成，详细结果请参考上方分步日志。（LLM 总结生成暂时不可用）"
                : "任务未完全完成，详细信息请参考上方分步日志。（LLM 总结生成暂时不可用）";
    }

    /**
     * 发送总结结果到流式输出
     */
    private void sendSummaryResult(AutoExecuteContext dynamicContext,
                                   String summaryResult, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummaryResult(
                summaryResult, sessionId);
        sendSseResult(dynamicContext, result);
    }

    /**
     * 发送总结阶段细分结果到流式输出
     */
    private void sendSummarySubResult(AutoExecuteContext dynamicContext,
                                      String subType, String content, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSummarySubResult(
                subType, content, sessionId);
        sendSseResult(dynamicContext, result);
    }

    /**
     * 发送完成标识到流式输出
     */
    private void sendCompleteResult(AutoExecuteContext dynamicContext, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createCompleteResult(sessionId);
        sendSseResult(dynamicContext, result);
        log.info("✅ 已发送完成标识");
    }

    /**
     * 检测总结部分标识
     */
    private String detectSummarySection(String content) {
        if (content.contains("已完成的工作") || content.contains("完成的工作") || content.contains("工作内容和成果")) {
            return "completed_work";
        } else if (content.contains("未完成的原因") || content.contains("未完成原因")) {
            return "incomplete_reasons";
        } else if (content.contains("关键因素") || content.contains("完成的关键因素")) {
            return "key_factors";
        } else if (content.contains("执行效率") || content.contains("执行效率和质量")) {
            return "efficiency_quality";
        } else if (content.contains("完成剩余任务的建议") || content.contains("建议") || content.contains("优化建议") || content.contains("经验总结")) {
            return "suggestions";
        } else if (content.contains("整体执行效果") || content.contains("评估")) {
            return "evaluation";
        }
        return null;
    }
}
