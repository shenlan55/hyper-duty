package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.mapper.DutyAssignmentMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.stereotype.Service;

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
}