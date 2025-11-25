package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.valobj.AiClientAdvisorVO;
import com.tricoq.domain.agent.model.valobj.AiClientApiVO;
import com.tricoq.domain.agent.model.valobj.AiClientModelVO;
import com.tricoq.domain.agent.model.valobj.AiClientSystemPromptVO;
import com.tricoq.domain.agent.model.valobj.AiClientToolMcpVO;
import com.tricoq.domain.agent.model.valobj.AiClientVO;

import java.util.List;
import java.util.Map;

/**
 * 客户端相关仓储
 */
public interface IClientRepository {

    List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIdList);

    List<AiClientModelVO> queryAiClientModelVOByClientIds(List<String> clientIdList);

    List<AiClientToolMcpVO> queryAiClientToolMcpVOByClientIds(List<String> clientIdList);

    Map<String, AiClientSystemPromptVO> queryAiClientSystemPromptVOByClientIds(List<String> clientIdList);

    List<AiClientAdvisorVO> queryAiClientAdvisorVOByClientIds(List<String> clientIdList);

    List<AiClientVO> queryAiClientVOByClientIds(List<String> clientIdList);

    List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIdList);

    List<AiClientModelVO> queryAiClientModelVOByModelIds(List<String> modelIdList);
}
