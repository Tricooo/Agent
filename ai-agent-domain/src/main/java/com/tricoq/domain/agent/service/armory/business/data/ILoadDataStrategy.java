package com.tricoq.domain.agent.service.armory.business.data;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;

/**
 * @author trico qiang
 * @date 10/23/25
 */
public interface ILoadDataStrategy {

    void loadData(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext);

    AiAgentEnumVO support();
}
