package com.tricoq.types.framework.schedual;


import com.tricoq.types.framework.schedual.config.TaskJobAutoProperties;
import com.tricoq.types.framework.schedual.service.ITaskJobService;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 任务调度作业
 * 定时获取有效的任务调度配置，并动态创建新的任务
 *
 * @author trico qiang
 */
public class TaskJob {

    private final TaskJobAutoProperties properties;
    private final ITaskJobService taskJobService;

    public TaskJob(TaskJobAutoProperties properties, ITaskJobService taskJobService) {
        this.properties = properties;
        this.taskJobService = taskJobService;
    }

    /**
     * 定时刷新任务调度配置
     */
    @Scheduled(fixedRateString = "${wrench.task.job.refresh-interval:60000}")
    public void refreshTasks() {
        if (!properties.isEnabled()) {
            return;
        }
        taskJobService.refreshTasks();
    }

    /**
     * 定时清理无效任务
     */
    @Scheduled(cron = "${wrench.task.job.clean-invalid-tasks-cron:0 0/10 * * * ?}")
    public void cleanInvalidTasks() {
        if (!properties.isEnabled()) {
            return;
        }
        taskJobService.cleanInvalidTasks();
    }

}