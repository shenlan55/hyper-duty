package com.lasu.hyperduty.score.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分记录实体
 */
@Data
@TableName("score_record")
public class ScoreRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 积分事件ID */
    private Long eventId;

    /** 实际分值（正数=加分，负数=扣分） */
    private Integer score;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 详细描述 */
    private String description;

    /** 录入人ID */
    private Long createBy;

    /** 所属年份 */
    private Integer periodYear;

    /** 所属月份 */
    private Integer periodMonth;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 员工姓名（非持久化） */
    @TableField(exist = false)
    private String employeeName;

    /** 事件名称（非持久化） */
    @TableField(exist = false)
    private String eventName;
}