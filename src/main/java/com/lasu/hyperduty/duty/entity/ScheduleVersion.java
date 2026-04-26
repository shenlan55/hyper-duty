package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.ScheduleVersion;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;








@Data
@TableName("schedule_version")
public class ScheduleVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private String versionName;

    private String versionCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private Integer isCurrent;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime publishTime;

}
