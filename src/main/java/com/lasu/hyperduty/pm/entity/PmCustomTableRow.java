package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.pm.entity.PmCustomTableRow;
import com.lasu.hyperduty.typehandler.PostgreSqlJsonTypeHandler;
import java.time.LocalDateTime;
import lombok.Data;








@Data
@TableName("pm_custom_table_row")
public class PmCustomTableRow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tableId;

    @TableField(typeHandler = PostgreSqlJsonTypeHandler.class)
    private String rowData;

    private Integer sortOrder;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
