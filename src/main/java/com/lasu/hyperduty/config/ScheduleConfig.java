package com.lasu.hyperduty.config;

import com.lasu.hyperduty.entity.SysScheduleJob;
import com.lasu.hyperduty.service.SysScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务配置类，基于SchedulingConfigurer实现动态配置
 */
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Autowired
    private SysScheduleJobService scheduleJobService;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    // 存储定时任务的Map，用于后续的修改和删除操作
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        // 使用自定义的线程池
        registrar.setTaskScheduler(taskScheduler);

        // 初始化所有启用的定时任务
        List<SysScheduleJob> jobs = scheduleJobService.getEnabledJobs();
        for (SysScheduleJob job : jobs) {
            addTask(job, registrar);
        }
    }

    /**
     * 添加定时任务
     * @param job 定时任务
     * @param registrar 任务注册器
     */
    private void addTask(SysScheduleJob job, ScheduledTaskRegistrar registrar) {
        // 创建Runnable
        Runnable task = () -> executeTask(job);
        
        // 创建Trigger
        Trigger trigger = triggerContext -> {
            CronTrigger cronTrigger = new CronTrigger(job.getCronExpression());
            java.util.Date nextExecutionDate = cronTrigger.nextExecutionTime(triggerContext);
            return nextExecutionDate.toInstant();
        };

        // 注册任务并获取Future对象
        ScheduledFuture<?> future = registrar.getScheduler().schedule(
                task,
                trigger
        );

        // 存储任务信息
        scheduledTasks.put(job.getId(), future);
    }

    /**
     * 执行定时任务
     * @param job 定时任务
     */
    private void executeTask(SysScheduleJob job) {
        // 检查任务状态
        SysScheduleJob currentJob = scheduleJobService.getById(job.getId());
        if (currentJob == null || currentJob.getStatus() != 1) {
            // 任务已被删除或暂停，取消执行
            cancelTask(job.getId());
            return;
        }

        // 执行任务
        // 使用线程池异步执行，避免阻塞调度线程
        taskScheduler.execute(() -> {
            try {
                // 直接调用接口方法执行任务
                scheduleJobService.executeJob(job);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 取消定时任务
     * @param jobId 任务ID
     */
    public void cancelTask(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.get(jobId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(jobId);
        }
    }

    /**
     * 刷新定时任务
     * @param registrar 任务注册器
     */
    public void refreshTasks(ScheduledTaskRegistrar registrar) {
        // 取消所有现有任务
        for (Long jobId : scheduledTasks.keySet()) {
            cancelTask(jobId);
        }

        // 重新加载所有启用的任务
        List<SysScheduleJob> jobs = scheduleJobService.getEnabledJobs();
        for (SysScheduleJob job : jobs) {
            addTask(job, registrar);
        }
    }

    /**
     * 获取定时任务数量
     * @return 任务数量
     */
    public int getTaskCount() {
        return scheduledTasks.size();
    }

}
