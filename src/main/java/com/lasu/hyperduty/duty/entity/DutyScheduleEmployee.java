package com.lasu.hyperduty.duty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.duty.entity.DutyScheduleEmployee;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;








@Data
@TableName("duty_schedule_employee")
public class DutyScheduleEmployee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private Long employeeId;

    private Integer isLeader;

    private Integer sortOrder;

    private LocalDateTime createTime;

}
