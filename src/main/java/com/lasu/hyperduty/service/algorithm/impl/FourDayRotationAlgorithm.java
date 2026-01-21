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
 * 四天一轮排班算法
 * 排班规则：第一天白班，第二天夜班，第三天休息，第四天休息，周期性轮换
 */
@Component
public class FourDayRotationAlgorithm implements ScheduleAlgorithm {
    
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
            // 计算当前日期在周期中的位置（0-3代表四天一轮）
            long daysSinceStart = startDate.until(currentDate, java.time.temporal.ChronoUnit.DAYS);
            int cyclePosition = (int) (daysSinceStart % 4);
            
            // 根据周期位置分配班次
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                List<SysEmployee> group = groups.get(groupIndex);
                int groupCyclePosition = (cyclePosition + groupIndex) % 4;
                
                // 只有白班和夜班需要排班
                if (groupCyclePosition == 0) {
                    // 白班 (值为1)
                    for (SysEmployee employee : group) {
                        DutyAssignment assignment = new DutyAssignment();
                        assignment.setScheduleId(schedule.getId());
                        assignment.setDutyDate(currentDate);
                        assignment.setDutyShift(1); // 白班
                        assignment.setEmployeeId(employee.getId());
                        assignment.setStatus(1);
                        assignments.add(assignment);
                    }
                } else if (groupCyclePosition == 1) {
                    // 夜班 (值为3)
                    for (SysEmployee employee : group) {
                        DutyAssignment assignment = new DutyAssignment();
                        assignment.setScheduleId(schedule.getId());
                        assignment.setDutyDate(currentDate);
                        assignment.setDutyShift(3); // 夜班
                        assignment.setEmployeeId(employee.getId());
                        assignment.setStatus(1);
                        assignments.add(assignment);
                    }
                }
                // 第三天和第四天休息，不生成排班
            }
            
            // 日期加一天
            currentDate = currentDate.plusDays(1);
        }
        
        return assignments;
    }
    
    @Override
    public String getAlgorithmName() {
        return "四天一轮排班算法";
    }
    
    @Override
    public String getAlgorithmDescription() {
        return "第一天白班，第二天夜班，第三天休息，第四天休息，周期性轮换";
    }
    
    @Override
    public List<AlgorithmParam> getSupportedParams() {
        List<AlgorithmParam> params = new ArrayList<>();
        params.add(new AlgorithmParam("每组人数", "groupSize", "Integer", true, "每组参与轮班的人数", 2));
        return params;
    }
}