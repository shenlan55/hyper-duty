package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.pm.entity.PmTaskComment;
import java.time.LocalDateTime;
import lombok.Data;








/**
 * 任务批注实体类
 */
@Data
@TableName("pm_task_comment")
public class PmTaskComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 评论人ID
     */
    private Long employeeId;

    /**
     * 评论人名称
     */
    private String employeeName;

    /**
     * 批注内容
     */
    private String content;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
