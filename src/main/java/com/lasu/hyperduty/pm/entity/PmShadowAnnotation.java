package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 影子任务批注表 (v2)
 */
@Data
@TableName("pm_shadow_annotation")
public class PmShadowAnnotation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 影子任务关联ID
     */
    private Long shadowId;

    /**
     * 批注内容
     */
    private String content;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
