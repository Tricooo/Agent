package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.valobj.AiAgentTaskScheduleVO;

import java.util.List;

/**
 * 任务调度仓储
 */
public interface ITaskScheduleRepository {

    List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();
}
