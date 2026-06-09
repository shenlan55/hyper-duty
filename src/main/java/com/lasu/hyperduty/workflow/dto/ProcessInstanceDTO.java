package com.lasu.hyperduty.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.Date;

/**
 * 流程实例DTO（避免直接暴露Flowable实体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程实例信息")
public class ProcessInstanceDTO {

    @Schema(description = "流程实例ID")
    private String id;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程定义Key")
    private String processDefinitionKey;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务Key")
    private String businessKey;

    @Schema(description = "流程实例名称")
    private String name;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "开始用户ID")
    private String startUserId;

    @Schema(description = "是否挂起")
    private Boolean suspended;

    @Schema(description = "是否结束")
    private Boolean ended;

    /**
     * 从Flowable ProcessInstance转换
     */
    public static ProcessInstanceDTO fromProcessInstance(ProcessInstance pi) {
        return ProcessInstanceDTO.builder()
                .id(pi.getId())
                .processDefinitionId(pi.getProcessDefinitionId())
                .processDefinitionKey(pi.getProcessDefinitionKey())
                .processDefinitionName(pi.getProcessDefinitionName())
                .businessKey(pi.getBusinessKey())
                .name(pi.getName())
                .startTime(pi.getStartTime())
                .startUserId(pi.getStartUserId())
                .suspended(pi.isSuspended())
                .ended(pi.isEnded())
                .build();
    }
}