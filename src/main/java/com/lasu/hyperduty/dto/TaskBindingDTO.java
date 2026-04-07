package com.lasu.hyperduty.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskBindingDTO {
    private Long id;
    private Long taskId;
    private Long tableId;
    private Long rowId;
    private String tableName;
    private String rowData;
    private String orderNo;
    private LocalDateTime createTime;
}
