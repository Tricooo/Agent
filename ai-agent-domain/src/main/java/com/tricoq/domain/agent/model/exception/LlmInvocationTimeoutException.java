package com.tricoq.domain.agent.model.exception;

/**
 * @description: 统一 LLM 调用入口的外层超时异常。
 * 代表 Gateway 已经到达业务超时上限并主动止损，
 * 但不等于底层 HTTP 请求或供应商侧生成一定已经停止。
 * @author：trico qiang
 * @date: 4/18/26
 */
public class LlmInvocationTimeoutException extends RuntimeException {

    public LlmInvocationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
