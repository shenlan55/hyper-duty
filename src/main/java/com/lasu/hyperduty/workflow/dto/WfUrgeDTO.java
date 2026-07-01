package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 催办 DTO
 * ----------------------------------------------------------------------------
 * 语义：当前审批人 / 发起人 / 管理员 给"待办人"发催办通知
 * - taskId        必填
 * - toUserId      必填（接收催办人）
 * - content       必填（催办内容）
 * ----------------------------------------------------------------------------
 */
@Data
public class WfUrgeDTO implements Serializable {

    /**
     * 当前任务 ID
     */
    private String taskId;

    /**
     * 催办接收人 userId（多个逗号分隔暂不支持，避免 SQL 复杂度）
     */
    private Long toUserId;

    /**
     * 催办内容
     */
    private String content;
}
