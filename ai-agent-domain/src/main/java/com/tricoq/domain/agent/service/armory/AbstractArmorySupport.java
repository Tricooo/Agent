package com.tricoq.domain.agent.service.armory;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.AbstractMultiThreadStrategyRouter;

/**
 * @author trico qiang
 * @date 10/23/25
 */
public abstract class AbstractArmorySupport
        extends AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext,String> {

    /**
     * 异步加载数据
     *
     * @param requestParam
     * @param dynamicContext
     */
    @Override
    protected void multiThread(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {

    }


}
