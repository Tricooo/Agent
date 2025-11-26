package com.tricoq.domain.agent.service.execute.auto.step.factory;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.service.execute.auto.step.RootExecuteNode;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.HashMap;
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
        @NonNull
        private final String originalUserInput;
        @Builder.Default
        private final Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = new HashMap<>();
        private String currentTask;
        @Builder.Default
        private boolean isCompleted = false;
        @Builder.Default
        private Integer step = 1;
        private Integer maxStep;
        @Builder.Default
        private StringBuilder executionHistory  = new StringBuilder();
        private String analyzeResult;
        private String executeResult;
        private String supervisionResult;
        private String finalSummary;
        private ResponseBodyEmitter emitter;
    }
}
