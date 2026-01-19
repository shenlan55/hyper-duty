package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.LeaveRequest;

import java.util.List;

public interface LeaveRequestService extends IService<LeaveRequest> {

    String generateRequestNo();

    LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest);

    boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion);

    List<LeaveRequest> getPendingApprovals(Long approverId);

    List<LeaveRequest> getMyLeaveRequests(Long employeeId);

    List<LeaveRequest> getPendingApprovalsByScheduleId(Long scheduleId);
}
