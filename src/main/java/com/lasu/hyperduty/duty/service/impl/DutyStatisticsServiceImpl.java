package com.lasu.hyperduty.duty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.entity.DutyRecord;
import com.lasu.hyperduty.duty.entity.DutySchedule;
import com.lasu.hyperduty.duty.entity.DutyShiftConfig;
import com.lasu.hyperduty.duty.entity.DutyStatistics;
import com.lasu.hyperduty.duty.entity.LeaveRequest;
import com.lasu.hyperduty.duty.mapper.DutyStatisticsMapper;
import com.lasu.hyperduty.duty.service.DutyAssignmentService;
import com.lasu.hyperduty.duty.service.DutyHolidayService;
import com.lasu.hyperduty.duty.service.DutyRecordService;
import com.lasu.hyperduty.duty.service.DutyScheduleService;
import com.lasu.hyperduty.duty.service.DutyShiftConfigService;
import com.lasu.hyperduty.duty.service.DutyStatisticsService;
import com.lasu.hyperduty.duty.service.LeaveRequestService;
import com.lasu.hyperduty.system.entity.SysDept;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.service.SysDeptService;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;










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

    @Autowired
    private DutyHolidayService dutyHolidayService;

    /**
     * 按排班日期类型选用加班工时
     * <p>
     * 优先级：法定节假日 → 休息日（周末且非调休工作日） → 日常
     * 未配置时回退到 overtimeHours（保持向后兼容）
     *
     * @param shiftConfig 班次配置
     * @param dutyDate    排班日期
     * @return 加班工时；班次非加班班次/未配置时返回 null
     */
    private BigDecimal pickOvertimeHoursByDate(DutyShiftConfig shiftConfig, LocalDate dutyDate) {
        if (shiftConfig == null
            || shiftConfig.getIsOvertimeShift() == null
            || shiftConfig.getIsOvertimeShift() != 1) {
            return null;
        }
        BigDecimal fallback = shiftConfig.getOvertimeHours();
        if (dutyDate == null) {
            return fallback;
        }
        // 1. 法定节假日
        try {
            if (dutyHolidayService != null && dutyHolidayService.isHoliday(dutyDate)) {
                return shiftConfig.getHolidayOvertimeHours() != null
                    ? shiftConfig.getHolidayOvertimeHours() : fallback;
            }
        } catch (Exception ignore) {
            // 节假日服务异常时走默认规则
        }
        // 2. 周末（且非调休工作日）
        java.time.DayOfWeek dow = dutyDate.getDayOfWeek();
        if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
            return shiftConfig.getWeekendOvertimeHours() != null
                ? shiftConfig.getWeekendOvertimeHours() : fallback;
        }
        // 3. 普通工作日
        return fallback;
    }

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

        // 计算总加班时长（只统计已审批通过的）
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        for (DutyRecord record : records) {
            if (record.getOvertimeHours() != null) {
                // 只统计已审批通过的加班记录
                boolean isApproved = record.getApprovalStatus() != null && 
                                    ("approved".equals(record.getApprovalStatus()) || 
                                     "已批准".equals(record.getApprovalStatus()));
                if (isApproved) {
                    totalOvertimeHours = totalOvertimeHours.add(record.getOvertimeHours());
                }
            }
        }
        statistics.put("totalOvertimeHours", totalOvertimeHours);

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
        List<DutyAssignment> assignments = dutyAssignmentService.list();

        // 构建 assignmentId 到 dutyAssignment 的映射
        Map<Long, DutyAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(DutyAssignment::getId, a -> a, (a1, a2) -> a1));

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
            BigDecimal overtime = BigDecimal.ZERO;

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
                
                // 统计当月的加班时长（支持通过 dutyDate 或 assignmentId 获取日期）
                if (record.getOvertimeHours() != null) {
                    LocalDate recordDate = null;
                    
                    // 优先使用 record 中的 dutyDate 字段
                    if (record.getDutyDate() != null) {
                        recordDate = record.getDutyDate();
                    } else if (record.getAssignmentId() != null) {
                        // 如果没有 dutyDate，则通过 assignmentId 获取
                        DutyAssignment assignment = assignmentMap.get(record.getAssignmentId());
                        if (assignment != null && assignment.getDutyDate() != null) {
                            recordDate = assignment.getDutyDate();
                        }
                    } else if (record.getCheckInTime() != null) {
                        // 如果前两种都没有，则通过签到时间获取
                        recordDate = record.getCheckInTime().toLocalDate();
                    }
                    
                    if (recordDate != null && 
                        recordDate.getYear() == month.getYear() && 
                        recordDate.getMonthValue() == month.getMonthValue()) {
                        // 只统计已审批通过的加班记录
                        boolean isApproved = record.getApprovalStatus() != null && 
                                            ("approved".equals(record.getApprovalStatus()) || 
                                             "已批准".equals(record.getApprovalStatus()));
                        if (isApproved) {
                            overtime = overtime.add(record.getOvertimeHours());
                        }
                    }
                }
            }

            // 存储月度统计数据
            monthlyHours.put(monthKey, hours);
            monthlyOvertime.put(monthKey, overtime);
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
        // 2026-06-27 修复：员工统计只统计启用员工（status=1），禁用员工不再出现在个人统计里
        List<SysEmployee> employees = sysEmployeeService.lambdaQuery()
                .eq(SysEmployee::getStatus, 1)
                .list();
        List<DutyShiftConfig> shiftConfigs = dutyShiftConfigService.list();

        // 构建员工ID到员工的映射
        Map<Long, SysEmployee> employeeMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, e -> e));

        // 构建班次配置ID到班次配置的映射
        Map<Long, DutyShiftConfig> shiftConfigMap = shiftConfigs.stream()
                .collect(Collectors.toMap(DutyShiftConfig::getId, c -> c));

        // 获取当前日期
        LocalDate now = LocalDate.now();
        // 确定统计的年份（默认为当前年）
        int targetYear = year != null ? year : now.getYear();
        
        // 判断是按年份查询还是按月份查询
        boolean isYearQuery = month == null;
        List<Integer> targetMonths = new ArrayList<>();
        
        if (isYearQuery) {
            // 按年份查询：统计该年所有月份（1-12月）
            for (int m = 1; m <= 12; m++) {
                targetMonths.add(m);
            }
        } else {
            // 按月份查询：只统计指定月份
            targetMonths.add(month);
        }

        // 构建 assignmentId 到 dutyAssignment 的映射，方便获取日期
        Map<Long, DutyAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(DutyAssignment::getId, a -> a, (a1, a2) -> a1));

        // 构建员工统计结果
        List<Map<String, Object>> employeeStats = new ArrayList<>();
        LocalDate today = now;

        // 按年份查询：每个员工一条汇总记录
        if (isYearQuery) {
            // 按员工分组统计排班（整个年份的排班，用于计算计划工时）
            Map<Long, List<DutyAssignment>> yearAssignmentsByEmployee = assignments.stream()
                    .filter(a -> {
                        LocalDate assignmentDate = a.getDutyDate();
                        return assignmentDate != null && 
                               assignmentDate.getYear() == targetYear &&
                               a.getStatus() != null && a.getStatus() == 1;
                    })
                    .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

            // 按员工分组统计排班（当前时间之前的排班，用于计算实际工时）
            Map<Long, List<DutyAssignment>> actualAssignmentsByEmployee = assignments.stream()
                    .filter(a -> {
                        LocalDate assignmentDate = a.getDutyDate();
                        return assignmentDate != null && 
                               assignmentDate.getYear() == targetYear &&
                               !assignmentDate.isAfter(today) &&
                               a.getStatus() != null && a.getStatus() == 1;
                    })
                    .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

            // 按员工分组统计审批通过的加班记录（用于计算实际工时和可调休工时）
            Map<Long, List<DutyRecord>> approvedOvertimeByEmployee = records.stream()
                    .filter(r -> {
                        // 检查审批状态（支持中英文两种格式）
                        boolean isApproved = r.getApprovalStatus() != null && 
                                            ("approved".equals(r.getApprovalStatus()) || 
                                             "已批准".equals(r.getApprovalStatus()));
                        if (!isApproved) {
                            return false;
                        }
                        
                        // 获取日期（优先通过 dutyDate，其次通过 assignmentId 从 dutyAssignment 获取，最后通过 checkInTime）
                        LocalDate recordDate = null;
                        
                        // 优先使用 record 中的 dutyDate 字段
                        if (r.getDutyDate() != null) {
                            recordDate = r.getDutyDate();
                        } else if (r.getAssignmentId() != null) {
                            DutyAssignment assignment = assignmentMap.get(r.getAssignmentId());
                            if (assignment != null && assignment.getDutyDate() != null) {
                                recordDate = assignment.getDutyDate();
                            }
                        } else if (r.getCheckInTime() != null) {
                            recordDate = r.getCheckInTime().toLocalDate();
                        }
                        
                        if (recordDate == null) {
                            return false;
                        }
                        
                        // 检查是否在目标年份范围内，且不超过今天
                        return recordDate.getYear() == targetYear &&
                               !recordDate.isAfter(today);
                    })
                    .collect(Collectors.groupingBy(DutyRecord::getEmployeeId));

            // 为每个员工构建该年的汇总统计
            for (SysEmployee employee : employees) {
                Map<String, Object> stat = new HashMap<>();
                Long employeeId = employee.getId();

                // 基础信息
                stat.put("employeeId", employeeId);
                stat.put("employeeName", employee.getEmployeeName());
                stat.put("year", targetYear);
                stat.put("month", null); // 年份查询时month为null

                // 获取该员工整个年份的排班列表（用于计算计划工时）
                List<DutyAssignment> yearAssignments = yearAssignmentsByEmployee.getOrDefault(employeeId, new ArrayList<>());
                
                // 计算计划工时：年度所有已排班的时长总和
                BigDecimal plannedHours = BigDecimal.ZERO;
                for (DutyAssignment assignment : yearAssignments) {
                    DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                    if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                        plannedHours = plannedHours.add(shiftConfig.getDurationHours());
                    }
                }
                stat.put("plannedHours", plannedHours);

                // 获取该员工当前时间之前的排班列表（用于计算实际工时）
                List<DutyAssignment> actualAssignments = actualAssignmentsByEmployee.getOrDefault(employeeId, new ArrayList<>());
                
                // 计算实际工时：当前时间之前的排班时长总和 + 审批通过的加班工时
                BigDecimal actualHours = BigDecimal.ZERO;
                // 1. 当前时间之前的排班时长总和
                for (DutyAssignment assignment : actualAssignments) {
                    DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                    if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                        actualHours = actualHours.add(shiftConfig.getDurationHours());
                    }
                }
                // 2. 加上审批通过的加班工时
                List<DutyRecord> approvedOvertime = approvedOvertimeByEmployee.getOrDefault(employeeId, new ArrayList<>());
                for (DutyRecord record : approvedOvertime) {
                    if (record.getOvertimeHours() != null) {
                        actualHours = actualHours.add(record.getOvertimeHours());
                    }
                }
                stat.put("actualHours", actualHours);
                
                // 计算实际天数：实际工时转换（实际工时 / 8）
                BigDecimal actualDays = actualHours.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
                stat.put("actualDays", actualDays);

                // 计算加班工时：审批通过的加班时长 + 截至当前排班是加班班次的加班时长
                BigDecimal overtimeHours = BigDecimal.ZERO;
                // 1. 审批通过的加班记录时长
                for (DutyRecord record : approvedOvertime) {
                    if (record.getOvertimeHours() != null) {
                        overtimeHours = overtimeHours.add(record.getOvertimeHours());
                    }
                }
                // 2. 截至当前正常排班是加班班次的加班时长
                for (DutyAssignment assignment : actualAssignments) {
                    DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                    if (shiftConfig != null &&
                        shiftConfig.getIsOvertimeShift() != null &&
                        shiftConfig.getIsOvertimeShift() == 1 &&
                        shiftConfig.getOvertimeHours() != null) {
                        overtimeHours = overtimeHours.add(shiftConfig.getOvertimeHours());
                    }
                }
                stat.put("overtimeHours", overtimeHours);
                
                // 计算已调休工时：审批通过的调休请假时长
                BigDecimal usedCompensatoryHours = BigDecimal.ZERO;
                List<LeaveRequest> leaveRequests = leaveRequestService.list();
                for (LeaveRequest request : leaveRequests) {
                    if (request.getEmployeeId().equals(employeeId) && 
                        request.getApprovalStatus() != null && 
                        "approved".equals(request.getApprovalStatus()) &&
                        request.getLeaveType() != null && 
                        request.getLeaveType() == 4 && // 调休类型
                        request.getTotalHours() != null) {
                        // 检查请假时间是否在目标年份
                        LocalDate leaveDate = request.getStartDate();
                        if (leaveDate != null && leaveDate.getYear() == targetYear) {
                            usedCompensatoryHours = usedCompensatoryHours.add(request.getTotalHours());
                        }
                    }
                }
                stat.put("usedCompensatoryHours", usedCompensatoryHours);
                
                // 计算可调休工时：审批通过的加班时长 - 审批通过的调休请假时长
                BigDecimal compensatoryHours = BigDecimal.ZERO;
                compensatoryHours = overtimeHours.subtract(usedCompensatoryHours);
                
                // 确保可调休工时不小于0
                if (compensatoryHours.compareTo(BigDecimal.ZERO) < 0) {
                    compensatoryHours = BigDecimal.ZERO;
                }
                
                stat.put("compensatoryHours", compensatoryHours);

                employeeStats.add(stat);
            }
        } 
        // 按月份查询：每个员工每个月一条记录
        else {
            // 对每个目标月份进行统计
            for (Integer targetMonth : targetMonths) {
                // 按员工分组统计排班（整个月份的排班，用于计算计划工时）
                Map<Long, List<DutyAssignment>> monthAssignmentsByEmployee = assignments.stream()
                        .filter(a -> {
                            LocalDate assignmentDate = a.getDutyDate();
                            return assignmentDate != null && 
                                   assignmentDate.getYear() == targetYear && 
                                   assignmentDate.getMonthValue() == targetMonth &&
                                   a.getStatus() != null && a.getStatus() == 1;
                        })
                        .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

                // 按员工分组统计排班（当前时间之前的排班，用于计算实际工时）
                Map<Long, List<DutyAssignment>> actualAssignmentsByEmployee = assignments.stream()
                        .filter(a -> {
                            LocalDate assignmentDate = a.getDutyDate();
                            return assignmentDate != null && 
                                   assignmentDate.getYear() == targetYear && 
                                   assignmentDate.getMonthValue() == targetMonth &&
                                   !assignmentDate.isAfter(today) &&
                                   a.getStatus() != null && a.getStatus() == 1;
                        })
                        .collect(Collectors.groupingBy(DutyAssignment::getEmployeeId));

                // 按员工分组统计审批通过的加班记录（用于计算实际工时和可调休工时）
                Map<Long, List<DutyRecord>> approvedOvertimeByEmployee = records.stream()
                        .filter(r -> {
                            // 检查审批状态（支持中英文两种格式）
                            boolean isApproved = r.getApprovalStatus() != null && 
                                                ("approved".equals(r.getApprovalStatus()) || 
                                                 "已批准".equals(r.getApprovalStatus()));
                            if (!isApproved) {
                                return false;
                            }
                            
                            // 获取日期（优先通过 dutyDate，其次通过 assignmentId 从 dutyAssignment 获取，最后通过 checkInTime）
                            LocalDate recordDate = null;
                            
                            // 优先使用 record 中的 dutyDate 字段
                            if (r.getDutyDate() != null) {
                                recordDate = r.getDutyDate();
                            } else if (r.getAssignmentId() != null) {
                                DutyAssignment assignment = assignmentMap.get(r.getAssignmentId());
                                if (assignment != null && assignment.getDutyDate() != null) {
                                    recordDate = assignment.getDutyDate();
                                }
                            } else if (r.getCheckInTime() != null) {
                                recordDate = r.getCheckInTime().toLocalDate();
                            }
                            
                            if (recordDate == null) {
                                return false;
                            }
                            
                            // 检查是否在目标年月范围内，且不超过今天
                            return recordDate.getYear() == targetYear && 
                                   recordDate.getMonthValue() == targetMonth &&
                                   !recordDate.isAfter(today);
                        })
                        .collect(Collectors.groupingBy(DutyRecord::getEmployeeId));

                // 为每个员工构建该月的统计
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
                    
                    // 计算计划工时：月度所有已排班的时长总和
                    BigDecimal plannedHours = BigDecimal.ZERO;
                    for (DutyAssignment assignment : monthAssignments) {
                        DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                        if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                            plannedHours = plannedHours.add(shiftConfig.getDurationHours());
                        }
                    }
                    stat.put("plannedHours", plannedHours);

                    // 获取该员工当前时间之前的排班列表（用于计算实际工时）
                    List<DutyAssignment> actualAssignments = actualAssignmentsByEmployee.getOrDefault(employeeId, new ArrayList<>());
                    
                    // 计算实际工时：当前时间之前的排班时长总和 + 审批通过的加班工时
                    BigDecimal actualHours = BigDecimal.ZERO;
                    // 1. 当前时间之前的排班时长总和
                    for (DutyAssignment assignment : actualAssignments) {
                        DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                        if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                            actualHours = actualHours.add(shiftConfig.getDurationHours());
                        }
                    }
                    // 2. 加上审批通过的加班工时
                    List<DutyRecord> approvedOvertime = approvedOvertimeByEmployee.getOrDefault(employeeId, new ArrayList<>());
                    for (DutyRecord record : approvedOvertime) {
                        if (record.getOvertimeHours() != null) {
                            actualHours = actualHours.add(record.getOvertimeHours());
                        }
                    }
                    stat.put("actualHours", actualHours);
                    
                    // 计算实际天数：实际工时转换（实际工时 / 8）
                    BigDecimal actualDays = actualHours.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
                    stat.put("actualDays", actualDays);

                    // 计算加班工时：审批通过的加班时长 + 截至当前排班是加班班次的加班时长
                    BigDecimal overtimeHours = BigDecimal.ZERO;
                    // 1. 审批通过的加班记录时长
                    for (DutyRecord record : approvedOvertime) {
                        if (record.getOvertimeHours() != null) {
                            overtimeHours = overtimeHours.add(record.getOvertimeHours());
                        }
                    }
                    // 2. 截至当前正常排班是加班班次的加班时长（按排班日期类型选用）
                    for (DutyAssignment assignment : actualAssignments) {
                        DutyShiftConfig shiftConfig = getShiftConfig(assignment, shiftConfigMap);
                        BigDecimal hours = pickOvertimeHoursByDate(shiftConfig, assignment.getDutyDate());
                        if (hours != null) {
                            overtimeHours = overtimeHours.add(hours);
                        }
                    }
                    stat.put("overtimeHours", overtimeHours);
                    
                    // 计算已调休工时：审批通过的调休请假时长
                    BigDecimal usedCompensatoryHours = BigDecimal.ZERO;
                    List<LeaveRequest> leaveRequests = leaveRequestService.list();
                    for (LeaveRequest request : leaveRequests) {
                        if (request.getEmployeeId().equals(employeeId) && 
                            request.getApprovalStatus() != null && 
                            "approved".equals(request.getApprovalStatus()) &&
                            request.getLeaveType() != null && 
                            request.getLeaveType() == 4 && // 调休类型
                            request.getTotalHours() != null) {
                            usedCompensatoryHours = usedCompensatoryHours.add(request.getTotalHours());
                        }
                    }
                    stat.put("usedCompensatoryHours", usedCompensatoryHours);
                    
                    // 计算可调休工时：审批通过的加班时长 - 审批通过的调休请假时长
                    BigDecimal compensatoryHours = BigDecimal.ZERO;
                    compensatoryHours = overtimeHours.subtract(usedCompensatoryHours);
                    
                    // 确保可调休工时不小于0
                    if (compensatoryHours.compareTo(BigDecimal.ZERO) < 0) {
                        compensatoryHours = BigDecimal.ZERO;
                    }
                    
                    stat.put("compensatoryHours", compensatoryHours);

                    employeeStats.add(stat);
                }
            }
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

    private DutyShiftConfig getShiftConfig(DutyAssignment assignment, Map<Long, DutyShiftConfig> shiftConfigMap) {
        if (assignment.getShiftConfigId() != null) {
            return shiftConfigMap.get(assignment.getShiftConfigId());
        }
        if (assignment.getDutyShift() != null) {
            return shiftConfigMap.get(assignment.getDutyShift().longValue());
        }
        return null;
    }
}
