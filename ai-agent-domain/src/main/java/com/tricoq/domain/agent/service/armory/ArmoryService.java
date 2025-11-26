package com.tricoq.domain.agent.service.armory;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.IArmoryService;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 装配服务
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/10/3 12:50
 */
@Service
@RequiredArgsConstructor
public class ArmoryService implements IArmoryService {

    private final IAgentRepository agentRepository;

    private final DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Override
    public List<AiAgentDTO> acceptArmoryAllAvailableAgents() {
        List<AiAgentDTO> aiAgents = agentRepository.queryAvailableAgents();
        aiAgents.forEach(aiAgentVO -> acceptArmoryAgent(aiAgentVO.getAgentId()));
        return aiAgents;
    }

    @Override
    public void acceptArmoryAgent(String agentId) {
        List<AiAgentClientFlowConfigDTO> flowConfigs = agentRepository.queryAiAgentClientsByAgentId(agentId);
        if (flowConfigs.isEmpty()) {
            return;
        }

        // 获取客户端集合
        List<String> commandIdList = flowConfigs.stream()
                .map(AiAgentClientFlowConfigDTO::getClientId)
                .toList();

        try {
            var armoryStrategyHandler =
                    defaultArmoryStrategyFactory.armoryStrategyHandler();

            armoryStrategyHandler.apply(
                    ArmoryCommandEntity.builder()
                            .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                            .commandIdList(commandIdList)
                            .build(),
                    new DefaultArmoryStrategyFactory.DynamicContext());
        } catch (Exception e) {
            throw new RuntimeException("装配智能体失败", e);
        }
    }

    @Override
    public List<AiAgentDTO> queryAvailableAgents() {
        return agentRepository.queryAvailableAgents();
    }

}
