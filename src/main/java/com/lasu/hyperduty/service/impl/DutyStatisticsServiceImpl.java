package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyStatistics;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.DutyStatisticsMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.DutyStatisticsService;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import com.lasu.hyperduty.service.LeaveRequestService;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.service.SysDeptService;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.entity.SysDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DutyStatisticsServiceImpl extends ServiceImpl<DutyStatisticsMapper, DutyStatistics> implements DutyStatisticsService {

    @Autowired
    private DutyScheduleService dutyScheduleService;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyRecordService dutyRecordService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private DutyShiftConfigService dutyShiftConfigService;

    @Override
    public Map<String, Object> getOverallStatistics() {
        // 创建统计结果Map
        Map<String, Object> statistics = new HashMap<>();

        // 获取所有基础数据
        List<DutySchedule> schedules = dutyScheduleService.list();
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        List<DutyRecord> records = dutyRecordService.list();
        List<LeaveRequest> leaveRequests = leaveRequestService.list();

        // 统计基础数量
        statistics.put("totalSchedules", schedules.size());
        statistics.put("totalAssignments", assignments.size());
        statistics.put("totalRecords", records.size());

        // 计算总工时和天数
        BigDecimal totalHours = BigDecimal.ZERO;
        int totalDays = 0;
        
        for (DutyRecord record : records) {
            // 只计算有签到签退记录的工时
            if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                // 计算工时（分钟转小时）
                long minutes = java.time.Duration.between(record.getCheckInTime(), record.getCheckOutTime()).toMinutes();
                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                totalHours = totalHours.add(hours);
                
                // 计算天数（去重）
                LocalDate checkInDate = record.getCheckInTime().toLocalDate();
                if (!records.stream().anyMatch(r -> r.getCheckInTime() != null && 
                        r.getCheckInTime().toLocalDate().equals(checkInDate) && 
                        r.getId() != record.getId())) {
                    totalDays++;
                }
            }
        }

        // 计算日均工时
        BigDecimal avgDailyHours = totalDays > 0 ? totalHours.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        statistics.put("avgDailyHours", avgDailyHours);

        // 计算总加班时长
        Integer totalOvertimeHours = 0;
        for (DutyRecord record : records) {
            if (record.getOvertimeHours() != null) {
                totalOvertimeHours += record.getOvertimeHours();
            }
        }
        statistics.put("totalOvertimeHours", BigDecimal.valueOf(totalOvertimeHours));

        // 统计请假申请数量
        statistics.put("totalLeaveRequests", leaveRequests.size());

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getDeptStatistics(Long deptId) {
        // 获取所有基础数据
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        List<SysEmployee> employees = sysEmployeeService.list();
        List<SysDept> depts = sysDeptService.list();

        // 构建员工ID到员工的映射
        Map<Long, SysEmployee> employeeMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, e -> e));

        // 按部门分组统计排班数量
        Map<Long, List<DutyAssignment>> assignmentsByDept = new HashMap<>();

        for (DutyAssignment assignment : assignments) {
            SysEmployee employee = employeeMap.get(assignment.getEmployeeId());
            if (employee != null && employee.getDeptId() != null) {
                assignmentsByDept.computeIfAbsent(employee.getDeptId(), k -> new ArrayList<>()).add(assignment);
            }
        }

        // 构建部门ID到部门的映射
        Map<Long, SysDept> deptMap = depts.stream()
                .collect(Collectors.toMap(SysDept::getId, d -> d));

        // 构建部门统计结果
        List<Map<String, Object>> deptStats = new ArrayList<>();

        for (Map.Entry<Long, List<DutyAssignment>> entry : assignmentsByDept.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            SysDept dept = deptMap.get(entry.getKey());
            stat.put("deptId", entry.getKey());
            stat.put("deptName", dept != null ? dept.getDeptName() : "未知部门");
            stat.put("assignmentCount", entry.getValue().size());
            deptStats.add(stat);
        }

        return deptStats;
    }

    @Override
    public List<Map<String, Object>> getShiftDistribution() {
        // 获取所有排班数据
        List<DutyAssignment> assignments = dutyAssignmentService.list();

        // 按班次ID统计数量，优先使用shiftConfigId，其次使用dutyShift
        Map<Long, Long> shiftCount = new HashMap<>();
        
        for (DutyAssignment assignment : assignments) {
            if (assignment.getShiftConfigId() != null) {
                // 使用shiftConfigId作为统计键
                shiftCount.put(assignment.getShiftConfigId(), shiftCount.getOrDefault(assignment.getShiftConfigId(), 0L) + 1);
            } else if (assignment.getDutyShift() != null) {
                // 使用dutyShift作为统计键
                shiftCount.put(assignment.getDutyShift().longValue(), shiftCount.getOrDefault(assignment.getDutyShift().longValue(), 0L) + 1);
            }
        }

        // 构建班次名称映射，只使用启用状态的班次配置
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        // 获取所有班次配置
        List<DutyShiftConfig> shiftConfigs = dutyShiftConfigService.list();
        // 过滤出启用状态的班次（status = 1 或 status = null）
        List<DutyShiftConfig> enabledShiftConfigs = shiftConfigs.stream()
                .filter(config -> config.getStatus() == null || config.getStatus() == 1)
                .collect(Collectors.toList());

        // 构建班次分布结果
        for (DutyShiftConfig shiftConfig : enabledShiftConfigs) {
            Map<String, Object> item = new HashMap<>();
            item.put("shiftName", shiftConfig.getShiftName());
            // 统计该班次的总数量，包括通过shiftConfigId和dutyShift存储的情况
            long count = shiftCount.getOrDefault(shiftConfig.getId(), 0L);
            item.put("count", count);
            distribution.add(item);
        }

        return distribution;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend() {
        // 获取所有值班记录
        List<DutyRecord> records = dutyRecordService.list();

        // 创建月度工时和加班时长的Map
        Map<String, BigDecimal> monthlyHours = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyOvertime = new LinkedHashMap<>();

        // 获取当前日期
        LocalDate now = LocalDate.now();
        // 统计近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.getMonthValue() + "月";

            BigDecimal hours = BigDecimal.ZERO;
            Integer overtime = 0;

            // 遍历所有值班记录，统计当月的工时
            for (DutyRecord record : records) {
                if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                    LocalDate recordDate = record.getCheckInTime().toLocalDate();
                    if (recordDate.getYear() == month.getYear() && recordDate.getMonthValue() == month.getMonthValue()) {
                        // 计算工时（分钟转小时）
                        long minutes = java.time.Duration.between(record.getCheckInTime(), record.getCheckOutTime()).toMinutes();
                        BigDecimal recordHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                        hours = hours.add(recordHours);
                    }
                }
                
                // 统计当月的加班时长
                if (record.getOvertimeHours() != null && record.getCheckInTime() != null) {
                    LocalDate recordDate = record.getCheckInTime().toLocalDate();
                    if (recordDate.getYear() == month.getYear() && recordDate.getMonthValue() == month.getMonthValue()) {
                        overtime += record.getOvertimeHours();
                    }
                }
            }

            // 存储月度统计数据
            monthlyHours.put(monthKey, hours);
            monthlyOvertime.put(monthKey, BigDecimal.valueOf(overtime));
        }

        // 构建月度趋势结果
        List<Map<String, Object>> trend = new ArrayList<>();
        for (String month : monthlyHours.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("hours", monthlyHours.get(month));
            item.put("overtime", monthlyOvertime.get(month));
            trend.add(item);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getEmployeeStatistics(Integer year, Integer month) {
        // 获取所有基础数据
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        List<DutyRecord> records = dutyRecordService.list();
        List<SysEmployee> employees = sysEmployeeService.list();
        List<DutyShiftConfig> shiftConfigs = dutyShiftConfigService.list();

        // 构建员工ID到员工的映射
        Map<Long, SysEmployee> employeeMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, e -> e));

        // 构建班次配置ID到班次配置的映射
        Map<Long, DutyShiftConfig> shiftConfigMap = shiftConfigs.stream()
                .collect(Collectors.toMap(DutyShiftConfig::getId, c -> c));

        // 获取当前日期
        LocalDate now = LocalDate.now();
        // 确定统计的年月（默认为当前年月）
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDate today = now;

        // 按员工分组统计排班（整个月份的排班，用于计算计划工时）
        Map<Long, List<DutyAssignment>> monthAssignmentsByEmployee = assignments.stream()
                .filter(a -> {
                    LocalDate assignmentDate = a.getDutyDate();
                    return assignmentDate != null && 
                           assignmentDate.getYear() == targetYear && 
                           assignmentDate.getMonthValue() == targetMonth;
                })
                .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

        // 按员工分组统计排班（当前时间之前的排班，用于计算实际工时）
        Map<Long, List<DutyAssignment>> actualAssignmentsByEmployee = assignments.stream()
                .filter(a -> {
                    LocalDate assignmentDate = a.getDutyDate();
                    return assignmentDate != null && 
                           assignmentDate.getYear() == targetYear && 
                           assignmentDate.getMonthValue() == targetMonth &&
                           !assignmentDate.isAfter(today);
                })
                .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

        // 构建员工统计结果
        List<Map<String, Object>> employeeStats = new ArrayList<>();

        for (SysEmployee employee : employees) {
            Map<String, Object> stat = new HashMap<>();
            Long employeeId = employee.getId();

            // 基础信息
            stat.put("employeeId", employeeId);
            stat.put("employeeName", employee.getEmployeeName());
            stat.put("year", targetYear);
            stat.put("month", targetMonth);

            // 获取该员工整个月份的排班列表（用于计算计划工时）
            List<DutyAssignment> monthAssignments = monthAssignmentsByEmployee.getOrDefault(employeeId, new ArrayList<>());
            
            // 计算计划工时：整个月份的排班工时求和（从班次配置中获取时长，默认8小时）
            BigDecimal plannedHours = BigDecimal.ZERO;
            for (DutyAssignment assignment : monthAssignments) {
                if (assignment.getShiftConfigId() != null) {
                    DutyShiftConfig shiftConfig = shiftConfigMap.get(assignment.getShiftConfigId());
                    if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                        // 使用班次配置中的时长
                        plannedHours = plannedHours.add(shiftConfig.getDurationHours());
                    } else {
                        // 班次配置不存在或时长为空，默认8小时
                        plannedHours = plannedHours.add(BigDecimal.valueOf(8));
                    }
                } else {
                    // 没有班次配置ID，默认8小时
                    plannedHours = plannedHours.add(BigDecimal.valueOf(8));
                }
            }
            stat.put("plannedHours", plannedHours);

            // 获取该员工当前时间之前的排班列表（用于计算实际工时）
            List<DutyAssignment> actualAssignments = actualAssignmentsByEmployee.getOrDefault(employeeId, new ArrayList<>());
            
            // 计算实际工时：当前时间之前的排班工时求和
            BigDecimal actualHours = BigDecimal.ZERO;
            for (DutyAssignment assignment : actualAssignments) {
                if (assignment.getShiftConfigId() != null) {
                    DutyShiftConfig shiftConfig = shiftConfigMap.get(assignment.getShiftConfigId());
                    if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                        // 使用班次配置中的时长
                        actualHours = actualHours.add(shiftConfig.getDurationHours());
                    } else {
                        // 班次配置不存在或时长为空，默认8小时
                        actualHours = actualHours.add(BigDecimal.valueOf(8));
                    }
                } else {
                    // 没有班次配置ID，默认8小时
                    actualHours = actualHours.add(BigDecimal.valueOf(8));
                }
            }
            stat.put("actualHours", actualHours);
            
            // 计算实际天数：实际工时 / 8
            BigDecimal actualDays = actualHours.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
            stat.put("actualDays", actualDays);

            // 计算可调休工时：审批通过的加班时长
            BigDecimal compensatoryHours = BigDecimal.ZERO;
            for (DutyRecord record : records) {
                if (record.getEmployeeId().equals(employeeId) && 
                    record.getApprovalStatus() != null && 
                    "已批准".equals(record.getApprovalStatus()) &&
                    record.getOvertimeHours() != null) {
                    compensatoryHours = compensatoryHours.add(new BigDecimal(record.getOvertimeHours()));
                }
            }
            
            // 减去审批通过的调休请假时长
            List<LeaveRequest> leaveRequests = leaveRequestService.list();
            for (LeaveRequest request : leaveRequests) {
                if (request.getEmployeeId().equals(employeeId) && 
                    request.getApprovalStatus() != null && 
                    "approved".equals(request.getApprovalStatus()) &&
                    request.getLeaveType() != null && 
                    request.getLeaveType() == 4 && // 调休类型
                    request.getTotalHours() != null) {
                    compensatoryHours = compensatoryHours.subtract(BigDecimal.valueOf(request.getTotalHours()));
                }
            }
            
            // 确保可调休工时不小于0
            if (compensatoryHours.compareTo(BigDecimal.ZERO) < 0) {
                compensatoryHours = BigDecimal.ZERO;
            }
            
            stat.put("compensatoryHours", compensatoryHours);

            employeeStats.add(stat);
        }

        // 按员工姓名排序
        return employeeStats.stream()
                .sorted((a, b) -> {
                    String nameA = (String) a.get("employeeName");
                    String nameB = (String) b.get("employeeName");
                    return nameA.compareTo(nameB);
                })
                .collect(Collectors.toList());
    }
}
