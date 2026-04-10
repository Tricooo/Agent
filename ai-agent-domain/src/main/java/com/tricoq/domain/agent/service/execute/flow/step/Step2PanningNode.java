package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.dto.FlowStepDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 执行步骤规划节点。
 * 基于用户请求和工具分析结果，通过 Spring AI Structured Output 直接输出结构化执行步骤。
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step2PanningNode extends AbstractExecuteSupport {

    private final Step3ParseStepsNode step3ParseStepsNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n--- 步骤2: 执行步骤规划 ---");

        AiAgentClientFlowConfigDTO config = Optional
                .ofNullable(dynamicContext.getFlowConfigMap().get(AiClientTypeEnumVO.PLANNING_CLIENT.getCode()))
                .orElseThrow();

        ChatClient planningClient = Optional
                .ofNullable(getChatClient(config.getClientId()))
                .orElseThrow();

        DefaultFlowAgentExecuteStrategyFactory.FlowInput input = dynamicContext.getInput();
        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();

        String planningPrompt = buildPlanningPrompt(
                input.getUserInput(),
                state.getMcpAnalysisResult(),
                state.getToolListPrompt());

        // 使用 Spring AI Structured Output 直接输出结构化步骤
        List<FlowStepDTO> plannedSteps = planningClient.prompt()
                .user(planningPrompt)
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });

        if (CollectionUtils.isEmpty(plannedSteps)) {
            throw new RuntimeException("规划节点失败");
        }
        state.setPlannedSteps(plannedSteps);

        log.info("规划完成，共 {} 个步骤", plannedSteps.size());
        for (FlowStepDTO step : plannedSteps) {
            log.info("  步骤{}: {} | 工具: {} | 目标: {}",
                    step.getStepNo(), step.getTitle(), step.getToolHint(), step.getGoal());
        }

        // 发送SSE结果 — 把结构化步骤格式化为可读文本推给前端
        String planSummary = formatPlanForDisplay(plannedSteps);
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                state.getCurrentStep(),
                "analysis_strategy",
                planSummary,
                requestParam.getSessionId());
        sendSseResult(dynamicContext, result);

        state.setCurrentStep(state.getCurrentStep() + 1);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step3ParseStepsNode;
    }

    /**
     * 构建规划提示词。
     * 不再需要 Markdown 格式规范和质量检查清单 — Spring AI 会自动注入 JSON Schema。
     */
    private String buildPlanningPrompt(String userRequest, String mcpToolsAnalysis, String toolListPrompt) {
        return String.format("""
                        # 智能执行计划生成
                        
                        ## 用户请求
                        ```
                        %s
                        ```
                        
                        ## MCP工具能力分析结果
                        %s
                        
                        ## 可用工具清单
                        %s
                        
                        ## 规划要求
                        请基于用户请求和工具分析结果，生成 3-5 个执行步骤。
                        
                        核心原则：
                        1. 完整保留用户需求中的所有详细信息，传递到每个步骤的 executionInstruction 中
                        2. 每个步骤的 toolHint 必须使用可用工具清单中的确切工具名称
                        3. 每个步骤应专注于单一功能，避免混合不同类型的操作
                        4. 合理安排步骤顺序，前置步骤的产出应为后续步骤提供输入
                        5. executionInstruction 要具体可执行，包含工具调用参数和操作细节
                        6. expectedOutput 要明确，便于判断步骤是否执行成功
                        """,
                userRequest,
                mcpToolsAnalysis,
                toolListPrompt);
    }

    /**
     * 将结构化步骤格式化为前端可读的展示文本。
     */
    private String formatPlanForDisplay(List<FlowStepDTO> steps) {
        StringBuilder sb = new StringBuilder("## 执行步骤规划\n\n");
        for (FlowStepDTO step : steps) {
            sb.append(String.format("### 第%d步: %s\n", step.getStepNo(), step.getTitle()));
            sb.append(String.format("- **目标**: %s\n", step.getGoal()));
            sb.append(String.format("- **使用工具**: %s\n", step.getToolHint()));
            sb.append(String.format("- **执行指令**: %s\n", step.getExecutionInstruction()));
            sb.append(String.format("- **预期产出**: %s\n\n", step.getExpectedOutput()));
        }
        return sb.toString();
    }
}
