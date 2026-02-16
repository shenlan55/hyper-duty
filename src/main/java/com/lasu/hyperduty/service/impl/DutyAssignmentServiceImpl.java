package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.controller.DutyAssignmentController;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.entity.LeaveSubstitute;
import com.lasu.hyperduty.mapper.DutyAssignmentMapper;
import com.lasu.hyperduty.mapper.LeaveRequestMapper;
import com.lasu.hyperduty.mapper.LeaveSubstituteMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DutyAssignmentServiceImpl extends ServiceImpl<DutyAssignmentMapper, DutyAssignment> implements DutyAssignmentService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;
    
    @Autowired
    private LeaveSubstituteMapper leaveSubstituteMapper;

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

    @Override
    public int batchSchedule(DutyAssignmentController.BatchScheduleRequest request) {
        List<DutyAssignment> assignments = new ArrayList<>();
        int rotationIndex = 0; // 维护全局的轮换索引
        Set<Long> lastAssignedEmployees = new HashSet<>(); // 记录上一次排班的员工
        
        // 遍历过滤后的日期列表
        for (String dateStr : request.getFilteredDates()) {
            LocalDate date = LocalDate.parse(dateStr);
            
            // 1. 先正常排班
            List<Long> selectedEmployeeIds = new ArrayList<>();
            
            if (request.getScheduleType() == 1) {
                // 轮换排班模式
                Set<Long> usedEmployees = new HashSet<>();
                int needSelectCount = request.getShiftEmployeeCount();
                
                // 从轮换索引开始，按原始员工列表顺序选择员工
                for (int i = 0; i < request.getEmployeeIds().size() * 2 && selectedEmployeeIds.size() < needSelectCount; i++) {
                    int currentIndex = (rotationIndex + i) % request.getEmployeeIds().size();
                    Long currentEmployeeId = request.getEmployeeIds().get(currentIndex);
                    
                    // 检查该员工是否不是上一次排班的员工
                    if (!lastAssignedEmployees.contains(currentEmployeeId) && 
                        !usedEmployees.contains(currentEmployeeId)) {
                        selectedEmployeeIds.add(currentEmployeeId);
                        usedEmployees.add(currentEmployeeId);
                    }
                }
                
                // 更新全局轮换索引和上一次排班的员工
                if (!selectedEmployeeIds.isEmpty()) {
                    // 找到最后一个选中的员工在原始员工列表中的位置
                    int lastSelectedIndex = -1;
                    for (int i = 0; i < request.getEmployeeIds().size(); i++) {
                        if (selectedEmployeeIds.contains(request.getEmployeeIds().get(i))) {
                            lastSelectedIndex = i;
                        }
                    }
                    
                    // 更新轮换索引到最后一个选中员工的下一个位置
                    if (lastSelectedIndex != -1) {
                        rotationIndex = (lastSelectedIndex + 1) % request.getEmployeeIds().size();
                    } else {
                        // 如果都没找到，简单递增轮换索引
                        rotationIndex = (rotationIndex + 1) % request.getEmployeeIds().size();
                    }
                    
                    // 更新上一次排班的员工
                    lastAssignedEmployees.clear();
                    lastAssignedEmployees.addAll(selectedEmployeeIds);
                }
            } else {
                // 固定排班模式
                // 使用员工列表的所有员工
                int needSelectCount = request.getEmployeeIds().size();
                // 直接从员工列表中选择所有员工
                for (int i = 0; i < needSelectCount; i++) {
                    selectedEmployeeIds.add(request.getEmployeeIds().get(i));
                }
            }
            
            // 2. 为每个选中的员工创建排班记录
            for (Long employeeId : selectedEmployeeIds) {
                DutyAssignment assignment = new DutyAssignment();
                assignment.setScheduleId(request.getScheduleId());
                assignment.setDutyDate(date);
                assignment.setDutyShift(request.getDutyShift());
                assignment.setEmployeeId(employeeId);
                assignment.setStatus(1);
                assignment.setRemark(request.getRemark());
                assignments.add(assignment);
            }
        }
        
        // 3. 检查并替换请假人员为顶岗人员
        for (DutyAssignment assignment : assignments) {
            Long employeeId = assignment.getEmployeeId();
            LocalDate dutyDate = assignment.getDutyDate();
            Integer dutyShift = assignment.getDutyShift();
            
            // 检查该员工是否在当天请假
            if (isEmployeeOnLeave(employeeId, dutyDate.toString(), assignment.getScheduleId(), dutyShift)) {
                // 查找对应的顶岗人员
                Long substituteEmployeeId = findSubstituteEmployee(employeeId, dutyDate, dutyShift.longValue());
                if (substituteEmployeeId != null) {
                    // 替换为顶岗人员
                    System.out.println("替换请假人员 " + employeeId + " 为顶岗人员 " + substituteEmployeeId + 
                            " (日期: " + dutyDate + ", 班次: " + dutyShift + ")");
                    assignment.setEmployeeId(substituteEmployeeId);
                }
            }
        }
        
        // 4. 保存排班记录到数据库
        if (!assignments.isEmpty()) {
            saveBatch(assignments);
        }
        
        return assignments.size();
    }
    
    /**
     * 查找请假员工的顶岗人员
     */
    private Long findSubstituteEmployee(Long originalEmployeeId, LocalDate dutyDate, Long shiftConfigId) {
        try {
            // 1. 先查询请假记录
            LambdaQueryWrapper<LeaveRequest> leaveWrapper = new LambdaQueryWrapper<>();
            leaveWrapper.eq(LeaveRequest::getEmployeeId, originalEmployeeId)
                    .eq(LeaveRequest::getApprovalStatus, "approved")
                    .le(LeaveRequest::getStartDate, dutyDate)
                    .ge(LeaveRequest::getEndDate, dutyDate);
            LeaveRequest leaveRequest = leaveRequestMapper.selectOne(leaveWrapper);
            
            if (leaveRequest != null) {
                // 2. 根据请假记录查询顶岗信息
                LambdaQueryWrapper<LeaveSubstitute> substituteWrapper = new LambdaQueryWrapper<>();
                substituteWrapper.eq(LeaveSubstitute::getLeaveRequestId, leaveRequest.getId())
                        .eq(LeaveSubstitute::getDutyDate, dutyDate)
                        .eq(LeaveSubstitute::getShiftConfigId, shiftConfigId)
                        .eq(LeaveSubstitute::getStatus, 1);
                LeaveSubstitute substitute = leaveSubstituteMapper.selectOne(substituteWrapper);
                
                if (substitute != null) {
                    return substitute.getSubstituteEmployeeId();
                }
            }
        } catch (Exception e) {
            // 查找顶岗人员失败，记录错误但继续排班
            System.err.println("查找顶岗人员失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 检查员工在指定日期、值班表和班次是否请假
     */
    private boolean isEmployeeOnLeave(Long employeeId, String dateStr, Long scheduleId, Integer dutyShift) {
        LocalDate date = LocalDate.parse(dateStr);
        
        // 查询员工在指定日期的请假记录
        LambdaQueryWrapper<LeaveRequest> leaveWrapper = new LambdaQueryWrapper<>();
        leaveWrapper.eq(LeaveRequest::getEmployeeId, employeeId)
                .eq(LeaveRequest::getApprovalStatus, "approved")
                .le(LeaveRequest::getStartDate, date)
                .ge(LeaveRequest::getEndDate, date);
        LeaveRequest leaveRequest = leaveRequestMapper.selectOne(leaveWrapper);
        
        if (leaveRequest == null) {
            return false;
        }
        
        // 查询该请假记录的顶岗信息，检查是否包含当前班次
        LambdaQueryWrapper<LeaveSubstitute> substituteWrapper = new LambdaQueryWrapper<>();
        substituteWrapper.eq(LeaveSubstitute::getLeaveRequestId, leaveRequest.getId())
                .eq(LeaveSubstitute::getDutyDate, date)
                .eq(LeaveSubstitute::getShiftConfigId, dutyShift.longValue())
                .eq(LeaveSubstitute::getStatus, 1);
        LeaveSubstitute substitute = leaveSubstituteMapper.selectOne(substituteWrapper);
        
        return substitute != null;
    }
}