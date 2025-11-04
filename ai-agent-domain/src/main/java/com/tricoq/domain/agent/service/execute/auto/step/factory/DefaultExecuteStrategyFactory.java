package com.tricoq.domain.agent.service.execute.auto.step.factory;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.service.execute.auto.step.RootExecuteNode;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;

/**
 * @author trico qiang
 * @date 11/3/25
 */
@Service
@RequiredArgsConstructor
public class DefaultExecuteStrategyFactory {

    private final RootExecuteNode rootExecuteNode;

    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.ExecuteContext, String> strategy() {
        return rootExecuteNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ExecuteContext {

        private final String originalUserInput;
        private final Map<String, AiAgentClientFlowConfigVO> flowConfigMap;
        private String currentTask;
        private boolean isCompleted;
        private Integer step;
        private Integer maxStep;
        private StringBuilder executionHistory;
        private String analyzeResult;
        private String executeResult;
        private String supervisionResult;
        private String finalSummary;
        private ResponseBodyEmitter emitter;
    }
}
