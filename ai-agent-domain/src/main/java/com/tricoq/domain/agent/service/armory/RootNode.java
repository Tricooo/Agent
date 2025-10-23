package com.tricoq.domain.agent.service.armory;

import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author trico qiang
 * @date 10/20/25
 */
@Component
@RequiredArgsConstructor
public class RootNode extends AbstractArmorySupport{

    private final Map<String, ILoadDataStrategy> strategyMap;

    /**
     * 异步加载数据
     *
     * @param requestParam
     * @param dynamicContext
     */
    @Override
    protected void multiThread(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {

    }

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam
     * @param dynamicContext
     * @return
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return "";
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return getDefaultHandler();
    }
}
