package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * 历史任务DTO
 */
@Data
public class HistoricTaskDTO {
    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务定义ID
     */
    private String taskDefinitionKey;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 办理人
     */
    private String assignee;

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
     * 任务删除原因
     */
    private String deleteReason;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 任务到期时间
     */
    private Date dueDate;

    /**
     * 任务表单Key
     */
    private String formKey;
}
