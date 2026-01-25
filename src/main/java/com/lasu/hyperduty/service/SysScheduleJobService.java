package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysScheduleJob;

import java.util.List;

/**
 * 定时任务Service接口
 */
public interface SysScheduleJobService extends IService<SysScheduleJob> {

    /**
     * 获取所有启用的定时任务
     * @return 定时任务列表
     */
    List<SysScheduleJob> getEnabledJobs();

    /**
     * 保存定时任务
     * @param job 定时任务
     * @return 保存结果
     */
    boolean saveJob(SysScheduleJob job);

    /**
     * 更新定时任务
     * @param job 定时任务
     * @return 更新结果
     */
    boolean updateJob(SysScheduleJob job);

    /**
     * 删除定时任务
     * @param jobId 任务ID
     * @return 删除结果
     */
    boolean deleteJob(Long jobId);

    /**
     * 暂停定时任务
     * @param jobId 任务ID
     * @return 暂停结果
     */
    boolean pauseJob(Long jobId);

    /**
     * 恢复定时任务
     * @param jobId 任务ID
     * @return 恢复结果
     */
    boolean resumeJob(Long jobId);

    /**
     * 立即执行定时任务
     * @param jobId 任务ID
     * @return 执行结果
     */
    boolean runJob(Long jobId);

    /**
     * 执行定时任务
     * @param job 定时任务
     */
    void executeJob(SysScheduleJob job);

}
