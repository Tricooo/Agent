package com.tricoq.domain.agent.service.execute.flow.step.factory;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.service.execute.flow.step.RootFlowNode;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author trico qiang
 * @date 11/12/25
 */
@Service
@RequiredArgsConstructor
public class DefaultFlowAgentExecuteStrategyFactory {

    private final RootFlowNode rootFlowNode;

    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> strategy() {
        return rootFlowNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext{

        private String userInput;

        private String sessionId;

        private int step;
        private int maxStep;

        @Builder.Default
        private boolean completed = false;

        private Map<String, Object> executionContext;

        private String mcpAnalysisResult;

        private String planningResult;

        private Map<String, String> stepsMap;

        private ExecuteOutputPort port;
        @Builder.Default
        private StringBuilder executeHistory = new StringBuilder();

        @Builder.Default
        private final Map<String, AiAgentClientFlowConfigDTO> configMap  = new HashMap<>();

        @Builder.Default
        private final Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }
    }
}
