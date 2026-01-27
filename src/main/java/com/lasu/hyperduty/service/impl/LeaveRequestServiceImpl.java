package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.LeaveRequestMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyScheduleEmployeeService;
import com.lasu.hyperduty.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyScheduleEmployeeService dutyScheduleEmployeeService;

    @Override
    public String generateRequestNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "LV" + timestamp + random;
    }

    @Override
    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        leaveRequest.setRequestNo(generateRequestNo());
        leaveRequest.setApprovalStatus("pending");
        leaveRequest.setApprovalLevel(1);
        leaveRequest.setSubstituteStatus("pending");
        leaveRequest.setCreateTime(LocalDateTime.now());
        leaveRequest.setUpdateTime(LocalDateTime.now());
        
        if (leaveRequest.getScheduleId() != null) {
            List<com.lasu.hyperduty.entity.DutyScheduleEmployee> leaders = dutyScheduleEmployeeService.lambdaQuery()
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getScheduleId, leaveRequest.getScheduleId())
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getIsLeader, 1)
                    .list();
            if (!leaders.isEmpty()) {
                // 如果有多个值班长，选择第一个作为审批人
                leaveRequest.setCurrentApproverId(leaders.get(0).getEmployeeId());
            }
        }
        
        save(leaveRequest);
        return leaveRequest;
    }

    @Override
    public boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion, String scheduleAction, String scheduleType, String scheduleDateRange) {
        LeaveRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        // 检查请假申请是否处于待审批状态，如果不是，则表示已经被审批过了
        if (!"pending".equals(request.getApprovalStatus())) {
            return false;
        }

        if ("approved".equals(approvalStatus) && "check".equals(scheduleAction)) {
            Map<String, Object> scheduleCheck = checkEmployeeSchedule(request.getEmployeeId(), request.getStartDate().toString(), request.getEndDate().toString());
            if ((Boolean) scheduleCheck.get("hasSchedule")) {
                return false;
            }
        }

        request.setApprovalStatus(approvalStatus);
        request.setCurrentApproverId(approverId);
        request.setUpdateTime(LocalDateTime.now());

        if ("rejected".equals(approvalStatus)) {
            request.setRejectReason(opinion);
        }

        if ("approved".equals(approvalStatus) && "auto".equals(scheduleAction)) {
            request.setScheduleCompleted(1);
            request.setScheduleCompletedTime(LocalDateTime.now());
            request.setScheduleCompletedBy(approverId);
        }

        return updateById(request);
    }

     @Override
    public List<Object> getApprovalRecords(Long requestId) {
        return lambdaQuery()
                .eq(LeaveRequest::getId, requestId)
                .isNotNull(LeaveRequest::getCurrentApproverId)
                .isNotNull(LeaveRequest::getApprovalStatus)
                .orderByAsc(LeaveRequest::getUpdateTime)
                .list()
                .stream()
                .map(req -> {
                    java.util.Map<String, Object> record = new java.util.HashMap<>();
                    record.put("approverId", req.getCurrentApproverId());
                    record.put("approverName", getApproverName(req.getCurrentApproverId()));
                    record.put("approvalStatus", req.getApprovalStatus());
                    record.put("approvalOpinion", req.getApprovalOpinion());
                    record.put("rejectReason", req.getRejectReason());
                    record.put("createTime", req.getUpdateTime());
                    return record;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> checkEmployeeSchedule(Long employeeId, String startDate, String endDate) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);

        List<com.lasu.hyperduty.entity.DutyAssignment> assignments = dutyAssignmentService.lambdaQuery()
                .eq(com.lasu.hyperduty.entity.DutyAssignment::getEmployeeId, employeeId)
                .ge(com.lasu.hyperduty.entity.DutyAssignment::getDutyDate, start)
                .le(com.lasu.hyperduty.entity.DutyAssignment::getDutyDate, end)
                .list();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("hasSchedule", !assignments.isEmpty());
        result.put("assignments", assignments);
        return result;
    }

    private String getApproverName(Long approverId) {
        if (approverId == null) {
            return "系统";
        }
        return approverId.toString();
    }

    @Override
    public List<LeaveRequest> getPendingApprovals(Long approverId) {
        // 查询当前审批人负责审批的请假申请，或者是发起人自己的请假申请
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("approval_status", "pending");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        queryWrapper.orderByDesc("create_time");
        List<LeaveRequest> result = baseMapper.selectList(queryWrapper);
        System.out.println("Pending approvals count: " + result.size());
        for (LeaveRequest request : result) {
            System.out.println("Request ID: " + request.getId() + ", Status: " + request.getApprovalStatus());
        }
        return result;
    }

    @Override
    public List<LeaveRequest> getMyLeaveRequests(Long employeeId) {
        return lambdaQuery()
                .eq(LeaveRequest::getEmployeeId, employeeId)
                .orderByDesc(LeaveRequest::getCreateTime)
                .list();
    }

    @Override
    public List<LeaveRequest> getPendingApprovalsByScheduleId(Long scheduleId) {
        return lambdaQuery()
                .eq(LeaveRequest::getScheduleId, scheduleId)
                .eq(LeaveRequest::getApprovalStatus, "pending")
                .orderByDesc(LeaveRequest::getCreateTime)
                .list();
    }

    @Override
    public List<LeaveRequest> getApprovedApprovals(Long approverId) {
        // 查询当前审批人负责审批的请假申请，或者是发起人自己的请假申请
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("approval_status", "approved", "rejected");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        queryWrapper.orderByDesc("update_time");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<LeaveRequest> getApprovedApprovalsByScheduleId(Long scheduleId) {
        return lambdaQuery()
                .eq(LeaveRequest::getScheduleId, scheduleId)
                .in(LeaveRequest::getApprovalStatus, "approved", "rejected")
                .orderByDesc(LeaveRequest::getUpdateTime)
                .list();
    }

    @Override
    public boolean confirmScheduleCompletion(Long requestId, Long approverId) {
        LeaveRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        request.setScheduleCompleted(1);
        request.setScheduleCompletedTime(LocalDateTime.now());
        request.setScheduleCompletedBy(approverId);
        request.setApprovalStatus("approved");
        request.setUpdateTime(LocalDateTime.now());

        return updateById(request);
    }

    @Override
    public Map<Long, Map<String, Map<String, List<Long>>>> getEmployeeLeaveInfo(List<Long> employeeIds, String startDate, String endDate) {
        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = new java.util.HashMap<>();
        
        if (employeeIds == null || employeeIds.isEmpty()) {
            return leaveInfo;
        }
        
        // 查询员工在指定日期范围内的请假记录
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("employee_id", employeeIds);
        queryWrapper.ge("start_date", startDate);
        queryWrapper.le("end_date", endDate);
        queryWrapper.in("approval_status", "approved", "pending"); // 只考虑已通过和待审批的请假
        
        List<LeaveRequest> leaveRequests = this.list(queryWrapper);
        
        // 处理请假记录，提取请假日期、值班表ID和班次配置ID列表
        for (LeaveRequest request : leaveRequests) {
            Long employeeId = request.getEmployeeId();
            Long scheduleId = request.getScheduleId();
            String shiftConfigIdsStr = request.getShiftConfigIds();
            
            // 解析班次配置ID列表
            List<Long> shiftConfigIds = new java.util.ArrayList<>();
            if (shiftConfigIdsStr != null && !shiftConfigIdsStr.isEmpty()) {
                String[] ids = shiftConfigIdsStr.split(",");
                for (String idStr : ids) {
                    try {
                        shiftConfigIds.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的ID
                        System.err.println("班次配置ID格式错误: " + idStr);
                    }
                }
            }
            
            // 如果没有班次配置ID，尝试使用单个班次配置ID
            if (shiftConfigIds.isEmpty() && request.getShiftConfigId() != null) {
                shiftConfigIds.add(request.getShiftConfigId());
            }
            
            java.time.LocalDate start = java.time.LocalDate.parse(request.getStartDate().toString());
            java.time.LocalDate end = java.time.LocalDate.parse(request.getEndDate().toString());
            
            // 遍历请假期间的所有日期
            java.time.LocalDate current = start;
            while (!current.isAfter(end)) {
                String dateStr = current.toString();
                
                // 初始化嵌套映射结构
                Map<String, Map<String, List<Long>>> employeeLeaveMap = leaveInfo.computeIfAbsent(employeeId, k -> new java.util.HashMap<>());
                Map<String, List<Long>> dateLeaveMap = employeeLeaveMap.computeIfAbsent(dateStr, k -> new java.util.HashMap<>());
                
                // 存储值班表ID和班次配置ID列表
                String scheduleIdStr = scheduleId != null ? scheduleId.toString() : "null";
                dateLeaveMap.put(scheduleIdStr, shiftConfigIds);
                
                current = current.plusDays(1);
            }
        }
        
        return leaveInfo;
    }

    @Override
    public IPage<LeaveRequest> getMyLeaveRequestsPage(Long employeeId, Integer page, Integer size, Integer leaveType, String approvalStatus, String startDate, String endDate) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：员工ID
        queryWrapper.eq("employee_id", employeeId);
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：审批状态
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            queryWrapper.eq("approval_status", approvalStatus);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", startDate);
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", endDate);
        }
        
        // 排序：创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        return this.page(iPage, queryWrapper);
    }

    @Override
    public IPage<LeaveRequest> getPendingApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String startDate, String endDate) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：审批状态为待审批
        queryWrapper.eq("approval_status", "pending");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        
        // 可选条件：值班表ID
        if (scheduleId != null) {
            queryWrapper.eq("schedule_id", scheduleId);
        }
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", startDate);
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", endDate);
        }
        
        // 排序：创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        return this.page(iPage, queryWrapper);
    }

    @Override
    public IPage<LeaveRequest> getApprovedApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String approvalStatus, String startDate, String endDate) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：审批状态为已审批或已拒绝
        queryWrapper.in("approval_status", "approved", "rejected");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        
        // 可选条件：值班表ID
        if (scheduleId != null) {
            queryWrapper.eq("schedule_id", scheduleId);
        }
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：审批状态
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            queryWrapper.eq("approval_status", approvalStatus);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", startDate);
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", endDate);
        }
        
        // 排序：更新时间倒序
        queryWrapper.orderByDesc("update_time");
        
        return this.page(iPage, queryWrapper);
    }
}
