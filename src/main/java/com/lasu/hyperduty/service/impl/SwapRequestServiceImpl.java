package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SwapRequest;
import com.lasu.hyperduty.mapper.SwapRequestMapper;
import com.lasu.hyperduty.service.SwapRequestService;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class SwapRequestServiceImpl extends ServiceImpl<SwapRequestMapper, SwapRequest> implements SwapRequestService {

    private static final Logger log = LoggerFactory.getLogger(SwapRequestServiceImpl.class);

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
        // 生成申请编号
        swapRequest.setRequestNo(generateRequestNo());
        swapRequest.setApprovalStatus("pending");
        swapRequest.setApprovalLevel(1);
        swapRequest.setOriginalConfirmStatus("pending");
        swapRequest.setTargetConfirmStatus("pending");
        swapRequest.setCreateTime(LocalDateTime.now());
        swapRequest.setUpdateTime(LocalDateTime.now());
        
        // 保存调班申请
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

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

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
            
            // 当双方都确认后，自动替换班次
            if ("confirmed".equals(request.getOriginalConfirmStatus()) && "confirmed".equals(request.getTargetConfirmStatus())) {
                request.setApprovalStatus("approved");
                
                // 自动替换班次
                try {
                    dutyAssignmentService.swapDutyAssignments(
                        request.getOriginalEmployeeId(),
                        request.getTargetEmployeeId(),
                        request.getSwapDate(),
                        request.getSwapShift()
                    );
                } catch (Exception e) {
                    // 记录替换班次失败的日志
                    log.error("自动替换班次失败: {}", e.getMessage(), e);
                    // 不影响调班申请的状态更新
                }
            }
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
    public IPage<SwapRequest> getMySwapRequestsPage(Long employeeId, Integer page, Integer size, String approvalStatus, Long scheduleId, String startDate, String endDate) {
        IPage<SwapRequest> pageInfo = new Page<>(page, size);
        return lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(SwapRequest::getOriginalEmployeeId, employeeId)
                        .or()
                        .eq(SwapRequest::getTargetEmployeeId, employeeId))
                .eq(approvalStatus != null && !approvalStatus.isEmpty(), SwapRequest::getApprovalStatus, approvalStatus)
                .eq(scheduleId != null, SwapRequest::getScheduleId, scheduleId)
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
