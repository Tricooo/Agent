package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * mcp工具分析节点
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step1McpToolsAnalysisNode extends AbstractExecuteSupport {

    private final Step2PanningNode step2PanningNode;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam,
                             DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n--- 步骤1: MCP工具能力分析（仅分析阶段，不执行用户请求） ---");

        Map<String, AiAgentClientFlowConfigDTO> configMap = dynamicContext.getFlowConfigMap();
        AiAgentClientFlowConfigDTO config = Optional
                .ofNullable(configMap.get(AiClientTypeEnumVO.TOOL_MCP_CLIENT.getCode()))
                .orElseThrow();

        //todo client 类型的命名和定位可能需要重新审视 TOOL_MCP_CLIENT
        ChatClient mcpAnalysisClient = Optional
                .ofNullable(getChatClient(config.getClientId()))
                .orElseThrow();
        //todo
        /*  prompt 过长: 92 行的分析 prompt 非常详细（5个大节），但 Step1 的产出 mcpAnalysisResult 在 Step2 中只作为参考文本嵌入规划
            prompt。这么详细的分析指令可能导致 LLM 输出很长的分析报告，实际被 Step2
            消费时大部分内容是冗余的。可以考虑精简，聚焦在"工具匹配度"和"调用方式"两个核心点上
        */
        DefaultFlowAgentExecuteStrategyFactory.FlowInput input = dynamicContext.getInput();
        DefaultFlowAgentExecuteStrategyFactory.FlowState state = dynamicContext.getState();
        String mcpAnalysisPrompt = String.format(
                """
                        # MCP工具能力分析任务
                        
                        ## 重要说明
                        **注意：本阶段仅进行MCP工具能力分析，不执行用户的实际请求。**\s
                        这是一个纯分析阶段，目的是评估可用工具的能力和适用性，为后续的执行规划提供依据。
                        
                        ## 用户请求
                        %s
                        
                        ## 可用的MCP工具列表
                        %s
                        
                        ## 分析要求
                        请基于当前**可用的MCP工具列表**进行分析，针对用户请求进行详细的工具能力分析（仅分析，不执行）：
                        
                        ### 1. 工具匹配分析
                        - 分析每个可用工具的核心功能和适用场景
                        - 评估哪些工具能够满足用户请求的具体需求
                        - 标注每个工具的匹配度（高/中/低）
                        
                        ### 2. 工具使用指南
                        - 提供每个相关工具的具体调用方式
                        - 说明必需的参数和可选参数
                        - 给出参数的示例值和格式要求
                        
                        ### 3. 执行策略建议
                        - 推荐最优的工具组合方案
                        - 建议工具的调用顺序和依赖关系
                        - 提供备选方案和降级策略
                        
                        ### 4. 注意事项
                        - 标注工具的使用限制和约束条件
                        - 提醒可能的错误情况和处理方式
                        - 给出性能优化建议
                        
                        ### 5. 分析总结
                        - 明确说明这是分析阶段，不要执行用的任何实际操作
                        - 总结工具能力评估结果
                        - 为后续执行阶段提供建议
                        
                        请确保分析结果准确、详细、可操作，并再次强调这仅是分析阶段。""",
                input.getUserInput(),
                state.getToolListPrompt()
        );

        String mcpAnalysisResult = mcpAnalysisClient.prompt().user(mcpAnalysisPrompt).call().content();
        state.setMcpAnalysisResult(mcpAnalysisResult);

        log.info("MCP工具分析结果（仅分析，未执行实际操作）: {}", mcpAnalysisResult);

        // 发送SSE结果
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                state.getCurrentStep(),
                "analysis_tools",
                mcpAnalysisResult,
                requestParam.getSessionId());
        sendSseResult(dynamicContext, result);

        state.setCurrentStep(state.getCurrentStep() + 1);

        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity requestParam, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step2PanningNode;
    }
}
