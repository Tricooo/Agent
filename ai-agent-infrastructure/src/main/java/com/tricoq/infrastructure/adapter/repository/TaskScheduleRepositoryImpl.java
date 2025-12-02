package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.model.dto.AiAgentTaskScheduleDTO;
import com.tricoq.domain.agent.adapter.repository.ITaskScheduleRepository;
import com.tricoq.infrastructure.dao.IAiAgentTaskScheduleDao;
import com.tricoq.infrastructure.dao.po.AiAgentTaskSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author trico qiang
 * @date 11/25/25
 */
@Repository
@RequiredArgsConstructor
public class TaskScheduleRepositoryImpl
        extends MpAggregateRepository<AiAgentTaskScheduleDTO, AiAgentTaskSchedule, Long,Long, IAiAgentTaskScheduleDao>
        implements ITaskScheduleRepository {

    private final IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;

    @Override
    public List<AiAgentTaskScheduleDTO> queryAllValidTaskSchedule() {
        List<AiAgentTaskSchedule> schedules = aiAgentTaskScheduleDao.queryAllValidTaskSchedule();
        if (schedules.isEmpty()) {
            return List.of();
        }
        return schedules.stream().map(taskSchedule -> AiAgentTaskScheduleDTO.builder()
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

    @Override
    protected AiAgentTaskSchedule toPo(AiAgentTaskScheduleDTO aggregate) {
        if (aggregate == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return AiAgentTaskSchedule.builder()
                .id(aggregate.getId())
                .agentId(aggregate.getAgentId())
                .description(aggregate.getDescription())
                .cronExpression(aggregate.getCronExpression())
                .taskParam(aggregate.getTaskParam())
                // 默认启用，后续如需领域控制可扩展 VO
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    @Override
    protected AiAgentTaskScheduleDTO toAggregate(AiAgentTaskSchedule data) {
        if (data == null) {
            return null;
        }
        return AiAgentTaskScheduleDTO.builder()
                .id(data.getId())
                .agentId(data.getAgentId())
                .description(data.getDescription())
                .cronExpression(data.getCronExpression())
                .taskParam(data.getTaskParam())
                .build();
    }

    @Override
    protected Long toId(AiAgentTaskScheduleDTO aggregate) {
        return aggregate == null ? null : aggregate.getId();
    }

    @Override
    protected Long toDbId(AiAgentTaskSchedule data) {
        return data.getId();
    }

    @Override
    protected void fillDbId(AiAgentTaskSchedule target, Long dbId) {
        target.setId(dbId);
    }

    @Override
    protected Serializable toSerializableId(Long id) {
        return id;
    }

    @Override
    protected AiAgentTaskSchedule getByAggregateId(Long aLong) {
        return aiAgentTaskScheduleDao.queryById(aLong);
    }
}
