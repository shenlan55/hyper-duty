package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.util.Map;

/**
 * 任务审批DTO
 */
@Data
public class WfTaskCompleteDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

}
