package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pm_project")
public class PmProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectName;

    private String projectCode;

    private Long moduleId;

    private Integer priority;

    private Integer status;

    private Integer progress;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Long ownerId;

    private Long createBy;

    private Integer archived;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private Integer taskCount;

    @TableField(exist = false)
    private Integer completedTaskCount;

    @TableField(exist = false)
    private List<Long> participants;
}
