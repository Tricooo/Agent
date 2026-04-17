package com.tricoq.domain.agent.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.function.Supplier;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Data
@Builder
public class TextInvocationRequest implements InvocationPolicy<String> {

    private String operationName;

    private String clientId;

    private String prompt;

    private String sessionId;

    private String roleSuffix;

    private Integer retrieveSize;

    private Long timeoutMillis;

    private Integer maxAttempts;

    private Supplier<String> fallbackResult;
}
