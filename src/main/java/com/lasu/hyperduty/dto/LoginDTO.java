package com.lasu.hyperduty.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码（可选，根据配置决定是否需要）
     */
    private String code;

}