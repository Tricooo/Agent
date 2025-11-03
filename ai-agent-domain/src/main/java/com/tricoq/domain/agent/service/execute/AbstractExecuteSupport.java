package com.tricoq.domain.agent.service.execute;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.framework.chain.AbstractMultiThreadStrategyRouter;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Slf4j
public class AbstractExecuteSupport extends
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

    /**
     * 节点自身处理逻辑
     *
     * @param requestParam   请求参数
     * @param dynamicContext 链路上下文
     * @return 结果
     */
    @Override
    protected String doApply(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        return "";
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> get(ExecuteCommandEntity requestParam, DefaultExecuteStrategyFactory.ExecuteContext dynamicContext) {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }
}
