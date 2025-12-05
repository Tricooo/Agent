package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.spi.LLMGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Stream;

/**
 *
 *
 * @author trico qiang
 * @date 12/5/25
 */
@Component
@RequiredArgsConstructor
public class OpenAiLLMGateway implements LLMGateway {

    private final Map<String, ChatClient> chatClients;

    @Override
    public String chat(ChatRequest request) {

        return "";
    }

    @Override
    public Stream<String> chatStream(ChatRequest request) {
        return Stream.empty();
    }
}
