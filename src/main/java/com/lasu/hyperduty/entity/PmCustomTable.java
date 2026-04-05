package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_custom_table")
public class PmCustomTable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tableName;

    private String tableCode;

    private Long projectId;

    private String description;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
