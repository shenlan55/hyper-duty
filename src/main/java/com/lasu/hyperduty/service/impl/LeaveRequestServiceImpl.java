package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.LeaveRequestMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyScheduleEmployeeService;
import com.lasu.hyperduty.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            com.lasu.hyperduty.entity.DutyScheduleEmployee leader = dutyScheduleEmployeeService.lambdaQuery()
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getScheduleId, leaveRequest.getScheduleId())
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getIsLeader, 1)
                    .one();
            if (leader != null) {
                leaveRequest.setCurrentApproverId(leader.getEmployeeId());
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
        // 直接查询所有待审批的记录，不限制值班表
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("approval_status", "pending");
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
        // 直接查询所有已审批的记录，不限制值班表
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("approval_status", "approved", "rejected");
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
}
