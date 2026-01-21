package com.lasu.hyperduty.service;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.entity.SysEmployee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AutoScheduleService {

    List<DutyAssignment> generateAutoSchedule(Long scheduleId, LocalDate startDate, LocalDate endDate, Long ruleId);

    List<DutyAssignment> generateAutoScheduleByWorkHours(Long scheduleId, LocalDate startDate, LocalDate endDate, Long ruleId, Long employeeId);

    List<DutyAssignment> generateScheduleByMode(Long scheduleId, LocalDate startDate, LocalDate endDate, Long modeId, Map<String, Object> configParams);

    boolean checkConflict(Long employeeId, LocalDate dutyDate, Integer dutyShift);

    List<DutyAssignment> getAvailableEmployees(LocalDate dutyDate, Integer dutyShift, Long excludeEmployeeId);

    Map<Long, BigDecimal> getEmployeeMonthlyWorkHours(Long scheduleId, LocalDate startDate, LocalDate endDate);
}