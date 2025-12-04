package com.tricoq.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tricoq.infrastructure.dao.po.AiAgentTaskSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 智能体任务调度配置表 DAO
 * @author trico qiang
 * @description 智能体任务调度配置表数据访问对象
 */
@Mapper
public interface IAiAgentTaskScheduleDao extends BaseMapper<AiAgentTaskSchedule> {

    /**
     * 根据智能体ID删除任务调度配置
     * @param agentId 智能体ID
     * @return 影响行数
     */
    int deleteByAgentId(Long agentId);

    /**
     * 根据智能体ID查询任务调度配置列表
     * @param agentId 智能体ID
     * @return 智能体任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryByAgentId(Long agentId);

    /**
     * 查询所有有效的任务调度配置
     * @return 智能体任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryEnabledTasks();

    /**
     * 根据任务名称查询任务调度配置
     * @param taskName 任务名称
     * @return 智能体任务调度配置对象
     */
    AiAgentTaskSchedule queryByTaskName(String taskName);

    /**
     * 查询所有智能体任务调度配置
     * @return 智能体任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryAll();
    /**
     * 查询所有有效的任务调度配置
     * @return 智能体任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryAllValidTaskSchedule();

    /**
     * 查询所有无效的任务调度配置ID
     * @return 无效任务调度配置ID列表
     */
    List<Long> queryAllInvalidTaskScheduleIds();
}
