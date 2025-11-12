package com.tricoq.domain.agent.service.execute.flow.step;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.function.Supplier;

/**
 *
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Slf4j
public abstract class AbstractExecuteSupport extends AbstractMultiThreadStrategyRouter<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext,String> {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 异步加载数据
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     */
    @Override
    protected void multiThread(ExecuteCommandEntity requestParam, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {

    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    protected ChatClient getChatClient(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    /**
     * 带重试机制的执行方法
     */
    protected  <T> T executeWithRetry(Supplier<T> operation, String operationName, int maxRetries) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("执行操作: {} (第{}/{}次尝试)", operationName, attempt, maxRetries);
                T result = operation.get();
                if (attempt > 1) {
                    log.info("操作 {} 在第{}次尝试后成功", operationName, attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("操作 {} 第{}次尝试失败: {}", operationName, attempt, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        long waitTime = (long) Math.pow(2, attempt - 1) * 1000;
                        log.info("等待 {}ms 后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试过程被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException(String.format("操作 %s 在 %d 次尝试后仍然失败", operationName, maxRetries), lastException);
    }

    protected void sendSseResult(DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext,
                                 AutoAgentExecuteResultEntity resultEntity) {
        ResponseBodyEmitter emitter = dynamicContext.getEmitter();
        if (null != emitter) {
            try {
                emitter.send("data: " + JSON.toJSONString(resultEntity) + "\n\n");
            }catch (Exception e) {
                log.error("发送SSE结果失败：{}", e.getMessage(), e);
            }
        }
    }
}
