package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.LeaveRequestMapper;
import com.lasu.hyperduty.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

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
        save(leaveRequest);
        return leaveRequest;
    }

    @Override
    public boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion) {
        LeaveRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        request.setApprovalStatus(approvalStatus);
        request.setCurrentApproverId(approverId);
        request.setUpdateTime(LocalDateTime.now());

        if ("rejected".equals(approvalStatus)) {
            request.setRejectReason(opinion);
        }

        return updateById(request);
    }

    @Override
    public List<LeaveRequest> getPendingApprovals(Long approverId) {
        return lambdaQuery()
                .eq(LeaveRequest::getApprovalStatus, "pending")
                .eq(LeaveRequest::getCurrentApproverId, approverId)
                .orderByDesc(LeaveRequest::getCreateTime)
                .list();
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
}
