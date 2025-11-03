package com.tricoq.domain.agent.service.execute.factory;

import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.service.execute.RootExecuteNode;
import com.tricoq.domain.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        private boolean isCompleted = false;
        private String executeHistory;
        private Integer step = 1;
        private Integer maxStep = 1;
        private StringBuilder executionHistory;
        private String analyzeResult;
        private String executeResult;
        private String supervisionResult;
    }
}
