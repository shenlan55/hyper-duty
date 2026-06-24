package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 流程抄送 DTO
 */
@Data
public class WfCcDTO implements Serializable {

    private Long id;

    /** 流程实例ID */
    private String processInstanceId;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程名称 */
    private String processName;

    /** 节点ID */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 抄送人ID（单条新建时使用） */
    private Long ccUserId;

    /** 抄送人姓名 */
    private String ccUserName;

    /** 抄送人ID列表（批量新建时使用） */
    private List<Long> ccUserIds;

    /** 抄送人姓名列表（批量新建时使用） */
    private List<String> ccUserNames;

    /** 抄送标题 */
    private String title;

    /** 抄送内容 */
    private String content;

    /** 抄送发起人ID */
    private Long fromUserId;

    /** 抄送发起人姓名 */
    private String fromUserName;
}
