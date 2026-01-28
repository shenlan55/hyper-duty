package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假顶岗信息
 */
@Data
@TableName("leave_substitute")
public class LeaveSubstitute implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 请假申请ID
     */
    private Long leaveRequestId;

    /**
     * 原值班人员ID
     */
    private Long originalEmployeeId;

    /**
     * 顶岗人员ID
     */
    private Long substituteEmployeeId;

    /**
     * 值班日期
     */
    private LocalDate dutyDate;

    /**
     * 班次配置ID
     */
    private Long shiftConfigId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
