package com.lasu.hyperduty.common.dto;

import com.lasu.hyperduty.common.dto.TaskBindingDTO;
import java.time.LocalDateTime;
import lombok.Data;








@Data
public class TaskBindingDTO {
    private Long id;
    private Long taskId;
    private Long tableId;
    private Long rowId;
    private String tableName;
    private String rowData;
    private String orderNo;
    private String title;
    private LocalDateTime createTime;
}
