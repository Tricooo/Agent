package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.valobj.AiAgentVO;

import java.util.List;
import java.util.Map;

/**
 * 智能体聚合仓储
 */
public interface IAgentRepository {

    Map<String, AiAgentClientFlowConfigVO> queryAiAgentFlowConfigByAgentId(String agentId);

    List<AiAgentClientFlowConfigVO> queryAiAgentClientsByAgentId(String aiAgentId);

    AiAgentVO queryAgentByAgentId(String agentId);

    List<AiAgentVO> queryAvailableAgents();

    int insertAiAgent(AiAgentVO aiAgent);
}
