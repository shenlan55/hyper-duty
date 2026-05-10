package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 委托配置DTO
 */
@Data
public class WfDelegateConfigDTO {

    /**
     * 委托人ID
     */
    private Long delegatorId;

    /**
     * 代理人ID
     */
    private Long attorneyId;

    /**
     * 代理人姓名
     */
    private String attorneyName;

    /**
     * 流程定义KEY，空表示全部
     */
    private String processDefinitionKey;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;

}
