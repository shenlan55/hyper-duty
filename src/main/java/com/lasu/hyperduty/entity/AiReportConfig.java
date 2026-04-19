package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_report_config")
public class AiReportConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configName;

    private String configCode;

    private String reportType;

    private String promptTemplate;

    private String modelName;

    private String modelConfig;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
