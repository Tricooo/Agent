package com.tricoq.domain.agent.model.request;

import java.util.function.Supplier;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
public interface InvocationPolicy<T> {

    Long getTimeoutMillis();
    Integer getMaxAttempts();
    Supplier<T> getFallbackResult();
    Boolean getFallbackOnNonRetryable();
}
