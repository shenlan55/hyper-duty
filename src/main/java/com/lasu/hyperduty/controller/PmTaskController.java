package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.service.PmTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pm/task")
@RequiredArgsConstructor
public class PmTaskController {

    private final PmTaskService pmTaskService;

    @GetMapping("/page")
    public ResponseResult<Page<PmTask>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority) {
        
        Page<PmTask> page = pmTaskService.pageList(pageNum, pageSize, projectId, ownerId, status, priority);
        return ResponseResult.success(page);
    }

    @GetMapping("/project/{projectId}")
    public ResponseResult<List<PmTask>> getProjectTasks(@PathVariable Long projectId) {
        List<PmTask> tasks = pmTaskService.getProjectTasks(projectId);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/sub/{parentId}")
    public ResponseResult<List<PmTask>> getSubTasks(@PathVariable Long parentId) {
        List<PmTask> tasks = pmTaskService.getSubTasks(parentId);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<PmTask>> getMyTasks(@PathVariable Long employeeId) {
        List<PmTask> tasks = pmTaskService.getMyTasks(employeeId);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/{id}")
    public ResponseResult<PmTask> getTaskDetail(@PathVariable Long id) {
        PmTask task = pmTaskService.getTaskDetail(id);
        return ResponseResult.success(task);
    }

    @PostMapping
    public ResponseResult<PmTask> createTask(@RequestBody PmTask task) {
        PmTask created = pmTaskService.createTask(task);
        return ResponseResult.success(created);
    }

    @PutMapping
    public ResponseResult<PmTask> updateTask(@RequestBody PmTask task) {
        PmTask updated = pmTaskService.updateTask(task);
        return ResponseResult.success(updated);
    }

    @PutMapping("/progress/{taskId}")
    public ResponseResult<Void> updateProgress(
            @PathVariable Long taskId,
            @RequestParam Integer progress) {
        pmTaskService.updateProgress(taskId, progress);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTask(@PathVariable Long id) {
        pmTaskService.deleteTask(id);
        return ResponseResult.success();
    }

    @GetMapping("/upcoming")
    public ResponseResult<List<PmTask>> getUpcomingTasks() {
        List<PmTask> tasks = pmTaskService.getUpcomingTasks();
        return ResponseResult.success(tasks);
    }

    @GetMapping("/status/{status}")
    public ResponseResult<List<PmTask>> getTasksByStatus(@PathVariable Integer status) {
        List<PmTask> tasks = pmTaskService.getTasksByStatus(status);
        return ResponseResult.success(tasks);
    }

    @PutMapping("/pin/{taskId}")
    public ResponseResult<Void> pinTask(
            @PathVariable Long taskId,
            @RequestParam boolean pinned) {
        pmTaskService.pinTask(taskId, pinned);
        return ResponseResult.success();
    }
}
