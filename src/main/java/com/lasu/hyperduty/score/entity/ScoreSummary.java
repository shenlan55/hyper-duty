package com.lasu.hyperduty.score.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分月度汇总实体
 */
@Data
@TableName("score_summary")
public class ScoreSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 年份 */
    private Integer periodYear;

    /** 月份 */
    private Integer periodMonth;

    /** 当月积分合计 */
    private Integer totalScore;

    /** 当月加班工时 */
    private BigDecimal workHours;

    /** 综合评分 */
    private BigDecimal comprehensiveScore;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 员工姓名（非持久化） */
    @TableField(exist = false)
    private String employeeName;

    /** 部门名称（非持久化） */
    @TableField(exist = false)
    private String deptName;
}