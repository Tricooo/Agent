package com.tricoq.domain.agent.model.request;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Data
@Builder
public class TextInvocationRequest {

    private String operationName;

    private String clientId;

    private String prompt;

    private String sessionId;

    private String roleSuffix;

    private Integer retrieveSize;
}
