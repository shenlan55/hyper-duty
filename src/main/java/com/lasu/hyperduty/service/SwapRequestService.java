package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 分页获取我的调班申请
     * @param employeeId 员工ID
     * @param page 页码
     * @param size 每页大小
     * @param approvalStatus 审批状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<SwapRequest> getMySwapRequestsPage(Long employeeId, Integer page, Integer size, String approvalStatus, String startDate, String endDate);

    /**
     * 分页获取待审批调班申请
     * @param approverId 审批人ID
     * @param page 页码
     * @param size 每页大小
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<SwapRequest> getPendingApprovalsPage(Long approverId, Integer page, Integer size, String startDate, String endDate);
}
