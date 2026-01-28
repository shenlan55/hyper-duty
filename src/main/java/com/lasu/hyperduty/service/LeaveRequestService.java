package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.LeaveRequest;

import java.util.List;
import java.util.Map;

public interface LeaveRequestService extends IService<LeaveRequest> {

    String generateRequestNo();

    LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest);

    boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion, String scheduleAction, List<Map<String, Object>> substituteData);

    List<LeaveRequest> getPendingApprovals(Long approverId);

    List<LeaveRequest> getApprovedApprovals(Long approverId);

    List<LeaveRequest> getMyLeaveRequests(Long employeeId);

    List<LeaveRequest> getPendingApprovalsByScheduleId(Long scheduleId);

    List<LeaveRequest> getApprovedApprovalsByScheduleId(Long scheduleId);

    List<Object> getApprovalRecords(Long requestId);

    Map<String, Object> checkEmployeeSchedule(Long employeeId, String startDate, String endDate, Long scheduleId);

    boolean confirmScheduleCompletion(Long requestId, Long approverId);

    /**
     * 批量查询员工请假信息
     * @param employeeIds 员工ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 员工请假信息映射，结构为：员工ID -> 日期 -> 值班表ID -> 班次配置ID列表
     */
    Map<Long, Map<String, Map<String, List<Long>>>> getEmployeeLeaveInfo(List<Long> employeeIds, String startDate, String endDate);

    /**
     * 分页获取我的请假申请
     * @param employeeId 员工ID
     * @param page 页码
     * @param size 每页大小
     * @param leaveType 请假类型（可选）
     * @param approvalStatus 审批状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<LeaveRequest> getMyLeaveRequestsPage(Long employeeId, Integer page, Integer size, Integer leaveType, String approvalStatus, String startDate, String endDate);

    /**
     * 分页获取待审批请假申请
     * @param approverId 审批人ID
     * @param page 页码
     * @param size 每页大小
     * @param scheduleId 值班表ID（可选）
     * @param leaveType 请假类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<LeaveRequest> getPendingApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String startDate, String endDate);

    /**
     * 分页获取已审批请假申请
     * @param approverId 审批人ID
     * @param page 页码
     * @param size 每页大小
     * @param scheduleId 值班表ID（可选）
     * @param leaveType 请假类型（可选）
     * @param approvalStatus 审批状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<LeaveRequest> getApprovedApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String approvalStatus, String startDate, String endDate);

    /**
     * 获取可用的顶岗人员
     * @param scheduleId 值班表ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param leaveEmployeeId 请假员工ID
     * @return 可用的顶岗人员列表
     */
    List<com.lasu.hyperduty.entity.SysEmployee> getAvailableSubstitutes(Long scheduleId, String startDate, String endDate, Long leaveEmployeeId);
}
