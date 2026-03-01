package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.typehandler.PostgreSqlJsonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "pm_task_progress_update", autoResultMap = true)
public class PmTaskProgressUpdate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long employeeId;

    private Integer progress;

    private String description;

    @TableField(typeHandler = PostgreSqlJsonTypeHandler.class)
    private String attachments;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private List<Attachment> attachmentList;

    @Data
    public static class Attachment {
        private String name;
        private String url;
        private String previewUrl;
        private String type;
        private Long size;
    }
}
