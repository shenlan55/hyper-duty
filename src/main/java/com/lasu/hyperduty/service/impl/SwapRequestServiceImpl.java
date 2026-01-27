package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SwapRequest;
import com.lasu.hyperduty.mapper.SwapRequestMapper;
import com.lasu.hyperduty.service.SwapRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class SwapRequestServiceImpl extends ServiceImpl<SwapRequestMapper, SwapRequest> implements SwapRequestService {

    @Autowired
    private SwapRequestMapper swapRequestMapper;

    @Override
    public String generateRequestNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "SW" + timestamp + random;
    }

    @Override
    public SwapRequest submitSwapRequest(SwapRequest swapRequest) {
        swapRequest.setRequestNo(generateRequestNo());
        swapRequest.setApprovalStatus("pending");
        swapRequest.setApprovalLevel(1);
        swapRequest.setOriginalConfirmStatus("pending");
        swapRequest.setTargetConfirmStatus("pending");
        swapRequest.setCreateTime(LocalDateTime.now());
        swapRequest.setUpdateTime(LocalDateTime.now());
        save(swapRequest);
        return swapRequest;
    }

    @Override
    public boolean approveSwapRequest(Long requestId, Long approverId, String approvalStatus, String opinion) {
        SwapRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        request.setApprovalStatus(approvalStatus);
        request.setCurrentApproverId(approverId);
        request.setUpdateTime(LocalDateTime.now());
        if ("rejected".equals(approvalStatus)) {
            request.setRejectReason(opinion);
        }
        return updateById(request);
    }

    @Override
    public boolean confirmSwapRequest(Long requestId, Long employeeId, String confirmStatus) {
        SwapRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        if ("approved".equals(confirmStatus)) {
            if (employeeId.equals(request.getOriginalEmployeeId())) {
                request.setOriginalConfirmStatus("confirmed");
            } else if (employeeId.equals(request.getTargetEmployeeId())) {
                request.setTargetConfirmStatus("confirmed");
            }
            request.setUpdateTime(LocalDateTime.now());
        }

        return updateById(request);
    }

    @Override
    public List<SwapRequest> getPendingApprovals(Long approverId) {
        return lambdaQuery()
                .eq(SwapRequest::getApprovalStatus, "pending")
                .eq(SwapRequest::getCurrentApproverId, approverId)
                .orderByDesc(SwapRequest::getCreateTime)
                .list();
    }

    @Override
    public List<SwapRequest> getMySwapRequests(Long employeeId) {
        return lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(SwapRequest::getOriginalEmployeeId, employeeId)
                        .or()
                        .eq(SwapRequest::getTargetEmployeeId, employeeId))
                .orderByDesc(SwapRequest::getCreateTime)
                .list();
    }

    @Override
    public IPage<SwapRequest> getMySwapRequestsPage(Long employeeId, Integer page, Integer size, String approvalStatus, String startDate, String endDate) {
        IPage<SwapRequest> pageInfo = new Page<>(page, size);
        return lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(SwapRequest::getOriginalEmployeeId, employeeId)
                        .or()
                        .eq(SwapRequest::getTargetEmployeeId, employeeId))
                .eq(approvalStatus != null && !approvalStatus.isEmpty(), SwapRequest::getApprovalStatus, approvalStatus)
                .ge(startDate != null && !startDate.isEmpty(), SwapRequest::getCreateTime, startDate)
                .le(endDate != null && !endDate.isEmpty(), SwapRequest::getCreateTime, endDate)
                .orderByDesc(SwapRequest::getCreateTime)
                .page(pageInfo);
    }

    @Override
    public IPage<SwapRequest> getPendingApprovalsPage(Long approverId, Integer page, Integer size, String startDate, String endDate) {
        IPage<SwapRequest> pageInfo = new Page<>(page, size);
        return lambdaQuery()
                .eq(SwapRequest::getApprovalStatus, "pending")
                .eq(SwapRequest::getCurrentApproverId, approverId)
                .ge(startDate != null && !startDate.isEmpty(), SwapRequest::getCreateTime, startDate)
                .le(endDate != null && !endDate.isEmpty(), SwapRequest::getCreateTime, endDate)
                .orderByDesc(SwapRequest::getCreateTime)
                .page(pageInfo);
    }
}
