package com.tricoq.domain.agent.service.execute.auto.step.factory;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AutoAnalyzeResultDTO;
import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.auto.step.RootExecuteNode;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        //当前 Auto 链路是否已被系统认定为结束，进入总结/收尾阶段
        @Builder.Default
        private boolean isCompleted = false;
        @Builder.Default
        private Integer step = 1;
        private Integer maxStep;
        @Builder.Default
        private StringBuilder executionHistory = new StringBuilder();
        /** Step1 结构化分析结果，替代原 analyzeResult String */
        private AutoAnalyzeResultDTO analyzeResultDTO;
        /** Step2 结构化执行结果，替代原 executeResult String */
        private AutoExecuteResultDTO executeResultDTO;
        /** Step3 结构化监督结果，替代原 supervisionResult String */
        private AutoSupervisionResultDTO supervisionResultDTO;
        private String finalSummary;
        private ExecuteOutputPort port;
    }
}
