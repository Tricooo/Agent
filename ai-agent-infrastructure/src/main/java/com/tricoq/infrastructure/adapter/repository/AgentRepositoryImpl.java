package com.tricoq.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.valobj.AiAgentVO;
import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.infrastructure.dao.IAiAgentDao;
import com.tricoq.infrastructure.dao.IAiAgentFlowConfigDao;
import com.tricoq.infrastructure.dao.po.AiAgent;
import com.tricoq.infrastructure.dao.po.AiAgentFlowConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AgentRepositoryImpl extends ServiceImpl<IAiAgentDao, AiAgent> implements IAgentRepository {

    private final IAiAgentDao aiAgentDao;
    private final IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Override
    public Map<String, AiAgentClientFlowConfigVO> queryAiAgentFlowConfigByAgentId(String agentId) {
        if (agentId == null) {
            return Map.of();
        }
        List<AiAgentFlowConfig> configs = aiAgentFlowConfigDao.queryByAgentId(agentId);
        if (CollectionUtils.isEmpty(configs)) {
            return Map.of();
        }
        return configs.stream()
                .map(flowConfig -> AiAgentClientFlowConfigVO.builder()
                        .clientId(flowConfig.getClientId())
                        .sequence(flowConfig.getSequence())
                        .clientType(flowConfig.getClientType())
                        .clientName(flowConfig.getClientName())
                        .stepPrompt(flowConfig.getStepPrompt())
                        .build())
                .collect(Collectors.toMap(AiAgentClientFlowConfigVO::getClientType, Function.identity(), (v1, v2) -> v1));
    }

    @Override
    public List<AiAgentClientFlowConfigVO> queryAiAgentClientsByAgentId(String aiAgentId) {
        List<AiAgentFlowConfig> flowConfigs = aiAgentFlowConfigDao.queryByAgentId(aiAgentId);
        return flowConfigs.stream().map(flowConfig -> AiAgentClientFlowConfigVO.builder()
                .clientId(flowConfig.getClientId())
                .clientName(flowConfig.getClientName())
                .clientType(flowConfig.getClientType())
                .sequence(flowConfig.getSequence())
                .stepPrompt(flowConfig.getStepPrompt())
                .build()).toList();
    }

    @Override
    public AiAgentVO queryAgentByAgentId(String agentId) {
        AiAgent aiAgent = aiAgentDao.queryByAgentId(agentId);
        if (aiAgent == null) {
            return null;
        }
        return AiAgentVO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .build();
    }

    @Override
    public List<AiAgentVO> queryAvailableAgents() {
        List<AiAgent> aiAgents = aiAgentDao.queryEnabledAgents();
        return aiAgents.stream().map(aiAgent -> AiAgentVO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .build()).toList();
    }

    @Override
    public int insertAiAgent(AiAgentVO aiAgent) {
        if (aiAgent == null) {
            return 0;
        }
        AiAgent po = AiAgent.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .description(aiAgent.getDescription())
                .channel(aiAgent.getChannel())
                .strategy(aiAgent.getStrategy())
                .status(aiAgent.getStatus())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        return aiAgentDao.insert(po);
    }
}
