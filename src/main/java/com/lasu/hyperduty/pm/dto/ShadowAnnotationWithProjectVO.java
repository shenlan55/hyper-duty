package com.lasu.hyperduty.pm.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 影子任务批注（包含影子项目信息）VO
 */
@Data
public class ShadowAnnotationWithProjectVO {

    private Long id;

    private Long shadowId;

    private String shadowAlias;

    private String targetProjectName;

    private String content;

    private String createdBy;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
