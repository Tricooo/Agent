package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.aggregate.AiAgentAggregate;
import com.tricoq.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.tricoq.domain.agent.model.dto.AiAgentClientFlowConfigDTO;
import com.tricoq.domain.agent.model.dto.AiAgentDTO;

import java.util.List;
import java.util.Map;

/**
 * 智能体聚合仓储
 * @author trico qiang
 * @date 11/25/25
 */
public interface IAgentRepository extends IAggregateRepository<AiAgentAggregate, String> {

    Map<String, AiAgentClientFlowConfigDTO> queryAiAgentFlowConfigByAgentId(String agentId);

    List<AiAgentClientFlowConfigDTO> queryAiAgentClientsByAgentId(String aiAgentId);

    AiAgentDTO queryAgentByAgentId(String agentId);

    List<AiAgentDTO> queryAvailableAgents();

    boolean saveFlowConfig(List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigs);
}
