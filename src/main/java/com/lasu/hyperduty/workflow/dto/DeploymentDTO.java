package com.lasu.hyperduty.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.engine.repository.Deployment;

import java.util.Date;

/**
 * 流程部署DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程部署信息")
public class DeploymentDTO {

    @Schema(description = "部署ID")
    private String id;

    @Schema(description = "部署名称")
    private String name;

    @Schema(description = "部署时间")
    private Date deploymentTime;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 从Flowable Deployment实体转换
     */
    public static DeploymentDTO fromDeployment(Deployment deployment) {
        return DeploymentDTO.builder()
                .id(deployment.getId())
                .name(deployment.getName())
                .deploymentTime(deployment.getDeploymentTime())
                .category(deployment.getCategory())
                .tenantId(deployment.getTenantId())
                .build();
    }
}
