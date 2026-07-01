package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务加签/减签 DTO
 * ----------------------------------------------------------------------------
 * 加签 = 把指定 userId 加到 task 的 candidateUsers（多人会签）
 * 减签 = 从 task 的 candidateUsers 移除指定 userId
 * 复用规则：assignee 始终只有一个（当前审批人），candidateUsers 是会签池
 * ----------------------------------------------------------------------------
 */
@Data
public class WfTaskSignDTO implements Serializable {

    /**
     * 当前任务 ID
     */
    private String taskId;

    /**
     * 被加签 / 减签的员工 userId
     */
    private Long userId;

    /**
     * 加签/减签原因（便于审计）
     */
    private String reason;
}
