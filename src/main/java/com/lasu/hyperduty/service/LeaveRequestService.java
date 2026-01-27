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
     * @return 员工请假信息映射，结构为：员工ID -> 日期 -> 值班表ID -> 班次配置ID列表
     */
    Map<Long, Map<String, Map<String, List<Long>>>> getEmployeeLeaveInfo(List<Long> employeeIds, String startDate, String endDate);
}
