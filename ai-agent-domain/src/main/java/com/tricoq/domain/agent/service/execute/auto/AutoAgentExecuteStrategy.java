package com.tricoq.domain.agent.service.execute.auto;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.IExecuteStrategy;
import com.tricoq.domain.agent.service.execute.auto.context.AutoExecuteContext;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final AutoLoopController autoLoopController;

    @Override
    public void execute(ExecuteCommandEntity commandEntity, ExecuteOutputPort port) {
        AutoExecuteContext context = AutoExecuteContext.builder()
                .originalUserInput(commandEntity.getUserInput())
                .currentTask(commandEntity.getUserInput())
                .isCompleted(false)
                .step(1)
                .maxStep(commandEntity.getMaxSteps() == null ? 3 : commandEntity.getMaxSteps())
                .port(port)
                .build();
        autoLoopController.run(commandEntity, context);
        log.info("执行完毕");
    }
}
