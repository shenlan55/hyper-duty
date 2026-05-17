package com.lasu.hyperduty.pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 影子任务 VO - 用于返回给前端
 * 设计理念：影子任务也是"任务"，与真实任务在同一个列表中
 */
@Data
public class ShadowTaskVO {

    // ========================================
    // 标识和类型
    // ========================================
    private Long id;              // 影子ID（如果是影子，taskId 为 null）
    private Long taskId;          // 真实任务ID（如果是真实任务，shadowId 为 null）
    private String taskType;      // "task" 或 "shadow"
    private Integer isShadow;     // 0-真实任务，1-影子任务

    // ========================================
    // 核心字段（来自源任务，如果是影子）
    // ========================================
    private Long sourceTaskId;    // 源任务ID（影子才有）
    private Long sourceProjectId; // 源项目ID（影子才有）
    private String sourceTaskName; // 源任务名称（影子才有）
    private String sourceProjectName; // 源项目名称（影子才有）
    private String shadowAlias;   // 影子别名（影子才有）
    private String shadowDescription; // 影子描述（影子才有）
    private String taskName;      // 显示名称（COALESCE(shadowAlias, sourceTaskName)）
    private String description;   // 显示描述
    private Integer progress;
    private Integer status;
    private Integer priority;
    private LocalDate startDate;
    private LocalDate endDate;    // 结束日期
    private LocalDate dueDate;    // 截止日期（同结束日期）
    private Long assignee;        // 负责人ID
    private Long ownerId;         // 负责人ID（同 assignee）
    private String assigneeName;  // 负责人名称
    private String ownerName;     // 负责人名称（同 assigneeName）

    // ========================================
    // 项目字段
    // ========================================
    private Long projectId;       // 当前项目ID
    private String projectName;   // 当前项目名称

    // ========================================
    // 任务字段
    // ========================================
    private Integer isPinned;     // 是否置顶
    private Integer isFocus;      // 是否重点
    private Long parentId;        // 父任务ID
    private String taskCode;      // 任务编码
    private String taskModule;    // 任务模块
    private String attachments;   // 附件
    private String stakeholders;  // 干系人
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime lastProgressUpdateTime; // 最后进度更新时间

    // ========================================
    // 权限字段
    // ========================================
    private Boolean hasPermission;    // 是否有编辑权限
    private Boolean hasDeletePermission; // 是否有删除权限

    // ========================================
    // 元数据
    // ========================================
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========================================
    // 影子特有
    // ========================================
    private Integer annotationCount; // 批注数量
}
