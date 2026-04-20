package com.tricoq.domain.agent.service.execute.auto.step;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import com.tricoq.types.framework.chain.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.IllegalFormatException;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Slf4j
public abstract class AbstractExecuteSupport extends
        AbstractMultiThreadStrategyRouter<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> {

    protected static final String ANALYZER_MEMORY_SUFFIX = "-analyzer";
    protected static final String EXECUTOR_MEMORY_SUFFIX = "-executor";
    protected static final String SUPERVISOR_MEMORY_SUFFIX = "-supervisor";
    protected static final String SUMMARY_MEMORY_SUFFIX = "-summary";

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 异步加载数据
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     */
    @Override
    protected void multiThread(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {

    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected String buildConversationId(String sessionId, String roleSuffix) {
        return sessionId + roleSuffix;
    }

    protected String resolveStepPrompt(String configuredStepPrompt,
                                       String fallbackPrompt,
                                       boolean appendStructuredOutputReminder,
                                       Object... args) {
        String template = StringUtils.hasText(configuredStepPrompt) ? configuredStepPrompt : fallbackPrompt;
        try {
            String prompt = String.format(template, args);
            return appendStructuredOutputReminder ? appendStructuredOutputReminder(prompt) : prompt;
        } catch (IllegalFormatException e) {
            log.warn("stepPrompt 占位符格式不匹配，回退到默认提示词。configuredStepPrompt={}", configuredStepPrompt, e);
            String prompt = String.format(fallbackPrompt, args);
            return appendStructuredOutputReminder ? appendStructuredOutputReminder(prompt) : prompt;
        }
    }

    private String appendStructuredOutputReminder(String prompt) {
        return prompt + System.lineSeparator() + System.lineSeparator() + """
                ## 结构化输出约束
                请忽略模板中旧的文本输出格式示例，严格按照系统自动注入的 JSON Schema 返回结果。
                """;
    }

    protected void sendSseResult(DefaultExecuteStrategyFactory.ExecuteContext dynamicContext,
                                 AutoAgentExecuteResultEntity resultEntity) {
        ExecuteOutputPort emitter = dynamicContext.getPort();
        if (null != emitter) {
            try {
                emitter.send(JSON.toJSONString(resultEntity));
            }catch (Exception e) {
                log.error("发送SSE结果失败：{}", e.getMessage(), e);
            }
        }
    }
}
