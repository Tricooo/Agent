package com.tricoq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @description: llm调用重试线程池
 * @author：trico qiang
 * @date: 4/17/26
 */
@Configuration
public class LlmInvocationExecutorConfig {

    @Bean("llmInvocationExecutor")
    public ExecutorService llmInvocationExecutor() {
        return new ThreadPoolExecutor(8, 16, 1,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(50), r -> {
            Thread thread = new Thread(r);
            thread.setName("llm-invoke-" + thread.threadId());
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());
    }
}
