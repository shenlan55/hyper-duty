package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutyScheduleShift;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;








/**
 * 值班表班次关联实体
 */
@Data
@TableName("duty_schedule_shift")
public class DutyScheduleShift implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long scheduleId;
    
    private Long shiftConfigId;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;

}
