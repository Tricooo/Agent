package com.tricoq.types.framework.chain;

import lombok.Getter;
import lombok.Setter;

/**
 * 模版模式定义节点的执行和路由规则
 * 1.定义节点处理和路由流程
 * 2.增强节点自身处理逻辑
 *
 * @author trico qiang
 * @date 10/20/25
 */
public abstract class AbstractMultiThreadStrategyRouter<T, D, R> implements StrategyHandler<T, D, R>,
                                                                            StrategyMapper<T, D, R> {
    @Getter
    @Setter
    @SuppressWarnings("unchecked")
    private StrategyHandler<T, D, R> defaultHandler = StrategyHandler.DEFAULT;

    protected R router(T requestParam, D dynamicContext) {
        StrategyHandler<T, D, R> handler = get(requestParam, dynamicContext);
        if (null != handler) {
            return handler.apply(requestParam, dynamicContext);
        }
        return defaultHandler.apply(requestParam, dynamicContext);
    }

    @Override
    public R apply(T requestParam, D dynamicContext) {

        multiThread(requestParam, dynamicContext);

        return doApply(requestParam, dynamicContext);
    }

    /**
     * 异步加载数据
     *
     * @param requestParam 请求参数
     * @param dynamicContext 链路上下文
     */
    protected abstract void multiThread(T requestParam, D dynamicContext);

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam 请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    protected abstract R doApply(T requestParam, D dynamicContext);
}
