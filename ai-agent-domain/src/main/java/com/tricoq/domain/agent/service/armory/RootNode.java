package com.tricoq.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.ArmoryCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentEnumVO;
import com.tricoq.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.tricoq.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.tricoq.domain.framework.chain.StrategyHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根节点
 * 主要做异步加载数据库数据
 *
 * @author trico qiang
 * @date 10/20/25
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RootNode extends AbstractArmorySupport {

    private final List<ILoadDataStrategy> strategies;

    private Map<AiAgentEnumVO, ILoadDataStrategy> byEnum;

    @PostConstruct
    public void init() {
        byEnum = strategies.stream().collect(Collectors.toUnmodifiableMap(ILoadDataStrategy::support, Function.identity()));
    }

    /**
     * 异步加载数据
     */
    @Override
    protected void multiThread(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        loadDataDispatch(requestParam, dynamicContext);
    }

    /**
     * 把分支/空值判断收敛到 一个地方（Dispatcher/Resolver），业务调用处保持干净。
     * 如果未来要做熔断/灰度，Dispatcher 是天然的切入点（可在这里加 metrics、降级逻辑）。
     */
    private void loadDataDispatch(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        //优雅判空
        AiAgentEnumVO enumVO = Optional.ofNullable(AiAgentEnumVO.getByCode(requestParam.getCommandType()))
                //用 异常 代替静默 return，至少要打日志，避免悄悄丢请求
                .orElseThrow(() -> new IllegalArgumentException("不存在的指令类型: " + requestParam.getCommandType()));
        ILoadDataStrategy strategy = Optional.ofNullable(byEnum.get(enumVO))
                .orElseThrow(() -> new IllegalStateException("没有此策略实体: " + enumVO));
        strategy.loadData(requestParam, dynamicContext);
    }

    /**
     * 节点自身处理逻辑
     */
    @Override
    protected String doApply(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        log.info("Ai Agent 构建，数据加载节点 {}", JSON.toJSONString(requestParam));
        return router(requestParam, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity requestParam, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        return getDefaultHandler();
    }
}
