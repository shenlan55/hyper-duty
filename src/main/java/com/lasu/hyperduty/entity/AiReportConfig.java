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

    /**
     * 温度参数：控制输出随机性，0-1之间，越小越确定
     */
    private Double temperature;

    /**
     * 最大token数：限制输出长度
     */
    private Integer maxTokens;

    /**
     * top_p参数：核采样参数，0-1之间
     */
    private Double topP;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * System Prompt：AI的角色设定
     */
    private String systemPrompt;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
