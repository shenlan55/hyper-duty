package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.LeaveRequest;

import java.util.List;
import java.util.Map;

public interface LeaveRequestService extends IService<LeaveRequest> {

    String generateRequestNo();

    LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest);

    boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion, String scheduleAction, String scheduleType, String scheduleDateRange);

    List<LeaveRequest> getPendingApprovals(Long approverId);

    List<LeaveRequest> getApprovedApprovals(Long approverId);

    List<LeaveRequest> getMyLeaveRequests(Long employeeId);

    List<LeaveRequest> getPendingApprovalsByScheduleId(Long scheduleId);

    List<LeaveRequest> getApprovedApprovalsByScheduleId(Long scheduleId);

    List<Object> getApprovalRecords(Long requestId);

    Map<String, Object> checkEmployeeSchedule(Long employeeId, String startDate, String endDate);

    boolean confirmScheduleCompletion(Long requestId, Long approverId);
}
