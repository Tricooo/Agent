package com.tricoq.domain.agent.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Data
@Builder
public class StructuredInvocationRequest<T> implements InvocationPolicy<T>{

    private String operationName;

    private String clientId;

    private String prompt;

    private String sessionId;

    private String roleSuffix;

    private Class<T> responseType;

    private Integer retrieveSize;

    private Consumer<T> validate;

    private Long timeoutMillis;

    private Integer maxAttempts;

    /**
     * 在正常路径不会被用到。如果直接传一个实例，那每次调用都会提前构造一个永远不用的对象。
     * Supplier 是惰性求值（lazy），只在真正需要 fallback时才执行
     * LLM 调用彻底失败后的兜底结果；null 表示不启用 fallback，让异常往上抛
     */
    private Supplier<T> fallbackResult;

    /**
     * 是否允许“不可重试异常”直接走 fallback。
     * 默认 false，仅对像 Step4 这类展示层收尾节点显式打开。
     */
    private Boolean fallbackOnNonRetryable;
}
