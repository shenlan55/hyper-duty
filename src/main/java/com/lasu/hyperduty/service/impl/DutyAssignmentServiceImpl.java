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
        
        // 获取所有请假人员的顶岗信息
        Map<String, Long> substituteInfo = fetchAllSubstituteInfo(request.getEmployeeIds(), request.getStartDate(), request.getEndDate());
        
        // 遍历过滤后的日期列表
        for (String dateStr : request.getFilteredDates()) {
            LocalDate date = LocalDate.parse(dateStr);
            
            // 首先处理请假人员的顶岗替换
            Map<Long, Long> substituteMap = new HashMap<>();
            
            // 检查每个员工是否请假，如果请假且有顶岗人员，则记录顶岗关系
            for (Long employeeId : request.getEmployeeIds()) {
                if (isEmployeeOnLeave(employeeId, dateStr, request.getScheduleId(), request.getDutyShift())) {
                    // 查找该员工在当天该班次的顶岗人员
                    String substituteKey = employeeId + "_" + dateStr + "_" + request.getDutyShift();
                    Long substituteEmployeeId = substituteInfo.get(substituteKey);
                    if (substituteEmployeeId != null) {
                        substituteMap.put(employeeId, substituteEmployeeId);
                    }
                }
            }
            
            // 过滤出当天没有请假的员工，加上顶岗人员
            List<Long> dayAvailableEmployees = new ArrayList<>();
            Set<Long> usedSubstitutes = new HashSet<>();
            
            // 首先添加顶岗人员
            for (Map.Entry<Long, Long> entry : substituteMap.entrySet()) {
                Long substituteEmployeeId = entry.getValue();
                if (!usedSubstitutes.contains(substituteEmployeeId)) {
                    dayAvailableEmployees.add(substituteEmployeeId);
                    usedSubstitutes.add(substituteEmployeeId);
                }
            }
            
            // 然后添加其他没有请假的员工
            for (Long employeeId : request.getEmployeeIds()) {
                if (!isEmployeeOnLeave(employeeId, dateStr, request.getScheduleId(), request.getDutyShift()) && 
                    !usedSubstitutes.contains(employeeId)) {
                    dayAvailableEmployees.add(employeeId);
                }
            }
            
            if (dayAvailableEmployees.isEmpty()) {
                // 当天没有可用员工，跳过
                continue;
            }
            
            if (request.getScheduleType() == 1) {
                // 轮换排班模式
                List<Long> selectedEmployeeIds = new ArrayList<>();
                Set<Long> usedEmployees = new HashSet<>();
                
                // 需要选择的人数
                int needSelectCount = request.getShiftEmployeeCount();
                
                // 从轮换索引开始，按原始员工列表顺序选择可用员工
                for (int i = 0; i < request.getEmployeeIds().size() * 2 && selectedEmployeeIds.size() < needSelectCount; i++) {
                    int currentIndex = (rotationIndex + i) % request.getEmployeeIds().size();
                    Long currentEmployeeId = request.getEmployeeIds().get(currentIndex);
                    
                    // 检查该员工是否在当天可用列表中、未被使用、且不是上一次排班的员工
                    if (dayAvailableEmployees.contains(currentEmployeeId) && 
                        !usedEmployees.contains(currentEmployeeId) && 
                        !lastAssignedEmployees.contains(currentEmployeeId)) {
                        selectedEmployeeIds.add(currentEmployeeId);
                        usedEmployees.add(currentEmployeeId);
                    }
                }
                
                // 如果还需要更多员工，从可用列表中补充（包括顶岗人员）
                if (selectedEmployeeIds.size() < needSelectCount) {
                    for (Long employeeId : dayAvailableEmployees) {
                        if (!usedEmployees.contains(employeeId) && selectedEmployeeIds.size() < needSelectCount) {
                            selectedEmployeeIds.add(employeeId);
                            usedEmployees.add(employeeId);
                        }
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
                    
                    // 为每个选中的员工创建排班记录
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
            } else {
                // 固定排班模式
                // 限制每个班次的人数
                int needSelectCount = request.getShiftEmployeeCount();
                List<Long> selectedEmployees = dayAvailableEmployees.subList(0, Math.min(needSelectCount, dayAvailableEmployees.size()));
                
                // 为每个选中的员工创建排班记录
                for (Long employeeId : selectedEmployees) {
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
        }
        
        // 保存排班记录到数据库
        if (!assignments.isEmpty()) {
            saveBatch(assignments);
        }
        
        return assignments.size();
    }
    
    /**
     * 获取所有请假人员的顶岗信息
     */
    private Map<String, Long> fetchAllSubstituteInfo(List<Long> employeeIds, String startDate, String endDate) {
        Map<String, Long> substituteMap = new HashMap<>();
        
        if (employeeIds == null || employeeIds.isEmpty()) {
            return substituteMap;
        }
        
        // 查询请假记录
        LambdaQueryWrapper<LeaveRequest> leaveWrapper = new LambdaQueryWrapper<>();
        leaveWrapper.in(LeaveRequest::getEmployeeId, employeeIds)
                .eq(LeaveRequest::getApprovalStatus, "已批准") // 已批准
                .le(LeaveRequest::getStartDate, endDate)
                .ge(LeaveRequest::getEndDate, startDate);
        List<LeaveRequest> leaveRequests = leaveRequestMapper.selectList(leaveWrapper);
        
        // 查询对应的顶岗信息
        for (LeaveRequest leaveRequest : leaveRequests) {
            LambdaQueryWrapper<LeaveSubstitute> substituteWrapper = new LambdaQueryWrapper<>();
            substituteWrapper.eq(LeaveSubstitute::getLeaveRequestId, leaveRequest.getId());
            List<LeaveSubstitute> substitutes = leaveSubstituteMapper.selectList(substituteWrapper);
            
            for (LeaveSubstitute substitute : substitutes) {
                String key = leaveRequest.getEmployeeId() + "_" + substitute.getDutyDate() + "_" + substitute.getShiftConfigId();
                substituteMap.put(key, substitute.getSubstituteEmployeeId());
            }
        }
        
        return substituteMap;
    }
    
    /**
     * 检查员工在指定日期、值班表和班次是否请假
     */
    private boolean isEmployeeOnLeave(Long employeeId, String dateStr, Long scheduleId, Integer dutyShift) {
        LocalDate date = LocalDate.parse(dateStr);
        
        // 查询员工在指定日期的请假记录
        LambdaQueryWrapper<LeaveRequest> leaveWrapper = new LambdaQueryWrapper<>();
        leaveWrapper.eq(LeaveRequest::getEmployeeId, employeeId)
                .eq(LeaveRequest::getApprovalStatus, "已批准") // 已批准
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
                .eq(LeaveSubstitute::getShiftConfigId, dutyShift.longValue());
        LeaveSubstitute substitute = leaveSubstituteMapper.selectOne(substituteWrapper);
        
        return substitute != null;
    }
}