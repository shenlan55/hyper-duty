package com.lasu.hyperduty.controller.v1;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.pm.TaskCreateDTO;
import com.lasu.hyperduty.dto.pm.TaskQueryDTO;
import com.lasu.hyperduty.dto.pm.TaskUpdateDTO;
import com.lasu.hyperduty.dto.pm.TaskVO;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.service.PmTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
        Long currentEmployeeId = null;
        Page<TaskVO> page = pmTaskService.pageTaskList(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    /**
     * 获取任务详情（带权限信息）
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<TaskVO> getTaskDetail(
            @PathVariable Long id) {
        Long currentEmployeeId = null;
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
}
