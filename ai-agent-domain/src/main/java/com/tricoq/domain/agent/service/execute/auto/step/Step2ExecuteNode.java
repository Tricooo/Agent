package com.tricoq.domain.agent.service.execute.auto.step;

import com.tricoq.domain.agent.model.dto.AutoAnalyzeResultDTO;
import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 精准任务执行节点。
 * 使用 Spring AI Structured Output 直接输出 {@link AutoExecuteResultDTO}，
 * 替代原来的字符串 split + section 关键字匹配解析。
 *
 * @author trico qiang
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
    protected String doApply(ExecuteCommandEntity requestParam,
                             DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        log.info("\n阶段2: 精准任务执行");

        AutoAnalyzeResultDTO analyzeResult = Optional.ofNullable(dynamicContext.getAnalyzeResultDTO())
                .orElseThrow(() -> new RuntimeException("任务分析未执行"));

        Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = dynamicContext.getFlowConfigMap();
        if (MapUtils.isEmpty(flowConfigMap)) {
            throw new IllegalArgumentException("flowConfigMap 为空");
        }

        AiAgentClientFlowConfigDTO flowConfig = Optional
                .ofNullable(flowConfigMap.get(AiClientTypeEnumVO.PRECISION_EXECUTOR_CLIENT.getCode()))
                .orElseThrow(() -> new IllegalArgumentException("没有此 client"));
        ChatClient executeClient = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(flowConfig.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的执行 client"));

        String executionPrompt = buildExecutionPrompt(flowConfig, requestParam.getUserInput(), analyzeResult);

        AutoExecuteResultDTO executeResult = executeClient.prompt(executionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, buildConversationId(requestParam.getSessionId(),
                                EXECUTOR_MEMORY_SUFFIX))
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 120))
                .call()
                .entity(AutoExecuteResultDTO.class);

        if (executeResult == null) {
            throw new RuntimeException("任务执行失败");
        }
        executeResult.validate();

        log.info("执行完成: target={}", executeResult.getExecutionTarget());

        pushExecutionToSse(dynamicContext, executeResult, requestParam.getSessionId());
        dynamicContext.setExecuteResultDTO(executeResult);

        // 更新执行历史（文本格式，供 Step1 下一轮分析用）
        dynamicContext.getExecutionHistory().append(String.format("""
                === 第 %d 步执行记录 ===
                【分析策略】%s
                【执行目标】%s
                【执行结果】%s
                """,
                dynamicContext.getStep(),
                analyzeResult.getNextStrategy(),
                executeResult.getExecutionTarget(),
                executeResult.getExecutionResult()));

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> get(
            ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        return getBean("step3QualitySupervisorNode");
    }

    /**
     * 构建执行阶段提示词。
     * 直接使用分析节点输出的结构化策略，无需重新解析字符串。
     */
    private String buildExecutionPrompt(AiAgentClientFlowConfigDTO flowConfig,
                                        String userInput,
                                        AutoAnalyzeResultDTO analyzeResult) {
        String fallbackPrompt = """
                # 精准任务执行

                ## 用户原始目标
                %s

                ## 本次执行策略（来自任务分析节点）
                %s

                ## 执行要求
                请严格按照执行策略完成任务：
                1. executionTarget：明确本次执行的具体目标
                2. executionProcess：详细描述执行过程和使用的方法
                3. executionResult：提供具体的执行输出和结果数据
                4. qualityCheck：对执行结果进行自检，指出可能的问题
                """;
        return resolveStepPrompt(flowConfig.getStepPrompt(), fallbackPrompt, true,
                userInput, analyzeResult.getNextStrategy());
    }

    /**
     * 将结构化执行结果推送到 SSE。
     */
    private void pushExecutionToSse(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext,
                                     AutoExecuteResultDTO result, String sessionId) {
        int step = dynamicContext.getStep();

        if (result.getExecutionTarget() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createExecutionSubResult(
                    step, "execution_target", result.getExecutionTarget(), sessionId));
        }
        if (result.getExecutionProcess() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createExecutionSubResult(
                    step, "execution_process", result.getExecutionProcess(), sessionId));
        }
        if (result.getExecutionResult() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createExecutionSubResult(
                    step, "execution_result", result.getExecutionResult(), sessionId));
        }
        if (result.getQualityCheck() != null) {
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createExecutionSubResult(
                    step, "execution_quality", result.getQualityCheck(), sessionId));
        }
    }
}
