package com.lasu.hyperduty.controller.v1;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.pm.TaskCreateDTO;
import com.lasu.hyperduty.dto.pm.TaskQueryDTO;
import com.lasu.hyperduty.dto.pm.TaskUpdateDTO;
import com.lasu.hyperduty.dto.pm.TaskVO;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.PmTaskService;
import com.lasu.hyperduty.service.SysEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/pm/task")
@RequiredArgsConstructor
public class PmTaskV1Controller {

    private final PmTaskService pmTaskService;
    private final SysEmployeeService sysEmployeeService;

    /**
     * 获取当前登录用户的 employeeId
     */
    private Long getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String username = authentication.getName();
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getUsername, username);
        SysEmployee employee = sysEmployeeService.getOne(wrapper);
        return employee != null ? employee.getId() : null;
    }

    /**
     * 分页查询任务列表（带权限信息）
     */
    @GetMapping("/page")
    public ResponseResult<Page<TaskVO>> pageList(
            @Valid TaskQueryDTO query) {
        Long currentEmployeeId = getCurrentEmployeeId();
        Page<TaskVO> page = pmTaskService.pageTaskList(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    /**
     * 获取任务详情（带权限信息）
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<TaskVO> getTaskDetail(
            @PathVariable Long id) {
        Long currentEmployeeId = getCurrentEmployeeId();
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
