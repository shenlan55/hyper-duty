package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysScheduleJob;
import com.lasu.hyperduty.system.entity.SysScheduleLog;
import com.lasu.hyperduty.system.service.SysScheduleJobService;
import com.lasu.hyperduty.system.service.SysScheduleLogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;







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
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 定时任务列表
     */
    @GetMapping("/job/list")
    public ResponseResult<Page<SysScheduleJob>> getJobList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysScheduleJob> page = scheduleJobService.getJobList(pageNum, pageSize, keyword);
        return ResponseResult.success(page);
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
    @RateLimit(window = 60, max = 20, message = "新增定时任务操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "更新定时任务操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "删除定时任务操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "暂停定时任务操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "恢复定时任务操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "立即执行定时任务操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> runJob(@PathVariable Long jobId) {
        boolean result = scheduleJobService.runJob(jobId);
        return ResponseResult.success(result);
    }

    /**
     * 获取定时任务日志列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param jobId 任务ID（可选）
     * @param keyword 搜索关键词（可选）
     * @return 日志列表
     */
    @GetMapping("/log/list")
    public ResponseResult<Page<SysScheduleLog>> getLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String keyword) {
        Page<SysScheduleLog> page = scheduleLogService.getLogList(pageNum, pageSize, jobId, keyword);
        return ResponseResult.success(page);
    }

    /**
     * 清理定时任务日志
     * @param days 天数
     * @return 删除的记录数
     */
    @PostMapping("/log/clean")
    @RateLimit(window = 60, max = 10, message = "清理定时任务日志操作过于频繁，请60秒后再试")
    public ResponseResult<Integer> cleanLogs(@RequestParam int days) {
        int count = scheduleLogService.cleanLogs(days);
        return ResponseResult.success(count);
    }

}
