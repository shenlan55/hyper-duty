package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import org.flowable.engine.history.HistoricProcessInstance;

import java.util.Date;

/**
 * 历史流程实例DTO
 */
@Data
public class HistoricProcessInstanceDTO {
    /**
     * 流程实例ID
     */
    private String id;

    /**
     * 业务Key
     */
    private String businessKey;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 流程定义版本
     */
    private Integer processDefinitionVersion;

    /**
     * 部署ID
     */
    private String deploymentId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 持续时间
     */
    private Long durationInMillis;

    /**
     * 发起人
     */
    private String startUserId;

    /**
     * 开始活动ID
     */
    private String startActivityId;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 超级流程实例ID
     */
    private String superProcessInstanceId;

    /**
     * 从Flowable HistoricProcessInstance实体转换
     */
    public static HistoricProcessInstanceDTO fromHistoricProcessInstance(HistoricProcessInstance hpi) {
        HistoricProcessInstanceDTO dto = new HistoricProcessInstanceDTO();
        dto.setId(hpi.getId());
        dto.setBusinessKey(hpi.getBusinessKey());
        dto.setProcessDefinitionId(hpi.getProcessDefinitionId());
        dto.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
        dto.setProcessDefinitionName(hpi.getProcessDefinitionName());
        dto.setProcessDefinitionVersion(hpi.getProcessDefinitionVersion());
        dto.setDeploymentId(hpi.getDeploymentId());
        dto.setStartTime(hpi.getStartTime());
        dto.setEndTime(hpi.getEndTime());
        dto.setDurationInMillis(hpi.getDurationInMillis());
        dto.setStartUserId(hpi.getStartUserId());
        dto.setStartActivityId(hpi.getStartActivityId());
        dto.setDeleteReason(hpi.getDeleteReason());
        dto.setSuperProcessInstanceId(hpi.getSuperProcessInstanceId());
        return dto;
    }
}
