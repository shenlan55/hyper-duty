package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyAssignment;

import java.util.List;

public interface DutyAssignmentService extends IService<DutyAssignment> {

    void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, java.time.LocalDate swapDate, Integer swapShift);
    
    List<String> getEmployeeDutyDates(Long scheduleId, Long employeeId);
    
    List<Integer> getEmployeeDutyShifts(Long scheduleId, Long employeeId, String date);
}