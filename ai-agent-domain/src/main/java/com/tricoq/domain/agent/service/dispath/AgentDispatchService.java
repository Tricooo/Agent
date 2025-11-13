package com.tricoq.domain.agent.service.dispath;

import com.tricoq.domain.agent.adapter.repository.IAgentRepository;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentVO;
import com.tricoq.domain.agent.service.IAgentDispatchService;
import com.tricoq.domain.agent.service.execute.IExecuteStrategy;
import com.tricoq.types.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 *
 * @author trico qiang
 * @date 11/13/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDispatchService implements IAgentDispatchService {

    private final IAgentRepository agentRepository;

    private final Map<String, IExecuteStrategy> executeStrategies;

    private final ThreadPoolExecutor executor;

    @Override
    public void dispatch(ExecuteCommandEntity commandEntity, ResponseBodyEmitter emitter) {
        AiAgentVO aiAgentVO = agentRepository.queryAgentByAgentId(commandEntity.getAgentId());

        String strategy = aiAgentVO.getStrategy();
        IExecuteStrategy executeStrategy = executeStrategies.get(strategy);
        if (null == executeStrategy) {
            throw new BizException("不存在的执行策略类型 strategy:" + strategy);
        }

        // 3. 异步执行AutoAgent
        executor.execute(() -> {
            try {
                executeStrategy.execute(commandEntity, emitter);
            } catch (Exception e) {
                log.error("AutoAgent执行异常：{}", e.getMessage(), e);
                try {
                    emitter.send("执行异常：" + e.getMessage());
                } catch (Exception ex) {
                    log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                }
            } finally {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("完成流式输出失败：{}", e.getMessage(), e);
                }
            }
        });
    }
}
