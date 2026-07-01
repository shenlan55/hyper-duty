package com.lasu.hyperduty.workflow.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人员解析器（P1-10 配套）
 * ----------------------------------------------------------------------------
 * 把"业务标识（employeeId / deptId / roleId）"翻译成 Flowable 任务系统使用的"username"，
 * 屏蔽组织架构模块的具体实现差异。
 *
 * 注：当前实现为简化版：
 *   - employeeId -> username 用 sys_employee.username 字段（需保证有该列）
 *   - 部门负责人 / 角色负责人 占位返回空列表，避免阻塞流程创建（前端可后续接入）
 *
 * 后续优化：注入 SysEmployeeService / SysDeptService / SysRoleService 做真实查询。
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WfPersonResolver {

    /**
     * 多个 employeeId 转为 username 列表
     * 当前简化：默认 employeeId == username（常见做法：登录账号=员工号）
     */
    public List<String> toUsernames(List<String> employeeIds, DelegateTask task) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return employeeIds.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 部门负责人：默认返回空，让流程跑通；后续接入 sys_employee + dept 表后实现
     */
    public List<String> findDeptLeaders(List<String> deptIds, DelegateTask task) {
        if (deptIds == null || deptIds.isEmpty()) {
            // 默认查发起人部门
            log.info("[部门负责人] 未指定 deptId，使用发起人部门（占位返回空，后续接 sys_dept）");
            return Collections.emptyList();
        }
        log.info("[部门负责人] 待查 deptIds={}，占位返回空", deptIds);
        return Collections.emptyList();
    }

    /**
     * 角色负责人：默认返回空
     */
    public List<String> findRoleLeaders(List<String> roleIds, DelegateTask task) {
        log.info("[角色负责人] 待查 roleIds={}，占位返回空", roleIds);
        return Collections.emptyList();
    }
}
