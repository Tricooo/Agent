package com.tricoq.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tricoq.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.tricoq.domain.agent.adapter.repository.ITaskScheduleRepository;
import com.tricoq.infrastructure.dao.IAiAgentTaskScheduleDao;
import com.tricoq.infrastructure.dao.po.AiAgentTaskSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TaskScheduleRepositoryImpl extends ServiceImpl<IAiAgentTaskScheduleDao, AiAgentTaskSchedule> implements ITaskScheduleRepository {

    private final IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;

    @Override
    public List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule() {
        List<AiAgentTaskSchedule> schedules = aiAgentTaskScheduleDao.queryAllValidTaskSchedule();
        if (schedules.isEmpty()) {
            return List.of();
        }
        return schedules.stream().map(taskSchedule -> AiAgentTaskScheduleVO.builder()
                .id(taskSchedule.getId())
                .agentId(taskSchedule.getAgentId())
                .description(taskSchedule.getDescription())
                .cronExpression(taskSchedule.getCronExpression())
                .taskParam(taskSchedule.getTaskParam())
                .build()).toList();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return aiAgentTaskScheduleDao.queryAllInvalidTaskScheduleIds();
    }
}
