package com.lasu.hyperduty.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程实例扩展实体
 */
@Data
@TableName("wf_instance")
public class WfInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processInstanceId;

    private String processDefinitionId;

    private String processName;

    private String businessKey;

    private String businessData;

    private Long startUserId;

    private String startUserName;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime endTime;

    /** 撤回人ID */
    private Long withdrawUserId;

    /** 撤回人姓名 */
    private String withdrawUserName;

    /** 撤回时间 */
    private LocalDateTime withdrawTime;

    /** 撤回原因 */
    private String withdrawReason;

}
