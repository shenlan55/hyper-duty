package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long requestId;

    private String requestType;

    private Long approverId;

    private Integer approvalLevel;

    private String approvalStatus;

    private String approvalOpinion;

    private LocalDateTime approvalTime;

    private LocalDateTime createTime;

}
