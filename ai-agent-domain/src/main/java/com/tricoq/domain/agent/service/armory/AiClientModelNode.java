package com.tricoq.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author trico qiang
 * @date 10/27/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiClientModelNode extends AbstractArmorySupport {

    private final AiClientAdvisorNode aiClientAdvisorNode;

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，Mode 对话模型{}", JSON.toJSONString(requestParam));
        List<AiClientModelVO> modelNodes = dynamicContext.getValue(dataName());

        if (CollectionUtils.isEmpty(modelNodes)) {
            log.warn("没有需要被初始化的 ai client model");
            return router(requestParam, dynamicContext);
        }

        for (AiClientModelVO modelNode : modelNodes) {
            OpenAiApi openAiApi = getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(modelNode.getApiId()));
            if (null == openAiApi) {
                throw new RuntimeException("mode 2 api is null");
            }

            //mcpTools应该在chatClient层配置，chatModel应该是作为通用工具，得保证灵活性
//            List<String> toolMcpIds = modelNode.getToolMcpIds();
//            List<McpSyncClient> list = toolMcpIds.stream().map(id ->
//                    (McpSyncClient) getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id))).toList();

            ChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(modelNode.getModelName())
//                            .toolCallbacks(new SyncMcpToolCallbackProvider(list).getToolCallbacks())
                            .build())
                    .build();
            registerBean(beanName(modelNode.getModelId()), ChatModel.class, chatModel);
        }
        return router(requestParam, dynamicContext);
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getDataName();
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return aiClientAdvisorNode;
    }
}
