package com.tricoq.domain.agent.service.armory;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.valobj.AiAgentVO;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.IArmoryService;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 装配服务
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/10/3 12:50
 */
@Service
@RequiredArgsConstructor
public class ArmoryService implements IArmoryService {

    private final IAgentRepository repository;

    private final DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Override
    public List<AiAgentVO> acceptArmoryAllAvailableAgents() {
        List<AiAgentVO> aiAgentVOS = repository.queryAvailableAgents();
        for (AiAgentVO aiAgentVO : aiAgentVOS) {
            String agentId = aiAgentVO.getAgentId();
            acceptArmoryAgent(agentId);
        }
        return aiAgentVOS;
    }

    @Override
    public void acceptArmoryAgent(String agentId) {
        List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOS = repository.queryAiAgentClientsByAgentId(agentId);
        if (aiAgentClientFlowConfigVOS.isEmpty()) return;

        // 获取客户端集合
        List<String> commandIdList = aiAgentClientFlowConfigVOS.stream()
                .map(AiAgentClientFlowConfigVO::getClientId)
                .collect(Collectors.toList());

        try {
            StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
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
    public List<AiAgentVO> queryAvailableAgents() {
        return repository.queryAvailableAgents();
    }

}
