package com.lasu.hyperduty.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class WorkloadDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long taskId;
    private String taskName;
    private Integer taskStatus;
    private String taskStatusText;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private Long assigneeId;
    private String assigneeName;
    private List<BindingInfo> bindings;

    @Data
    public static class BindingInfo {
        private Long id;
        private Long tableId;
        private String tableName;
        private Long rowId;
        private String orderNo;
        private Map<String, Object> rowData;
        private LocalDateTime createTime;
    }
}
