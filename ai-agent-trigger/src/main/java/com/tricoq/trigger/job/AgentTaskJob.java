package com.tricoq.trigger.job;

import com.tricoq.domain.agent.adapter.repository.ITaskScheduleRepository;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.tricoq.domain.agent.service.IAgentDispatchService;
import com.tricoq.types.framework.schedule.model.TaskScheduleVO;
import com.tricoq.types.framework.schedule.provider.ITaskDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

/**
 *
 *
 * @author trico qiang
 * @date 11/18/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentTaskJob implements ITaskDataProvider {

    private final ITaskScheduleRepository taskScheduleRepository;

    private final IAgentDispatchService agentDispatchService;

    /**
     * 查询所有有效的任务调度配置
     *
     * @return 任务调度配置列表
     */
    @Override
    public List<TaskScheduleVO> queryAllValidTaskSchedule() {
        List<AiAgentTaskScheduleVO> schedules = taskScheduleRepository.queryAllValidTaskSchedule();
        if (schedules.isEmpty()) {
            return List.of();
        }
        return schedules.stream().map(schedule -> {
                    TaskScheduleVO taskSchedule = TaskScheduleVO.builder()
                            .taskParam(schedule.getTaskParam())
                            .id(schedule.getId())
                            .description(schedule.getDescription())
                            .cronExpression(schedule.getCronExpression())
                            .build();
                    taskSchedule.setTaskLogic((id, taskParam) -> {
                        try {
                            agentDispatchService.dispatch(ExecuteCommandEntity.builder()
                                    .agentId(schedule.getAgentId())
                                    .userInput(taskParam)
                                    .maxSteps(1)
                                    .sessionId(String.valueOf(System.nanoTime()))
                                    .build(), new ResponseBodyEmitter()
                            );
                        } catch (Exception e) {
                            log.error("任务执行失败:{}", e.getMessage());
                        }
                    });
                    return taskSchedule;
                }
        ).toList();
    }

    /**
     * 查询所有无效的任务ID
     *
     * @return 无效任务ID列表
     */
    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return taskScheduleRepository.queryAllInvalidTaskScheduleIds();
    }
}
