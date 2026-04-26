package com.lasu.hyperduty.common.dto;

import com.lasu.hyperduty.common.dto.LoginDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;







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