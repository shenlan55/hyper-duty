package com.lasu.hyperduty.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程部署DTO
 */
@Data
@Schema(description = "流程部署请求参数")
public class DeployProcessDTO {

    @Schema(description = "流程名称", required = true)
    private String name;

    @Schema(description = "BPMN XML内容", required = true)
    private String bpmnXml;
}