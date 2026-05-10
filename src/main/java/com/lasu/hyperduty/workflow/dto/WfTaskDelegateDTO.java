package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务委托DTO
 */
@Data
public class WfTaskDelegateDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 代理人ID
     */
    private Long userId;

    /**
     * 代理人姓名
     */
    private String userName;

    /**
     * 委托原因
     */
    private String reason;

}
