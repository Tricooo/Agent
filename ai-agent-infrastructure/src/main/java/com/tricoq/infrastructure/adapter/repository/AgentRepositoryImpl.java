package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.infrastructure.dao.IAiAgentDao;
import com.tricoq.infrastructure.dao.IAiAgentFlowConfigDao;
import com.tricoq.infrastructure.dao.po.AiAgent;
import com.tricoq.infrastructure.dao.po.AiAgentFlowConfig;
import com.tricoq.infrastructure.adapter.repository.MpAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author trico qiang
 * @date 11/25/25
 */
@Repository
@RequiredArgsConstructor
public class AgentRepositoryImpl extends MpAggregateRepository<AiAgentAggregate, AiAgent, String, IAiAgentDao>
        implements IAgentRepository {

    private final IAiAgentDao aiAgentDao;
    private final IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Override
    public Map<String, AiAgentClientFlowConfigDTO> queryAiAgentFlowConfigByAgentId(String agentId) {
        if (agentId == null) {
            return Map.of();
        }
        List<AiAgentFlowConfig> configs = aiAgentFlowConfigDao.queryByAgentId(agentId);
        if (CollectionUtils.isEmpty(configs)) {
            return Map.of();
        }
        return configs.stream()
                .map(flowConfig -> AiAgentClientFlowConfigDTO.builder()
                        .clientId(flowConfig.getClientId())
                        .sequence(flowConfig.getSequence())
                        .clientType(flowConfig.getClientType())
                        .clientName(flowConfig.getClientName())
                        .stepPrompt(flowConfig.getStepPrompt())
                        .build())
                .collect(Collectors.toMap(AiAgentClientFlowConfigDTO::getClientType, Function.identity(), (v1, v2) -> v1));
    }

    @Override
    public List<AiAgentClientFlowConfigDTO> queryAiAgentClientsByAgentId(String aiAgentId) {
        List<AiAgentFlowConfig> flowConfigs = aiAgentFlowConfigDao.queryByAgentId(aiAgentId);
        return flowConfigs.stream().map(flowConfig -> AiAgentClientFlowConfigDTO.builder()
                .clientId(flowConfig.getClientId())
                .clientName(flowConfig.getClientName())
                .clientType(flowConfig.getClientType())
                .sequence(flowConfig.getSequence())
                .stepPrompt(flowConfig.getStepPrompt())
                .build()).toList();
    }

    @Override
    public AiAgentDTO queryAgentByAgentId(String agentId) {
        AiAgent aiAgent = aiAgentDao.queryByAgentId(agentId);
        if (aiAgent == null) {
            return null;
        }
        return AiAgentDTO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .build();
    }

    @Override
    public List<AiAgentDTO> queryAvailableAgents() {
        List<AiAgent> aiAgents = aiAgentDao.queryEnabledAgents();
        return aiAgents.stream().map(aiAgent -> AiAgentDTO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .build()).toList();
    }

    /**
     * 领域对象 -> 数据库实体
     *
     * @param aggregate
     */
    @Override
    protected AiAgent toPo(AiAgentAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return AiAgent.builder()
                .agentId(aggregate.getAgentId())
                .agentName(aggregate.getAgentName())
                .description(aggregate.getDescription())
                .channel(aggregate.getChannel())
                .strategy(aggregate.getStrategy())
                .status(aggregate.getStatus())
                .createTime(now)
                .updateTime(now)
                .build();
    }

    @Override
    protected AiAgentAggregate toAggregate(AiAgent data) {
        if (data == null) {
            return null;
        }
        return AiAgentAggregate.builder()
                .agentId(data.getAgentId())
                .agentName(data.getAgentName())
                .description(data.getDescription())
                .channel(data.getChannel())
                .strategy(data.getStrategy())
                .status(data.getStatus())
                .build();
    }

    @Override
    protected String toId(AiAgentAggregate aggregate) {
        return aggregate == null ? null : aggregate.getAgentId();
    }

    @Override
    protected Serializable toSerializableId(String id) {
        return id;
    }
}
