package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.DutyRecordMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
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
}