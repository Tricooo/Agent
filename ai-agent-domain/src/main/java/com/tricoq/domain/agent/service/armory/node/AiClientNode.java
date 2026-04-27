package com.tricoq.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.enums.AiClientAdvisorTypeEnumVO;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.agent.spi.AiClientRuntimeRegistry;
import com.tricoq.types.framework.chain.StrategyHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author trico qiang
 * @date 10/28/25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiClientNode extends AbstractArmorySupport {

    private static final Comparator<AiClientAdvisorDTO> ADVISOR_CONFIG_ORDER =
            Comparator.comparing(AiClientAdvisorDTO::getOrderNum, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(AiClientAdvisorDTO::getAdvisorId, Comparator.nullsLast(String::compareTo));

    private final AiClientRuntimeRegistry runtimeRegistry;

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

        List<AiClientDTO> clients = dynamicContext.getClients();
        if (CollectionUtils.isEmpty(clients)) {
            return router(requestParam, dynamicContext);
        }
        Map<String, AiClientAdvisorDTO> advisorConfigMap = dynamicContext.getAdvisorConfigMap();
        Map<String, AiClientSystemPromptDTO> promptMap = dynamicContext.getSystemPromptMap();
        Map<String, AiClientModelDTO> modelConfigMap = dynamicContext.getClientModels().stream()
                .filter(modelConfig -> StringUtils.hasText(modelConfig.getModelId()))
                .collect(Collectors.toMap(AiClientModelDTO::getModelId,
                        modelConfig -> modelConfig,
                        (existing, ignored) -> existing));

        for (AiClientDTO client : clients) {
            ChatModel model = getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(client.getModelId()));
            ChatClient.Builder clientBuilder = ChatClient.builder(model);

            AiClientRuntimeProfile.AiClientRuntimeProfileBuilder profileBuilder = AiClientRuntimeProfile.builder();
            profileBuilder.clientId(client.getClientId());

            //系统提示词构建
            List<String> promptIds = client.getPromptIdList();
            if (CollectionUtils.isNotEmpty(promptIds) && MapUtils.isNotEmpty(promptMap)) {
                String prompt = promptIds.stream()
                        .map(promptMap::get)
                        .filter(Objects::nonNull)
                        .map(AiClientSystemPromptDTO::getPromptContent)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(System.lineSeparator()));

                if (StringUtils.hasText(prompt)) {
                    clientBuilder.defaultSystem("AI 智能体" + System.lineSeparator() + prompt);
                    profileBuilder.systemPromptEnabled(true);
                }
            }
            //mcp工具构建
            List<String> toolMcpIds = client.getMcpIdList();
            boolean clientMcpEnabled = false;
            if (CollectionUtils.isNotEmpty(toolMcpIds)) {
                List<McpSyncClient> mcpTools = toolMcpIds.stream()
                        .map(id -> (McpSyncClient) getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id)))
                        .filter(Objects::nonNull)
                        .toList();
                if (CollectionUtils.isNotEmpty(mcpTools)) {
                    clientBuilder.defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpTools).getToolCallbacks());
                    clientMcpEnabled = true;
                }
            }
            if (clientMcpEnabled || hasModelLevelMcp(client, modelConfigMap)) {
                profileBuilder.mcpEnabled(true);
            }

            //构建顾问
            List<String> advisorIdList = client.getAdvisorIdList();
            if (CollectionUtils.isNotEmpty(advisorIdList)) {
                List<AiClientAdvisorDTO> sortedAdvisorConfigs = resolveSortedAdvisorConfigs(advisorIdList, advisorConfigMap);
                List<Advisor> advisors = sortedAdvisorConfigs.stream()
                        .map(config -> (Advisor) getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(config.getAdvisorId())))
                        .filter(Objects::nonNull)
                        .toList();
                if (CollectionUtils.isNotEmpty(advisors)) {
                    clientBuilder.defaultAdvisors(advisors);
                    Set<String> advisorTypes = sortedAdvisorConfigs.stream()
                            .map(AiClientAdvisorDTO::getAdvisorType)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    profileBuilder.advisorTypes(advisorTypes);
                    enrichRuntimeProfile(profileBuilder, sortedAdvisorConfigs);
                }
            }

            ChatClient chatClient = clientBuilder.build();
            registerBean(beanName(client.getClientId()), ChatClient.class, chatClient);

            AiClientRuntimeProfile runtimeProfile = profileBuilder.build();
            runtimeRegistry.register(client.getClientId(), runtimeProfile);
        }
        return router(requestParam, dynamicContext);
    }

    private List<AiClientAdvisorDTO> resolveSortedAdvisorConfigs(List<String> advisorIdList,
                                                                 Map<String, AiClientAdvisorDTO> advisorConfigMap) {
        if (CollectionUtils.isEmpty(advisorIdList) || MapUtils.isEmpty(advisorConfigMap)) {
            return List.of();
        }

        return advisorIdList.stream()
                .map(advisorConfigMap::get)
                .filter(Objects::nonNull)
                .sorted(ADVISOR_CONFIG_ORDER)
                .toList();
    }

    private boolean hasModelLevelMcp(AiClientDTO client, Map<String, AiClientModelDTO> modelConfigMap) {
        if (client == null || MapUtils.isEmpty(modelConfigMap)) {
            return false;
        }
        AiClientModelDTO modelConfig = modelConfigMap.get(client.getModelId());
        return modelConfig != null && CollectionUtils.isNotEmpty(modelConfig.getToolMcpIds());
    }

    private void enrichRuntimeProfile(AiClientRuntimeProfile.AiClientRuntimeProfileBuilder profileBuilder,
                                      List<AiClientAdvisorDTO> advisorConfigs) {
        for (AiClientAdvisorDTO advisorConfig : advisorConfigs) {
            AiClientAdvisorTypeEnumVO type = AiClientAdvisorTypeEnumVO.getByCode(advisorConfig.getAdvisorType());
            type.enrichRuntimeProfile(profileBuilder, advisorConfig);
        }
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
