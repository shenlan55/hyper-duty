package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.pm.entity.PmCustomTable;
import java.time.LocalDateTime;
import lombok.Data;








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
