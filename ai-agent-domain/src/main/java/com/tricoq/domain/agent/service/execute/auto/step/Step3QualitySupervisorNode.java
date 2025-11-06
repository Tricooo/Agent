package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step3QualitySupervisorNode extends AbstractExecuteSupport {

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {

        String executeResult = Optional.ofNullable(dynamicContext.getExecuteResult())
                .orElseThrow(() -> new RuntimeException("任务未执行"));
        String originalUserInput = dynamicContext.getOriginalUserInput();
        if (StringUtils.isBlank(originalUserInput)) {
            throw new IllegalArgumentException("用户输入异常");
        }
        Map<String, AiAgentClientFlowConfigVO> flowConfigMap = dynamicContext.getFlowConfigMap();
        if (MapUtils.isEmpty(flowConfigMap)) {
            throw new IllegalArgumentException("flowConfigMap 为空");
        }
        // 第三阶段：质量监督
        log.info("\n🔍 阶段3: 质量监督检查");

        AiAgentClientFlowConfigVO flowConfig = Optional
                .ofNullable(flowConfigMap.get(AiClientTypeEnumVO.QUALITY_SUPERVISOR_CLIENT.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("没有此 client"));
        ChatClient qualitySupervisorClient = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(flowConfig.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的任务分析 client"));

        String supervisionPrompt = String.format(flowConfig.getStepPrompt(), originalUserInput, executeResult);

        String supervisionResult = Optional.ofNullable(qualitySupervisorClient
                .prompt(supervisionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParam.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 80))
                .call().content()).orElseThrow(() -> new RuntimeException("分析任务执行失败"));

        Integer step = dynamicContext.getStep();
        parseSupervisionResult(dynamicContext, supervisionResult, requestParam.getSessionId());

        String currentTask = dynamicContext.getCurrentTask();
        // 根据监督结果决定是否需要重新执行
        if (supervisionResult.contains("是否通过: FAIL")) {
            log.info("❌ 质量检查未通过，需要重新执行");
            currentTask = "根据质量监督的建议重新执行任务";
        } else if (supervisionResult.contains("是否通过: OPTIMIZE")) {
            log.info("🔧 质量检查建议优化，继续改进");
            currentTask = "根据质量监督的建议优化执行结果";
        } else {
            log.info("✅ 质量检查通过");
            dynamicContext.setCompleted(Boolean.TRUE);
        }
        dynamicContext.setCurrentTask(currentTask);
        dynamicContext.setSupervisionResult(supervisionResult);
        dynamicContext.setStep(step + 1);

        // 更新执行历史
        String stepSummary = String.format("""
                        === 第 %d 步完整记录 ===
                        【分析阶段】%s
                        【执行阶段】%s
                        【监督阶段】%s
                        """, step,
                dynamicContext.getAnalyzeResult(),
                dynamicContext.getExecuteResult(),
                supervisionResult);

        dynamicContext.getExecutionHistory().append(stepSummary);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> get(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getBean("step4LogExecutionSummaryNode");
        }
        String currentTask = extractNextTask(dynamicContext.getAnalyzeResult(),
                dynamicContext.getExecuteResult(),
                dynamicContext.getSupervisionResult());
        dynamicContext.setCurrentTask(currentTask);
        return getBean("step1AnalyzeNode");
    }

    /**
     * 提取下一步任务
     */
    private String extractNextTask(String analysisResult, String executionResult, String currentTask) {
        // 从分析结果中提取下一步策略
        String[] analysisLines = analysisResult.split("\n");
        for (String line : analysisLines) {
            if (line.contains("下一步策略:") && analysisLines.length > 1) {
                // 获取策略内容的下一行
                for (int i = 0; i < analysisLines.length - 1; i++) {
                    if (analysisLines[i].contains("下一步策略:") && !analysisLines[i + 1].trim().isEmpty()) {
                        String nextTask = analysisLines[i + 1].trim();
                        log.info("\n🎯 下一步任务: {}", nextTask);
                        return nextTask;
                    }
                }
            }
        }

        // 如果分析结果中没有找到，从执行结果中提取
        String[] executionLines = executionResult.split("\n");
        for (String line : executionLines) {
            if (line.contains("下一步") && !line.trim().isEmpty()) {
                String nextTask = line.trim();
                log.info("\n🎯 下一步任务: {}", nextTask);
                return nextTask;
            }
        }

        // 默认继续当前任务
        log.info("\n🔄 继续当前任务");
        return currentTask;
    }

    /**
     * 解析监督结果
     */
    private void parseSupervisionResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext, String supervisionResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n🔍 === 第 {} 步监督结果 ===", step);

        String[] lines = supervisionResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.contains("质量评估:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "assessment";
                sectionContent.setLength(0);
                log.info("\n📊 质量评估:");
                continue;
            } else if (line.contains("问题识别:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "issues";
                sectionContent.setLength(0);
                log.info("\n⚠️ 问题识别:");
                continue;
            } else if (line.contains("改进建议:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "suggestions";
                sectionContent.setLength(0);
                log.info("\n💡 改进建议:");
                continue;
            } else if (line.contains("质量评分:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "score";
                sectionContent.setLength(0);
                String score = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 质量评分: {}", score);
                sectionContent.append(score);
                continue;
            } else if (line.contains("是否通过:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "pass";
                sectionContent.setLength(0);
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("PASS")) {
                    log.info("\n✅ 检查结果: 通过");
                } else if (status.equals("FAIL")) {
                    log.info("\n❌ 检查结果: 未通过");
                } else {
                    log.info("\n🔧 检查结果: 需要优化");
                }
                sectionContent.append(status);
                continue;
            }

            // 收集当前部分的内容
            if (!currentSection.isEmpty()) {
                if (!sectionContent.isEmpty()) {
                    sectionContent.append("\n");
                }
                sectionContent.append(line);
            }

            switch (currentSection) {
                case "assessment":
                    log.info("   📋 {}", line);
                    break;
                case "issues":
                    log.info("   ⚠️ {}", line);
                    break;
                case "suggestions":
                    log.info("   💡 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }

        // 发送最后一个部分的内容
        sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);

        // 发送完整的监督结果
        sendSupervisionResult(dynamicContext, supervisionResult, sessionId);
    }

    /**
     * 发送监督结果到流式输出
     */
    private void sendSupervisionResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext,
                                       String supervisionResult, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSupervisionResult(
                dynamicContext.getStep(), supervisionResult, sessionId);
        sendSseResult(dynamicContext, result);
    }

    /**
     * 发送监督子结果到流式输出（细粒度标识）
     */
    private void sendSupervisionSubResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext,
                                          String section, String content, String sessionId) {
        // 抽取的通用判断逻辑
        if (!content.isEmpty() && !section.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSupervisionSubResult(
                    dynamicContext.getStep(), section, content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }
}
