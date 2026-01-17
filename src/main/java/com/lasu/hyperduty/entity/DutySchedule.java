package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("duty_schedule")
public class DutySchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String scheduleName;
    
    private String description;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer status;
    
    private Long createBy;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

}