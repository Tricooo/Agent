package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiAgentTaskScheduleDTO;

import java.util.List;

/**
 * 任务调度仓储
 *
 * @author trico qiang
 * @date 11/25/25
 */
public interface ITaskScheduleRepository extends IAggregateRepository<AiAgentTaskScheduleDTO, Long> {

    List<AiAgentTaskScheduleDTO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();
}
