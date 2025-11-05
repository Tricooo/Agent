package com.tricoq.domain.agent.service.execute.auto.step;

import com.alibaba.fastjson.JSON;
import com.tricoq.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.IExecuteStrategy;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoAgentExecuteStrategy implements IExecuteStrategy {

    private final DefaultExecuteStrategyFactory executeStrategyFactory;

    @Override
    public void execute(ExecuteCommandEntity commandEntity, ResponseBodyEmitter emitter) {

        var strategyHandler = executeStrategyFactory.strategy();
        DefaultExecuteStrategyFactory.ExecuteContext context = DefaultExecuteStrategyFactory.ExecuteContext.builder()
                .originalUserInput(commandEntity.getUserInput())
                .currentTask(commandEntity.getUserInput())
                .isCompleted(false)
                .executionHistory(new StringBuilder())
                .step(1)
                .maxStep(commandEntity.getMaxSteps() == null ? 3 : commandEntity.getMaxSteps())
                .emitter(emitter)
                .build();
        String apply = strategyHandler.apply(commandEntity, context);
        log.info("执行完毕:{}", apply);

        try {
            AutoAgentExecuteResultEntity completeResult = AutoAgentExecuteResultEntity
                    .createCompleteResult(commandEntity.getSessionId());
            String response = "data: " + JSON.toJSONString(completeResult) + "\n\n";
            emitter.send(response);
        } catch (Exception e) {
            log.error("发送完成标识失败：{}", e.getMessage(), e);
        }
    }
}
