package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("employee_available_time")
public class EmployeeAvailableTime implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private Integer dayOfWeek;

    private String startTime;

    private String endTime;

    private Integer isAvailable;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
