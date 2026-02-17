package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 加班记录实体
 * 存储员工加班的详细信息，包括签到签退时间、加班时长、审批状态等
 */
@Data
@TableName("duty_record")
public class DutyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 值班安排ID
     */
    private Long assignmentId;
    
    /**
     * 员工ID
     */
    private Long employeeId;
    
    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 签退时间
     */
    private LocalDateTime checkOutTime;
    
    /**
     * 值班状态：0-未签到，1-已签到，2-已签退，3-请假
     */
    private Integer dutyStatus;
    
    /**
     * 签到备注
     */
    private String checkInRemark;
    
    /**
     * 签退备注
     */
    private String checkOutRemark;
    
    /**
     * 加班时长（小时）
     */
    private Integer overtimeHours;
    
    /**
     * 审批状态：待审批，已批准，已拒绝
     */
    private String approvalStatus;
    
    /**
     * 管理员备注
     */
    private String managerRemark;

    /**
     * 替补人员ID
     */
    private Long substituteEmployeeId;

    /**
     * 替补类型：1-自动匹配，2-手动选择
     */
    private Integer substituteType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}