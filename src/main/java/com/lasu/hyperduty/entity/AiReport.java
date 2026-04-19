package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_report")
public class AiReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportTitle;

    private String reportType;

    private LocalDate reportDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long projectId;

    private String projectName;

    private String reportContent;

    private Long configId;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
