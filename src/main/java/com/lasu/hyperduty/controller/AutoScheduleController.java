package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.service.AutoScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/duty/auto-schedule")
public class AutoScheduleController {

    @Autowired
    private AutoScheduleService autoScheduleService;

    @PostMapping("/generate")
    public ResponseResult<List<DutyAssignment>> generateSchedule(
            @RequestParam Long scheduleId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long ruleId) {
        List<DutyAssignment> assignments = autoScheduleService.generateAutoSchedule(
                scheduleId, startDate, endDate, ruleId
        );
        return ResponseResult.success(assignments);
    }

    @GetMapping("/check-conflict")
    public ResponseResult<Boolean> checkConflict(
            @RequestParam Long employeeId,
            @RequestParam LocalDate dutyDate,
            @RequestParam Integer dutyShift) {
        boolean hasConflict = autoScheduleService.checkConflict(employeeId, dutyDate, dutyShift);
        return ResponseResult.success(hasConflict);
    }

    @GetMapping("/available-employees")
    public ResponseResult<List<DutyAssignment>> getAvailableEmployees(
            @RequestParam LocalDate dutyDate,
            @RequestParam Integer dutyShift,
            @RequestParam(required = false) Long excludeEmployeeId) {
        List<DutyAssignment> employees = autoScheduleService.getAvailableEmployees(
                dutyDate, dutyShift, excludeEmployeeId
        );
        return ResponseResult.success(employees);
    }
}
