package com.tricoq.infrastructure.gateway.llm;

import com.tricoq.domain.agent.model.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.model.exception.LlmInvocationTimeoutException;
import com.tricoq.domain.agent.model.request.InvocationPolicy;
import com.tricoq.domain.agent.model.request.StructuredInvocationRequest;
import com.tricoq.domain.agent.model.request.TextInvocationRequest;
import com.tricoq.domain.agent.spi.LlmInvocationFacade;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.context.ApplicationContext;
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
public class SpringAiLlmInvocationGateway implements LlmInvocationFacade {

    private static final int DEFAULT_RETRIEVE_SIZE = 100;

    private final ExecutorService llmInvocationExecutor;

    @Resource
    private ApplicationContext applicationContext;

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public <T> T invokeStructured(StructuredInvocationRequest<T> request) {

        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(request.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的任务分析 client"));

        Supplier<T> action = () -> {
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
        };
        String opName = resolveOperationName(request.getOperationName(), "invokeStructured");
        return invokeWithPolicy(action, request, opName);
    }

    @Override
    public String invokeText(TextInvocationRequest request) {
        ChatClient client = Optional
                .ofNullable((ChatClient) getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(request.getClientId())))
                .orElseThrow(() -> new IllegalArgumentException("不存在的文本调用 client"));

        Supplier<String> action = () -> {
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

                if (!shouldRetry(e)) {
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

    private String resolveOperationName(String explicit, String fallback) {
        return StringUtils.isBlank(explicit) ? fallback : explicit;
    }

    /**
     * 判断一个异常是否值得重试。
     * 原则：优先尊重 Spring AI 的瞬态/非瞬态语义；
     * 再补业务白名单；未知异常默认不重试。
     */
    private boolean shouldRetry(Throwable e) {
        if (e == null) {
            return false;
        }
        // 1. 解包：策略壳内部的包装异常 / CompletionException 等，要看真正的 cause
        Throwable cause = unwrap(e);
        if (cause == null) {
            return false;
        }

        if (cause instanceof NonTransientAiException) {
            return false;
        }
        if (cause instanceof TransientAiException) {
            return true;
        }
        if (cause instanceof LlmInvocationTimeoutException) {
            return false;
        }

        // 2. 明确不重试的：编程错误 / 参数错 / 线程中断
        if (cause instanceof IllegalArgumentException) {
            return false;
        }
        if (cause instanceof NullPointerException) {
            return false;
        }
        if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return false;
        }

        // 5. timeout 默认不自动重试；部分网络 IO 仍允许重试
        if (cause instanceof SocketTimeoutException) {
            return false;
        }
        if (cause instanceof TimeoutException) {
            return false;
        }

        if (cause instanceof IOException) {
            return true;
        }

        // 3. HTTP 4xx：大部分不重试，但 408（请求超时）、429（限流）例外
        if (isHttpClientError(cause)) {
            int status = extractHttpStatus(cause);
            return status == 408 || status == 429;
        }

        // 4. HTTP 5xx：基本都重试（服务端故障，可能很快恢复）
        if (isHttpServerError(cause)) {
            return true;
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
