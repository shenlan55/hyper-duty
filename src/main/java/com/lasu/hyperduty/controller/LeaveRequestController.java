package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/duty/leave-request")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping("/list")
    public ResponseResult<List<LeaveRequest>> getAllLeaveRequests() {
        List<LeaveRequest> list = leaveRequestService.list();
        return ResponseResult.success(list);
    }

    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<LeaveRequest>> getMyLeaveRequests(@PathVariable Long employeeId) {
        List<LeaveRequest> list = leaveRequestService.getMyLeaveRequests(employeeId);
        return ResponseResult.success(list);
    }

    @GetMapping("/pending/{approverId}")
    public ResponseResult<List<LeaveRequest>> getPendingApprovals(@PathVariable Long approverId) {
        List<LeaveRequest> list = leaveRequestService.getPendingApprovals(approverId);
        return ResponseResult.success(list);
    }

    @GetMapping("/approved/{approverId}")
    public ResponseResult<List<LeaveRequest>> getApprovedApprovals(@PathVariable Long approverId) {
        List<LeaveRequest> list = leaveRequestService.getApprovedApprovals(approverId);
        return ResponseResult.success(list);
    }

    @GetMapping("/pending/schedule/{scheduleId}")
    public ResponseResult<List<LeaveRequest>> getPendingApprovalsByScheduleId(@PathVariable Long scheduleId) {
        List<LeaveRequest> list = leaveRequestService.getPendingApprovalsByScheduleId(scheduleId);
        return ResponseResult.success(list);
    }

    @GetMapping("/approved/schedule/{scheduleId}")
    public ResponseResult<List<LeaveRequest>> getApprovedApprovalsByScheduleId(@PathVariable Long scheduleId) {
        List<LeaveRequest> list = leaveRequestService.getApprovedApprovalsByScheduleId(scheduleId);
        return ResponseResult.success(list);
    }

    @GetMapping("/{id}")
    public ResponseResult<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequest request = leaveRequestService.getById(id);
        return ResponseResult.success(request);
    }

    @PostMapping
    public ResponseResult<LeaveRequest> submitLeaveRequest(@Validated @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest result = leaveRequestService.submitLeaveRequest(leaveRequest);
        return ResponseResult.success(result);
    }

    @PutMapping("/approve/{requestId}")
    public ResponseResult<Void> approveLeaveRequest(@PathVariable Long requestId,
                                                 @RequestParam Long approverId,
                                                 @RequestParam String approvalStatus,
                                                 @RequestParam(required = false) String opinion,
                                                 @RequestParam(required = false) String scheduleAction,
                                                 @RequestParam(required = false) String scheduleType,
                                                 @RequestParam(required = false) String scheduleDateRange) {
        boolean success = leaveRequestService.approveLeaveRequest(requestId, approverId, approvalStatus, opinion, scheduleAction, scheduleType, scheduleDateRange);
        return success ? ResponseResult.success() : ResponseResult.error("审批失败");
    }

    @GetMapping("/approval-records/{requestId}")
    public ResponseResult<List<Object>> getApprovalRecords(@PathVariable Long requestId) {
        List<Object> records = leaveRequestService.getApprovalRecords(requestId);
        return ResponseResult.success(records);
    }

    @GetMapping("/check-schedule")
    public ResponseResult<Map<String, Object>> checkEmployeeSchedule(@RequestParam Long employeeId,
                                                                     @RequestParam String startDate,
                                                                     @RequestParam String endDate) {
        Map<String, Object> result = leaveRequestService.checkEmployeeSchedule(employeeId, startDate, endDate);
        return ResponseResult.success(result);
    }

    @PutMapping("/confirm-schedule/{requestId}")
    public ResponseResult<Void> confirmScheduleCompletion(@PathVariable Long requestId, @RequestParam Long approverId) {
        boolean success = leaveRequestService.confirmScheduleCompletion(requestId, approverId);
        return success ? ResponseResult.success() : ResponseResult.error("确认失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 批量查询员工请假信息
     */
    @PostMapping("/employee-leave-info")
    public ResponseResult<Map<Long, List<String>>> getEmployeeLeaveInfo(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("employeeIds") List<Long> employeeIds) {
        Map<Long, List<String>> leaveInfo = leaveRequestService.getEmployeeLeaveInfo(employeeIds, startDate, endDate);
        return ResponseResult.success(leaveInfo);
    }
}
