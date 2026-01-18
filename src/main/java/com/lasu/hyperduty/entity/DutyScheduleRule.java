package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("duty_schedule_rule")
public class DutyScheduleRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String ruleName;

    private String ruleCode;

    private Integer scheduleCycle;

    private Integer maxDailyShifts;

    private BigDecimal maxWeeklyHours;

    private BigDecimal maxMonthlyHours;

    private Integer minRestDays;

    private String substitutePriorityRule;

    private String conflictDetectionRule;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
