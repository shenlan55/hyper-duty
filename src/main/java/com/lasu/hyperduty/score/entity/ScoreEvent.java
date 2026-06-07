package com.lasu.hyperduty.score.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分事件定义实体
 */
@Data
@TableName("score_event")
public class ScoreEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 事件名称 */
    private String eventName;

    /** 事件类型：1=加分，2=扣分 */
    private Integer eventType;

    /** 默认分值 */
    private Integer defaultScore;

    /** 分类 */
    private String category;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}