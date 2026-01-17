package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("swap_request")
public class SwapRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String requestNo;

    private Long originalEmployeeId;

    private Long targetEmployeeId;

    private Long originalAssignmentId;

    private Long targetAssignmentId;

    private LocalDate swapDate;

    private Integer swapShift;

    private String reason;

    private String approvalStatus;

    private Long currentApproverId;

    private Integer approvalLevel;

    private Integer totalApprovalLevels;

    private String originalConfirmStatus;

    private String targetConfirmStatus;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
