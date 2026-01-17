package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                                 @RequestParam(required = false) String opinion) {
        boolean success = leaveRequestService.approveLeaveRequest(requestId, approverId, approvalStatus, opinion);
        return success ? ResponseResult.success() : ResponseResult.error("审批失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.removeById(id);
        return ResponseResult.success();
    }
}
