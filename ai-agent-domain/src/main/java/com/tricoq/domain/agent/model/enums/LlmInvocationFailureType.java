package com.tricoq.domain.agent.model.enums;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/25/26
 */
public enum LlmInvocationFailureType {
    STRUCTURED_PARSE_FAILED,
    DTO_VALIDATION_FAILED,
    SEMANTIC_CONFLICT,
    TRANSIENT_PROVIDER_ERROR,
    NON_TRANSIENT_PROVIDER_ERROR,
    TIMEOUT,
    EMPTY_RESPONSE,
    UNKNOWN
}
