package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutyScheduleMode;
import com.lasu.hyperduty.typehandler.PostgreSqlJsonTypeHandler;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;








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
    
    @TableField(typeHandler = PostgreSqlJsonTypeHandler.class)
    private String configJson;
    
    private String description;
    
    private Integer status;
    
    private Integer sort;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

}