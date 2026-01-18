package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("leave_request")
public class LeaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String requestNo;

    private Long employeeId;

    private Integer leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String startTime;

    private String endTime;

    private BigDecimal totalHours;

    private String reason;

    private String attachmentUrl;

    private String approvalStatus;

    private Long currentApproverId;

    private Integer approvalLevel;

    private Integer totalApprovalLevels;

    private Long substituteEmployeeId;

    private Integer substituteType;

    private String substituteStatus;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
