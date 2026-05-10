package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程定义扩展实体
 */
@Data
@TableName("wf_definition")
public class WfDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processDefinitionId;

    private String processDefinitionKey;

    private String processName;

    private Long categoryId;

    private Long formId;

    private Integer version;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
