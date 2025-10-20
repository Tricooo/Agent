package com.tricoq.domain.framework.chain;

/**
 * 赋予节点处理自身的数据的能力
 *
 * @author trico qiang
 * @date 10/20/25
 */
@FunctionalInterface
public interface StrategyHandler<T, D, R> {

    StrategyHandler DEFAULT = (t, d) -> null;

    R apply(T requestParam, D dynamicContext);
}
