package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_task_custom_row")
public class PmTaskCustomRow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long tableId;

    private Long rowId;

    private String orderNo;

    private LocalDateTime createTime;
}
