package com.tricoq.domain.agent.service.armory.business.data.impl;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author trico qiang
 * @date 10/23/25
 */
@Component
@RequiredArgsConstructor
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    private final IAgentRepository agentRepository;

    @Override
    public void loadData(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {

    }
}
