package com.tricoq.domain.agent.service.execute.flow.step.factory;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import com.tricoq.domain.agent.service.execute.flow.step.RootNode;
import com.tricoq.domain.framework.chain.StrategyHandler;
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

    private final RootNode rootNode;

    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> strategy() {
        return rootNode;
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

        private ResponseBodyEmitter emitter;
        @Builder.Default
        private StringBuilder executeHistory = new StringBuilder();

        @Builder.Default
        private final Map<String, AiAgentClientFlowConfigVO> configMap  = new HashMap<>();

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
