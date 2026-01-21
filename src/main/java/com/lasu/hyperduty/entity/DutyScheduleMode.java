package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("duty_schedule_mode")
public class DutyScheduleMode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String modeName;
    
    private String modeCode;
    
    private Integer modeType;
    
    private String algorithmClass;
    
    private String configJson;
    
    private String description;
    
    private Integer status;
    
    private Integer sort;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

}