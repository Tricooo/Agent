package com.tricoq.infrastructure.adapter.repository;

import com.alibaba.fastjson2.TypeReference;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.valobj.AiAgentEnumVO;
import com.tricoq.domain.agent.model.valobj.AiClientAdvisorVO;
import com.tricoq.domain.agent.model.valobj.AiClientApiVO;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.domain.agent.model.valobj.AiClientSystemPromptVO;
import com.tricoq.domain.agent.model.valobj.AiClientToolMcpVO;
import com.tricoq.domain.agent.model.valobj.AiClientVO;
import com.tricoq.infrastructure.dao.IAiClientAdvisorDao;
import com.tricoq.infrastructure.dao.IAiClientApiDao;
import com.tricoq.infrastructure.dao.IAiClientConfigDao;
import com.tricoq.infrastructure.dao.IAiClientDao;
import com.tricoq.infrastructure.dao.IAiClientModelDao;
import com.tricoq.infrastructure.dao.IAiClientSystemPromptDao;
import com.tricoq.infrastructure.dao.IAiClientToolMcpDao;
import com.tricoq.infrastructure.dao.po.AiClient;
import com.tricoq.infrastructure.dao.po.AiClientAdvisor;
import com.tricoq.infrastructure.dao.po.AiClientApi;
import com.tricoq.infrastructure.dao.po.AiClientConfig;
import com.tricoq.infrastructure.dao.po.AiClientModel;
import com.tricoq.infrastructure.dao.po.AiClientSystemPrompt;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author trico qiang
 * @date 10/23/25
 */
@Repository
@RequiredArgsConstructor
public class AgentRepository implements IAgentRepository {

    private final IAiClientDao aiClientDao;

    private final IAiClientConfigDao aiClientConfigDao;

    private final IAiClientModelDao aiClientModelDao;

    private final IAiClientApiDao aiClientApiDao;

    private final IAiClientToolMcpDao aiClientToolMcpDao;

    private final IAiClientSystemPromptDao aiClientSystemPromptDao;

    private final IAiClientAdvisorDao aiClientAdvisorDao;

    @Override
    public List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        //linkedHashSet和distinct都会保持去重后元素的顺序
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        //mybatis默认会返回空集合 如果用stream处理无需判空
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> modelIds = aiClientConfigs.stream()
                .filter(config ->
                        config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_MODEL.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (modelIds.isEmpty()) {
            return List.of();
        }
        List<AiClientModel> models = aiClientModelDao.queryByIds(modelIds);
        Set<String> apiIds = models.stream()
                .map(AiClientModel::getApiId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (apiIds.isEmpty()) {
            return List.of();
        }

        List<AiClientApi> apis = aiClientApiDao.queryByApiIdsEnabled(apiIds);
        return apis.stream().map(api ->
                        AiClientApiVO.builder()
                                .apiId(api.getApiId())
                                .baseUrl(api.getBaseUrl())
                                .apiKey(api.getApiKey())
                                .completionsPath(api.getCompletionsPath())
                                .embeddingsPath(api.getEmbeddingsPath())
                                .build())
                .toList();
    }

    @Override
    public List<AiClientModelVO> queryAiClientModelVOByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> modelIds = aiClientConfigs.stream()
                .filter(config ->
                        config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_MODEL.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (modelIds.isEmpty()) {
            return List.of();
        }
        List<AiClientModel> models = aiClientModelDao.queryByIds(modelIds);
        return models.stream().map(model -> AiClientModelVO.builder()
                        .modelId(model.getModelId())
                        .apiId(model.getApiId())
                        .modelName(model.getModelName())
                        .modelType(model.getModelType())
                        //.toolMcpIds(toolMcpIds)
                        .build())
                .toList();
    }

    @Override
    public List<AiClientToolMcpVO> queryAiClientToolMcpVOByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> mcpToolIds = aiClientConfigs.stream()
                .filter(config ->
                        config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (mcpToolIds.isEmpty()) {
            return List.of();
        }
        List<AiClientToolMcp> mcps = aiClientToolMcpDao.queryByMcpIdsEnabled(mcpToolIds);
        return mcps.stream().map(mcp -> {
            String transportConfig = mcp.getTransportConfig();
            if (!StringUtils.hasText(transportConfig)) {
                return null;
            }
            AiClientToolMcpVO mcpVO = AiClientToolMcpVO.builder()
                    .mcpId(mcp.getMcpId())
                    .mcpName(mcp.getMcpName())
                    .transportType(mcp.getTransportType())
                    .transportConfig(transportConfig)
                    .requestTimeout(mcp.getRequestTimeout())
                    .build();
            switch (mcp.getTransportType()) {
                case "sse" -> {
                    AiClientToolMcpVO.TransportConfigSse seeConfig =
                            new TypeReference<AiClientToolMcpVO.TransportConfigSse>() {
                            }.parseObject(transportConfig);
                    mcpVO.setTransportConfigSse(seeConfig);
                    return mcpVO;
                }
                case "stdio" -> {
                    Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap =
                            new TypeReference<Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio>>() {
                            }.parseObject(transportConfig);
                    mcpVO.setTransportConfigStdio(new AiClientToolMcpVO.TransportConfigStdio(stdioMap));
                    return mcpVO;
                }
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public Map<String, AiClientSystemPromptVO> queryAiClientSystemPromptVOByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return Map.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> systemPromptIds = aiClientConfigs.stream()
                .filter(config ->
                        config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (systemPromptIds.isEmpty()) {
            return Map.of();
        }
        List<AiClientSystemPrompt> systemPrompts = aiClientSystemPromptDao.queryByIdsPromptsEnabled(systemPromptIds);
        return systemPrompts.stream().map(prompt ->
                        AiClientSystemPromptVO.builder()
                                .promptId(prompt.getPromptId())
                                .promptName(prompt.getPromptName())
                                .promptContent(prompt.getPromptContent())
                                .description(prompt.getDescription())
                                .build())
                .collect(Collectors.toMap(AiClientSystemPromptVO::getPromptId, Function.identity()));
    }

    @Override
    public List<AiClientAdvisorVO> queryAiClientAdvisorVOByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> advisorIds = aiClientConfigs.stream()
                .filter(config ->
                        config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_ADVISOR.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (advisorIds.isEmpty()) {
            return List.of();
        }
        List<AiClientAdvisor> advisors = aiClientAdvisorDao.queryByAdvisorIdsEnabled(advisorIds);
        return advisors.stream().map(advisor -> {
            String advisorType = advisor.getAdvisorType();
            if (!StringUtils.hasText(advisorType)) {
                return null;
            }
            AiClientAdvisorVO advisorVO = AiClientAdvisorVO.builder()
                    .advisorId(advisor.getAdvisorId())
                    .advisorName(advisor.getAdvisorName())
                    .advisorType(advisorType)
                    .orderNum(advisor.getOrderNum())
                    .build();
            String extParam = advisor.getExtParam();
            switch (advisorType) {
                case "ChatMemory" -> {
                    AiClientAdvisorVO.ChatMemory chatMemory = new TypeReference<AiClientAdvisorVO.ChatMemory>() {
                    }.parseObject(extParam);
                    advisorVO.setChatMemory(chatMemory);
                    return advisorVO;
                }
                case "RagAnswer" -> {
                    AiClientAdvisorVO.RagAnswer ragAnswer = new TypeReference<AiClientAdvisorVO.RagAnswer>() {
                    }.parseObject(extParam);
                    advisorVO.setRagAnswer(ragAnswer);
                    return advisorVO;
                }
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<AiClientVO> queryAiClientVOByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Map<String, List<AiClientConfig>> clientId2ConfigMap = aiClientConfigs.stream()
                .collect(Collectors.groupingBy(AiClientConfig::getSourceId));
        List<AiClient> clients = aiClientDao.queryByClientIdEnabled(clientIdSet);
        return clients.stream().map(client -> {
            AiClientVO aiClientVO = AiClientVO.builder()
                    .clientId(client.getClientId())
                    .clientName(client.getClientName())
                    .description(client.getDescription())
                    .build();
            List<AiClientConfig> clientConfigs = clientId2ConfigMap.get(client.getClientId());
            if (clientId2ConfigMap.isEmpty() || CollectionUtils.isEmpty(clientConfigs)) {
                return aiClientVO;
            }
            Map<String, List<AiClientConfig>> target2ConfigMap = clientConfigs.stream()
                    .collect(Collectors.groupingBy(AiClientConfig::getTargetType));
            target2ConfigMap.forEach((type, configs) -> {
                if (CollectionUtils.isEmpty(configs)) {
                    return;
                }
                List<String> targetIds = configs.stream().map(AiClientConfig::getTargetId).toList();
                switch (type) {
                    case "mcp" -> {
                        aiClientVO.setMcpIdList(targetIds);
                    }
                    case "advisor" -> {
                        aiClientVO.setAdvisorIdList(targetIds);
                    }
                    case "prompt" -> {
                        aiClientVO.setPromptIdList(targetIds);
                    }
                    case "model" -> {
                        aiClientVO.setModelId(targetIds.stream().findFirst().orElseThrow());
                    }
                }
            });
            return aiClientVO;
        }).toList();
    }

    @Override
    public List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIdList) {
        if (CollectionUtils.isEmpty(modelIdList)) {
            return List.of();
        }
        Set<String> modelIdSet = new HashSet<>(modelIdList);
        List<AiClientModel> aiClientModels = aiClientModelDao.queryByIds(modelIdSet);
        Set<String> apiIdSet = aiClientModels.stream().map(AiClientModel::getApiId).collect(Collectors.toSet());
        if (apiIdSet.isEmpty()) {
            return List.of();
        }
        List<AiClientApi> apis = aiClientApiDao.queryByApiIdsEnabled(apiIdSet);
        return apis.stream().map(api ->
                AiClientApiVO.builder()
                        .apiId(api.getApiId())
                        .baseUrl(api.getBaseUrl())
                        .apiKey(api.getApiKey())
                        .completionsPath(api.getCompletionsPath())
                        .embeddingsPath(api.getEmbeddingsPath())
                        .build()
        ).toList();
    }

    @Override
    public List<AiClientModelVO> queryAiClientModelVOByModelIds(List<String> modelIdList) {
        if (CollectionUtils.isEmpty(modelIdList)) {
            return List.of();
        }
        Set<String> modelIdSet = new HashSet<>(modelIdList);
        List<AiClientModel> aiClientModels = aiClientModelDao.queryByIds(modelIdSet);
        return aiClientModels.stream().map(model -> AiClientModelVO.builder()
                .modelId(model.getModelId())
                .apiId(model.getApiId())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .build()).toList();
    }
}
