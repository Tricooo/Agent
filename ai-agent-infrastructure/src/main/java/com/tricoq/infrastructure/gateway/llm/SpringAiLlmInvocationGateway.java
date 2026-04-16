package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.model.request.TextInvocationRequest;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Service
@RequiredArgsConstructor
public class SpringAiLlmInvocationGateway implements LlmInvocationFacade {

    private static final int DEFAULT_RETRIEVE_SIZE = 100;

    @Resource
    private ApplicationContext applicationContext;

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public <T> T invokeStructured(StructuredInvocationRequest<T> request) {

        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(request.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的任务分析 client"));

        T response = client.prompt(request.getPrompt())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, buildConversationId(request.getSessionId(),
                                request.getRoleSuffix()))
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, getRetrieveSize(request.getRetrieveSize())))
                .call()
                .entity(request.getResponseType());

        if (response == null) {
            throw new RuntimeException("任务解析结果为空");
        }

        Optional.ofNullable(request.getValidate()).ifPresent(v -> v.accept(response));

        return response;
    }

    @Override
    public String invokeText(TextInvocationRequest request) {
        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(request.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的文本调用 client"));

        String response = client.prompt(request.getPrompt())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, buildConversationId(request.getSessionId(),
                                request.getRoleSuffix()))
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, getRetrieveSize(request.getRetrieveSize())))
                .call()
                .content();

        if (response == null) {
            throw new RuntimeException("文本调用结果为空");
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected String buildConversationId(String sessionId, String roleSuffix) {
        return sessionId + roleSuffix;
    }

    protected int getRetrieveSize(Integer retrieveSize) {
        return retrieveSize != null ? retrieveSize : DEFAULT_RETRIEVE_SIZE;
    }
}
