package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step2ExecuteNode extends AbstractExecuteSupport {

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        log.info("\n⚡ 阶段2: 精准任务执行");
        String analyzeResult = Optional.ofNullable(dynamicContext.getAnalyzeResult())
                .orElseThrow(() -> new RuntimeException("任务分析未执行"));

        Map<String, AiAgentClientFlowConfigVO> flowConfigMap = dynamicContext.getFlowConfigMap();
        if (MapUtils.isEmpty(flowConfigMap)) {
            throw new IllegalArgumentException("flowConfigMap 为空");
        }
        String executionPrompt = String.format("""
                **用户原始需求:** %s
                
                **分析师策略:** %s
                
                **执行指令:** 你是一个精准任务执行器，需要根据用户需求和分析师策略，实际执行具体的任务。
                
                **执行要求:**
                1. 直接执行用户的具体需求（如搜索、检索、生成内容等）
                2. 如果需要搜索信息，请实际进行搜索和检索
                3. 如果需要生成计划、列表等，请直接生成完整内容
                4. 提供具体的执行结果，而不只是描述过程
                5. 确保执行结果能直接回答用户的问题
                
                **输出格式:**
                执行目标: [明确的执行目标]
                执行过程: [实际执行的步骤和调用的工具]
                执行结果: [具体的执行成果和获得的信息/内容]
                质量检查: [对执行结果的质量评估]
                """, requestParam.getUserInput(), analyzeResult);

        AiAgentClientFlowConfigVO flowConfig = Optional
                .ofNullable(flowConfigMap.get(AiClientTypeEnumVO.PRECISION_EXECUTOR_CLIENT.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("没有此 client"));
        ChatClient executeClient = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(flowConfig.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的任务分析 client"));

        String executionResult = Optional.ofNullable(executeClient
                .prompt(executionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParam.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 120))
                .call().content()).orElseThrow(() -> new RuntimeException("任务执行失败"));

        parseExecutionResult(dynamicContext, executionResult, requestParam.getSessionId());
        dynamicContext.setExecuteResult(executionResult);

        // 更新执行历史
        String stepSummary = String.format("""
                === 第 %d 步执行记录 ===
                【分析阶段】%s
                【执行阶段】%s
                """, dynamicContext.getStep(), analyzeResult, executionResult);

        dynamicContext.getExecutionHistory().append(stepSummary);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> get(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        return getBean("step3QualitySupervisorNode");
    }

    /**
     * 解析执行结果
     */
    private void parseExecutionResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext, String executionResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n⚡ === 第 {} 步执行结果 ===", step);

        String[] lines = executionResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("执行目标:")) {
                // 发送上一个section的内容
                sendExecutionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "execution_target";
                sectionContent = new StringBuilder();
                log.info("\n🎯 执行目标:");
                continue;
            } else if (line.contains("执行过程:")) {
                // 发送上一个section的内容
                sendExecutionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "execution_process";
                sectionContent = new StringBuilder();
                log.info("\n🔧 执行过程:");
                continue;
            } else if (line.contains("执行结果:")) {
                // 发送上一个section的内容
                sendExecutionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "execution_result";
                sectionContent = new StringBuilder();
                log.info("\n📈 执行结果:");
                continue;
            } else if (line.contains("质量检查:")) {
                // 发送上一个section的内容
                sendExecutionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "execution_quality";
                sectionContent = new StringBuilder();
                log.info("\n🔍 质量检查:");
                continue;
            }

            // 收集当前section的内容
            if (!currentSection.isEmpty()) {
                sectionContent.append(line).append("\n");
                switch (currentSection) {
                    case "execution_target":
                        log.info("   🎯 {}", line);
                        break;
                    case "execution_process":
                        log.info("   ⚙️ {}", line);
                        break;
                    case "execution_result":
                        log.info("   📊 {}", line);
                        break;
                    case "execution_quality":
                        log.info("   ✅ {}", line);
                        break;
                    default:
                        log.info("   📝 {}", line);
                        break;
                }
            }
        }

        // 发送最后一个section的内容
        sendExecutionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
    }

    /**
     * 发送执行阶段细分结果到流式输出
     */
    private void sendExecutionSubResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext,
                                        String subType, String content, String sessionId) {
        // 抽取的通用判断逻辑
        if (!subType.isEmpty() && !content.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createExecutionSubResult(
                    dynamicContext.getStep(), subType, content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }
}
