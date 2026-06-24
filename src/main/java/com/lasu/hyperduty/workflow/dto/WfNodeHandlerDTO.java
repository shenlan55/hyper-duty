package com.lasu.hyperduty.workflow.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 节点处理人配置 DTO
 *
 * <p>用于前端 BPMN 设计师属性面板与后端交互的数据结构。
 */
@Data
public class WfNodeHandlerDTO implements Serializable {

    /** 主键（更新时必填） */
    private Long id;

    /** 流程定义ID（部署时由后端注入） */
    private String processDefinitionId;

    /** 流程定义KEY（部署时由后端注入） */
    private String processDefinitionKey;

    /** 节点ID（UserTask BPMN id） */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 处理人类型（字典 wf_handler_type） */
    private String handlerType;

    /** 处理人配置 JSON：人员ID/角色ID/变量名/表单字段等 */
    private String handlerConfig;

    /** 多实例类型：none/countersign/or_sign/sequential/ratio */
    private String multiInstanceType;

    /** 多实例配置 JSON：完成条件、比例等 */
    private String multiInstanceConfig;

    /** 抄送配置 JSON：人员、角色、触发时机 */
    private String ccConfig;

    /** 排序 */
    private Integer seq;
}
