package com.lasu.hyperduty.duty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.duty.controller.DutyAssignmentController;
import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.service.DutyAssignmentService;
import java.time.LocalDate;
import java.util.List;








public interface DutyAssignmentService extends IService<DutyAssignment> {

    void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, LocalDate swapDate, Integer swapShift);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, LocalDate originalDate, Integer originalShift, LocalDate targetDate, Integer targetShift);
    
    List<String> getEmployeeDutyDates(Long scheduleId, Long employeeId);
    
    List<Integer> getEmployeeDutyShifts(Long scheduleId, Long employeeId, String date);
    
    int batchSchedule(DutyAssignmentController.BatchScheduleRequest request);
}