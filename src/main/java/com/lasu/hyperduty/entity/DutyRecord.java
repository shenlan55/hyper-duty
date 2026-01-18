package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("duty_record")
public class DutyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long assignmentId;
    
    private Long employeeId;
    
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime;
    
    private Integer dutyStatus;
    
    private String checkInRemark;
    
    private String checkOutRemark;
    
    private Integer overtimeHours;
    
    private String approvalStatus;
    
    private String managerRemark;

    private Long substituteEmployeeId;

    private Integer substituteType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}