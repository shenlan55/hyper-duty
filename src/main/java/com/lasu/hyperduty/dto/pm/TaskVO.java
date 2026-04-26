package com.lasu.hyperduty.dto.pm;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务 VO 对象
 */
@Data
public class TaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long projectId;

    private Long parentId;

    private String taskName;

    private String taskCode;

    private Integer priority;

    private Integer status;

    private Integer progress;

    private Long assigneeId;

    private String ownerName;

    private String projectName;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String attachments;

    private Integer isPinned;

    private Integer isFocus;

    private String stakeholders;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastProgressUpdateTime;

    private Boolean hasPermission;

    private Boolean hasDeletePermission;
}
