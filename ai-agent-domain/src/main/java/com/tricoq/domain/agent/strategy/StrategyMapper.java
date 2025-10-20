package com.tricoq.domain.agent.strategy;

/**
 * 赋予节点路由到别的节点的能力
 *
 * @author trico qiang
 * @date 10/20/25
 */
public interface StrategyMapper<T, D, R> {

    StrategyHandler<T, D, R> get(T requestParam, D dynamicContext);

}
