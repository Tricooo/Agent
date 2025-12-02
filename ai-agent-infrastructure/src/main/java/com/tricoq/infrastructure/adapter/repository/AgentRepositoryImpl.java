package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.infrastructure.dao.IAiAgentDao;
import com.tricoq.infrastructure.dao.po.AiAgent;
import com.tricoq.infrastructure.dao.po.AiAgentFlowConfig;
import com.tricoq.infrastructure.service.AiAgentFlowConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 主表走父类BaseMapper，关联表走小仓储
 *
 * @author trico qiang
 * @date 11/25/25
 */
@Repository
@RequiredArgsConstructor
public class AgentRepositoryImpl extends MpAggregateRepository<AiAgentAggregate, AiAgent, String, Long, IAiAgentDao>
        implements IAgentRepository {

    private final AiAgentFlowConfigService agentFlowConfigService;

    @Override
    public Map<String, AiAgentClientFlowConfigDTO> queryAiAgentFlowConfigByAgentId(String agentId) {
        if (agentId == null) {
            return Map.of();
        }
        List<AiAgentFlowConfig> configs = agentFlowConfigService.queryByAgentId(agentId);
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
                .collect(Collectors.toMap(AiAgentClientFlowConfigDTO::getClientType,
                        Function.identity(),
                        (v1, v2) -> v1));
    }

    @Override
    public List<AiAgentClientFlowConfigDTO> queryAiAgentClientsByAgentId(String aiAgentId) {
        if (StringUtils.isBlank(aiAgentId)) {
            return Collections.emptyList();
        }
        List<AiAgentFlowConfig> flowConfigs = agentFlowConfigService.queryByAgentId(aiAgentId);
        return flowConfigs.stream().map(flowConfig -> AiAgentClientFlowConfigDTO.builder()
                        .clientId(flowConfig.getClientId())
                        .clientName(flowConfig.getClientName())
                        .clientType(flowConfig.getClientType())
                        .sequence(flowConfig.getSequence())
                        .stepPrompt(flowConfig.getStepPrompt())
                        .build())
                .toList();
    }

    @Override
    public AiAgentDTO queryAgentByAgentId(String agentId) {
        AiAgent aiAgent = this.baseMapper.queryByAgentId(agentId);
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
        List<AiAgent> aiAgents = this.baseMapper.queryEnabledAgents();
        return aiAgents.stream().map(aiAgent -> AiAgentDTO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .build()).toList();
    }

    @Override
    public boolean saveFlowConfig(List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigs) {
        if (CollectionUtils.isEmpty(aiAgentClientFlowConfigs)) {
            return true;
        }
        List<AiAgentFlowConfig> configs = aiAgentClientFlowConfigs.stream()
                .map(flowConfig -> AiAgentFlowConfig.builder()
                        .agentId(flowConfig.getAgentId())
                        .clientId(flowConfig.getClientId())
                        .clientName(flowConfig.getClientName())
                        .clientType(flowConfig.getClientType())
                        .sequence(flowConfig.getSequence())
                        .stepPrompt(flowConfig.getStepPrompt())
                        .build())
                .toList();
        return agentFlowConfigService.saveBatch(configs);
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
        return AiAgent.builder()
                .agentId(aggregate.getAgentId())
                .agentName(aggregate.getAgentName())
                .description(aggregate.getDescription())
                .channel(aggregate.getChannel())
                .strategy(aggregate.getStrategy())
                .status(aggregate.getStatus())
                .build();
    }

    @Override
    protected AiAgentAggregate toAggregate(AiAgent data) {
        if (data == null) {
            return null;
        }
        List<AiAgentClientFlowConfigVO> flowConfigs = agentFlowConfigService.queryByAgentId(data.getAgentId())
                .stream()
                .map(flow -> AiAgentClientFlowConfigVO.builder()
                        .agentId(flow.getAgentId())
                        .clientId(flow.getClientId())
                        .clientName(flow.getClientName())
                        .clientType(flow.getClientType())
                        .sequence(flow.getSequence())
                        .stepPrompt(flow.getStepPrompt())
                        .build())
                .toList();
        return AiAgentAggregate.restore(data.getAgentId(),
                data.getAgentName(),
                data.getDescription(),
                data.getChannel(),
                data.getStrategy(),
                data.getStatus(),
                flowConfigs);
    }

    @Override
    protected String toId(AiAgentAggregate aggregate) {
        return aggregate == null ? null : aggregate.getAgentId();
    }

    @Override
    protected Long toDbId(AiAgent data) {
        return data.getId();
    }

    @Override
    protected void fillDbId(AiAgent target, Long dbId) {
        target.setId(dbId);
    }

    @Override
    protected Serializable toSerializableId(String id) {
        return id;
    }

    @Override
    protected AiAgent getByAggregateId(String s) {
        return this.baseMapper.queryByAgentId(s);
    }
}
