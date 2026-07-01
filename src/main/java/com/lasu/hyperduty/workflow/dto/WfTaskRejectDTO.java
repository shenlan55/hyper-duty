package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务驳回 DTO
 * ----------------------------------------------------------------------------
 * 驳回模式：
 *   PREVIOUS  → 驳回到上一 UserTask（按历史顺序自动找）
 *   ACTIVITY  → 驳回到指定 activityId（前置活动）
 *   INITIATOR → 驳回到发起人，runtime 跳回 startActivityId
 * ----------------------------------------------------------------------------
 * 字段校验由 Service 层 validateRejectTask 统一处理（项目内其他 DTO 也无 @NotBlank 注解）
 */
@Data
public class WfTaskRejectDTO implements Serializable {

    /**
     * 当前任务 ID
     */
    private String taskId;

    /**
     * 驳回类型：PREVIOUS / ACTIVITY / INITIATOR
     */
    private String rejectType;

    /**
     * 目标节点 activityId（仅 ACTIVITY 模式必填）
     */
    private String targetActivityId;

    /**
     * 驳回原因（必填，便于审计）
     */
    private String reason;
}
