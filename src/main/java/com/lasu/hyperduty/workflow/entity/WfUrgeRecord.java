package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程催办记录
 * ----------------------------------------------------------------------------
 * 记录"谁催办了谁、催什么内容、关联哪个流程实例和任务"
 * 通知推送（站内信/邮件）后续接入 sys_message 模块，本表只做审计/查询
 * ----------------------------------------------------------------------------
 */
@Data
@TableName("wf_urge_record")
public class WfUrgeRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程实例 ID */
    @TableField("process_instance_id")
    private String processInstanceId;

    /** 任务 ID */
    @TableField("task_id")
    private String taskId;

    /** 催办人 userId */
    @TableField("from_user_id")
    private Long fromUserId;

    /** 催办接收人 userId */
    @TableField("to_user_id")
    private Long toUserId;

    /** 催办内容 */
    @TableField("content")
    private String content;

    /** 催办时间 */
    @TableField("create_time")
    private Date createTime;
}
