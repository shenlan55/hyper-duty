package com.lasu.hyperduty.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String password;
    private Long employeeId;
    private String employeeName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
