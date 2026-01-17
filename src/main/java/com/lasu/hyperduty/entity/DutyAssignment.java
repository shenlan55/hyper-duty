package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("duty_assignment")
public class DutyAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long scheduleId;
    
    private LocalDate dutyDate;
    
    private Integer dutyShift;
    
    private Long employeeId;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

}