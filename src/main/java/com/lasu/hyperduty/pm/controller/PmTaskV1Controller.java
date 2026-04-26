package com.lasu.hyperduty.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;







@Slf4j
@RestController
@RequestMapping("/api/v1/pm/task")
@RequiredArgsConstructor
public class PmTaskV1Controller {

    private final PmTaskService pmTaskService;

    /**
     * 分页查询任务列表（带权限信息）
     */
    @GetMapping("/page")
    public ResponseResult<Page<TaskVO>> pageList(
            @Valid TaskQueryDTO query) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        Page<TaskVO> page = pmTaskService.pageTaskList(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    /**
     * 获取任务详情（带权限信息）
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<TaskVO> getTaskDetail(
            @PathVariable Long id) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        TaskVO taskVO = pmTaskService.getTaskDetailWithPermission(id, currentEmployeeId);
        return ResponseResult.success(taskVO);
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
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTask(@PathVariable Long id) {
        pmTaskService.deleteTask(id);
        return ResponseResult.success();
    }
}
