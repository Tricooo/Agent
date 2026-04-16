package com.tricoq.domain.agent.spi;

import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.model.request.TextInvocationRequest;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
public interface LlmInvocationFacade {

    <T> T invokeStructured(StructuredInvocationRequest<T> request);

    String invokeText(TextInvocationRequest request);
}
