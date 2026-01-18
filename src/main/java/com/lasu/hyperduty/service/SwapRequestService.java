package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SwapRequest;

import java.util.List;

public interface SwapRequestService extends IService<SwapRequest> {

    String generateRequestNo();

    SwapRequest submitSwapRequest(SwapRequest swapRequest);

    boolean approveSwapRequest(Long requestId, Long approverId, String approvalStatus, String opinion);

    boolean confirmSwapRequest(Long requestId, Long employeeId, String confirmStatus);

    List<SwapRequest> getPendingApprovals(Long approverId);

    List<SwapRequest> getMySwapRequests(Long employeeId);
}
