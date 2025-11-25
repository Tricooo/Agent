package com.tricoq.domain.agent.service.task;


import com.tricoq.domain.agent.adapter.repository.ITaskScheduleRepository;
import com.tricoq.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.tricoq.domain.agent.service.ITaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体执行任务
 *
 * @author trico qiang
 * 2025/9/13 16:09
 */
@Service
public class AiAgentTaskService implements ITaskService {

    @Resource
    private ITaskScheduleRepository taskScheduleRepository;

    @Override
    public List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule() {
        return taskScheduleRepository.queryAllValidTaskSchedule();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return taskScheduleRepository.queryAllInvalidTaskScheduleIds();
    }

}
