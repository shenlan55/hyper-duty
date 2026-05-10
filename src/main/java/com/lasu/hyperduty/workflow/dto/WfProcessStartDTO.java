package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.util.Map;

/**
 * 流程启动DTO
 */
@Data
public class WfProcessStartDTO {

    /**
     * 流程定义KEY
     */
    private String processDefinitionKey;

    /**
     * 业务KEY
     */
    private String businessKey;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 业务数据
     */
    private String businessData;

}
