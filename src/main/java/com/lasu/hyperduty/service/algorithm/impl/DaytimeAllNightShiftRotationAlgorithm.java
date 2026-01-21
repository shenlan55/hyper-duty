package com.lasu.hyperduty.service.algorithm.impl;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.algorithm.AlgorithmParam;
import com.lasu.hyperduty.service.algorithm.ScheduleAlgorithm;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 白班组内全员、夜班轮值排班算法
 * 排班规则：白天组内所有人都值班，晚上按指定人数轮值
 */
@Component
public class DaytimeAllNightShiftRotationAlgorithm implements ScheduleAlgorithm {

    @Override
    public List<DutyAssignment> generateSchedule(DutySchedule schedule, List<SysEmployee> employees, 
                                              LocalDate startDate, LocalDate endDate, Map<String, Object> configParams) {
        List<DutyAssignment> assignments = new ArrayList<>();
        
        // 获取配置参数
        Integer nightShiftCount = (Integer) configParams.getOrDefault("nightShiftCount", 2);
        
        // 检查员工数量是否足够
        if (employees.isEmpty()) {
            return assignments;
        }
        
        // 生成排班安排
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 白班：所有人都值班（值为1）
            for (SysEmployee employee : employees) {
                DutyAssignment dayShift = new DutyAssignment();
                dayShift.setScheduleId(schedule.getId());
                dayShift.setDutyDate(currentDate);
                dayShift.setDutyShift(1); // 白班
                dayShift.setEmployeeId(employee.getId());
                dayShift.setStatus(1);
                assignments.add(dayShift);
            }
            
            // 夜班：按指定人数轮值（值为3）
            long daysSinceStart = startDate.until(currentDate, java.time.temporal.ChronoUnit.DAYS);
            int startIndex = (int) (daysSinceStart * nightShiftCount % employees.size());
            
            for (int i = 0; i < nightShiftCount; i++) {
                int employeeIndex = (startIndex + i) % employees.size();
                SysEmployee employee = employees.get(employeeIndex);
                
                DutyAssignment nightShift = new DutyAssignment();
                nightShift.setScheduleId(schedule.getId());
                nightShift.setDutyDate(currentDate);
                nightShift.setDutyShift(3); // 夜班
                nightShift.setEmployeeId(employee.getId());
                nightShift.setStatus(1);
                assignments.add(nightShift);
            }
            
            // 日期加一天
            currentDate = currentDate.plusDays(1);
        }
        
        return assignments;
    }

    @Override
    public String getAlgorithmName() {
        return "白班全员夜班轮值算法";
    }

    @Override
    public String getAlgorithmDescription() {
        return "白天组内所有人都值班，晚上按指定人数轮值";
    }

    @Override
    public List<AlgorithmParam> getSupportedParams() {
        List<AlgorithmParam> params = new ArrayList<>();
        params.add(new AlgorithmParam("夜班值班人数", "nightShiftCount", "Integer", true, "每晚值班的人数", 2));
        return params;
    }
}