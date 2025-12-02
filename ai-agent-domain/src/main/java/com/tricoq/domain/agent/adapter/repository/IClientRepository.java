package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.aggregate.AiClientAggregate;
import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import com.tricoq.domain.agent.model.dto.AiClientApiDTO;
import com.tricoq.domain.agent.model.dto.AiClientModelDTO;
import com.tricoq.domain.agent.model.dto.AiClientSystemPromptDTO;
import com.tricoq.domain.agent.model.dto.AiClientToolMcpDTO;
import com.tricoq.domain.agent.model.dto.AiClientDTO;

import java.util.List;
import java.util.Map;

/**
 * 客户端相关仓储
 *
 * @author trico qiang
 * @date 11/25/25
 */
public interface IClientRepository extends IAggregateRepository<AiClientAggregate, String> {

    List<AiClientApiDTO> queryAiClientApisByClientIds(List<String> clientIdList);

    List<AiClientModelDTO> queryAiClientModelsByClientIds(List<String> clientIdList);

    List<AiClientToolMcpDTO> queryAiClientToolMcpsByClientIds(List<String> clientIdList);

    Map<String, AiClientSystemPromptDTO> queryAiClientSystemPromptsByClientIds(List<String> clientIdList);

    List<AiClientAdvisorDTO> queryAiClientAdvisorsByClientIds(List<String> clientIdList);

    List<AiClientDTO> queryAiClientsByClientIds(List<String> clientIdList);

    List<AiClientApiDTO> queryAiClientApisByModelIds(List<String> modelIdList);

    List<AiClientModelDTO> queryAiClientModelsByModelIds(List<String> modelIdList);

    boolean saveOrUpdateClientConfigByAggregate(List<AiClientAggregate> aiClientAggregates, String extraParams);
}
