package com.lasu.hyperduty.dto.pm;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

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
