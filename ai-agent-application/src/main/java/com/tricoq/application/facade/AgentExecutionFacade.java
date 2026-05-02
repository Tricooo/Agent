package com.tricoq.application.facade;

import com.alibaba.fastjson2.JSON;
import com.tricoq.api.dto.AiAgentResponseDTO;
import com.tricoq.api.dto.ArmoryAgentRequestDTO;
import com.tricoq.api.dto.AutoAgentRequestDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.IAgentDispatchService;
import com.tricoq.domain.agent.service.IArmoryService;
import com.tricoq.domain.agent.shared.ExecuteOutputPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 执行相关的应用服务，用例编排层。
 * 负责参数校验、指令构建和调度领域服务，屏蔽 Controller 对领域的直接依赖。
 *
 * @author tricoqiang
 */
@Service
@Slf4j
public class AgentExecutionFacade {

    private final IAgentDispatchService agentDispatchService;
    private final IArmoryService armoryService;

    public AgentExecutionFacade(IAgentDispatchService agentDispatchService,
                                IArmoryService armoryService) {
        this.agentDispatchService = agentDispatchService;
        this.armoryService = armoryService;
    }

    /**
     * 执行 AutoAgent，用例层负责组装命令和异常边界。
     */
    public ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request, ResponseBodyEmitter emitter) {
        if (request == null || StringUtils.isBlank(request.getAiAgentId())) {
            throw new IllegalArgumentException("agentId不能为空");
        }
        ExecuteCommandEntity command = ExecuteCommandEntity.builder()
                .agentId(request.getAiAgentId())
                .sessionId(request.getSessionId())
                .maxSteps(request.getMaxStep())
                .userInput(request.getMessage())
                .build();
        agentDispatchService.dispatch(command, ssePort(emitter));
        return emitter;
    }

    private ExecuteOutputPort ssePort(ResponseBodyEmitter emitter) {
        return new ExecuteOutputPort() {
            @Override
            public void send(String json) {
                try {
                    emitter.send("data: " + json + "\n\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void complete() {
                emitter.complete();
            }

            @Override
            public void error(Throwable t) {
                // 不再调 emitter.completeWithError(t)：Spring MVC 默认 ExceptionHandler 会把错误包成
                // LinkedHashMap 写 JSON 响应，但 Content-Type 已锁成 text/event-stream，
                // 触发 HttpMessageNotWritableException，客户端只看到 connection reset 而拿不到任何错误信息。
                // 改为往 SSE 流里发一个明确的 error 事件，runner / 前端可直接解析失败原因；
                // emitter.complete() 由 AgentDispatchService 的 finally 统一收尾，避免重复 complete。
                try {
                    Map<String, Object> errorEvent = new LinkedHashMap<>();
                    errorEvent.put("type", "error");
                    errorEvent.put("errorClass", t.getClass().getSimpleName());
                    errorEvent.put("message", t.getMessage() == null ? "" : t.getMessage());
                    emitter.send("data: " + JSON.toJSONString(errorEvent) + "\n\n");
                } catch (Exception sendError) {
                    log.warn("发送 error SSE 事件失败：{}", sendError.getMessage());
                }
            }
        };
    }

    /**
     * 装配智能体。
     */
    public void armoryAgent(ArmoryAgentRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getAgentId())) {
            throw new IllegalArgumentException("agentId不能为空");
        }
        armoryService.acceptArmoryAgent(request.getAgentId());
    }

    /**
     * 查询可用智能体列表并转换为响应 DTO。
     */
    public List<AiAgentResponseDTO> queryAvailableAgents() {
        List<AiAgentDTO> aiAgentVOList = armoryService.queryAvailableAgents();
        List<AiAgentResponseDTO> responseList = new ArrayList<>();
        for (AiAgentDTO aiAgentVO : aiAgentVOList) {
            AiAgentResponseDTO responseDTO = AiAgentResponseDTO.builder()
                    .agentId(aiAgentVO.getAgentId())
                    .agentName(aiAgentVO.getAgentName())
                    .description(aiAgentVO.getDescription())
                    .channel(aiAgentVO.getChannel())
                    .strategy(aiAgentVO.getStrategy())
                    .status(aiAgentVO.getStatus())
                    .build();
            responseList.add(responseDTO);
        }
        return responseList;
    }
}
