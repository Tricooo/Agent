package com.tricoq.domain.agent.model.exception;

import com.tricoq.domain.agent.model.enums.LlmInvocationFailureType;
import lombok.Getter;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/25/26
 */
@Getter
public class LlmInvocationException extends RuntimeException {

    private final LlmInvocationFailureType failureType;
    private final String operationName;

    public LlmInvocationException(LlmInvocationFailureType failureType, String operationName,
                                  String message,
                                  Throwable cause
    ) {
        super(message, cause);
        this.failureType = failureType;
        this.operationName = operationName;
    }
}
