package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.controller.DutyAssignmentController;
import com.lasu.hyperduty.entity.DutyAssignment;

import java.util.List;

public interface DutyAssignmentService extends IService<DutyAssignment> {

    void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, java.time.LocalDate swapDate, Integer swapShift);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, java.time.LocalDate originalDate, Integer originalShift, java.time.LocalDate targetDate, Integer targetShift);
    
    List<String> getEmployeeDutyDates(Long scheduleId, Long employeeId);
    
    List<Integer> getEmployeeDutyShifts(Long scheduleId, Long employeeId, String date);
    
    int batchSchedule(DutyAssignmentController.BatchScheduleRequest request);
}