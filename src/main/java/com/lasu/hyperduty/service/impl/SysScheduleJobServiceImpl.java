package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysScheduleJob;
import com.lasu.hyperduty.mapper.SysScheduleJobMapper;
import com.lasu.hyperduty.service.SysScheduleJobService;
import com.lasu.hyperduty.service.SysScheduleLogService;
import com.lasu.hyperduty.entity.SysScheduleLog;
import com.lasu.hyperduty.service.impl.HolidayTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 定时任务Service实现类
 */
@Service
public class SysScheduleJobServiceImpl extends ServiceImpl<SysScheduleJobMapper, SysScheduleJob> implements SysScheduleJobService {

    @Autowired
    private SysScheduleLogService scheduleLogService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private HolidayTaskExecutor holidayTaskExecutor;

    @Override
    public List<SysScheduleJob> getEnabledJobs() {
        QueryWrapper<SysScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean saveJob(SysScheduleJob job) {
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        return this.save(job);
    }

    @Override
    @Transactional
    public boolean updateJob(SysScheduleJob job) {
        job.setUpdateTime(LocalDateTime.now());
        return this.updateById(job);
    }

    @Override
    @Transactional
    public boolean deleteJob(Long jobId) {
        return this.removeById(jobId);
    }

    @Override
    @Transactional
    public boolean pauseJob(Long jobId) {
        SysScheduleJob job = this.getById(jobId);
        if (job != null) {
            job.setStatus(0);
            job.setUpdateTime(LocalDateTime.now());
            return this.updateById(job);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean resumeJob(Long jobId) {
        SysScheduleJob job = this.getById(jobId);
        if (job != null) {
            job.setStatus(1);
            job.setUpdateTime(LocalDateTime.now());
            return this.updateById(job);
        }
        return false;
    }

    @Override
    public boolean runJob(Long jobId) {
        SysScheduleJob job = this.getById(jobId);
        if (job != null) {
            // 异步执行任务
            taskExecutor.execute(() -> {
                executeJob(job);
            });
            return true;
        }
        return false;
    }

    /**
     * 执行定时任务
     * @param job 定时任务
     */
    public void executeJob(SysScheduleJob job) {
        SysScheduleLog log = new SysScheduleLog();
        log.setJobId(job.getId());
        log.setJobName(job.getJobName());
        log.setJobGroup(job.getJobGroup());
        log.setJobCode(job.getJobCode());
        log.setParams(job.getParams());
        log.setStartTime(LocalDateTime.now());

        long startTime = System.currentTimeMillis();
        try {
            // 根据jobCode执行不同的任务
            if ("holidaySync".equals(job.getJobCode())) {
                // 执行节假日信息获取任务
                holidayTaskExecutor.execute();
            } else {
                // 这里可以通过反射调用具体的任务方法
                // 暂时只记录日志，后续可以扩展
                System.out.println("执行定时任务: " + job.getJobName() + "，参数: " + job.getParams());
            }
            log.setStatus(1);
        } catch (Exception e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage());
            e.printStackTrace();
        } finally {
            log.setEndTime(LocalDateTime.now());
            log.setExecuteTime(System.currentTimeMillis() - startTime);
            scheduleLogService.saveLog(log);
        }
    }

}
