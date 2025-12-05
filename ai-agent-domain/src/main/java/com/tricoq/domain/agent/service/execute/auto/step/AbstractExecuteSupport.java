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

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Slf4j
public abstract class AbstractExecuteSupport extends
        AbstractMultiThreadStrategyRouter<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> {

    protected static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    protected static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

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
