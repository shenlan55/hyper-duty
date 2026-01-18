package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SwapRequest;
import com.lasu.hyperduty.service.SwapRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duty/swap-request")
public class SwapRequestController {

    @Autowired
    private SwapRequestService swapRequestService;

    @GetMapping("/list")
    public ResponseResult<List<SwapRequest>> getAllSwapRequests() {
        List<SwapRequest> list = swapRequestService.list();
        return ResponseResult.success(list);
    }

    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<SwapRequest>> getMySwapRequests(@PathVariable Long employeeId) {
        List<SwapRequest> list = swapRequestService.getMySwapRequests(employeeId);
        return ResponseResult.success(list);
    }

    @GetMapping("/pending/{approverId}")
    public ResponseResult<List<SwapRequest>> getPendingApprovals(@PathVariable Long approverId) {
        List<SwapRequest> list = swapRequestService.getPendingApprovals(approverId);
        return ResponseResult.success(list);
    }

    @GetMapping("/{id}")
    public ResponseResult<SwapRequest> getSwapRequestById(@PathVariable Long id) {
        SwapRequest request = swapRequestService.getById(id);
        return ResponseResult.success(request);
    }

    @PostMapping
    public ResponseResult<SwapRequest> submitSwapRequest(@Validated @RequestBody SwapRequest swapRequest) {
        SwapRequest result = swapRequestService.submitSwapRequest(swapRequest);
        return ResponseResult.success(result);
    }

    @PutMapping("/approve/{requestId}")
    public ResponseResult<Void> approveSwapRequest(@PathVariable Long requestId,
                                             @RequestParam Long approverId,
                                             @RequestParam String approvalStatus,
                                             @RequestParam(required = false) String opinion) {
        boolean success = swapRequestService.approveSwapRequest(requestId, approverId, approvalStatus, opinion);
        return success ? ResponseResult.success() : ResponseResult.error("审批失败");
    }

    @PutMapping("/confirm/{requestId}")
    public ResponseResult<Void> confirmSwapRequest(@PathVariable Long requestId,
                                             @RequestParam Long employeeId,
                                             @RequestParam String confirmStatus) {
        boolean success = swapRequestService.confirmSwapRequest(requestId, employeeId, confirmStatus);
        return success ? ResponseResult.success() : ResponseResult.error("确认失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteSwapRequest(@PathVariable Long id) {
        swapRequestService.removeById(id);
        return ResponseResult.success();
    }
}
