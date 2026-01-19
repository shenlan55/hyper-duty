package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.LoginDTO;
import com.lasu.hyperduty.entity.SysUser;
import com.lasu.hyperduty.service.SysUserService;
import com.lasu.hyperduty.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        // 进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 将认证信息存入SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getUsername, loginDTO.getUsername())
                .one();

        // 生成JWT令牌
        String token = jwtUtil.generateToken(loginDTO.getUsername());

        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", loginDTO.getUsername());
        userInfo.put("token", token);
        if (user != null) {
            userInfo.put("employeeId", user.getEmployeeId());
        }

        return ResponseResult.success("登录成功", userInfo);
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout() {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        return ResponseResult.success();
    }

}