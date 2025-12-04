package com.tricoq.infrastructure.adapter.repository.root;

import com.alibaba.fastjson2.TypeReference;
import com.tricoq.domain.agent.model.aggregate.AiClientAggregate;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientApiDTO;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.adapter.repository.IClientRepository;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.infrastructure.dao.IAiClientDao;
import com.tricoq.infrastructure.dao.po.AiClient;
import com.tricoq.infrastructure.dao.po.AiClientAdvisor;
import com.tricoq.infrastructure.dao.po.AiClientApi;
import com.tricoq.infrastructure.dao.po.AiClientConfig;
import com.tricoq.infrastructure.dao.po.AiClientModel;
import com.tricoq.infrastructure.dao.po.AiClientSystemPrompt;
import com.tricoq.infrastructure.dao.po.AiClientToolMcp;
import com.tricoq.infrastructure.support.AiClientAdvisorDaoSupport;
import com.tricoq.infrastructure.support.AiClientApiDaoSupport;
import com.tricoq.infrastructure.support.AiClientConfigDaoSupport;
import com.tricoq.infrastructure.support.AiClientModelDaoSupport;
import com.tricoq.infrastructure.support.AiClientDaoSupport;
import com.tricoq.infrastructure.support.AiClientSystemPromptDaoSupport;
import com.tricoq.infrastructure.support.AiClientToolMcpDaoSupport;
import com.tricoq.types.common.DrawConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author trico qiang
 * @date 11/25/25
 */
@Repository
@RequiredArgsConstructor
public class ClientRepositoryImpl extends MpAggregateRepository<AiClientAggregate, AiClient, String, Long, IAiClientDao>
        implements IClientRepository {

    private final AiClientDaoSupport clientDaoSupport;
    private final AiClientConfigDaoSupport clientConfigDaoSupport;
    private final AiClientModelDaoSupport clientModelDaoSupport;
    private final AiClientApiDaoSupport clientApiDaoSupport;
    private final AiClientToolMcpDaoSupport toolMcpDaoSupport;
    private final AiClientSystemPromptDaoSupport systemPromptDaoSupport;
    private final AiClientAdvisorDaoSupport clientAdvisorDaoSupport;

    @Override
    public List<AiClientApiDTO> queryAiClientApisByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> modelIds = aiClientConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_MODEL.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (modelIds.isEmpty()) {
            return List.of();
        }
        List<AiClientModel> models = clientModelDaoSupport.queryByIds(modelIds);
        Set<String> apiIds = models.stream()
                .map(AiClientModel::getApiId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (apiIds.isEmpty()) {
            return List.of();
        }

        List<AiClientApi> apis = clientApiDaoSupport.queryByApiIdsEnabled(apiIds);
        return apis.stream().map(api ->
                        AiClientApiDTO.builder()
                                .apiId(api.getApiId())
                                .baseUrl(api.getBaseUrl())
                                .apiKey(api.getApiKey())
                                .completionsPath(api.getCompletionsPath())
                                .embeddingsPath(api.getEmbeddingsPath())
                                .build())
                .toList();
    }

    @Override
    public List<AiClientModelDTO> queryAiClientModelsByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> modelIds = aiClientConfigs.stream()
                .filter(config -> AiAgentEnumVO.AI_CLIENT_MODEL.getCode().equals(config.getTargetType()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (modelIds.isEmpty()) {
            return List.of();
        }
        List<AiClientConfig> modelConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT_MODEL.getCode(), modelIds);
        Map<String, List<String>> toolMcpIdMap = modelConfigs.stream()
                .filter(config -> AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getCode().equals(config.getTargetType()))
                .collect(Collectors.groupingBy(AiClientConfig::getSourceId,
                        Collectors.mapping(AiClientConfig::getTargetId, Collectors.toList())));
        List<AiClientModel> models = clientModelDaoSupport.queryByIds(modelIds);
        return models.stream().map(model -> AiClientModelDTO.builder()
                        .modelId(model.getModelId())
                        .apiId(model.getApiId())
                        .modelName(model.getModelName())
                        .modelType(model.getModelType())
                        .toolMcpIds(toolMcpIdMap.getOrDefault(model.getModelId(), List.of()))
                        .build())
                .toList();
    }

    @Override
    public List<AiClientToolMcpDTO> queryAiClientToolMcpsByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> clientMcpIdSet = aiClientConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());

        Set<String> modelIdSet = aiClientConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_MODEL.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        List<AiClientConfig> modelConfigs = new ArrayList<>();
        if (!modelIdSet.isEmpty()) {
            modelConfigs.addAll(clientConfigDaoSupport
                    .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT_MODEL.getCode(), modelIdSet));
        }
        Set<String> modelMcpIdSet = modelConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        Set<String> mcpIdSet = Stream.of(clientMcpIdSet, modelMcpIdSet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        List<AiClientToolMcp> mcps = toolMcpDaoSupport.queryByMcpIdsEnabled(mcpIdSet);
        return mcps.stream().map(mcp -> {
            String transportConfig = mcp.getTransportConfig();
            if (!StringUtils.hasText(transportConfig)) {
                return null;
            }
            AiClientToolMcpDTO mcpVO = AiClientToolMcpDTO.builder()
                    .mcpId(mcp.getMcpId())
                    .mcpName(mcp.getMcpName())
                    .transportType(mcp.getTransportType())
                    .transportConfig(transportConfig)
                    .requestTimeout(mcp.getRequestTimeout())
                    .build();
            switch (mcp.getTransportType()) {
                case "sse" -> {
                    AiClientToolMcpDTO.TransportConfigSse seeConfig =
                            new TypeReference<AiClientToolMcpDTO.TransportConfigSse>() {
                            }.parseObject(transportConfig);
                    mcpVO.setTransportConfigSse(seeConfig);
                    return mcpVO;
                }
                case "stdio" -> {
                    Map<String, AiClientToolMcpDTO.TransportConfigStdio.Stdio> stdioMap =
                            new TypeReference<Map<String, AiClientToolMcpDTO.TransportConfigStdio.Stdio>>() {
                            }.parseObject(transportConfig);
                    mcpVO.setTransportConfigStdio(new AiClientToolMcpDTO.TransportConfigStdio(stdioMap));
                    return mcpVO;
                }
                default -> {
                    return null;
                }
            }
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public Map<String, AiClientSystemPromptDTO> queryAiClientSystemPromptsByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return Map.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> systemPromptIds = aiClientConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (systemPromptIds.isEmpty()) {
            return Map.of();
        }
        List<AiClientSystemPrompt> systemPrompts = systemPromptDaoSupport.queryByIdsPromptsEnabled(systemPromptIds);
        return systemPrompts.stream().map(prompt ->
                        AiClientSystemPromptDTO.builder()
                                .promptId(prompt.getPromptId())
                                .promptName(prompt.getPromptName())
                                .promptContent(prompt.getPromptContent())
                                .description(prompt.getDescription())
                                .build())
                .collect(Collectors.toMap(AiClientSystemPromptDTO::getPromptId, Function.identity()));
    }

    @Override
    public List<AiClientAdvisorDTO> queryAiClientAdvisorsByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Set<String> advisorIds = aiClientConfigs.stream()
                .filter(config -> config.getTargetType().equals(AiAgentEnumVO.AI_CLIENT_ADVISOR.getCode()))
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toSet());
        if (advisorIds.isEmpty()) {
            return List.of();
        }
        List<AiClientAdvisor> advisors = clientAdvisorDaoSupport.queryByAdvisorIdsEnabled(advisorIds);
        return advisors.stream().map(advisor -> {
            String advisorType = advisor.getAdvisorType();
            if (!StringUtils.hasText(advisorType)) {
                return null;
            }
            AiClientAdvisorDTO advisorVO = AiClientAdvisorDTO.builder()
                    .advisorId(advisor.getAdvisorId())
                    .advisorName(advisor.getAdvisorName())
                    .advisorType(advisorType)
                    .orderNum(advisor.getOrderNum())
                    .build();
            String extParam = advisor.getExtParam();
            switch (advisorType) {
                case "ChatMemory" -> {
                    AiClientAdvisorDTO.ChatMemory chatMemory = new TypeReference<AiClientAdvisorDTO.ChatMemory>() {
                    }.parseObject(extParam);
                    advisorVO.setChatMemory(chatMemory);
                    return advisorVO;
                }
                case "RagAnswer" -> {
                    AiClientAdvisorDTO.RagAnswer ragAnswer = new TypeReference<AiClientAdvisorDTO.RagAnswer>() {
                    }.parseObject(extParam);
                    advisorVO.setRagAnswer(ragAnswer);
                    return advisorVO;
                }
                default -> {
                    return null;
                }
            }
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<AiClientDTO> queryAiClientsByClientIds(List<String> clientIdList) {
        if (CollectionUtils.isEmpty(clientIdList)) {
            return List.of();
        }
        Set<String> clientIdSet = new HashSet<>(clientIdList);
        List<AiClientConfig> aiClientConfigs = clientConfigDaoSupport
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdSet);
        Map<String, List<AiClientConfig>> clientId2ConfigMap = aiClientConfigs.stream()
                .collect(Collectors.groupingBy(AiClientConfig::getSourceId));
        List<AiClient> clients = clientDaoSupport.queryByClientIdEnabled(clientIdSet);
        return clients.stream().map(client -> {
            AiClientDTO aiClientVO = AiClientDTO.builder()
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
                    case "tool_mcp" -> aiClientVO.setMcpIdList(targetIds);
                    case "advisor" -> aiClientVO.setAdvisorIdList(targetIds);
                    case "prompt" -> aiClientVO.setPromptIdList(targetIds);
                    case "model" -> aiClientVO.setModelId(targetIds.stream().findFirst().orElse(null));
                    default -> {
                    }
                }
            });
            return aiClientVO;
        }).toList();
    }

    @Override
    public List<AiClientApiDTO> queryAiClientApisByModelIds(List<String> modelIdList) {
        if (CollectionUtils.isEmpty(modelIdList)) {
            return List.of();
        }
        Set<String> modelIdSet = new HashSet<>(modelIdList);
        List<AiClientModel> aiClientModels = clientModelDaoSupport.queryByIds(modelIdSet);
        Set<String> apiIdSet = aiClientModels.stream().map(AiClientModel::getApiId).collect(Collectors.toSet());
        if (apiIdSet.isEmpty()) {
            return List.of();
        }
        List<AiClientApi> apis = clientApiDaoSupport.queryByApiIdsEnabled(apiIdSet);
        return apis.stream().map(api ->
                AiClientApiDTO.builder()
                        .apiId(api.getApiId())
                        .baseUrl(api.getBaseUrl())
                        .apiKey(api.getApiKey())
                        .completionsPath(api.getCompletionsPath())
                        .embeddingsPath(api.getEmbeddingsPath())
                        .build()
        ).toList();
    }

    @Override
    public List<AiClientModelDTO> queryAiClientModelsByModelIds(List<String> modelIdList) {
        if (CollectionUtils.isEmpty(modelIdList)) {
            return List.of();
        }
        Set<String> modelIdSet = new HashSet<>(modelIdList);
        List<AiClientModel> aiClientModels = clientModelDaoSupport.queryByIds(modelIdSet);
        return aiClientModels.stream().map(model -> AiClientModelDTO.builder()
                .modelId(model.getModelId())
                .apiId(model.getApiId())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .build()).toList();
    }

    @Override
    public boolean saveOrUpdateClientConfigByAggregate(List<AiClientAggregate> aiClientAggregates, String extraParams) {
        List<AiClientConfig> saves = new ArrayList<>();
        for (AiClientAggregate clientAggregate : aiClientAggregates) {
            String clientId = clientAggregate.getClientId();
            saves.addAll(clientAggregate.getAdvisorIds().stream().map(advisor -> AiClientConfig.builder()
                            .sourceType(DrawConstants.NodeTypeConstants.CLIENT)
                            .sourceId(clientId)
                            .targetType(DrawConstants.NodeTypeConstants.ADVISOR)
                            .targetId(advisor)
                            .configId(clientAggregate.getConfigId())
                            .extParam(extraParams)
                            .status(1)
                            .build())
                    .toList());
            saves.addAll(clientAggregate.getMcpIds().stream().map(mcp -> AiClientConfig.builder()
                            .sourceType(DrawConstants.NodeTypeConstants.CLIENT)
                            .sourceId(clientId)
                            .targetType(DrawConstants.NodeTypeConstants.TOOL_MCP)
                            .targetId(mcp)
                            .configId(clientAggregate.getConfigId())
                            .extParam(extraParams)
                            .status(1)
                            .build())
                    .toList());
            saves.addAll(clientAggregate.getPromptIds().stream().map(prompt -> AiClientConfig.builder()
                            .sourceType(DrawConstants.NodeTypeConstants.CLIENT)
                            .sourceId(clientId)
                            .targetType(DrawConstants.NodeTypeConstants.PROMPT)
                            .targetId(prompt)
                            .configId(clientAggregate.getConfigId())
                            .extParam(extraParams)
                            .status(1)
                            .build())
                    .toList());
            AiClientModelVO model = clientAggregate.getModel();
            saves.addAll(model.getToolMcpIds().stream().map(mcp -> AiClientConfig.builder()
                            .sourceType(DrawConstants.NodeTypeConstants.MODEL)
                            .sourceId(model.getModelId())
                            .targetType(DrawConstants.NodeTypeConstants.TOOL_MCP)
                            .targetId(mcp)
                            .configId(clientAggregate.getConfigId())
                            .extParam(extraParams)
                            .status(1)
                            .build())
                    .toList());
        }
        return clientConfigDaoSupport.saveBatch(saves);
    }

    @Override
    public boolean insertClient(AiClientDTO clientDTO) {
        if (clientDTO == null) {
            return false;
        }
        AiClient po = toClientPo(clientDTO);
        LocalDateTime now = LocalDateTime.now();
        if (po.getCreateTime() == null) {
            po.setCreateTime(now);
        }
        po.setUpdateTime(now);
        return save(po);
    }

    @Override
    public boolean updateClientById(AiClientDTO clientDTO) {
        if (clientDTO == null || clientDTO.getId() == null) {
            return false;
        }
        AiClient po = toClientPo(clientDTO);
        po.setId(clientDTO.getId());
        po.setUpdateTime(LocalDateTime.now());
        return this.updateById(po);
    }

    @Override
    public boolean updateClientByClientId(AiClientDTO clientDTO) {
        if (clientDTO == null || !StringUtils.hasText(clientDTO.getClientId())) {
            return false;
        }
        AiClient po = toClientPo(clientDTO);
        po.setUpdateTime(LocalDateTime.now());
        return this.baseMapper.updateByClientId(po) > 0;
    }

    @Override
    public boolean deleteClientById(Long id) {
        return id != null && this.removeById(id);
    }

    @Override
    public boolean deleteClientByClientId(String clientId) {
        return StringUtils.hasText(clientId) && this.baseMapper.deleteByClientId(clientId) > 0;
    }

    @Override
    public AiClientDTO queryClientById(Long id) {
        if (id == null) {
            return null;
        }
        return toClientDto(this.getById(id));
    }

    @Override
    public AiClientDTO queryClientByClientId(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        return toClientDto(this.baseMapper.queryByClientId(clientId));
    }

    @Override
    public List<AiClientDTO> queryClientsByName(String clientName) {
        if (!StringUtils.hasText(clientName)) {
            return List.of();
        }
        return this.baseMapper.queryByClientName(clientName).stream()
                .map(this::toClientDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientDTO> queryAllClients() {
        return this.baseMapper.queryAll().stream()
                .map(this::toClientDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AiClientDTO> queryEnabledClients() {
        return this.baseMapper.queryEnabledClients().stream()
                .map(this::toClientDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private AiClientDTO toClientDto(AiClient aiClient) {
        if (aiClient == null) {
            return null;
        }
        return AiClientDTO.builder()
                .id(aiClient.getId())
                .clientId(aiClient.getClientId())
                .clientName(aiClient.getClientName())
                .description(aiClient.getDescription())
                .status(aiClient.getStatus())
                .createTime(aiClient.getCreateTime())
                .updateTime(aiClient.getUpdateTime())
                .build();
    }

    private AiClient toClientPo(AiClientDTO dto) {
        if (dto == null) {
            return null;
        }
        return AiClient.builder()
                .id(dto.getId())
                .clientId(dto.getClientId())
                .clientName(dto.getClientName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .updateTime(dto.getUpdateTime())
                .build();
    }

    @Override
    protected AiClient toPo(AiClientAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return AiClient.builder()
                .clientId(aggregate.getClientId())
                .clientName(aggregate.getClientName())
                .description(aggregate.getDescription())
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    @Override
    protected AiClientAggregate toAggregate(AiClient data) {
        if (data == null) {
            return null;
        }
        // 这里仅还原基础信息，关联关系从 config 表加载，可在应用层补充后再挂载到聚合根
        return AiClientAggregate.restore(data.getClientId(), data.getClientName(), data.getDescription(),
                null, null, List.of(), List.of(), List.of());
    }

    @Override
    protected String toId(AiClientAggregate aggregate) {
        return aggregate == null ? null : aggregate.getClientId();
    }

    @Override
    protected Long toDbId(AiClient data) {
        return data.getId();
    }

    @Override
    protected void fillDbId(AiClient target, Long dbId) {
        target.setId(dbId);
    }

    @Override
    protected Serializable toSerializableId(String id) {
        return id;
    }

    @Override
    protected AiClient getByAggregateId(String s) {
        return clientDaoSupport.queryByClientId(s);
    }
}
