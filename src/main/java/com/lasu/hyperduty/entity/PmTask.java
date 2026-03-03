package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_task")
public class PmTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long parentId;

    private Integer taskLevel;

    private String taskName;

    private String taskCode;

    private Long moduleId;

    private Integer priority;

    private Integer status;

    private Integer progress;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long assigneeId;

    private String description;

    private String attachments;

    private Integer isPinned;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private Integer subTaskCount;

    @TableField(exist = false)
    private Integer completedSubTaskCount;

    @TableField(exist = false)
    private String parentTaskName;

    private String stakeholders;
}
