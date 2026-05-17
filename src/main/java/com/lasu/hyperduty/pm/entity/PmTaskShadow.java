package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 影子任务关联表 (v2)
 * 设计理念：影子是源任务在项目中的"视图"
 */
@Data
@TableName("pm_task_shadow")
public class PmTaskShadow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 源任务ID
     */
    private Long sourceTaskId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 在本项目中的别名（可选，不填显示源任务名称）
     */
    private String shadowAlias;

    /**
     * 本项目的描述（可选）
     */
    private String shadowDescription;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ========================================
    // 非数据库字段
    // ========================================

    @TableField(exist = false)
    private PmTask sourceTask;

    @TableField(exist = false)
    private String sourceTaskName;

    @TableField(exist = false)
    private String sourceProjectName;

    @TableField(exist = false)
    private Integer annotationCount;
}
