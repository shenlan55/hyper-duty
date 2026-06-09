package com.lasu.hyperduty.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 绑定表单请求DTO
 */
@Data
@Schema(description = "绑定表单请求")
public class BindFormDTO {

    @Schema(description = "流程定义KEY")
    private String processKey;

    @Schema(description = "表单ID，null表示解绑")
    private Long formId;
}