package com.lasu.hyperduty.pm.dto;

import com.lasu.hyperduty.pm.dto.TaskUpdateDTO;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;








/**
 * 任务更新 DTO
 */
@Data
public class TaskUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    private Long id;

    private Long parentId;

    private String taskName;

    private Integer priority;

    private Integer status;

    private Integer progress;

    private Long assigneeId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String attachments;

    private Integer isFocus;

    private String stakeholders;
}
