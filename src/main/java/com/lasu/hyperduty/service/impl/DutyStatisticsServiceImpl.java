package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyStatistics;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.DutyStatisticsMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.DutyStatisticsService;
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

    @Override
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        List<DutySchedule> schedules = dutyScheduleService.list();
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        List<DutyRecord> records = dutyRecordService.list();
        List<LeaveRequest> leaveRequests = leaveRequestService.list();

        statistics.put("totalSchedules", schedules.size());
        statistics.put("totalAssignments", assignments.size());
        statistics.put("totalRecords", records.size());

        BigDecimal totalHours = BigDecimal.ZERO;
        int totalDays = 0;
        
        for (DutyRecord record : records) {
            if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                long minutes = java.time.Duration.between(record.getCheckInTime(), record.getCheckOutTime()).toMinutes();
                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                totalHours = totalHours.add(hours);
                LocalDate checkInDate = record.getCheckInTime().toLocalDate();
                if (!records.stream().anyMatch(r -> r.getCheckInTime() != null && 
                        r.getCheckInTime().toLocalDate().equals(checkInDate) && 
                        r.getId() != record.getId())) {
                    totalDays++;
                }
            }
        }

        BigDecimal avgDailyHours = totalDays > 0 ? totalHours.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        statistics.put("avgDailyHours", avgDailyHours);

        Integer totalOvertimeHours = 0;
        for (DutyRecord record : records) {
            if (record.getOvertimeHours() != null) {
                totalOvertimeHours += record.getOvertimeHours();
            }
        }
        statistics.put("totalOvertimeHours", BigDecimal.valueOf(totalOvertimeHours));

        statistics.put("totalLeaveRequests", leaveRequests.size());

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getDeptStatistics(Long deptId) {
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        List<SysEmployee> employees = sysEmployeeService.list();
        List<SysDept> depts = sysDeptService.list();

        Map<Long, SysEmployee> employeeMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, e -> e));

        Map<Long, List<DutyAssignment>> assignmentsByDept = new HashMap<>();

        for (DutyAssignment assignment : assignments) {
            SysEmployee employee = employeeMap.get(assignment.getEmployeeId());
            if (employee != null && employee.getDeptId() != null) {
                assignmentsByDept.computeIfAbsent(employee.getDeptId(), k -> new ArrayList<>()).add(assignment);
            }
        }

        Map<Long, SysDept> deptMap = depts.stream()
                .collect(Collectors.toMap(SysDept::getId, d -> d));

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
        List<DutyAssignment> assignments = dutyAssignmentService.list();

        Map<Integer, Long> shiftCount = assignments.stream()
                .collect(Collectors.groupingBy(DutyAssignment::getDutyShift, Collectors.counting()));

        List<Map<String, Object>> distribution = new ArrayList<>();

        Map<Integer, String> shiftNames = Map.of(
                1, "早班",
                2, "中班",
                3, "晚班",
                4, "全天",
                5, "夜班"
        );

        for (Map.Entry<Integer, String> entry : shiftNames.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("shiftName", entry.getValue());
            item.put("count", shiftCount.getOrDefault(entry.getKey(), 0L).longValue());
            distribution.add(item);
        }

        return distribution;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend() {
        List<DutyRecord> records = dutyRecordService.list();

        Map<String, BigDecimal> monthlyHours = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyOvertime = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.getMonthValue() + "月";

            BigDecimal hours = BigDecimal.ZERO;
            Integer overtime = 0;

            for (DutyRecord record : records) {
                if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                    LocalDate recordDate = record.getCheckInTime().toLocalDate();
                    if (recordDate.getYear() == month.getYear() && recordDate.getMonthValue() == month.getMonthValue()) {
                        long minutes = java.time.Duration.between(record.getCheckInTime(), record.getCheckOutTime()).toMinutes();
                        BigDecimal recordHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                        hours = hours.add(recordHours);
                    }
                }
                
                if (record.getOvertimeHours() != null && record.getCheckInTime() != null) {
                    LocalDate recordDate = record.getCheckInTime().toLocalDate();
                    if (recordDate.getYear() == month.getYear() && recordDate.getMonthValue() == month.getMonthValue()) {
                        overtime += record.getOvertimeHours();
                    }
                }
            }

            monthlyHours.put(monthKey, hours);
            monthlyOvertime.put(monthKey, BigDecimal.valueOf(overtime));
        }

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
}
