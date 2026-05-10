package com.lasu.hyperduty.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * 流程定义DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程定义信息")
public class ProcessDefinitionDTO {

    @Schema(description = "流程定义ID")
    private String id;

    @Schema(description = "流程定义名称")
    private String name;

    @Schema(description = "流程定义Key")
    private String key;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "是否挂起")
    private Boolean suspended;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "表单ID")
    private Long formId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "备注")
    private String remark;

    /**
     * 从Flowable ProcessDefinition实体转换
     */
    public static ProcessDefinitionDTO fromProcessDefinition(ProcessDefinition processDefinition) {
        return ProcessDefinitionDTO.builder()
                .id(processDefinition.getId())
                .name(processDefinition.getName())
                .key(processDefinition.getKey())
                .version(processDefinition.getVersion())
                .category(processDefinition.getCategory())
                .deploymentId(processDefinition.getDeploymentId())
                .resourceName(processDefinition.getResourceName())
                .suspended(processDefinition.isSuspended())
                .description(processDefinition.getDescription())
                .tenantId(processDefinition.getTenantId())
                .build();
    }

    /**
     * 从Flowable ProcessDefinition实体转换（带扩展信息）
     */
    public static ProcessDefinitionDTO fromProcessDefinition(ProcessDefinition processDefinition, Long formId, Long categoryId, String remark) {
        return ProcessDefinitionDTO.builder()
                .id(processDefinition.getId())
                .name(processDefinition.getName())
                .key(processDefinition.getKey())
                .version(processDefinition.getVersion())
                .category(processDefinition.getCategory())
                .deploymentId(processDefinition.getDeploymentId())
                .resourceName(processDefinition.getResourceName())
                .suspended(processDefinition.isSuspended())
                .description(processDefinition.getDescription())
                .tenantId(processDefinition.getTenantId())
                .formId(formId)
                .categoryId(categoryId)
                .remark(remark)
                .build();
    }
}
