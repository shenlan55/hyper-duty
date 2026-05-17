package com.lasu.hyperduty.pm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建影子批注 DTO
 */
@Data
public class ShadowAnnotationCreateDTO {

    /**
     * 影子任务ID（必须）
     */
    @NotNull(message = "影子任务ID不能为空")
    private Long shadowId;

    /**
     * 批注内容（必须）
     */
    @NotBlank(message = "批注内容不能为空")
    private String content;
}
