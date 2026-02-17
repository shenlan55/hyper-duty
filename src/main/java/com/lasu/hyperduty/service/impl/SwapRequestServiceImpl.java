package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SwapRequest;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.mapper.SwapRequestMapper;
import com.lasu.hyperduty.service.SwapRequestService;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class SwapRequestServiceImpl extends ServiceImpl<SwapRequestMapper, SwapRequest> implements SwapRequestService {

    private static final Logger log = LoggerFactory.getLogger(SwapRequestServiceImpl.class);

    @Autowired
    private SwapRequestMapper swapRequestMapper;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

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
        
        // 查找原值班人员的值班安排ID
        Long originalAssignmentId = null;
        if (swapRequest.getOriginalAssignmentId() == null && 
            swapRequest.getScheduleId() != null && 
            swapRequest.getOriginalEmployeeId() != null && 
            (swapRequest.getOriginalSwapDate() != null || swapRequest.getSwapDate() != null) && 
            (swapRequest.getOriginalSwapShift() != null || swapRequest.getSwapShift() != null)) {
            LocalDate originalDate = swapRequest.getOriginalSwapDate() != null ? 
                swapRequest.getOriginalSwapDate() : swapRequest.getSwapDate();
            Integer originalShift = swapRequest.getOriginalSwapShift() != null ? 
                swapRequest.getOriginalSwapShift() : swapRequest.getSwapShift();
                
            originalAssignmentId = findDutyAssignmentId(
                swapRequest.getScheduleId(),
                swapRequest.getOriginalEmployeeId(),
                originalDate,
                originalShift
            );
            if (originalAssignmentId == null) {
                throw new RuntimeException("原值班人员在指定日期和班次没有值班安排");
            }
            swapRequest.setOriginalAssignmentId(originalAssignmentId);
            
            // 为swapDate字段设置值，使用原值班日期
            if (swapRequest.getSwapDate() == null) {
                swapRequest.setSwapDate(originalDate);
            }
            
            // 为swapShift字段设置值，使用原值班班次
            if (swapRequest.getSwapShift() == null) {
                swapRequest.setSwapShift(originalShift);
            }
        }
        
        // 查找目标值班人员的值班安排ID
        Long targetAssignmentId = null;
        if (swapRequest.getTargetAssignmentId() == null && 
            swapRequest.getScheduleId() != null && 
            swapRequest.getTargetEmployeeId() != null && 
            swapRequest.getTargetSwapDate() != null && 
            swapRequest.getTargetSwapShift() != null) {
            targetAssignmentId = findDutyAssignmentId(
                swapRequest.getScheduleId(),
                swapRequest.getTargetEmployeeId(),
                swapRequest.getTargetSwapDate(),
                swapRequest.getTargetSwapShift()
            );
            if (targetAssignmentId == null) {
                throw new RuntimeException("目标值班人员在指定日期和班次没有值班安排");
            }
            swapRequest.setTargetAssignmentId(targetAssignmentId);
        }
        
        // 保存调班申请
        save(swapRequest);
        return swapRequest;
    }
    
    /**
     * 根据值班表ID、员工ID、日期和班次查找值班安排ID
     */
    private Long findDutyAssignmentId(Long scheduleId, Long employeeId, LocalDate dutyDate, Integer dutyShift) {
        try {
            log.debug("查找值班安排: scheduleId={}, employeeId={}, dutyDate={}, dutyShift={}", 
                     scheduleId, employeeId, dutyDate, dutyShift);
            
            // 使用lambdaQuery查找符合条件的值班安排
            DutyAssignment assignment = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getScheduleId, scheduleId)
                .eq(DutyAssignment::getEmployeeId, employeeId)
                .eq(DutyAssignment::getDutyDate, dutyDate)
                .eq(DutyAssignment::getDutyShift, dutyShift)
                .one();
            
            log.debug("查找结果: assignment={}", assignment);
            return assignment != null ? assignment.getId() : null;
        } catch (Exception e) {
            log.error("查找值班安排ID失败: {}", e.getMessage(), e);
            return null;
        }
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
            // 只有目标值班人员才能确认调班申请
            if (employeeId.equals(request.getTargetEmployeeId())) {
                request.setTargetConfirmStatus("confirmed");
                request.setApprovalStatus("approved");
                request.setUpdateTime(LocalDateTime.now());
                
                // 自动替换班次
                try {
                    // 使用originalSwapDate和originalSwapShift作为原值班人员的日期和班次
                    // 使用targetSwapDate和targetSwapShift作为目标值班人员的日期和班次
                    dutyAssignmentService.swapDutyAssignments(
                        request.getOriginalEmployeeId(),
                        request.getTargetEmployeeId(),
                        request.getOriginalSwapDate(),
                        request.getOriginalSwapShift(),
                        request.getTargetSwapDate(),
                        request.getTargetSwapShift()
                    );
                } catch (Exception e) {
                    // 记录替换班次失败的日志
                    log.error("自动替换班次失败: {}", e.getMessage(), e);
                    // 不影响调班申请的状态更新
                }
            } else {
                // 原值班人员（发起者）只能撤销申请，不能确认
                return false;
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
