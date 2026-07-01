package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程自由跳转 DTO
 * ----------------------------------------------------------------------------
 * 用于管理员/特权角色把当前 runtime 跳到任意指定节点
 * 与驳回区别：自由跳转需要 processInstanceId + 目标 activityId，驳回是按历史顺序
 * ----------------------------------------------------------------------------
 */
@Data
public class WfTaskJumpDTO implements Serializable {

    /**
     * 当前任务 ID（用于校验任务存在性 + 拿 processInstanceId）
     */
    private String taskId;

    /**
     * 目标 activityId（taskDefinitionKey）
     */
    private String targetActivityId;

    /**
     * 跳转原因（必填，便于审计）
     */
    private String reason;
}
