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
        //mybatis默认会返回空集合 如果用stream处理无需判空
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdList);
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
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdList);
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
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdList);
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
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdList);
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
        List<AiClientConfig> aiClientConfigs = aiClientConfigDao
                .queryBySourceTypeAndIdsEnabled(AiAgentEnumVO.AI_CLIENT.getCode(), clientIdList);
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

        return List.of();
    }

    @Override
    public List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIdList) {
        return List.of();
    }

    @Override
    public List<AiClientModelVO> queryAiClientModelVOByModelIds(List<String> modelIdList) {
        return List.of();
    }
}
