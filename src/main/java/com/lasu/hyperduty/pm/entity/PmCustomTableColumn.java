package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.pm.entity.PmCustomTableColumn;
import com.lasu.hyperduty.typehandler.PostgreSqlJsonTypeHandler;
import java.time.LocalDateTime;
import lombok.Data;








@Data
@TableName(value = "pm_custom_table_column", autoResultMap = true)
public class PmCustomTableColumn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tableId;

    private String columnName;

    private String columnCode;

    private String columnType;

    private Integer columnWidth;

    private Integer required;

    private Integer sortOrder;

    @TableField(typeHandler = PostgreSqlJsonTypeHandler.class)
    private String options;

    private LocalDateTime createTime;
}
