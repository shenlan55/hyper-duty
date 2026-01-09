package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_employee")
public class SysEmployee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String employeeName;

    private Long deptId;

    private String employeeCode;

    private String phone;

    private String email;

    private Integer gender;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}