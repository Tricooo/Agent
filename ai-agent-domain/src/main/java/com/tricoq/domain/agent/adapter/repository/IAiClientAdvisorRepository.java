package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;

import java.util.List;

/**
 * 顾问配置仓储接口
 */
public interface IAiClientAdvisorRepository {

    boolean insert(AiClientAdvisorDTO advisor);

    boolean updateById(AiClientAdvisorDTO advisor);

    boolean updateByAdvisorId(AiClientAdvisorDTO advisor);

    boolean deleteById(Long id);

    boolean deleteByAdvisorId(String advisorId);

    AiClientAdvisorDTO queryById(Long id);

    AiClientAdvisorDTO queryByAdvisorId(String advisorId);

    List<AiClientAdvisorDTO> queryByStatus(Integer status);

    List<AiClientAdvisorDTO> queryByAdvisorType(String advisorType);

    List<AiClientAdvisorDTO> queryAll();
}
