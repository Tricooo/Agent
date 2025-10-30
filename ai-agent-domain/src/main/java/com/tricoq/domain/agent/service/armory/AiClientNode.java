package com.tricoq.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientSystemPromptVO;
import com.tricoq.domain.agent.model.valobj.AiClientVO;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author trico qiang
 * @date 10/28/25
 */
@Component
@Slf4j
public class AiClientNode extends AbstractArmorySupport {
    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建节点，客户端{}", JSON.toJSONString(requestParam));

        List<AiClientVO> clients = dynamicContext.getValue(dataName());
        if (CollectionUtils.isEmpty(clients)) {
            return router(requestParam, dynamicContext);
        }
        for (AiClientVO client : clients) {
            ChatModel model = getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(client.getModelId()));
            ChatClient.Builder clientBuilder = ChatClient.builder(model);

            //系统提示词构建
            Map<String, AiClientSystemPromptVO> promptMap = dynamicContext
                    .getValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName());
            List<String> promptIds = client.getPromptIdList();
            if (CollectionUtils.isNotEmpty(promptIds) && MapUtils.isNotEmpty(promptMap)) {
                String prompt = promptIds.stream()
                        .map(promptMap::get)
                        .filter(Objects::nonNull)
                        .map(AiClientSystemPromptVO::getPromptContent)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(System.lineSeparator()));

                if (StringUtils.hasText(prompt)) {
                    clientBuilder.defaultSystem("AI 智能体" + System.lineSeparator() + prompt);
                }
            }
            //mcp工具构建
            List<String> toolMcpIds = client.getMcpIdList();
            if (CollectionUtils.isNotEmpty(toolMcpIds)) {
                List<McpSyncClient> mcpTools = toolMcpIds.stream()
                        .map(id -> (McpSyncClient) getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id)))
                        .filter(Objects::nonNull)
                        .toList();
                if (CollectionUtils.isNotEmpty(mcpTools)) {
                    clientBuilder.defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpTools).getToolCallbacks());
                }
            }

            //构建顾问
            List<String> advisorIdList = client.getAdvisorIdList();
            //Optional.of--确信值不为null，否则会抛出异常 不过Optional适合处理可能为空的单个值（局部的简单处理的值也不要用）,以后应该注意
            //用于方法返回值比较常见
            if (CollectionUtils.isNotEmpty(advisorIdList)) {
                List<Advisor> advisors = advisorIdList.stream()
                        .map(id -> (Advisor) getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(id)))
                        .filter(Objects::nonNull)
                        .toList();
                if (CollectionUtils.isNotEmpty(advisors)) {
                    clientBuilder.defaultAdvisors(advisors);
                }
            }

            ChatClient chatClient = clientBuilder.build();
            registerBean(beanName(client.getClientId()), ChatClient.class, chatClient);
        }
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return getDefaultHandler();
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT.getDataName();
    }
}
