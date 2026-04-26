package com.lasu.hyperduty.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.ai.entity.AiReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;









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
