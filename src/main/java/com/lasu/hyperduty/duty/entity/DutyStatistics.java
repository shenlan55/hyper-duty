package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutyStatistics;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;








@Data
@TableName("duty_statistics")
public class DutyStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long deptId;

    private String deptName;

    private Integer totalSchedules;

    private Integer totalAssignments;

    private Integer totalRecords;

    private BigDecimal avgDailyHours;

    private BigDecimal totalOvertimeHours;

    private Integer totalLeaveRequests;

}
