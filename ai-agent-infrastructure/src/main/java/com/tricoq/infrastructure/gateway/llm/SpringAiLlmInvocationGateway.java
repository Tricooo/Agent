package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.model.dto.AiClientRuntimeProfile;
import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.enums.LlmInvocationFailureType;
import com.tricoq.domain.agent.model.exception.LlmInvocationException;
import com.tricoq.domain.agent.model.exception.LlmInvocationTimeoutException;
import com.tricoq.domain.agent.model.request.InvocationPolicy;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.model.request.TextInvocationRequest;
import com.tricoq.domain.agent.spi.AiClientRuntimeRegistry;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @description:
 * @author：trico qiang
 * @date: 4/16/26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpringAiLlmInvocationGateway extends SpringAiSupport implements LlmInvocationFacade {

    private static final int DEFAULT_RETRIEVE_SIZE = 100;

    private final ExecutorService llmInvocationExecutor;

    private final AiClientRuntimeRegistry runtimeRegistry;

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public <T> T invokeStructured(StructuredInvocationRequest<T> request) {

        String clientId = request.getClientId();
        String opName = resolveOperationName(request.getOperationName(), "invokeStructured");
        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId)))
                .orElseThrow(() -> new IllegalArgumentException("不存在的任务分析 client"));
        //lambda 执行在invokeWithPolicy方法里，异常也能被invokeWithPolicy中捕获
        Supplier<T> action = () -> {
            ChatClient.ChatClientRequestSpec clientRequestSpec = buildRequestSpec(client, request.getPrompt(),
                    clientId, request.getSessionId(), request.getRoleSuffix(), request.getRetrieveSize());
            T response;
            try {
                response = clientRequestSpec.call().entity(request.getResponseType());
            } catch (Exception e) {
                LlmInvocationFailureType type = classify(e);

                if (type == LlmInvocationFailureType.TRANSIENT_PROVIDER_ERROR
                        || type == LlmInvocationFailureType.NON_TRANSIENT_PROVIDER_ERROR
                        || type == LlmInvocationFailureType.TIMEOUT) {
                    throw new LlmInvocationException(type, opName, "LLM provider 调用失败", e);
                }

                throw new LlmInvocationException(
                        LlmInvocationFailureType.STRUCTURED_PARSE_FAILED,
                        opName,
                        "结构化输出解析失败",
                        e
                );
            }

            if (response == null) {
                throw new LlmInvocationException(
                        LlmInvocationFailureType.EMPTY_RESPONSE,
                        opName,
                        "结构化调用结果为空",
                        null
                );
            }

            try {
                Optional.ofNullable(request.getValidate()).ifPresent(v -> v.accept(response));
            } catch (Exception e) {
                throw new LlmInvocationException(
                        LlmInvocationFailureType.DTO_VALIDATION_FAILED,
                        opName,
                        "结构化输出 DTO 校验失败",
                        e
                );
            }
            return response;
        };
        return invokeWithPolicy(action, request, opName);
    }

    @Override
    public String invokeText(TextInvocationRequest request) {
        String clientId = request.getClientId();
        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId)))
                .orElseThrow(() -> new IllegalArgumentException("不存在的文本调用 client"));

        Supplier<String> action = () -> {
            ChatClient.ChatClientRequestSpec clientRequestSpec = buildRequestSpec(client, request.getPrompt(),
                    clientId, request.getSessionId(), request.getRoleSuffix(), request.getRetrieveSize());

            String response = clientRequestSpec.call().content();

            if (response == null) {
                throw new RuntimeException("文本调用结果为空");
            }
            return response;
        };

        String opName = resolveOperationName(request.getOperationName(), "invokeText");
        return invokeWithPolicy(action, request, opName);
    }

    private <T> T invokeWithPolicy(Supplier<T> action, InvocationPolicy<T> policy, String operationName) {
        Integer maxAttempts = policy.getMaxAttempts();
        maxAttempts = (maxAttempts == null || maxAttempts < 1) ? 1 : maxAttempts;
        Long timeoutMillis = policy.getTimeoutMillis();
        Exception last = null;
        //todo 未来可以使用Resilience4j
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("[{}] 第 {}/{} 次调用", operationName, attempt, maxAttempts);
                return timeoutMillis == null ? action.get() : callWithTimeout(action, timeoutMillis, operationName);
            } catch (Exception e) {
                last = e;
                log.warn("[{}] 第 {} 次失败: {}", operationName, attempt, e.getMessage());

                LlmInvocationFailureType failureType = classify(e);

                if (!shouldRetry(failureType)) {
                    Throwable cause = unwrap(e);
                    if (shouldFallbackOnNonRetryable(policy) && policy.getFallbackResult() != null) {
                        log.warn("[{}] 非重试异常（{}），直接使用 fallback", operationName,
                                cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName());
                        return policy.getFallbackResult().get();
                    }

                    log.warn("[{}] 异常不可重试（{}），立即抛出", operationName,
                            cause != null ? cause.getClass().getSimpleName() : e.getClass().getSimpleName());
                    throw new RuntimeException(
                            String.format("[%s] 不可重试异常", operationName), e);
                }

                if (attempt < maxAttempts) {
                    backoff(attempt);
                }
            }
        }

        Supplier<T> fallbackResult = policy.getFallbackResult();
        if (fallbackResult != null) {
            log.warn("[{}] 重试耗尽，使用 fallback", operationName);
            return fallbackResult.get();
        }

        throw new RuntimeException(
                String.format("[%s] 重试 %d 次后仍失败", operationName, maxAttempts), last);
    }

    private LlmInvocationFailureType classify(Throwable error) {
        Throwable cause = unwrap(error);

        if (cause instanceof LlmInvocationTimeoutException) {
            return LlmInvocationFailureType.TIMEOUT;
        }
        if (cause instanceof TransientAiException) {
            return LlmInvocationFailureType.TRANSIENT_PROVIDER_ERROR;
        }

        if (cause instanceof NonTransientAiException) {
            return LlmInvocationFailureType.NON_TRANSIENT_PROVIDER_ERROR;
        }

        if (cause instanceof LlmInvocationException e) {
            return e.getFailureType();
        }

        if (isHttpClientError(cause)) {
            int status = extractHttpStatus(cause);
            return status == 408 || status == 429
                    ? LlmInvocationFailureType.TRANSIENT_PROVIDER_ERROR
                    : LlmInvocationFailureType.NON_TRANSIENT_PROVIDER_ERROR;
        }

        if (isHttpServerError(cause)) {
            return LlmInvocationFailureType.TRANSIENT_PROVIDER_ERROR;
        }


        return LlmInvocationFailureType.UNKNOWN;
    }

    private <T> T callWithTimeout(Supplier<T> action, Long timeoutMillis, String operationName) {
        Future<T> future = llmInvocationExecutor.submit(action::get);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            //尝试终止worker
            future.cancel(true);
            throw new LlmInvocationTimeoutException("[" + operationName + "] 调用超时", e);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException("[" + operationName + "] 调用被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("[" + operationName + "] 调用异常", cause);
        }
    }

    private void backoff(Integer attempt) {
        try {
            // 指数退避：1s, 2s, 4s, 8s...
            long waitMillis = (long) Math.pow(2, attempt - 1) * 1000L;
            Thread.sleep(waitMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重试等待被中断", ie);
        }
    }

    private ChatClient.ChatClientRequestSpec buildRequestSpec(ChatClient client,
                                                              String prompt,
                                                              String clientId,
                                                              String sessionId,
                                                              String roleSuffix,
                                                              Integer retrieveSize) {
        ChatClient.ChatClientRequestSpec clientRequestSpec = client.prompt(prompt);
        applyChatMemoryIfNeeded(clientRequestSpec, clientId, sessionId, roleSuffix, retrieveSize);
        return clientRequestSpec;
    }

    private void applyChatMemoryIfNeeded(ChatClient.ChatClientRequestSpec clientRequestSpec,
                                         String clientId,
                                         String sessionId,
                                         String roleSuffix,
                                         Integer retrieveSize) {
        AiClientRuntimeProfile profile = runtimeRegistry.getRequired(clientId);
        if (!profile.isChatMemoryEnabled()) {
            return;
        }

        clientRequestSpec.advisors(a -> a
                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, buildConversationId(sessionId, roleSuffix))
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, getRetrieveSize(retrieveSize)));
    }

    protected String buildConversationId(String sessionId, String roleSuffix) {
        return sessionId + roleSuffix;
    }

    protected int getRetrieveSize(Integer retrieveSize) {
        return retrieveSize != null ? retrieveSize : DEFAULT_RETRIEVE_SIZE;
    }

    private String resolveOperationName(String explicit, String fallback) {
        return StringUtils.isBlank(explicit) ? fallback : explicit;
    }

    /**
     * 判断一个异常是否值得重试。
     * 原则：优先尊重 Spring AI 的瞬态/非瞬态语义；
     * 再补业务白名单；未知异常默认不重试。
     */
    private boolean shouldRetry(LlmInvocationFailureType failureType) {
        // 解包：策略壳内部的包装异常 / CompletionException 等，要看真正的 cause

        if (failureType != null) {
            return switch (failureType) {
                case UNKNOWN -> false;
                case TIMEOUT -> false;
                case EMPTY_RESPONSE -> false;
                case SEMANTIC_CONFLICT -> false;
                case DTO_VALIDATION_FAILED -> false;
                case STRUCTURED_PARSE_FAILED -> true;
                case TRANSIENT_PROVIDER_ERROR -> true;
                case NON_TRANSIENT_PROVIDER_ERROR -> false;
            };
        }

        return false;
    }

    private boolean shouldFallbackOnNonRetryable(InvocationPolicy<?> policy) {
        return Boolean.TRUE.equals(policy.getFallbackOnNonRetryable());
    }

    private Throwable unwrap(Throwable e) {
        Throwable t = e;
        while ((t instanceof CompletionException || t instanceof ExecutionException) && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private boolean isHttpClientError(Throwable e) {
        String name = e.getClass().getName();
        return name.contains("HttpClientErrorException")
                || name.contains("WebClientResponseException$BadRequest")
                || name.contains("4xx");
    }

    private boolean isHttpServerError(Throwable e) {
        String name = e.getClass().getName();
        return name.contains("HttpServerErrorException")
                || name.contains("WebClientResponseException$InternalServerError")
                || name.contains("5xx");
    }

    /**
     * 从 Spring 的 HTTP 异常里提取状态码。
     * 不直接 import Spring Web 的类，用反射兜底，避免强绑定。
     */
    private int extractHttpStatus(Throwable e) {
        try {
            // 大多数 Spring HTTP 异常都有 getStatusCode() 方法
            Object statusCode = e.getClass().getMethod("getStatusCode").invoke(e);
            Object value = statusCode.getClass().getMethod("value").invoke(statusCode);
            return (int) value;
        } catch (Exception ignore) {
            return -1;
        }
    }
}
