package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

/**
 * 任务转办DTO
 */
@Data
public class WfTaskReassignDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 目标用户ID
     */
    private Long userId;

    /**
     * 目标用户姓名
     */
    private String userName;

    /**
     * 转办原因
     */
    private String reason;

}
