package com.lasu.hyperduty.pm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建影子任务 DTO
 */
@Data
public class ShadowTaskCreateDTO {

    /**
     * 源任务ID（必须）
     */
    @NotNull(message = "源任务ID不能为空")
    private Long sourceTaskId;

    /**
     * 目标项目ID（必须）
     */
    @NotNull(message = "目标项目ID不能为空")
    private Long projectId;

    /**
     * 在本项目中的父任务ID（可选，0表示根任务）
     */
    private Long parentId;

    /**
     * 影子别名（可选）
     */
    private String shadowAlias;

    /**
     * 影子描述（可选）
     */
    private String shadowDescription;
}
