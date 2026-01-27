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
import com.lasu.hyperduty.service.LeaveRequestService;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Autowired
    private LeaveRequestService leaveRequestService;

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

        // 获取所有员工ID列表
        List<Long> employeeIds = allEmployees.stream()
                .map(SysEmployee::getId)
                .collect(Collectors.toList());

        // 获取员工请假信息
        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = leaveRequestService.getEmployeeLeaveInfo(
                employeeIds,
                dutyDate.toString(),
                dutyDate.toString()
        );

        return allEmployees.stream()
                .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                .filter(emp -> {
                    // 检查员工是否在当天请假
                    Map<String, Map<String, List<Long>>> empLeaveInfo = leaveInfo.get(emp.getId());
                    if (empLeaveInfo == null) {
                        return true;
                    }
                    
                    Map<String, List<Long>> dateLeaveInfo = empLeaveInfo.get(dutyDate.toString());
                    if (dateLeaveInfo == null) {
                        return true;
                    }
                    
                    // 检查是否在任何值班表中请假
                    for (Map.Entry<String, List<Long>> entry : dateLeaveInfo.entrySet()) {
                        List<Long> shiftIds = entry.getValue();
                        if (shiftIds != null && shiftIds.contains(Long.valueOf(dutyShift))) {
                            return false;
                        }
                    }
                    
                    return true;
                })
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

        // 获取员工请假信息
        List<Long> employeeIds = employees.stream()
                .map(SysEmployee::getId)
                .collect(Collectors.toList());

        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = leaveRequestService.getEmployeeLeaveInfo(
                employeeIds,
                dutyDate.toString(),
                dutyDate.toString()
        );

        return employees.stream()
                .filter(emp -> !checkConflict(emp.getId(), dutyDate, shiftConfig.getShiftType()))
                .filter(emp -> isEmployeeAvailable(emp.getId(), dayOfWeek, dutyDate, shiftConfig))
                .filter(emp -> !isOverloaded(emp.getId(), employeeWorkHours, rule))
                .filter(emp -> !isOverShiftLimit(emp.getId(), employeeShiftCount, rule))
                .filter(emp -> !isOverMonthlyHours(emp.getId(), employeeWorkHours, rule))
                .filter(emp -> {
                    // 检查员工是否在当天请假
                    Map<String, Map<String, List<Long>>> empLeaveInfo = leaveInfo.get(emp.getId());
                    if (empLeaveInfo == null) {
                        return true;
                    }
                    
                    Map<String, List<Long>> dateLeaveInfo = empLeaveInfo.get(dutyDate.toString());
                    if (dateLeaveInfo == null) {
                        return true;
                    }
                    
                    // 检查是否在任何值班表中请假
                    for (Map.Entry<String, List<Long>> entry : dateLeaveInfo.entrySet()) {
                        List<Long> shiftIds = entry.getValue();
                        if (shiftIds != null && shiftIds.contains(Long.valueOf(shiftConfig.getShiftType()))) {
                            return false;
                        }
                    }
                    
                    return true;
                })
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

    /**
     * 根据排班模式生成排班
     * @param scheduleId 值班表ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param modeId 排班模式ID
     * @param configParams 配置参数
     * @return 生成的值班安排列表
     */
    @Override
    public List<DutyAssignment> generateScheduleByMode(Long scheduleId, LocalDate startDate, LocalDate endDate, Long modeId, Map<String, Object> configParams) {
        List<DutyAssignment> assignments = new ArrayList<>();
        
        // 获取值班表信息
        DutySchedule schedule = dutyScheduleService.getById(scheduleId);
        if (schedule == null) {
            return assignments;
        }
        
        // 获取参与排班的员工列表
        List<SysEmployee> employees;
        // 优先使用前端传入的员工列表
        if (configParams != null && configParams.containsKey("employeeIds")) {
            List<Long> employeeIds = (List<Long>) configParams.get("employeeIds");
            if (employeeIds != null && !employeeIds.isEmpty()) {
                employees = sysEmployeeService.listByIds(employeeIds);
            } else {
                // 如果前端没有传入员工列表，使用值班表中的所有员工
                employees = sysEmployeeService.listByIds(dutyScheduleService.getEmployeeIdsByScheduleId(scheduleId));
            }
        } else {
            // 如果前端没有传入员工列表，使用值班表中的所有员工
            employees = sysEmployeeService.listByIds(dutyScheduleService.getEmployeeIdsByScheduleId(scheduleId));
        }
        if (employees.isEmpty()) {
            return assignments;
        }
        
        // 获取排班模式和配置
        DutyScheduleMode mode = dutyScheduleModeService.getById(modeId);
        if (mode == null) {
            return assignments;
        }
        
        // 获取排班模式配置
        Map<String, Object> modeConfig = dutyScheduleModeService.getModeConfig(modeId);
        if (modeConfig == null) {
            System.err.println("排班模式配置为空，modeId: " + modeId);
            return assignments;
        }
        System.out.println("排班模式配置: " + modeConfig);
        
        // 合并默认配置和传入配置
        Map<String, Object> combinedConfig = new HashMap<>();
        combinedConfig.putAll(modeConfig);
        if (configParams != null) {
            combinedConfig.putAll(configParams);
        }
        
        // 解析前端配置格式
        List<Map<String, Object>> groupsConfig = new ArrayList<>();
        Integer cycleDays = 7; // 默认周期天数
        
        // 处理前端传递的teams配置
        if (combinedConfig.containsKey("teams")) {
            List<Map<String, Object>> teams = (List<Map<String, Object>>) combinedConfig.get("teams");
            if (!teams.isEmpty()) {
                // 转换teams配置为groups配置
                for (Map<String, Object> team : teams) {
                    List<Map<String, Object>> shifts = (List<Map<String, Object>>) team.getOrDefault("shifts", new ArrayList<>());
                    Map<String, Object> groupConfig = new HashMap<>();
                    List<Map<String, Object>> dayConfigs = new ArrayList<>();
                    
                    // 为每天创建配置
                    for (int i = 0; i < shifts.size(); i++) {
                        Map<String, Object> shift = shifts.get(i);
                        Map<String, Object> dayConfig = new HashMap<>();
                        
                        // 解析班次ID和人数
                        String shiftId = (String) shift.getOrDefault("shiftId", "");
                        Integer count = (Integer) shift.getOrDefault("count", 0);
                        
                        if (!shiftId.isEmpty() && count > 0) {
                            dayConfig.put("shiftType", shiftId);
                            dayConfig.put("employeeCount", count);
                        }
                        
                        dayConfigs.add(dayConfig);
                    }
                    
                    groupConfig.put("days", dayConfigs);
                    groupsConfig.add(groupConfig);
                }
                
                // 设置周期天数为班次配置的天数
                if (!groupsConfig.isEmpty()) {
                    Map<String, Object> firstGroup = groupsConfig.get(0);
                    List<Map<String, Object>> firstGroupDays = (List<Map<String, Object>>) firstGroup.getOrDefault("days", new ArrayList<>());
                    cycleDays = firstGroupDays.size();
                }
            }
        } else if (combinedConfig.containsKey("groups")) {
            // 兼容旧格式
            groupsConfig = (List<Map<String, Object>>) combinedConfig.getOrDefault("groups", new ArrayList<>());
            cycleDays = (Integer) combinedConfig.getOrDefault("cycleDays", 7);
        }
        
        System.out.println("转换后的组数: " + groupsConfig.size());
        System.out.println("周期天数: " + cycleDays);
        
        if (groupsConfig.isEmpty()) {
            System.err.println("各组配置为空");
            return assignments;
        }
        
        // 计算员工分组
        List<List<SysEmployee>> employeeGroups = groupEmployees(employees, groupsConfig.size());
        
        // 获取员工请假信息
        Map<Long, Map<LocalDate, Map<Long, List<Long>>>> leaveInfo = new HashMap<>();
        if (configParams != null && configParams.containsKey("leaveInfo")) {
            try {
                Object leaveInfoObj = configParams.get("leaveInfo");
                if (leaveInfoObj instanceof Map) {
                    Map<?, ?> rawLeaveInfo = (Map<?, ?>) leaveInfoObj;
                    for (Map.Entry<?, ?> entry : rawLeaveInfo.entrySet()) {
                        try {
                            String employeeIdStr = entry.getKey().toString();
                            Long employeeId = Long.parseLong(employeeIdStr);
                            Map<LocalDate, Map<Long, List<Long>>> employeeLeaveMap = new HashMap<>();
                            
                            Object dateLeaveMapObj = entry.getValue();
                            if (dateLeaveMapObj instanceof Map) {
                                Map<?, ?> dateLeaveMap = (Map<?, ?>) dateLeaveMapObj;
                                for (Map.Entry<?, ?> dateEntry : dateLeaveMap.entrySet()) {
                                    try {
                                        String dateStr = dateEntry.getKey().toString();
                                        LocalDate date = LocalDate.parse(dateStr);
                                        Map<Long, List<Long>> scheduleLeaveMap = new HashMap<>();
                                        
                                        Object scheduleLeaveRawMapObj = dateEntry.getValue();
                                        if (scheduleLeaveRawMapObj instanceof Map) {
                                            Map<?, ?> scheduleLeaveRawMap = (Map<?, ?>) scheduleLeaveRawMapObj;
                                            for (Map.Entry<?, ?> scheduleEntry : scheduleLeaveRawMap.entrySet()) {
                                                try {
                                                    String scheduleIdStr = scheduleEntry.getKey().toString();
                                                    Long currentScheduleId = Long.parseLong(scheduleIdStr);
                                                    
                                                    Object shiftConfigIdsObj = scheduleEntry.getValue();
                                                    if (shiftConfigIdsObj instanceof List) {
                                                        List<?> shiftConfigIdsList = (List<?>) shiftConfigIdsObj;
                                                        List<Long> shiftConfigIds = new ArrayList<>();
                                                        for (Object shiftIdObj : shiftConfigIdsList) {
                                                            try {
                                                                if (shiftIdObj instanceof Number) {
                                                                    shiftConfigIds.add(((Number) shiftIdObj).longValue());
                                                                } else if (shiftIdObj instanceof String) {
                                                                    shiftConfigIds.add(Long.parseLong((String) shiftIdObj));
                                                                }
                                                            } catch (Exception e) {
                                                                // 班次ID格式错误，跳过
                                                                System.err.println("班次ID格式错误: " + shiftIdObj);
                                                            }
                                                        }
                                                        scheduleLeaveMap.put(currentScheduleId, shiftConfigIds);
                                                    }
                                                } catch (Exception e) {
                                                    // 值班表ID格式错误，跳过
                                                    System.err.println("值班表ID格式错误: " + scheduleEntry.getKey());
                                                }
                                            }
                                        }
                                        
                                        employeeLeaveMap.put(date, scheduleLeaveMap);
                                    } catch (Exception e) {
                                        // 日期格式错误，跳过
                                        System.err.println("日期格式错误: " + dateEntry.getKey());
                                    }
                                }
                            }
                            
                            leaveInfo.put(employeeId, employeeLeaveMap);
                        } catch (Exception e) {
                            // 员工ID格式错误，跳过
                            System.err.println("员工ID格式错误: " + entry.getKey());
                        }
                    }
                }
            } catch (Exception e) {
                // 请假信息处理失败，记录错误但继续排班
                System.err.println("处理请假信息失败: " + e.getMessage());
                e.printStackTrace();
                // 继续使用空的leaveInfo，确保排班过程不被中断
            }
        }
        
        // 生成排班安排
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 计算当前是周期的第几天（从0开始）
            int dayOfCycle = (int) ChronoUnit.DAYS.between(startDate, currentDate) % cycleDays;
            if (dayOfCycle < 0) {
                dayOfCycle += cycleDays;
            }
            
            // 为每个组生成排班
            for (int groupIndex = 0; groupIndex < groupsConfig.size(); groupIndex++) {
                Map<String, Object> groupConfig = groupsConfig.get(groupIndex);
                List<SysEmployee> groupEmployees = employeeGroups.get(groupIndex);
                
                // 获取当天该组的班次配置
                List<Map<String, Object>> dayConfigs = (List<Map<String, Object>>) groupConfig.getOrDefault("days", new ArrayList<>());
                if (dayConfigs.size() <= dayOfCycle) {
                    continue; // 该组当天没有配置，轮空
                }
                
                Map<String, Object> dayConfig = dayConfigs.get(dayOfCycle);
                Object shiftTypeObj = dayConfig.getOrDefault("shiftType", "");
                String shiftType = shiftTypeObj != null ? shiftTypeObj.toString() : "";
                Integer employeeCount = (Integer) dayConfig.getOrDefault("employeeCount", 0);
                
                // 如果没有设置班次或人数为0，轮空
                if (shiftType.isEmpty() || employeeCount <= 0) {
                    continue;
                }
                
                // 过滤出当天没有请假的员工
                List<SysEmployee> dayAvailableEmployees = new ArrayList<>();
                for (SysEmployee employee : groupEmployees) {
                    // 检查员工是否在当天、当前值班表、当前班次请假
                    boolean isOnLeave = false;
                    Map<LocalDate, Map<Long, List<Long>>> employeeLeaveMap = leaveInfo.get(employee.getId());
                    if (employeeLeaveMap != null) {
                        Map<Long, List<Long>> scheduleLeaveMap = employeeLeaveMap.get(currentDate);
                        if (scheduleLeaveMap != null) {
                            // 检查当前值班表的请假记录
                            List<Long> shiftConfigIds = scheduleLeaveMap.get(scheduleId);
                            if (shiftConfigIds != null) {
                                // 检查当前班次是否在请假的班次列表中
                                try {
                                    Long dutyShift = Long.parseLong(shiftType);
                                    if (shiftConfigIds.contains(dutyShift)) {
                                        isOnLeave = true;
                                    }
                                } catch (NumberFormatException e) {
                                    // 班次ID格式错误，跳过
                                    System.err.println("班次ID格式错误: " + shiftType);
                                }
                            }
                        }
                    }
                    
                    if (!isOnLeave) {
                        dayAvailableEmployees.add(employee);
                    }
                }
                
                if (dayAvailableEmployees.isEmpty()) {
                    // 当天该组没有可用员工，跳过
                    System.out.println("日期 " + currentDate + " 第 " + (groupIndex + 1) + " 组没有可用员工，跳过排班");
                    continue;
                }
                
                // 为该组当天生成排班
                // 维护一个轮换索引，确保顺次排班
                // 这里使用一个简单的实现，每次从可用员工列表中按顺序选择
                // 实际项目中可以考虑使用更复杂的轮换算法
                for (int i = 0; i < employeeCount && i < dayAvailableEmployees.size(); i++) {
                    SysEmployee employee = dayAvailableEmployees.get(i);
                    
                    DutyAssignment assignment = new DutyAssignment();
                    assignment.setScheduleId(scheduleId);
                    assignment.setDutyDate(currentDate);
                    assignment.setDutyShift(Integer.parseInt(shiftType)); // 直接使用班次ID作为dutyShift
                    assignment.setEmployeeId(employee.getId());
                    assignment.setStatus(1);
                    assignment.setCreateTime(LocalDateTime.now());
                    assignment.setUpdateTime(LocalDateTime.now());
                    
                    assignments.add(assignment);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return assignments;
    }
    
    /**
     * 将员工分组
     * @param employees 员工列表
     * @param groupCount 组数
     * @return 分组后的员工列表
     */
    private List<List<SysEmployee>> groupEmployees(List<SysEmployee> employees, int groupCount) {
        List<List<SysEmployee>> groups = new ArrayList<>();
        
        // 初始化分组
        for (int i = 0; i < groupCount; i++) {
            groups.add(new ArrayList<>());
        }
        
        // 分配员工到各组
        for (int i = 0; i < employees.size(); i++) {
            int groupIndex = i % groupCount;
            groups.get(groupIndex).add(employees.get(i));
        }
        
        return groups;
    }
    
    /**
     * 获取班次类型代码
     * @param shiftType 班次类型名称
     * @return 班次类型代码
     */
    private Integer getShiftTypeCode(String shiftType) {
        // 根据班次类型名称获取对应的代码
        Map<String, Integer> shiftTypeMap = new HashMap<>();
        shiftTypeMap.put("白班", 1);
        shiftTypeMap.put("夜班", 2);
        shiftTypeMap.put("值班", 3);
        shiftTypeMap.put("休息", 4);
        shiftTypeMap.put("选择班次", 0);
        
        return shiftTypeMap.getOrDefault(shiftType, 0);
    }
}
