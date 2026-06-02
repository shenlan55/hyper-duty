package com.lasu.hyperduty.duty.dto;

import com.lasu.hyperduty.duty.entity.LeaveRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假申请DTO
 * 包含请假申请及其关联的员工姓名、值班表名称等信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveRequestDTO extends LeaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 值班表名称
     */
    private String scheduleName;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 从实体转换为DTO
     */
    public static LeaveRequestDTO fromEntity(LeaveRequest entity) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(entity.getId());
        dto.setRequestNo(entity.getRequestNo());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setScheduleId(entity.getScheduleId());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setShiftConfigId(entity.getShiftConfigId());
        dto.setShiftConfigIds(entity.getShiftConfigIds());
        dto.setTotalHours(entity.getTotalHours());
        dto.setReason(entity.getReason());
        dto.setAttachmentUrl(entity.getAttachmentUrl());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setCurrentApproverId(entity.getCurrentApproverId());
        dto.setApprovalLevel(entity.getApprovalLevel());
        dto.setTotalApprovalLevels(entity.getTotalApprovalLevels());
        dto.setSubstituteEmployeeId(entity.getSubstituteEmployeeId());
        dto.setSubstituteType(entity.getSubstituteType());
        dto.setSubstituteStatus(entity.getSubstituteStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setApprovalOpinion(entity.getApprovalOpinion());
        dto.setScheduleCompleted(entity.getScheduleCompleted());
        dto.setScheduleCompletedTime(entity.getScheduleCompletedTime());
        dto.setScheduleCompletedBy(entity.getScheduleCompletedBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
