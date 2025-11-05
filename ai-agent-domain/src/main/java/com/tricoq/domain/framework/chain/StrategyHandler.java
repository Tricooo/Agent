package com.tricoq.domain.framework.chain;

/**
 * 赋予节点处理自身的数据的能力
 *
 * @author trico qiang
 * @date 10/20/25
 */
@FunctionalInterface
public interface StrategyHandler<T, D, R> {

    @SuppressWarnings("rawtypes")
    StrategyHandler DEFAULT = (t, d) -> "End";

//    static <T,D,R> StrategyHandler<T, D, R> defaultStrategyHandler() {
//        return (t, d) -> null;
//    }

    R apply(T requestParam, D dynamicContext);
}
