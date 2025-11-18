package com.tricoq.domain.framework.schedual.config;


import com.tricoq.domain.framework.schedual.TaskJob;
import com.tricoq.domain.framework.schedual.provider.ITaskDataProvider;
import com.tricoq.domain.framework.schedual.service.ITaskJobService;
import com.tricoq.domain.framework.schedual.service.TaskJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

/**
 * 任务调度器自动配置类
 *
 * @author trico qiang
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(TaskJobAutoProperties.class)
@ConditionalOnProperty(prefix = "wrench.task.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TaskJobAutoConfig {

    private final Logger log = LoggerFactory.getLogger(TaskJobAutoConfig.class);

    /**
     * 创建线程池任务调度器实例，用于执行定时任务和异步任务调度
     */
    @Bean("wrenchTaskScheduler")
    public TaskScheduler taskScheduler(TaskJobAutoProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
        scheduler.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        scheduler.initialize();
        
        log.info("wrench，任务调度器初始化完成。线程池大小: {}, 线程名前缀: {}",
                properties.getPoolSize(), properties.getThreadNamePrefix());
        
        return scheduler;
    }

    @Bean
    public ITaskJobService taskJobService(TaskScheduler wrenchTaskScheduler, List<ITaskDataProvider> taskDataProviders) {
        // 实例化任务并初始化调度
        TaskJobService taskJobService = new TaskJobService(wrenchTaskScheduler, taskDataProviders);
        taskJobService.initializeTasks();

        return taskJobService;
    }

    /**
     * 自动检测任务
     */
    @Bean
    public TaskJob taskJob(TaskJobAutoProperties properties, ITaskJobService taskJobService) {
        log.info("wrench，任务调度作业初始化完成。刷新间隔: {}ms, 清理cron: {}",
                properties.getRefreshInterval(), properties.getCleanInvalidTasksCron());
        return new TaskJob(properties, taskJobService);
    }

}