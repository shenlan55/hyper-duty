package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.mapper.DutyAssignmentMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DutyAssignmentServiceImpl extends ServiceImpl<DutyAssignmentMapper, DutyAssignment> implements DutyAssignmentService {

    @Override
    public void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate) {
        LambdaQueryWrapper<DutyAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyAssignment::getScheduleId, scheduleId)
                .ge(DutyAssignment::getDutyDate, startDate)
                .le(DutyAssignment::getDutyDate, endDate);
        remove(wrapper);
    }

    @Override
    public void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, java.time.LocalDate swapDate, Integer swapShift) {
        // 查找原值班人员的值班安排
        LambdaQueryWrapper<DutyAssignment> originalWrapper = new LambdaQueryWrapper<>();
        originalWrapper.eq(DutyAssignment::getEmployeeId, originalEmployeeId)
                .eq(DutyAssignment::getDutyDate, swapDate)
                .eq(DutyAssignment::getDutyShift, swapShift);
        DutyAssignment originalAssignment = getOne(originalWrapper);
        
        // 查找目标值班人员的值班安排
        LambdaQueryWrapper<DutyAssignment> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(DutyAssignment::getEmployeeId, targetEmployeeId)
                .eq(DutyAssignment::getDutyDate, swapDate)
                .eq(DutyAssignment::getDutyShift, swapShift);
        DutyAssignment targetAssignment = getOne(targetWrapper);
        
        // 如果两条记录都存在，交换员工ID
        if (originalAssignment != null && targetAssignment != null) {
            // 保存原始的员工ID
            Long tempEmployeeId = originalAssignment.getEmployeeId();
            
            // 交换员工ID
            originalAssignment.setEmployeeId(targetAssignment.getEmployeeId());
            targetAssignment.setEmployeeId(tempEmployeeId);
            
            // 保存更新后的记录
            updateById(originalAssignment);
            updateById(targetAssignment);
        }
    }

    @Override
    public List<String> getEmployeeDutyDates(Long scheduleId, Long employeeId) {
        // 查询指定值班表和员工的所有排班记录
        LambdaQueryWrapper<DutyAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyAssignment::getScheduleId, scheduleId)
                .eq(DutyAssignment::getEmployeeId, employeeId)
                .select(DutyAssignment::getDutyDate);
        
        // 提取日期并去重，转换为字符串格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return this.list(wrapper).stream()
                .map(assignment -> assignment.getDutyDate().format(formatter))
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<Integer> getEmployeeDutyShifts(Long scheduleId, Long employeeId, String date) {
        // 查询指定值班表、员工和日期的所有排班班次
        LambdaQueryWrapper<DutyAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyAssignment::getScheduleId, scheduleId)
                .eq(DutyAssignment::getEmployeeId, employeeId)
                .eq(DutyAssignment::getDutyDate, LocalDate.parse(date))
                .select(DutyAssignment::getDutyShift);
        
        // 提取班次并去重
        return this.list(wrapper).stream()
                .map(DutyAssignment::getDutyShift)
                .distinct()
                .sorted()
                .toList();
    }
}