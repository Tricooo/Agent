package com.tricoq.domain.agent.service;

import com.tricoq.domain.agent.model.valobj.AiAgentVO;

import java.util.List;

/**
 * 装配接口
 * @author trico qiang
 * 2025/10/3 12:48
 */
public interface IArmoryService {

    List<AiAgentVO> acceptArmoryAllAvailableAgents();

    void acceptArmoryAgent(String agentId);

    List<AiAgentVO> queryAvailableAgents();

}
