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

    /**
     * 批量查询员工请假信息
     * @param employeeIds 员工ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 员工请假信息映射，键为员工ID，值为请假日期集合
     */
    Map<Long, List<String>> getEmployeeLeaveInfo(List<Long> employeeIds, String startDate, String endDate);
}
