package com.tricoq.domain.agent.service.execute.auto.context;

import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AutoAnalyzeResultDTO;
import com.tricoq.domain.agent.model.dto.AutoExecuteResultDTO;
import com.tricoq.domain.agent.model.dto.AutoSupervisionResultDTO;
import com.tricoq.domain.agent.service.execute.auto.step.context.ExecutionHistoryBuffer;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Auto 链路显式执行上下文。
 * 负责承载循环过程中的状态、阶段结果和流式输出句柄。
 *
 * @author trico qiang
 * @date 4/20/26
 */
@Data
@Builder
@AllArgsConstructor
public class AutoExecuteContext {

    @NonNull
    private final String originalUserInput;

    @Builder.Default
    private final Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = new HashMap<>();

    private String currentTask;

    /**
     * 当前 Auto 链路是否已被系统认定为结束，进入总结/收尾阶段。
     */
    @Builder.Default
    private boolean isCompleted = false;

    @Builder.Default
    private Integer step = 1;

    private Integer maxStep;

    @Builder.Default
    private ExecutionHistoryBuffer executionHistoryBuffer = new ExecutionHistoryBuffer();

    /**
     * Step1 结构化分析结果，替代原 analyzeResult String。
     */
    private AutoAnalyzeResultDTO analyzeResultDTO;

    /**
     * Step2 结构化执行结果，替代原 executeResult String。
     */
    private AutoExecuteResultDTO executeResultDTO;

    /**
     * Step3 结构化监督结果，替代原 supervisionResult String。
     */
    private AutoSupervisionResultDTO supervisionResultDTO;

    private String finalSummary;

    private ExecuteOutputPort port;
}
