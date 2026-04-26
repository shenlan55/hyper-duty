package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutySchedule;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;








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
    
    private Long scheduleModeId;
    
    private Integer status;
    
    private Integer sortOrder;
    
    private Long createBy;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

}