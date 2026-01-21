package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyScheduleMode;
import com.lasu.hyperduty.entity.DutyScheduleRule;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.entity.EmployeeAvailableTime;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.algorithm.ScheduleAlgorithm;
import com.lasu.hyperduty.service.AutoScheduleService;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyHolidayService;
import com.lasu.hyperduty.service.DutyScheduleModeService;
import com.lasu.hyperduty.service.DutyScheduleRuleService;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import com.lasu.hyperduty.service.EmployeeAvailableTimeService;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutoScheduleServiceImpl implements AutoScheduleService {

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyScheduleRuleService dutyScheduleRuleService;

    @Autowired
    private DutyShiftConfigService dutyShiftConfigService;

    @Autowired
    private EmployeeAvailableTimeService employeeAvailableTimeService;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private DutyScheduleService dutyScheduleService;

    @Autowired
    private DutyScheduleModeService dutyScheduleModeService;

    @Autowired
    private DutyHolidayService dutyHolidayService;

    @Override
    public List<DutyAssignment> generateAutoSchedule(Long scheduleId, LocalDate startDate, LocalDate endDate, Long ruleId) {
        List<DutyAssignment> assignments = new ArrayList<>();

        DutyScheduleRule rule = dutyScheduleRuleService.getById(ruleId);
        if (rule == null) {
            return assignments;
        }

        List<SysEmployee> employees = sysEmployeeService.lambdaQuery()
                .eq(SysEmployee::getStatus, 1)
                .list();

        List<DutyShiftConfig> shiftConfigs = dutyShiftConfigService.lambdaQuery()
                .eq(DutyShiftConfig::getStatus, 1)
                .orderByAsc(DutyShiftConfig::getSort)
                .list();

        Map<Long, BigDecimal> employeeWorkHours = new HashMap<>();
        Map<Long, Integer> employeeShiftCount = new HashMap<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (DutyShiftConfig shiftConfig : shiftConfigs) {
                List<SysEmployee> availableEmployees = getAvailableEmployeesForShift(
                    currentDate, shiftConfig, employees, employeeWorkHours, employeeShiftCount, rule
                );

                if (availableEmployees.isEmpty()) {
                    continue;
                }

                SysEmployee selectedEmployee = selectEmployeeForShift(
                    availableEmployees, employeeWorkHours, employeeShiftCount, rule
                );

                if (selectedEmployee != null) {
                    DutyAssignment assignment = new DutyAssignment();
                    assignment.setScheduleId(scheduleId);
                    assignment.setDutyDate(currentDate);
                    assignment.setDutyShift(shiftConfig.getShiftType());
                    assignment.setEmployeeId(selectedEmployee.getId());
                    assignment.setStatus(1);
                    assignment.setShiftConfigId(shiftConfig.getId());

                    assignments.add(assignment);

                    employeeWorkHours.put(selectedEmployee.getId(), 
                            employeeWorkHours.getOrDefault(selectedEmployee.getId(), BigDecimal.ZERO).add(shiftConfig.getDurationHours()));
                    employeeShiftCount.put(selectedEmployee.getId(), 
                            employeeShiftCount.getOrDefault(selectedEmployee.getId(), 0) + 1);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return assignments;
    }

    @Override
    public boolean checkConflict(Long employeeId, LocalDate dutyDate, Integer dutyShift) {
        QueryWrapper<DutyAssignment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId);
        queryWrapper.eq("duty_date", dutyDate);
        queryWrapper.eq("duty_shift", dutyShift);
        queryWrapper.eq("status", 1);

        return dutyAssignmentService.count(queryWrapper) > 0;
    }

    @Override
    public List<DutyAssignment> getAvailableEmployees(LocalDate dutyDate, Integer dutyShift, Long excludeEmployeeId) {
        List<DutyAssignment> existingAssignments = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getDutyDate, dutyDate)
                .eq(DutyAssignment::getDutyShift, dutyShift)
                .eq(DutyAssignment::getStatus, 1)
                .list();

        List<Long> assignedEmployeeIds = existingAssignments.stream()
                .map(DutyAssignment::getEmployeeId)
                .collect(Collectors.toList());

        QueryWrapper<DutyAssignment> queryWrapper = new QueryWrapper<>();
        if (excludeEmployeeId != null) {
            queryWrapper.ne("employee_id", excludeEmployeeId);
        }

        List<DutyAssignment> allAssignments = dutyAssignmentService.list(queryWrapper);
        Map<Long, Integer> employeeWorkload = allAssignments.stream()
                .collect(Collectors.groupingBy(
                        DutyAssignment::getEmployeeId,
                        Collectors.summingInt(a -> 1)
                ));

        List<SysEmployee> allEmployees = sysEmployeeService.lambdaQuery()
                .eq(SysEmployee::getStatus, 1)
                .list();

        return allEmployees.stream()
                .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                .sorted((e1, e2) -> {
                    int workload1 = employeeWorkload.getOrDefault(e1.getId(), 0);
                    int workload2 = employeeWorkload.getOrDefault(e2.getId(), 0);
                    return Integer.compare(workload1, workload2);
                })
                .limit(5)
                .map(emp -> {
                    DutyAssignment assignment = new DutyAssignment();
                    assignment.setEmployeeId(emp.getId());
                    return assignment;
                })
                .collect(Collectors.toList());
    }

    private List<SysEmployee> getAvailableEmployeesForShift(
            LocalDate dutyDate, DutyShiftConfig shiftConfig, List<SysEmployee> employees,
            Map<Long, BigDecimal> employeeWorkHours, Map<Long, Integer> employeeShiftCount,
            DutyScheduleRule rule) {

        int dayOfWeek = dutyDate.getDayOfWeek().getValue();

        return employees.stream()
                .filter(emp -> !checkConflict(emp.getId(), dutyDate, shiftConfig.getShiftType()))
                .filter(emp -> isEmployeeAvailable(emp.getId(), dayOfWeek, dutyDate, shiftConfig))
                .filter(emp -> !isOverloaded(emp.getId(), employeeWorkHours, rule))
                .filter(emp -> !isOverShiftLimit(emp.getId(), employeeShiftCount, rule))
                .filter(emp -> !isOverMonthlyHours(emp.getId(), employeeWorkHours, rule))
                .collect(Collectors.toList());
    }

    private boolean isEmployeeAvailable(Long employeeId, int dayOfWeek, LocalDate dutyDate, DutyShiftConfig shiftConfig) {
        EmployeeAvailableTime availableTime = employeeAvailableTimeService.lambdaQuery()
                .eq(EmployeeAvailableTime::getEmployeeId, employeeId)
                .eq(EmployeeAvailableTime::getDayOfWeek, dayOfWeek)
                .one();

        if (availableTime == null || availableTime.getIsAvailable() == 0) {
            return false;
        }

        if (availableTime.getStartTime() != null && availableTime.getEndTime() != null) {
            String shiftStart = shiftConfig.getStartTime();
            String shiftEnd = shiftConfig.getEndTime();
            String availableStart = availableTime.getStartTime();
            String availableEnd = availableTime.getEndTime();

            if (shiftStart.compareTo(availableStart) < 0 || shiftEnd.compareTo(availableEnd) > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean isOverloaded(Long employeeId, Map<Long, BigDecimal> employeeWorkHours, DutyScheduleRule rule) {
        BigDecimal currentHours = employeeWorkHours.getOrDefault(employeeId, BigDecimal.ZERO);
        BigDecimal maxWeeklyHours = BigDecimal.valueOf(rule.getMaxWeeklyHours() != null ? rule.getMaxWeeklyHours().longValue() : 48L);
        return currentHours.compareTo(maxWeeklyHours) >= 0;
    }

    private boolean isOverShiftLimit(Long employeeId, Map<Long, Integer> employeeShiftCount, DutyScheduleRule rule) {
        int currentShifts = employeeShiftCount.getOrDefault(employeeId, 0);
        int maxDailyShifts = rule.getMaxDailyShifts() != null ? rule.getMaxDailyShifts() : 3;
        return currentShifts >= maxDailyShifts;
    }

    private boolean isOverMonthlyHours(Long employeeId, Map<Long, BigDecimal> employeeWorkHours, DutyScheduleRule rule) {
        BigDecimal currentHours = employeeWorkHours.getOrDefault(employeeId, BigDecimal.ZERO);
        BigDecimal maxMonthlyHours = BigDecimal.valueOf(rule.getMaxMonthlyHours() != null ? rule.getMaxMonthlyHours().longValue() : 200L);
        return currentHours.compareTo(maxMonthlyHours) >= 0;
    }

    private SysEmployee selectEmployeeForShift(
            List<SysEmployee> availableEmployees, Map<Long, BigDecimal> employeeWorkHours,
            Map<Long, Integer> employeeShiftCount, DutyScheduleRule rule) {
        if (availableEmployees.isEmpty()) {
            return null;
        }

        return availableEmployees.stream()
                .min((e1, e2) -> {
                    BigDecimal workload1 = employeeWorkHours.getOrDefault(e1.getId(), BigDecimal.ZERO);
                    BigDecimal workload2 = employeeWorkHours.getOrDefault(e2.getId(), BigDecimal.ZERO);
                    int shifts1 = employeeShiftCount.getOrDefault(e1.getId(), 0);
                    int shifts2 = employeeShiftCount.getOrDefault(e2.getId(), 0);
                    
                    if (workload1.compareTo(workload2) != 0) {
                        return workload1.compareTo(workload2);
                    }
                    
                    if (shifts1 != shifts2) {
                        return Integer.compare(shifts1, shifts2);
                    }
                    
                    return 0;
                })
                .orElse(null);
    }

    @Override
    public List<DutyAssignment> generateAutoScheduleByWorkHours(Long scheduleId, LocalDate startDate, LocalDate endDate, Long ruleId, Long employeeId) {
        List<DutyAssignment> assignments = new ArrayList<>();

        DutyScheduleRule rule = dutyScheduleRuleService.getById(ruleId != null ? ruleId : 1L);
        if (rule == null) {
            return assignments;
        }

        List<SysEmployee> employees = sysEmployeeService.lambdaQuery()
                .eq(SysEmployee::getId, employeeId)
                .eq(SysEmployee::getStatus, 1)
                .list();

        List<DutyShiftConfig> shiftConfigs = dutyShiftConfigService.lambdaQuery()
                .eq(DutyShiftConfig::getStatus, 1)
                .orderByAsc(DutyShiftConfig::getSort)
                .list();

        Map<Long, BigDecimal> employeeWorkHours = getEmployeeMonthlyWorkHours(scheduleId, startDate, endDate);

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (DutyShiftConfig shiftConfig : shiftConfigs) {
                List<SysEmployee> availableEmployees = getAvailableEmployeesForShift(
                        currentDate, shiftConfig, employees, employeeWorkHours, new HashMap<>(), rule
                );

                if (!availableEmployees.isEmpty()) {
                    SysEmployee selectedEmployee = availableEmployees.get(0);
                    DutyAssignment assignment = new DutyAssignment();
                    assignment.setScheduleId(scheduleId);
                    assignment.setDutyDate(currentDate);
                    assignment.setDutyShift(shiftConfig.getShiftType());
                    assignment.setEmployeeId(selectedEmployee.getId());
                    assignment.setStatus(1);
                    assignment.setShiftConfigId(shiftConfig.getId());
                    assignments.add(assignment);

                    employeeWorkHours.put(selectedEmployee.getId(), 
                            employeeWorkHours.getOrDefault(selectedEmployee.getId(), BigDecimal.ZERO).add(shiftConfig.getDurationHours()));
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return assignments;
    }

    @Override
    public Map<Long, BigDecimal> getEmployeeMonthlyWorkHours(Long scheduleId, LocalDate startDate, LocalDate endDate) {
        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = endDate.withDayOfMonth(endDate.lengthOfMonth());

        List<DutyAssignment> assignments = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getScheduleId, scheduleId)
                .ge(DutyAssignment::getDutyDate, monthStart)
                .le(DutyAssignment::getDutyDate, monthEnd)
                .eq(DutyAssignment::getStatus, 1)
                .list();

        Map<Long, BigDecimal> employeeWorkHours = new HashMap<>();
        Map<Long, BigDecimal> shiftConfigMap = dutyShiftConfigService.lambdaQuery()
                .eq(DutyShiftConfig::getStatus, 1)
                .list()
                .stream()
                .collect(Collectors.toMap(DutyShiftConfig::getId, DutyShiftConfig::getDurationHours));

        for (DutyAssignment assignment : assignments) {
            Long employeeId = assignment.getEmployeeId();
            Long shiftConfigId = assignment.getShiftConfigId();
            BigDecimal duration = shiftConfigMap.getOrDefault(shiftConfigId, BigDecimal.ZERO);
            
            employeeWorkHours.put(employeeId, 
                    employeeWorkHours.getOrDefault(employeeId, BigDecimal.ZERO).add(duration));
        }

        return employeeWorkHours;
    }

    @Override
    public List<DutyAssignment> generateScheduleByMode(Long scheduleId, LocalDate startDate, LocalDate endDate, Long modeId, Map<String, Object> configParams) {
        List<DutyAssignment> assignments = new ArrayList<>();
        
        // 获取值班表信息
        DutySchedule schedule = dutyScheduleService.getById(scheduleId);
        if (schedule == null) {
            return assignments;
        }
        
        // 获取参与排班的员工列表
        List<SysEmployee> employees = sysEmployeeService.listByIds(dutyScheduleService.getEmployeeIdsByScheduleId(scheduleId));
        if (employees.isEmpty()) {
            return assignments;
        }
        
        // 获取排班模式和对应的算法实例
        DutyScheduleMode mode = dutyScheduleModeService.getById(modeId);
        if (mode == null) {
            return assignments;
        }
        
        ScheduleAlgorithm algorithm = dutyScheduleModeService.getAlgorithmInstanceByModeId(modeId);
        if (algorithm == null) {
            return assignments;
        }
        
        // 合并默认配置和传入配置
        Map<String, Object> combinedConfig = new HashMap<>();
        Map<String, Object> defaultConfig = dutyScheduleModeService.getModeConfig(modeId);
        if (defaultConfig != null) {
            combinedConfig.putAll(defaultConfig);
        }
        if (configParams != null) {
            combinedConfig.putAll(configParams);
        }
        
        // 生成排班安排
        assignments = algorithm.generateSchedule(schedule, employees, startDate, endDate, combinedConfig);
        
        return assignments;
    }
}
