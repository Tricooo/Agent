package com.tricoq.domain.agent.service.execute.flow.step.factory;

import com.tricoq.domain.agent.model.dto.FlowStepDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.service.execute.flow.step.RootFlowNode;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import com.tricoq.types.framework.chain.StrategyHandler;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static final int DEFAULT_MAX_STEP = 4;

    private final RootFlowNode rootFlowNode;

    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> strategy() {
        return rootFlowNode;
    }

    @Data
    @Builder
    public static class DynamicContext{

        // ====== 第一层：不可变输入（构建后只读） ======\
        private final FlowInput input;
        // ====== 第二层：配置（Root 加载后只读） ======
        @Builder.Default
        private final Map<String, AiAgentClientFlowConfigDTO> flowConfigMap = new HashMap<>();
        // ====== 第三层：可变执行状态 ======
        private final FlowState state;
        // ====== 输出端口 ======
        private final ExecuteOutputPort port;

        // 仅用于临时调试或未来实验性字段，正式字段必须类型化
        @Builder.Default
        private final Map<String, Object> extensions = new HashMap<>();

    }

    @Value
    @Builder
    public static class FlowInput{
         String userInput;

         String sessionId;

         int maxStep;

         String agentId;
    }

    /**
     * "容器" vs "内容"的判断规则很简单：看产出方有几个。
     *
     *   - 产出方只有一个（如 Step2 产出 plannedSteps）→ 用 set 整体替换。@Builder.Default 的空集合只是防御性编程，防止下游在 Step2 还没执行时读到
     *   null。
     *   - 产出方有多个/同一个方法分多次写入（如 Step4 逐步 put 到 stepResults）→ 用 get().put() / get().add() 增量追加。@Builder.Default
     *   是必须的，因为第一次 put 之前容器必须存在。
     *
     *   你现在的代码，Root 和 Step1 用的 set、Step4 用的 put，都是对的。唯一错的就是 Step2 用了 addAll 而不是 set，现在已经改了。
     */
    @Data
    @Builder
    public static class FlowState{
        //控制流
        @Builder.Default
        private int currentStep = 0;
        @Builder.Default
        private boolean completed = false;
        //节点间流转的中间产物(（按产出顺序排列)
        //root
        private String toolListPrompt;
        //step1
        private String mcpAnalysisResult;
        /**
         * 结构化的执行步骤列表，由 Step2 通过 Spring AI Structured Output 产出。
         * 替代 planningResult + stepsMap 的松散链路。
         */
        //step2
        @Builder.Default
        private List<FlowStepDTO> plannedSteps = new ArrayList<>();

        // Step4 执行结果
        @Builder.Default
        private Map<Integer,FlowStepResultDTO> stepResults = new HashMap<>();
    }

    /**
     * Step4 执行结果的类型化
     */
    @Data
    @Builder
    public static class FlowStepResultDTO {
        private int stepNo;
        private String stepTitle;
        private FlowStepStatus status;
        private String result;
        private String errorMessage;
    }

    public enum FlowStepStatus {
        SUCCESS,
        FAILED,
        SKIPPED
    }
}
