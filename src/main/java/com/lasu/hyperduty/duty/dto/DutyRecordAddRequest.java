package com.lasu.hyperduty.duty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 加班记录新增请求 DTO
 *
 * <p>仅用于 {@code POST /duty/record}（新建加班记录）。
 * 与 {@code DutyRecord} 实体的区别：强制校验值班表/日期/班次必填；
 * 编辑/审批场景（{@code PUT /duty/record}）只改部分字段，不应受这些约束。</p>
 *
 * @author Hyper Duty
 * @since 2026-06-25
 */
@Data
public class DutyRecordAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 值班安排ID（可选，与 scheduleId/dutyDate/dutyShift 二选一）
     */
    private Long assignmentId;

    /**
     * 值班表ID
     */
    @NotNull(message = "值班表ID不能为空")
    private Long scheduleId;

    /**
     * 值班日期
     */
    @NotNull(message = "值班日期不能为空")
    private LocalDate dutyDate;

    /**
     * 班次
     */
    @NotNull(message = "班次不能为空")
    private Integer dutyShift;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 加班时长（小时）
     */
    private BigDecimal overtimeHours;

    /**
     * 加班原因
     */
    private String remark;

    /**
     * 审批状态（新建时可不传，默认 pending）
     */
    private String approvalStatus;
}
