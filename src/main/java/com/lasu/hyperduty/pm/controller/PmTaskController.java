package com.lasu.hyperduty.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.common.dto.WorkloadDTO;
import com.lasu.hyperduty.pm.dto.TaskCreateDTO;
import com.lasu.hyperduty.pm.dto.TaskQueryDTO;
import com.lasu.hyperduty.pm.dto.TaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.TaskVO;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.service.PmTaskService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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

    /**
     * 分页查询任务列表（带权限信息）
     */
    @GetMapping("/page")
    public ResponseResult<Page<TaskVO>> pageList(@Valid TaskQueryDTO query) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        Page<TaskVO> page = pmTaskService.pageTaskList(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    /**
     * 获取任务详情（带权限信息）
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<TaskVO> getTaskDetail(@PathVariable Long id) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        TaskVO taskVO = pmTaskService.getTaskDetailWithPermission(id, currentEmployeeId);
        return ResponseResult.success(taskVO);
    }

    /**
     * 获取项目任务列表
     */
    @GetMapping("/project/{projectId}")
    public ResponseResult<List<PmTask>> getProjectTasks(@PathVariable Long projectId) {
        List<PmTask> tasks = pmTaskService.getProjectTasks(projectId);
        return ResponseResult.success(tasks);
    }

    /**
     * 获取子任务列表
     */
    @GetMapping("/sub/{parentId}")
    public ResponseResult<List<PmTask>> getSubTasks(@PathVariable Long parentId) {
        List<PmTask> tasks = pmTaskService.getSubTasks(parentId);
        return ResponseResult.success(tasks);
    }

    /**
     * 获取我的任务列表
     */
    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<PmTask>> getMyTasks(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String taskName) {
        List<PmTask> tasks = pmTaskService.getMyTasks(employeeId, taskName);
        return ResponseResult.success(tasks);
    }

    /**
     * 获取我的任务列表（按项目）
     */
    @GetMapping("/my/{employeeId}/project/{projectId}")
    public ResponseResult<List<PmTask>> getMyTasksByProject(
            @PathVariable Long employeeId,
            @PathVariable Long projectId,
            @RequestParam(required = false) String taskName) {
        List<PmTask> tasks = pmTaskService.getMyTasksByProject(employeeId, projectId, taskName);
        return ResponseResult.success(tasks);
    }

    /**
     * 获取即将到期的任务
     */
    @GetMapping("/upcoming/{employeeId}")
    public ResponseResult<List<PmTask>> getUpcomingTasks(@PathVariable Long employeeId) {
        List<PmTask> tasks = pmTaskService.getUpcomingTasks(employeeId);
        return ResponseResult.success(tasks);
    }

    /**
     * 按状态获取任务列表
     */
    @GetMapping("/status/{status}")
    public ResponseResult<List<PmTask>> getTasksByStatus(@PathVariable Integer status) {
        List<PmTask> tasks = pmTaskService.getTasksByStatus(status);
        return ResponseResult.success(tasks);
    }

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseResult<PmTask> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        PmTask created = pmTaskService.createTask(dto);
        return ResponseResult.success(created);
    }

    /**
     * 更新任务
     */
    @PutMapping
    public ResponseResult<PmTask> updateTask(@Valid @RequestBody TaskUpdateDTO dto) {
        PmTask updated = pmTaskService.updateTask(dto);
        return ResponseResult.success(updated);
    }

    /**
     * 更新任务进度
     */
    @PutMapping("/progress/{taskId}")
    public ResponseResult<Void> updateProgress(
            @PathVariable Long taskId,
            @RequestParam Integer progress) {
        pmTaskService.updateProgress(taskId, progress);
        return ResponseResult.success();
    }

    /**
     * 置顶/取消置顶任务
     */
    @PutMapping("/pin/{taskId}")
    public ResponseResult<Void> pinTask(
            @PathVariable Long taskId,
            @RequestParam Boolean pinned) {
        pmTaskService.pinTask(taskId, pinned);
        return ResponseResult.success();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTask(@PathVariable Long id) {
        pmTaskService.deleteTask(id);
        return ResponseResult.success();
    }

    /**
     * 检查是否有任务权限
     */
    @GetMapping("/permission/{taskId}/{employeeId}")
    public ResponseResult<Boolean> hasTaskPermission(
            @PathVariable Long taskId,
            @PathVariable Long employeeId) {
        boolean hasPermission = pmTaskService.hasTaskPermission(taskId, employeeId);
        return ResponseResult.success(hasPermission);
    }

    /**
     * 检查是否有任务删除权限
     */
    @GetMapping("/permission/delete/{taskId}/{employeeId}")
    public ResponseResult<Boolean> hasTaskDeletePermission(
            @PathVariable Long taskId,
            @PathVariable Long employeeId) {
        boolean hasPermission = pmTaskService.hasTaskDeletePermission(taskId, employeeId);
        return ResponseResult.success(hasPermission);
    }

    /**
     * 重新计算所有项目进度
     */
    @PostMapping("/recalculate-project-progress")
    public ResponseResult<Void> recalculateAllProjectProgress() {
        pmTaskService.recalculateAllProjectProgress();
        return ResponseResult.success();
    }

    /**
     * 分页查询工作量
     */
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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime bindEndTime,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String title) {
        Page<WorkloadDTO> page = pmTaskService.getWorkloadPage(pageNum, pageSize, projectId, taskName, assigneeId, taskStartDate, taskEndDate, bindStartTime, bindEndTime, orderNo, title);
        return ResponseResult.success(page);
    }
}
