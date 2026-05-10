package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程表单实体
 */
@Data
@TableName("wf_form")
public class WfForm {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String formType;

    private Integer status;

    private String formConfig;

    private String formContent;

    private Integer version;

    private String remark;

    private Long createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
