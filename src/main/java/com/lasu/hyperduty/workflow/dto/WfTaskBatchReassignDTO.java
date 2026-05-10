package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量转办DTO
 */
@Data
public class WfTaskBatchReassignDTO {

    /**
     * 源用户ID
     */
    private Long fromUserId;

    /**
     * 目标用户ID
     */
    private Long toUserId;

    /**
     * 目标用户姓名
     */
    private String toUserName;

    /**
     * 任务ID列表，不传则转办所有
     */
    private List<String> taskIds;

    /**
     * 转办原因
     */
    private String reason;

}
