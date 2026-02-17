package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.DutyRecordMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DutyRecordServiceImpl extends ServiceImpl<DutyRecordMapper, DutyRecord> implements DutyRecordService {

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyScheduleService dutyScheduleService;

    @Override
    public List<SysEmployee> getAvailableSubstitutes(Long recordId) {
        DutyRecord record = getById(recordId);
        if (record == null) {
            return List.of();
        }
        
        DutyAssignment assignment = dutyAssignmentService.getById(record.getAssignmentId());
        if (assignment == null) {
            return List.of();
        }
        
        return getAvailableSubstitutes(record.getEmployeeId(), assignment.getDutyDate(), assignment.getDutyShift());
    }

    @Override
    public List<SysEmployee> getAvailableSubstitutes(Long employeeId, LocalDate dutyDate, Integer dutyShift) {
        SysEmployee currentEmployee = sysEmployeeService.getById(employeeId);
        if (currentEmployee == null) {
            return List.of();
        }

        List<SysEmployee> allEmployees = sysEmployeeService.getAllEmployees();

        List<Long> assignedEmployeeIds = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getDutyDate, dutyDate)
                .eq(DutyAssignment::getDutyShift, dutyShift)
                .eq(DutyAssignment::getStatus, 1)
                .list()
                .stream()
                .map(DutyAssignment::getEmployeeId)
                .collect(Collectors.toList());

        return allEmployees.stream()
                .filter(emp -> !emp.getId().equals(employeeId))
                .filter(emp -> emp.getStatus() == 1)
                .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                .sorted((e1, e2) -> {
                    if (e1.getDeptId().equals(currentEmployee.getDeptId()) && 
                        !e2.getDeptId().equals(currentEmployee.getDeptId())) {
                        return -1;
                    }
                    if (!e1.getDeptId().equals(currentEmployee.getDeptId()) && 
                        e2.getDeptId().equals(currentEmployee.getDeptId())) {
                        return 1;
                    }
                    return 0;
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<DutyRecord> getPendingApprovals(Long employeeId) {
        // 查询审批状态为待审批的加班记录
        List<DutyRecord> allPendingRecords = lambdaQuery()
                .eq(DutyRecord::getApprovalStatus, "待审批")
                .orderByDesc(DutyRecord::getCreateTime)
                .list();
        
        // 打印查询结果，用于调试
        System.out.println("查询到的待审批加班记录数量: " + allPendingRecords.size());
        for (DutyRecord record : allPendingRecords) {
            System.out.println("加班记录ID: " + record.getId() + ", 审批状态: " + record.getApprovalStatus());
        }
        
        // 过滤出当前用户有权审批的加班记录
        List<DutyRecord> authorizedRecords = allPendingRecords.stream()
                .filter(record -> {
                    try {
                        // 根据assignmentId查询对应的值班安排
                        DutyAssignment assignment = dutyAssignmentService.getById(record.getAssignmentId());
                        if (assignment != null) {
                            // 根据值班安排的scheduleId查询对应的值班表
                            Long scheduleId = assignment.getScheduleId();
                            if (scheduleId != null) {
                                // 查询该值班表的值班长列表
                                List<Long> leaderIds = dutyScheduleService.getLeaderIdsByScheduleId(scheduleId);
                                System.out.println("值班表ID: " + scheduleId + " 的值班长列表: " + leaderIds);
                                // 检查当前用户是否是值班长
                                boolean isLeader = leaderIds.contains(employeeId);
                                System.out.println("用户ID: " + employeeId + " 是否是值班长: " + isLeader);
                                return isLeader;
                            }
                        }
                        return false;
                    } catch (Exception e) {
                        System.err.println("处理加班记录ID: " + record.getId() + " 时出错: " + e.getMessage());
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        System.out.println("过滤后的加班记录数量: " + authorizedRecords.size());
        for (DutyRecord record : authorizedRecords) {
            System.out.println("授权的加班记录ID: " + record.getId());
        }
        
        return authorizedRecords;
    }
}