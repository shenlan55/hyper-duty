package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 委托代理实体
 */
@Data
@TableName("wf_delegate")
public class WfDelegate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long delegatorId;

    private String delegatorName;

    private Long attorneyId;

    private String attorneyName;

    private String processDefinitionKey;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
