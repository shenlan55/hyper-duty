package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程模板（P3-1 模板市场）
 * ----------------------------------------------------------------------------
 * 用于"流程模板市场"页面，预置 4-5 个常用模板（请假/报销/出差/通用审批）
 * 用户从模板市场一键"使用此模板新建流程"，加载 BPMN 模板到设计器
 * ----------------------------------------------------------------------------
 */
@Data
@TableName("wf_template")
public class WfTemplate implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板 KEY（唯一标识，对应 BPMN 中 process id 的来源） */
    @TableField("template_key")
    private String templateKey;

    /** 模板名称 */
    @TableField("template_name")
    private String templateName;

    /** 分类：leave/reimburse/trip/general */
    @TableField("category")
    private String category;

    /** 描述 */
    @TableField("description")
    private String description;

    /** BPMN 2.0 XML 模板内容 */
    @TableField("bpmn_xml")
    private String bpmnXml;

    /** 图标（element-plus icon 名） */
    @TableField("icon")
    private String icon;

    /** 使用次数 */
    @TableField("use_count")
    private Integer useCount;

    /** 排序号 */
    @TableField("sort_no")
    private Integer sortNo;

    /** 状态：1启用 0停用 */
    @TableField("status")
    private Integer status;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    private Date updateTime;

    /** 逻辑删除：0未删 1已删 */
    @TableField("deleted")
    private Integer deleted;
}
