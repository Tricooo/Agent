package com.tricoq.application.facade;

import com.tricoq.api.dto.AiAgentResponseDTO;
import com.tricoq.api.dto.ArmoryAgentRequestDTO;
import com.tricoq.api.dto.AutoAgentRequestDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.IAgentDispatchService;
import com.tricoq.domain.agent.service.IArmoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 执行相关的应用服务，用例编排层。
 * 负责参数校验、指令构建和调度领域服务，屏蔽 Controller 对领域的直接依赖。
 * @author tricoqiang
 */
@Service
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
    public ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getAiAgentId())) {
            throw new IllegalArgumentException("agentId不能为空");
        }
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);
        ExecuteCommandEntity command = ExecuteCommandEntity.builder()
                .agentId(request.getAiAgentId())
                .sessionId(request.getSessionId())
                .maxSteps(request.getMaxStep())
                .userInput(request.getMessage())
                .build();
        agentDispatchService.dispatch(command, emitter);
        return emitter;
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
