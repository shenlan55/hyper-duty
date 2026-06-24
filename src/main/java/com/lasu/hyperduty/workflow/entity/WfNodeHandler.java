package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程节点处理人配置
 *
 * <p>在 BPMN 设计师中为 UserTask 节点配置处理人类型（指定人员/候选人/角色/发起人/部门负责人/表单字段/变量 等），
 * 以及多实例（会签/或签/顺序会签/比例通过）和抄送配置。流程部署时持久化，便于后续发起和展示使用。
 */
@Data
@TableName("wf_node_handler")
public class WfNodeHandler {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程定义ID（Flowable processDefinitionId，如 leave:1:50004） */
    private String processDefinitionId;

    /** 流程定义KEY（如 leave） */
    private String processDefinitionKey;

    /** 节点ID（UserTask BPMN id） */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 处理人类型（字典 wf_handler_type）：ASSIGNEE/CANDIDATE_USERS/CANDIDATE_GROUPS/INITIATOR/DEPT_LEADER/ROLE_LEADER/PREV_ASSIGNEE/FORM_FIELD/VARIABLE */
    private String handlerType;

    /** 处理人配置 JSON：人员ID/角色ID/变量名/表单字段等 */
    private String handlerConfig;

    /** 多实例类型：none/parallel/sequential/countersign/or_sign/ratio */
    private String multiInstanceType;

    /** 多实例配置 JSON：完成条件、比例等 */
    private String multiInstanceConfig;

    /** 抄送配置 JSON：人员、角色、触发时机 */
    private String ccConfig;

    /** 排序 */
    private Integer seq;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
