package com.lasu.hyperduty.pm.dto;

import lombok.Data;

/**
 * 更新影子任务 DTO
 */
@Data
public class ShadowTaskUpdateDTO {

    /**
     * 影子别名（可选）
     */
    private String shadowAlias;

    /**
     * 影子描述（可选）
     */
    private String shadowDescription;
}
