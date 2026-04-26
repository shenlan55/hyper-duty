package com.lasu.hyperduty.ai.dto;

import com.lasu.hyperduty.ai.dto.AiReportDataDTO;
import com.lasu.hyperduty.pm.dto.TaskUpdateDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;









/**
 * AI报告数据聚合DTO - 结构化格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportDataDTO {

    /**
     * 项目信息列表
     */
    private List<ProjectInfoDTO> projectInfo;

    /**
     * 日报专属：今日任务数据
     */
    private DailyTaskDataDTO dailyTaskData;

    /**
     * 周报专属：周任务数据
     */
    private WeeklyTaskDataDTO weeklyTaskData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfoDTO {
        private Long projectId;
        private String projectName;
        private String projectCode;
        private String ownerName;
        private Integer progress;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDTO {
        private Long taskId;
        private String taskName;
        private Integer status;
        private String statusText;
        private Integer progress;
        private Integer priority;
        private Integer isFocus;
        private String description;
        private List<TaskUpdateDTO> updates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskUpdateDTO {
        private Long updateId;
        private Long taskId;
        private Long employeeId;
        private String employeeName;
        private Integer progress;
        private String description;
        private String updateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTaskDataDTO {
        private List<TaskDTO> focusTasks;
        private List<TaskDTO> highPriorityTasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTaskDataDTO {
        private List<TaskDTO> focusTasks;
        private List<TaskDTO> highPriorityTasks;
        private List<DailyUpdateDTO> dailyUpdates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUpdateDTO {
        private String date;
        private List<TaskUpdateDTO> updates;
    }
}
