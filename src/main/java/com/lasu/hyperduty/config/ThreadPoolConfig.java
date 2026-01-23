package com.lasu.hyperduty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 线程池配置类
 * 负责创建和配置线程池，避免循环依赖问题
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 创建任务执行线程池
     * @return ThreadPoolTaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(100);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("task-executor-");
        // 设置线程池关闭时等待所有任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待时间
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }

    /**
     * 创建任务调度线程池
     * @return ThreadPoolTaskScheduler
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 设置线程池大小
        scheduler.setPoolSize(10);
        // 设置线程名称前缀
        scheduler.setThreadNamePrefix("schedule-task-");
        // 设置线程池关闭时等待所有任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待时间
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
