package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * 驳回目标节点 DTO
 * ----------------------------------------------------------------------------
 * 用于前端下拉显示"可驳回到的历史 UserTask"：
 *   - activityId    节点定义 Key（用于 changeActivityState）
 *   - activityName  节点中文名（前端展示）
 *   - assigneeName  原审批人姓名（前端展示）
 *   - endTime       结束时间（前端按时间倒序）
 * ----------------------------------------------------------------------------
 */
@Data
public class RejectTargetDTO {

    /** 节点定义 Key（taskDefinitionKey） */
    private String activityId;

    /** 节点名称 */
    private String activityName;

    /** 原审批人工号（assignee） */
    private String assignee;

    /** 原审批人姓名（系统注入，前端直接展示） */
    private String assigneeName;

    /** 该历史任务结束时间 */
    private Date endTime;

    /** 历时毫秒 */
    private Long durationInMillis;
}
