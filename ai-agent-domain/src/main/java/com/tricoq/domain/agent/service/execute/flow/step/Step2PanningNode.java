package com.tricoq.domain.agent.service.execute.flow.step;

import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.enums.AiClientTypeEnumVO;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Step2PanningNode extends AbstractExecuteSupport {

    private final Step3ParseStepsNode step3ParseStepsNode;

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
        log.info("\n--- 步骤2: 执行步骤规划 ---");

        AiAgentClientFlowConfigDTO config = Optional
                .ofNullable(dynamicContext.getConfigMap().get(AiClientTypeEnumVO.PLANNING_CLIENT.getCode()))
                .orElseThrow();

        ChatClient planningClient = Optional
                .ofNullable(getChatClient(config.getClientId()))
                .orElseThrow();

        String planningPrompt = buildStructuredPlanningPrompt(dynamicContext.getUserInput(),
                dynamicContext.getMcpAnalysisResult());

        String refinedPrompt = planningPrompt + "\n\n## ⚠️ 工具映射验证反馈\n" +
                "\n\n**请根据上述验证反馈重新生成规划，确保：**\n" +
                "1. 只使用验证报告中列出的有效工具\n" +
                "2. 工具名称必须完全匹配（区分大小写）\n" +
                "3. 每个步骤明确指定使用的MCP工具\n" +
                "4. 避免使用不存在或无效的工具";

        String planningResult = planningClient.prompt()
                .user(refinedPrompt)
                .call()
                .content();

        dynamicContext.setPlanningResult(planningResult);

        log.info("执行步骤规划结果: {}", planningResult);

        // 发送SSE结果
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                dynamicContext.getStep(),
                "analysis_strategy",
                planningResult,
                requestParam.getSessionId());
        sendSseResult(dynamicContext, result);

        dynamicContext.setStep(dynamicContext.getStep() + 1);

        return router(requestParam, dynamicContext);

    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(
            ExecuteCommandEntity requestParam,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return step3ParseStepsNode;
    }

    /**
     * 构建结构化的规划提示词
     */
    private String buildStructuredPlanningPrompt(String userRequest, String mcpToolsAnalysis) {
        StringBuilder prompt = new StringBuilder();

        // 1. 任务分析部分 - 通用化用户需求分析
        prompt.append("# 智能执行计划生成\n\n");
        prompt.append("## 📋 用户需求分析\n");
        prompt.append("**完整用户请求：**\n");
        prompt.append("```\n");
        prompt.append(userRequest);
        prompt.append("\n```\n\n");
        prompt.append("**⚠️ 重要提醒：** 在生成执行计划时，必须完整保留和传递用户请求中的所有详细信息，包括但不限于：\n");
        prompt.append("- 任务的具体目标和期望结果\n");
        prompt.append("- 涉及的数据、参数、配置等详细信息\n");
        prompt.append("- 特定的业务规则、约束条件或要求\n");
        prompt.append("- 输出格式、质量标准或验收条件\n");
        prompt.append("- 时间要求、优先级或其他执行约束\n\n");

        // 2. 工具能力分析
        prompt.append("## 🔧 MCP工具能力分析结果\n");
        prompt.append(mcpToolsAnalysis).append("\n\n");

        // 3. 工具映射验证 - 使用动态获取的工具信息
        prompt.append("## ✅ 工具映射验证要求\n");
        prompt.append("**重要提醒：** 在生成执行步骤时，必须严格遵循以下工具映射规则：\n\n");

        // 动态获取实际的MCP工具信息
        String actualToolsInfo = getActualMcpToolsInfo();
        prompt.append("### 可用工具清单\n");
        prompt.append(actualToolsInfo).append("\n");

        prompt.append("### 工具选择原则\n");
        prompt.append("- **精确匹配**: 每个步骤必须使用上述工具清单中的确切函数名称\n");
        prompt.append("- **功能对应**: 根据MCP工具分析结果中的匹配度选择最适合的工具\n");
        prompt.append("- **参数完整**: 确保每个工具调用都包含必需的参数说明\n");
        prompt.append("- **依赖关系**: 考虑工具间的数据流转和依赖关系\n\n");

        // 4. 执行计划要求
        prompt.append("## 📝 执行计划要求\n");
        prompt.append("请基于上述用户详细需求、MCP工具分析结果和工具映射验证要求，生成精确的执行计划：\n\n");
        prompt.append("### 核心要求\n");
        prompt.append("1. **完整保留用户需求**: 必须将用户请求中的所有详细信息完整传递到每个执行步骤中\n");
        prompt.append("2. **严格遵循MCP分析结果**: 必须根据工具能力分析中的匹配度和推荐方案制定步骤\n");
        prompt.append("3. **精确工具映射**: 每个步骤必须使用确切的函数名称，不允许使用模糊或错误的工具名\n");
        prompt.append("4. **参数完整性**: 所有工具调用必须包含用户原始需求中的完整参数信息\n");
        prompt.append("5. **依赖关系明确**: 基于MCP分析结果中的执行策略建议安排步骤顺序\n");
        prompt.append("6. **合理粒度**: 避免过度细分，每个步骤应该是完整且独立的功能单元\n\n");

        // 4. 格式规范 - 通用化任务格式
        prompt.append("### 格式规范\n");
        prompt.append("请使用以下Markdown格式生成3-5个执行步骤：\n");
        prompt.append("```markdown\n");
        prompt.append("# 执行步骤规划\n\n");
        prompt.append("[ ] 第1步：[步骤描述]\n");
        prompt.append("[ ] 第2步：[步骤描述]\n");
        prompt.append("[ ] 第3步：[步骤描述]\n");
        prompt.append("...\n\n");
        prompt.append("## 步骤详情\n\n");
        prompt.append("### 第1步：[步骤描述]\n");
        prompt.append("- **优先级**: [HIGH/MEDIUM/LOW]\n");
        prompt.append("- **预估时长**: [分钟数]分钟\n");
        prompt.append("- **使用工具**: [必须使用确切的函数名称]\n");
        prompt.append("- **工具匹配度**: [引用MCP分析结果中的匹配度评估]\n");
        prompt.append("- **依赖步骤**: [前置步骤序号，如无依赖则填写'无']\n");
        prompt.append("- **执行方法**: [基于MCP分析结果的具体执行策略，包含工具调用参数]\n");
        prompt.append("- **工具参数**: [详细的参数说明和示例值，必须包含用户原始需求中的所有相关信息]\n");
        prompt.append("- **需求传递**: [明确说明如何将用户的详细要求传递到此步骤中]\n");
        prompt.append("- **预期输出**: [期望的最终结果]\n");
        prompt.append("- **成功标准**: [判断任务完成的标准]\n");
        prompt.append("- **MCP分析依据**: [引用具体的MCP工具分析结论]\n\n");
        prompt.append("```\n\n");

        // 5. 动态规划指导原则
        prompt.append("### 规划指导原则\n");
        prompt.append("请根据用户详细请求和可用工具能力，动态生成合适的执行步骤：\n");
        prompt.append("- **需求完整性原则**: 确保用户请求中的所有详细信息都被完整保留和传递\n");
        prompt.append("- **步骤分离原则**: 每个步骤应该专注于单一功能，避免混合不同类型的操作\n");
        prompt.append("- **工具映射原则**: 每个步骤应明确使用哪个具体的MCP工具\n");
        prompt.append("- **参数传递原则**: 确保用户的详细要求能够准确传递到工具参数中\n");
        prompt.append("- **依赖关系原则**: 合理安排步骤顺序，确保前置条件得到满足\n");
        prompt.append("- **结果输出原则**: 每个步骤都应有明确的输出结果和成功标准\n\n");

        // 6. 步骤类型指导
        prompt.append("### 步骤类型指导\n");
        prompt.append("根据可用工具和用户需求，常见的步骤类型包括：\n");
        prompt.append("- **数据获取步骤**: 使用搜索、查询等工具获取所需信息\n");
        prompt.append("- **数据处理步骤**: 对获取的信息进行分析、整理和加工\n");
        prompt.append("- **内容生成步骤**: 基于处理后的数据生成目标内容\n");
        prompt.append("- **结果输出步骤**: 将生成的内容发布、保存或传递给用户\n");
        prompt.append("- **通知反馈步骤**: 向用户或相关方发送执行结果通知\n\n");

        // 7. 执行要求
        prompt.append("### 执行要求\n");
        prompt.append("1. **步骤编号**: 使用第1步、第2步、第3步...格式\n");
        prompt.append("2. **Markdown格式**: 严格按照上述Markdown格式输出\n");
        prompt.append("3. **步骤描述**: 每个步骤描述要清晰、具体、可执行\n");
        prompt.append("4. **优先级**: 根据步骤重要性和紧急程度设定\n");
        prompt.append("5. **时长估算**: 基于步骤复杂度合理估算\n");
        prompt.append("6. **工具选择**: 从可用工具中选择最适合的，必须使用完整的函数名称\n");
        prompt.append("7. **依赖关系**: 明确步骤间的先后顺序\n");
        prompt.append("8. **执行细节**: 提供具体可操作的方法，包含详细的参数说明和用户需求传递\n");
        prompt.append("9. **需求传递**: 确保用户的所有详细要求都能准确传递到相应的执行步骤中\n");
        prompt.append("10. **功能独立**: 确保每个步骤功能独立，避免混合不同类型的操作\n");
        prompt.append("11. **工具映射**: 每个步骤必须明确指定使用的MCP工具函数名称\n");
        prompt.append("12. **质量标准**: 设定明确的完成标准\n\n");

        // 7. 步骤类型指导
        prompt.append("### 常见步骤类型指导\n");
        prompt.append("- **信息获取步骤**: 使用搜索工具，关注关键词选择和结果筛选\n");
        prompt.append("- **内容处理步骤**: 基于获取的信息进行分析、整理和创作\n");
        prompt.append("- **结果输出步骤**: 使用相应平台工具发布或保存处理结果\n");
        prompt.append("- **通知反馈步骤**: 使用通信工具进行状态通知或结果反馈\n");
        prompt.append("- **数据处理步骤**: 对获取的信息进行分析、转换和处理\n\n");

        // 8. 质量检查
        prompt.append("### 质量检查清单\n");
        prompt.append("生成计划后请确认：\n");
        prompt.append("- [ ] 每个步骤都有明确的序号和描述\n");
        prompt.append("- [ ] 使用了正确的Markdown格式\n");
        prompt.append("- [ ] 步骤描述清晰具体\n");
        prompt.append("- [ ] 优先级设置合理\n");
        prompt.append("- [ ] 时长估算现实可行\n");
        prompt.append("- [ ] 工具选择恰当\n");
        prompt.append("- [ ] 依赖关系清晰\n");
        prompt.append("- [ ] 执行方法具体可操作\n");
        prompt.append("- [ ] 成功标准明确可衡量\n\n");

        prompt.append("现在请开始生成Markdown格式的执行步骤规划：\n");

        return prompt.toString();
    }

    /**
     * 获取实际的MCP工具信息
     */
    private String getActualMcpToolsInfo() {
        StringBuilder toolsInfo = new StringBuilder();
        toolsInfo.append("# 当前注册的MCP工具列表\n\n");

        try {
            // 获取百度搜索工具信息
            toolsInfo.append("## 1. 百度搜索工具 (BaiduSearch)\n");
            toolsInfo.append("- **服务端点**: http://localhost:8080/mcp/baidu-search\n");
            toolsInfo.append("- **核心功能**: 通过百度搜索引擎检索技术资料和信息\n");
            toolsInfo.append("- **主要方法**: \n");
            toolsInfo.append("  - `baiduSearch(query)`: 执行百度搜索\n");
            toolsInfo.append("    - 参数: query (String) - 搜索关键词\n");
            toolsInfo.append("    - 返回: 搜索结果列表，包含标题、链接、摘要等信息\n");
            toolsInfo.append("- **适用场景**: 技术资料检索、行业信息收集、热点话题搜索\n");
            toolsInfo.append("- **调用示例**: functions.JavaSDKMCPClient_baiduSearch\n\n");

            // 获取CSDN工具信息
            toolsInfo.append("## 2. CSDN发布工具 (CSDN)\n");
            toolsInfo.append("- **服务端点**: http://localhost:8081/mcp/csdn\n");
            toolsInfo.append("- **核心功能**: 向CSDN平台发布技术文章\n");
            toolsInfo.append("- **主要方法**: \n");
            toolsInfo.append("  - `saveArticle(title, content, tags)`: 发布文章到CSDN\n");
            toolsInfo.append("    - 参数: \n");
            toolsInfo.append("      - title (String) - 文章标题\n");
            toolsInfo.append("      - content (String) - 文章内容（支持Markdown格式）\n");
            toolsInfo.append("      - tags (String) - 文章标签，多个标签用逗号分隔\n");
            toolsInfo.append("    - 返回: 发布结果，包含文章ID、发布状态等信息\n");
            toolsInfo.append("- **适用场景**: 技术文章发布、知识分享、内容创作\n");
            toolsInfo.append("- **调用示例**: functions.JavaSDKMCPClient_saveArticle\n\n");

            // 获取微信通知工具信息
            toolsInfo.append("## 3. 微信通知工具 (Weixin)\n");
            toolsInfo.append("- **服务端点**: http://localhost:8082/mcp/weixin\n");
            toolsInfo.append("- **核心功能**: 通过微信发送消息通知\n");
            toolsInfo.append("- **主要方法**: \n");
            toolsInfo.append("  - `weixinNotice(message, recipient)`: 发送微信通知\n");
            toolsInfo.append("    - 参数: \n");
            toolsInfo.append("      - message (String) - 通知消息内容\n");
            toolsInfo.append("      - recipient (String) - 接收者标识（可选）\n");
            toolsInfo.append("    - 返回: 发送结果，包含消息ID、发送状态等信息\n");
            toolsInfo.append("- **适用场景**: 任务完成通知、状态更新提醒、重要信息推送\n");
            toolsInfo.append("- **调用示例**: functions.JavaSDKMCPClient_weixinNotice\n\n");

            // 添加工具组合使用建议
            toolsInfo.append("## 工具组合使用模式\n");
            toolsInfo.append("### 典型工作流程\n");
            toolsInfo.append("1. **信息收集阶段**: 使用BaiduSearch检索相关技术资料\n");
            toolsInfo.append("2. **内容创作阶段**: 基于搜索结果整理和创作技术文章\n");
            toolsInfo.append("3. **内容发布阶段**: 使用CSDN工具发布文章到平台\n");
            toolsInfo.append("4. **通知推送阶段**: 使用Weixin工具发送完成通知\n\n");

            toolsInfo.append("### 注意事项\n");
            toolsInfo.append("- 所有工具调用都需要使用完整的函数名称格式\n");
            toolsInfo.append("- 参数传递需要符合JSON格式要求\n");
            toolsInfo.append("- 建议在工具调用间添加适当的延时以避免频率限制\n");
            toolsInfo.append("- 每个工具都有独立的错误处理机制\n");

        } catch (Exception e) {
            log.warn("获取MCP工具信息时发生错误: {}", e.getMessage());
            toolsInfo.append("\n⚠️ 注意: 部分工具信息获取失败，请检查MCP服务连接状态\n");
        }

        return toolsInfo.toString();
    }
}
