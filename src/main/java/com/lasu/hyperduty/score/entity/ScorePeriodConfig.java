package com.lasu.hyperduty.score.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评选周期配置实体
 */
@Data
@TableName("score_period_config")
public class ScorePeriodConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 周期类型：QUARTERLY=季度，YEARLY=年度 */
    private String periodType;

    /** 年份 */
    private Integer periodYear;

    /** 周期序号：季度(1-4)，年度固定1 */
    private Integer periodIndex;

    /** 积分权重 */
    private BigDecimal pointWeight;

    /** 工时权重 */
    private BigDecimal hourWeight;

    /** 状态：0=未开始，1=进行中，2=已结束 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}