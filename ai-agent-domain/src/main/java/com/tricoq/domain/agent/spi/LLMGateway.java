package com.tricoq.domain.agent.spi;

import java.util.Map;
import java.util.stream.Stream;

/**
 *
 *
 * @author trico qiang
 * @date 12/5/25
 */
public interface LLMGateway {

    String chat(ChatRequest request);

    Stream<String> chatStream(ChatRequest request);

    record ChatRequest(String modelKey, String prompt, String sessionId, Map<String,Object> params) {}

}
