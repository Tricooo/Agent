package com.tricoq.domain.agent.model.request;

import lombok.Builder;import lombok.Data;import java.util.function.Consumer;
/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Data
@Builder
public class StructuredInvocationRequest<T> {

    private String operationName;

    private String clientId;

    private String prompt;

    private String sessionId;

    private String roleSuffix;

    private Class<T> responseType;

    private Integer retrieveSize;

    private Consumer<T> validate;
}
