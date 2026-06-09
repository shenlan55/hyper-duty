package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.dto.*;
import com.lasu.hyperduty.workflow.service.WfTaskService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@Tag(name = "任务管理", description = "任务管理接口")
@RestController
@RequestMapping("/workflow/task")
@RequiredArgsConstructor
public class WfTaskController {

    private final WfTaskService wfTaskService;

    @Operation(summary = "获取任务详情")
    @GetMapping("/{taskId}")
    public ResponseResult<Task> getTask(@PathVariable String taskId) {
        Task task = wfTaskService.getTask(taskId);
        return ResponseResult.success(task);
    }

    @Operation(summary = "分页查询待办任务")
    @GetMapping("/todo/page")
    public ResponseResult<Page<Task>> pageTodoTask(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<Task> page = wfTaskService.pageTodoTask(pageNum, pageSize, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "分页查询已办任务")
    @GetMapping("/done/page")
    public ResponseResult<Page<HistoricTaskDTO>> pageDoneTask(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<HistoricTaskDTO> page = wfTaskService.pageDoneTask(pageNum, pageSize, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "获取任务变量")
    @GetMapping("/variables/{taskId}")
    public ResponseResult<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = wfTaskService.getTaskVariables(taskId);
        return ResponseResult.success(variables);
    }

    @Operation(summary = "设置任务变量")
    @PostMapping("/variables/{taskId}")
    public ResponseResult<Void> setTaskVariables(@PathVariable String taskId, @RequestBody Map<String, Object> variables) {
        wfTaskService.setTaskVariables(taskId, variables);
        return ResponseResult.success();
    }

    @Operation(summary = "完成任务")
    @PostMapping("/complete")
    public ResponseResult<Void> completeTask(@RequestBody WfTaskCompleteDTO dto) {
        wfTaskService.completeTask(dto);
        return ResponseResult.success();
    }

    @Operation(summary = "转办任务")
    @PostMapping("/reassign")
    public ResponseResult<Void> reassignTask(@RequestBody WfTaskReassignDTO dto) {
        wfTaskService.reassignTask(dto);
        return ResponseResult.success();
    }

    @Operation(summary = "委托任务")
    @PostMapping("/delegate")
    public ResponseResult<Void> delegateTask(@RequestBody WfTaskDelegateDTO dto) {
        wfTaskService.delegateTask(dto);
        return ResponseResult.success();
    }

    @Operation(summary = "批量转办任务")
    @PostMapping("/batch-reassign")
    public ResponseResult<Void> batchReassignTask(@RequestBody WfTaskBatchReassignDTO dto) {
        wfTaskService.batchReassignTask(dto);
        return ResponseResult.success();
    }

    @Operation(summary = "认领任务")
    @PostMapping("/claim/{taskId}")
    public ResponseResult<Void> claimTask(@PathVariable String taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        wfTaskService.claimTask(taskId, userId);
        return ResponseResult.success();
    }

    @Operation(summary = "取消认领任务")
    @PostMapping("/unclaim/{taskId}")
    public ResponseResult<Void> unclaimTask(@PathVariable String taskId) {
        wfTaskService.unclaimTask(taskId);
        return ResponseResult.success();
    }

    @Operation(summary = "获取流程实例的历史任务")
    @GetMapping("/history/list/{processInstanceId}")
    public ResponseResult<List<HistoricTaskDTO>> listHistoryTasks(@PathVariable String processInstanceId) {
        List<HistoricTaskDTO> list = wfTaskService.listHistoryTasks(processInstanceId);
        return ResponseResult.success(list);
    }

    @Operation(summary = "获取流程实例的历史流程记录")
    @GetMapping("/history/process/{processInstanceId}")
    public ResponseResult<HistoricProcessInstanceDTO> getHistoryProcessInstance(@PathVariable String processInstanceId) {
        HistoricProcessInstanceDTO historicProcessInstance = wfTaskService.getHistoryProcessInstance(processInstanceId);
        return ResponseResult.success(historicProcessInstance);
    }
}
