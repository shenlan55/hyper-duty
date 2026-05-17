package com.lasu.hyperduty.pm.dto;

import lombok.Data;

/**
 * 查询影子任务DTO
 */
@Data
public class ShadowTaskQueryDTO {

    private Long targetProjectId;

    private Long sourceTaskId;

    private Long sourceProjectId;

    private Long createBy;
}
