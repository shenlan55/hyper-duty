package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程抄送实体
 *
 * <p>由 BPMN 设计师中节点配置的抄送规则触发；或任务办理时手动选择抄送。
 * 抄送人可以在「抄送我的」页面查看流程信息和审批记录，但不能审批。
 */
@Data
@TableName("wf_cc")
public class WfCc {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程实例ID */
    private String processInstanceId;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程名称 */
    private String processName;

    /** 触发抄送的节点ID */
    private String nodeId;

    /** 触发抄送的节点名称 */
    private String nodeName;

    /** 抄送人ID */
    private Long ccUserId;

    /** 抄送人姓名 */
    private String ccUserName;

    /** 抄送标题 */
    private String title;

    /** 抄送内容/意见 */
    private String content;

    /** 抄送发起人ID */
    private Long fromUserId;

    /** 抄送发起人姓名 */
    private String fromUserName;

    /** 阅读状态：0=未读 1=已读 */
    private Integer readStatus;

    /** 阅读时间 */
    private LocalDateTime readTime;

    private LocalDateTime createTime;
}
