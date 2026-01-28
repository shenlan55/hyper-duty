package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班次互斥关系实体类
 */
@Data
@TableName("duty_shift_mutex")
public class DutyShiftMutex implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 班次配置ID
     */
    private Long shiftConfigId;

    /**
     * 互斥班次配置ID
     */
    private Long mutexShiftConfigId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}