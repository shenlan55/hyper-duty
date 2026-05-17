package com.lasu.hyperduty.pm.dto;

import lombok.Data;

/**
 * 更新影子任务 DTO
 */
@Data
public class ShadowTaskUpdateDTO {

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
