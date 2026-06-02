package com.lasu.hyperduty.duty.dto;

import com.lasu.hyperduty.duty.entity.SwapRequest;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调班申请DTO
 * 包含调班申请及其关联的员工姓名、值班表名称等信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SwapRequestDTO extends SwapRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原值班人姓名
     */
    private String originalEmployeeName;

    /**
     * 目标值班人姓名
     */
    private String targetEmployeeName;

    /**
     * 值班表名称
     */
    private String scheduleName;

    /**
     * 从实体转换为DTO
     */
    public static SwapRequestDTO fromEntity(SwapRequest entity) {
        SwapRequestDTO dto = new SwapRequestDTO();
        dto.setId(entity.getId());
        dto.setRequestNo(entity.getRequestNo());
        dto.setOriginalEmployeeId(entity.getOriginalEmployeeId());
        dto.setTargetEmployeeId(entity.getTargetEmployeeId());
        dto.setOriginalAssignmentId(entity.getOriginalAssignmentId());
        dto.setTargetAssignmentId(entity.getTargetAssignmentId());
        dto.setScheduleId(entity.getScheduleId());
        dto.setSwapDate(entity.getSwapDate());
        dto.setSwapShift(entity.getSwapShift());
        dto.setOriginalSwapDate(entity.getOriginalSwapDate());
        dto.setOriginalSwapShift(entity.getOriginalSwapShift());
        dto.setTargetSwapDate(entity.getTargetSwapDate());
        dto.setTargetSwapShift(entity.getTargetSwapShift());
        dto.setReason(entity.getReason());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setCurrentApproverId(entity.getCurrentApproverId());
        dto.setApprovalLevel(entity.getApprovalLevel());
        dto.setTotalApprovalLevels(entity.getTotalApprovalLevels());
        dto.setOriginalConfirmStatus(entity.getOriginalConfirmStatus());
        dto.setTargetConfirmStatus(entity.getTargetConfirmStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
