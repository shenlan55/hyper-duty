package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.entity.LeaveSubstitute;
import com.lasu.hyperduty.service.LeaveRequestService;
import com.lasu.hyperduty.service.LeaveSubstituteService;
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

    @Autowired
    private LeaveSubstituteService leaveSubstituteService;

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
                                                 @RequestBody(required = false) List<Map<String, Object>> substituteData) {
        boolean success = leaveRequestService.approveLeaveRequest(requestId, approverId, approvalStatus, opinion, scheduleAction, substituteData);
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
                                                                     @RequestParam String endDate,
                                                                     @RequestParam(required = false) Long scheduleId) {
        Map<String, Object> result = leaveRequestService.checkEmployeeSchedule(employeeId, startDate, endDate, scheduleId);
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
     * 分页获取我的请假申请
     */
    @GetMapping("/my/page/{employeeId}")
    public ResponseResult<com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest>> getMyLeaveRequestsPage(
            @PathVariable Long employeeId,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Integer leaveType,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> result = leaveRequestService.getMyLeaveRequestsPage(
                employeeId, page, size, leaveType, approvalStatus, startDate, endDate);
        return ResponseResult.success(result);
    }

    /**
     * 分页获取待审批请假申请
     */
    @GetMapping("/pending/page/{approverId}")
    public ResponseResult<com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest>> getPendingApprovalsPage(
            @PathVariable Long approverId,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Integer leaveType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> result = leaveRequestService.getPendingApprovalsPage(
                approverId, page, size, scheduleId, leaveType, startDate, endDate);
        return ResponseResult.success(result);
    }

    /**
     * 分页获取已审批请假申请
     */
    @GetMapping("/approved/page/{approverId}")
    public ResponseResult<com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest>> getApprovedApprovalsPage(
            @PathVariable Long approverId,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Integer leaveType,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> result = leaveRequestService.getApprovedApprovalsPage(
                approverId, page, size, scheduleId, leaveType, approvalStatus, startDate, endDate);
        return ResponseResult.success(result);
    }

    /**
     * 批量查询员工请假信息
     */
    @PostMapping("/employee-leave-info")
    public ResponseResult<Map<Long, Map<String, Map<String, List<Long>>>>> getEmployeeLeaveInfo(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("employeeIds") List<Long> employeeIds) {
        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = leaveRequestService.getEmployeeLeaveInfo(employeeIds, startDate, endDate);
        return ResponseResult.success(leaveInfo);
    }

    /**
     * 获取可用的顶岗人员
     */
    @GetMapping("/available-substitutes")
    public ResponseResult<List<com.lasu.hyperduty.entity.SysEmployee>> getAvailableSubstitutes(
            @RequestParam Long scheduleId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam Long leaveEmployeeId) {
        List<com.lasu.hyperduty.entity.SysEmployee> availableSubstitutes = leaveRequestService.getAvailableSubstitutes(scheduleId, startDate, endDate, leaveEmployeeId);
        return ResponseResult.success(availableSubstitutes);
    }

    /**
     * 获取请假顶岗信息
     */
    @GetMapping("/substitutes/{leaveRequestId}")
    public ResponseResult<List<LeaveSubstitute>> getLeaveSubstitutes(@PathVariable Long leaveRequestId) {
        List<LeaveSubstitute> substitutes = leaveSubstituteService.getByLeaveRequestId(leaveRequestId);
        return ResponseResult.success(substitutes);
    }

    /**
     * 根据员工ID和日期范围获取顶岗信息
     */
    @GetMapping("/substitutes-by-employees")
    public ResponseResult<List<LeaveSubstitute>> getSubstitutesByEmployees(
            @RequestParam List<Long> employeeIds,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<LeaveSubstitute> substitutes = leaveSubstituteService.lambdaQuery()
                .in(LeaveSubstitute::getOriginalEmployeeId, employeeIds)
                .ge(LeaveSubstitute::getDutyDate, java.time.LocalDate.parse(startDate))
                .le(LeaveSubstitute::getDutyDate, java.time.LocalDate.parse(endDate))
                .eq(LeaveSubstitute::getStatus, 1)
                .list();
        return ResponseResult.success(substitutes);
    }
}
