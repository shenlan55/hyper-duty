package com.lasu.hyperduty.duty.dto;

import com.lasu.hyperduty.duty.entity.DutyRecord;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 加班记录DTO
 * 包含加班记录及其关联的员工姓名等信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutyRecordDTO extends DutyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 替补人员姓名（如果有）
     */
    private String substituteEmployeeName;

    /**
     * 值班表名称
     */
    private String scheduleName;

    /**
     * 班次名称
     */
    private String dutyShiftName;

    /**
     * 从实体转换为DTO
     */
    public static DutyRecordDTO fromEntity(DutyRecord entity) {
        DutyRecordDTO dto = new DutyRecordDTO();
        dto.setId(entity.getId());
        dto.setAssignmentId(entity.getAssignmentId());
        dto.setScheduleId(entity.getScheduleId());
        dto.setDutyDate(entity.getDutyDate());
        dto.setDutyShift(entity.getDutyShift());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setCheckInTime(entity.getCheckInTime());
        dto.setCheckOutTime(entity.getCheckOutTime());
        dto.setDutyStatus(entity.getDutyStatus());
        dto.setCheckInRemark(entity.getCheckInRemark());
        dto.setCheckOutRemark(entity.getCheckOutRemark());
        dto.setOvertimeHours(entity.getOvertimeHours());
        dto.setRemark(entity.getRemark());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setManagerRemark(entity.getManagerRemark());
        dto.setSubstituteEmployeeId(entity.getSubstituteEmployeeId());
        dto.setSubstituteType(entity.getSubstituteType());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
