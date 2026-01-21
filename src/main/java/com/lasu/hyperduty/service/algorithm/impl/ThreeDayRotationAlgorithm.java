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
 * 三天一轮排班算法
 * 排班规则：第一天白班+夜班，第二天休息，第三天休息，周期性轮换
 */
@Component
public class ThreeDayRotationAlgorithm implements ScheduleAlgorithm {
    
    @Override
    public List<DutyAssignment> generateSchedule(DutySchedule schedule, List<SysEmployee> employees, 
                                              LocalDate startDate, LocalDate endDate, Map<String, Object> configParams) {
        List<DutyAssignment> assignments = new ArrayList<>();
        
        // 获取配置参数
        Integer groupSize = (Integer) configParams.getOrDefault("groupSize", 2);
        
        // 检查员工数量是否足够
        if (employees.isEmpty()) {
            return assignments;
        }
        
        // 按组大小分组
        List<List<SysEmployee>> groups = new ArrayList<>();
        for (int i = 0; i < employees.size(); i += groupSize) {
            int endIndex = Math.min(i + groupSize, employees.size());
            groups.add(employees.subList(i, endIndex));
        }
        
        // 生成排班安排
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 计算当前日期在周期中的位置（0-2代表三天一轮）
            long daysSinceStart = startDate.until(currentDate, java.time.temporal.ChronoUnit.DAYS);
            int cyclePosition = (int) (daysSinceStart % 3);
            
            // 根据周期位置分配班次
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                List<SysEmployee> group = groups.get(groupIndex);
                int groupCyclePosition = (cyclePosition + groupIndex) % 3;
                
                // 只有第一天需要排班（白班+夜班）
                if (groupCyclePosition == 0) {
                    // 白班 (值为1)
                    for (SysEmployee employee : group) {
                        DutyAssignment dayShift = new DutyAssignment();
                        dayShift.setScheduleId(schedule.getId());
                        dayShift.setDutyDate(currentDate);
                        dayShift.setDutyShift(1); // 白班
                        dayShift.setEmployeeId(employee.getId());
                        dayShift.setStatus(1);
                        assignments.add(dayShift);
                    }
                    
                    // 夜班 (值为3)
                    for (SysEmployee employee : group) {
                        DutyAssignment nightShift = new DutyAssignment();
                        nightShift.setScheduleId(schedule.getId());
                        nightShift.setDutyDate(currentDate);
                        nightShift.setDutyShift(3); // 夜班
                        nightShift.setEmployeeId(employee.getId());
                        nightShift.setStatus(1);
                        assignments.add(nightShift);
                    }
                }
                // 第二天和第三天休息，不生成排班
            }
            
            // 日期加一天
            currentDate = currentDate.plusDays(1);
        }
        
        return assignments;
    }
    
    @Override
    public String getAlgorithmName() {
        return "三天一轮排班算法";
    }
    
    @Override
    public String getAlgorithmDescription() {
        return "第一天白班+夜班，第二天休息，第三天休息，周期性轮换";
    }
    
    @Override
    public List<AlgorithmParam> getSupportedParams() {
        List<AlgorithmParam> params = new ArrayList<>();
        params.add(new AlgorithmParam("每组人数", "groupSize", "Integer", true, "每组参与轮班的人数", 2));
        return params;
    }
}