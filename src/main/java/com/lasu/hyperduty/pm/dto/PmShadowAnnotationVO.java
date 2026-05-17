package com.lasu.hyperduty.pm.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 影子任务批注 VO
 */
@Data
public class PmShadowAnnotationVO {

    private Long id;

    private Long shadowId;

    private String content;

    private String createdBy;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
