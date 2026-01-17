package com.lasu.hyperduty.service;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyShiftConfig;

import java.time.LocalDate;
import java.util.List;

public interface AutoScheduleService {

    List<DutyAssignment> generateAutoSchedule(Long scheduleId, LocalDate startDate, LocalDate endDate, Long ruleId);

    boolean checkConflict(Long employeeId, LocalDate dutyDate, Integer dutyShift);

    List<DutyAssignment> getAvailableEmployees(LocalDate dutyDate, Integer dutyShift, Long excludeEmployeeId);
}
