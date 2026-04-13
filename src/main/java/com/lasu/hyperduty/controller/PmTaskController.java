package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.WorkloadDTO;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.service.PmTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String assigneeName) {
        
        Page<PmTask> page = pmTaskService.pageList(pageNum, pageSize, projectId, assigneeId, status, priority, taskName, assigneeName);
        return ResponseResult.success(page);
    }

    @GetMapping("/project/{projectId}")
    public ResponseResult<List<PmTask>> getProjectTasks(@PathVariable Long projectId) {
        List<PmTask> tasks = pmTaskService.getProjectTasks(projectId);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/sub/{projectId}")
    public ResponseResult<List<PmTask>> getSubTasks(@PathVariable Long projectId) {
        List<PmTask> tasks = pmTaskService.getSubTasks(projectId);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<PmTask>> getMyTasks(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String taskName) {
        List<PmTask> tasks = pmTaskService.getMyTasks(employeeId, taskName);
        return ResponseResult.success(tasks);
    }

    @GetMapping("/my/{employeeId}/project/{projectId}")
    public ResponseResult<List<PmTask>> getMyTasksByProject(
            @PathVariable Long employeeId,
            @PathVariable Long projectId,
            @RequestParam(required = false) String taskName) {
        List<PmTask> tasks = pmTaskService.getMyTasksByProject(employeeId, projectId, taskName);
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

    @GetMapping("/upcoming/{employeeId}")
    public ResponseResult<List<PmTask>> getUpcomingTasks(@PathVariable Long employeeId) {
        List<PmTask> tasks = pmTaskService.getUpcomingTasks(employeeId);
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
            @RequestParam Boolean pinned) {
        pmTaskService.pinTask(taskId, pinned);
        return ResponseResult.success();
    }

    @GetMapping("/permission/{taskId}/{employeeId}")
    public ResponseResult<Boolean> hasTaskPermission(
            @PathVariable Long taskId,
            @PathVariable Long employeeId) {
        boolean hasPermission = pmTaskService.hasTaskPermission(taskId, employeeId);
        return ResponseResult.success(hasPermission);
    }

    @GetMapping("/permission/delete/{taskId}/{employeeId}")
    public ResponseResult<Boolean> hasTaskDeletePermission(
            @PathVariable Long taskId,
            @PathVariable Long employeeId) {
        boolean hasPermission = pmTaskService.hasTaskDeletePermission(taskId, employeeId);
        return ResponseResult.success(hasPermission);
    }

    @PostMapping("/recalculate-project-progress")
    public ResponseResult<Void> recalculateAllProjectProgress() {
        pmTaskService.recalculateAllProjectProgress();
        return ResponseResult.success();
    }

    @GetMapping("/workload/page")
    public ResponseResult<Page<WorkloadDTO>> getWorkloadPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate taskStartDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate taskEndDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime bindStartTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime bindEndTime) {
        
        Page<WorkloadDTO> page = pmTaskService.getWorkloadPage(pageNum, pageSize, projectId, taskName, assigneeId, taskStartDate, taskEndDate, bindStartTime, bindEndTime);
        return ResponseResult.success(page);
    }
}
