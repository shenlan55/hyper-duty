package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysScheduleJob;
import com.lasu.hyperduty.service.SysScheduleJobService;
import com.lasu.hyperduty.service.SysScheduleLogService;
import com.lasu.hyperduty.entity.SysScheduleLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 定时任务Controller
 */
@RestController
@RequestMapping("/sys/schedule")
public class SysScheduleJobController {

    @Autowired
    private SysScheduleJobService scheduleJobService;

    @Autowired
    private SysScheduleLogService scheduleLogService;

    /**
     * 获取定时任务列表
     * @return 定时任务列表
     */
    @GetMapping("/job/list")
    public ResponseResult<List<SysScheduleJob>> getJobList() {
        List<SysScheduleJob> jobs = scheduleJobService.list();
        return ResponseResult.success(jobs);
    }

    /**
     * 获取定时任务详情
     * @param jobId 任务ID
     * @return 定时任务详情
     */
    @GetMapping("/job/detail/{jobId}")
    public ResponseResult<SysScheduleJob> getJobDetail(@PathVariable Long jobId) {
        SysScheduleJob job = scheduleJobService.getById(jobId);
        return ResponseResult.success(job);
    }

    /**
     * 新增定时任务
     * @param job 定时任务
     * @return 新增结果
     */
    @PostMapping("/job/add")
    public ResponseResult<Boolean> addJob(@RequestBody SysScheduleJob job) {
        boolean result = scheduleJobService.saveJob(job);
        return ResponseResult.success(result);
    }

    /**
     * 更新定时任务
     * @param job 定时任务
     * @return 更新结果
     */
    @PutMapping("/job/update")
    public ResponseResult<Boolean> updateJob(@RequestBody SysScheduleJob job) {
        boolean result = scheduleJobService.updateJob(job);
        return ResponseResult.success(result);
    }

    /**
     * 删除定时任务
     * @param jobId 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/job/delete/{jobId}")
    public ResponseResult<Boolean> deleteJob(@PathVariable Long jobId) {
        boolean result = scheduleJobService.deleteJob(jobId);
        return ResponseResult.success(result);
    }

    /**
     * 暂停定时任务
     * @param jobId 任务ID
     * @return 暂停结果
     */
    @PostMapping("/job/pause/{jobId}")
    public ResponseResult<Boolean> pauseJob(@PathVariable Long jobId) {
        boolean result = scheduleJobService.pauseJob(jobId);
        return ResponseResult.success(result);
    }

    /**
     * 恢复定时任务
     * @param jobId 任务ID
     * @return 恢复结果
     */
    @PostMapping("/job/resume/{jobId}")
    public ResponseResult<Boolean> resumeJob(@PathVariable Long jobId) {
        boolean result = scheduleJobService.resumeJob(jobId);
        return ResponseResult.success(result);
    }

    /**
     * 立即执行定时任务
     * @param jobId 任务ID
     * @return 执行结果
     */
    @PostMapping("/job/run/{jobId}")
    public ResponseResult<Boolean> runJob(@PathVariable Long jobId) {
        boolean result = scheduleJobService.runJob(jobId);
        return ResponseResult.success(result);
    }

    /**
     * 获取定时任务日志列表
     * @param jobId 任务ID
     * @return 日志列表
     */
    @GetMapping("/log/list")
    public ResponseResult<List<SysScheduleLog>> getLogList(@RequestParam(required = false) Long jobId) {
        List<SysScheduleLog> logs;
        if (jobId != null) {
            logs = scheduleLogService.list();
            // 这里可以根据jobId过滤，后续可以扩展
        } else {
            logs = scheduleLogService.list();
        }
        return ResponseResult.success(logs);
    }

    /**
     * 清理定时任务日志
     * @param days 天数
     * @return 清理结果
     */
    @PostMapping("/log/clean")
    public ResponseResult<Boolean> cleanLogs(@RequestParam int days) {
        boolean result = scheduleLogService.cleanLogs(days);
        return ResponseResult.success(result);
    }

}
