package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutyShiftConfig;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;









@Data
@TableName("duty_shift_config")
public class DutyShiftConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String shiftName;

    private String shiftCode;

    private Integer shiftType;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer isCrossDay;

    private BigDecimal durationHours;

    private BigDecimal breakHours;

    private String restDayRule;

    private Integer isOvertimeShift;

    private Integer status;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
