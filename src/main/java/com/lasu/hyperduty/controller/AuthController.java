package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.LoginDTO;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.entity.SysMailConfig;
import com.lasu.hyperduty.service.SysMailConfigService;
import com.lasu.hyperduty.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private com.lasu.hyperduty.service.SysEmployeeService sysEmployeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysMailConfigService mailConfigService;

    @PostMapping("/login")
    @RateLimit(window = 60, max = 10, message = "登录尝试过于频繁，请60秒后再试")
    public ResponseResult<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        // 检查是否需要验证码验证
        SysMailConfig mailConfig = mailConfigService.getActiveConfig();
        if (mailConfig != null && mailConfig.getEnableEmailLogin() != null && mailConfig.getEnableEmailLogin() == 1) {
            // 需要验证码
            String code = loginDTO.getCode();
            if (code == null || code.isEmpty()) {
                // 没有验证码，先验证用户名密码是否正确
                SysEmployee employee = sysEmployeeService.lambdaQuery()
                        .eq(SysEmployee::getUsername, loginDTO.getUsername())
                        .one();
                
                if (employee == null) {
                    return ResponseResult.error("用户不存在");
                }
                
                if (!passwordEncoder.matches(loginDTO.getPassword(), employee.getPassword())) {
                    return ResponseResult.error("密码错误");
                }
                
                // 密码正确，需要验证码
                Map<String, Object> result = new HashMap<>();
                result.put("needCode", true);
                return ResponseResult.success("请输入验证码", result);
            }

            // 有验证码，继续验证
            // 获取用户信息用于验证
            SysEmployee employee = sysEmployeeService.lambdaQuery()
                    .eq(SysEmployee::getUsername, loginDTO.getUsername())
                    .one();

            if (employee == null || employee.getEmail() == null) {
                return ResponseResult.error("用户不存在或未配置邮箱");
            }

            // 验证验证码
            boolean codeValid = mailConfigService.verifyCode(employee.getEmail(), code);
            if (!codeValid) {
                return ResponseResult.error("验证码错误或已过期");
            }
        }

        // 进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 将认证信息存入SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息（从SysEmployee表中查询）
        com.lasu.hyperduty.entity.SysEmployee employee = sysEmployeeService.lambdaQuery()
                .eq(com.lasu.hyperduty.entity.SysEmployee::getUsername, loginDTO.getUsername())
                .one();

        // 生成访问令牌和刷新令牌
        String accessToken;
        if (employee != null) {
            accessToken = jwtUtil.generateAccessToken(loginDTO.getUsername(), employee.getId(), employee.getEmployeeName());
        } else {
            accessToken = jwtUtil.generateAccessToken(loginDTO.getUsername());
        }
        String refreshToken = jwtUtil.generateRefreshToken(loginDTO.getUsername());

        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", loginDTO.getUsername());
        userInfo.put("accessToken", accessToken);
        userInfo.put("refreshToken", refreshToken);
        if (employee != null) {
            userInfo.put("employeeId", employee.getId());
            userInfo.put("employeeName", employee.getEmployeeName());
        }

        return ResponseResult.success("登录成功", userInfo);
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        
        // 从请求头中提取访问令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(accessToken);
            jwtUtil.logout(accessToken, username);
        }
        
        return ResponseResult.success();
    }

    @PostMapping("/refresh-token")
    @RateLimit(window = 60, max = 5, message = "令牌刷新过于频繁，请60秒后再试")
    public ResponseResult<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshTokenInfo) {
        String refreshToken = refreshTokenInfo.get("refreshToken");
        String employeeIdStr = refreshTokenInfo.get("employeeId");
        String employeeName = refreshTokenInfo.get("employeeName");
        
        if (refreshToken == null) {
            return ResponseResult.error("刷新令牌不能为空");
        }
        
        try {
            // 生成新的访问令牌
            String newAccessToken;
            if (employeeIdStr != null) {
                Long employeeId = Long.parseLong(employeeIdStr);
                newAccessToken = jwtUtil.refreshAccessToken(refreshToken, employeeId, employeeName);
            } else {
                newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
            }
            
            // 构建返回数据
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("accessToken", newAccessToken);
            
            return ResponseResult.success("令牌刷新成功", tokenInfo);
        } catch (Exception e) {
            return ResponseResult.error("刷新令牌无效或已过期");
        }
    }

    @PostMapping("/change-password")
    @RateLimit(window = 60, max = 3, message = "密码修改过于频繁，请60秒后再试")
    public ResponseResult<String> changePassword(@RequestBody Map<String, String> passwordInfo) {
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.error("用户未登录");
        }

        String username = authentication.getName();
        // 从SysEmployee表中查询用户信息
        com.lasu.hyperduty.entity.SysEmployee employee = sysEmployeeService.lambdaQuery()
                .eq(com.lasu.hyperduty.entity.SysEmployee::getUsername, username)
                .one();

        if (employee == null) {
            return ResponseResult.error("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            return ResponseResult.error("原密码错误");
        }

        // 设置新密码（不加密，由SysEmployeeServiceImpl处理加密）
        employee.setPassword(newPassword);
        employee.setUpdateTime(java.time.LocalDateTime.now());
        boolean success = sysEmployeeService.updateById(employee);

        if (success) {
            return ResponseResult.success("密码修改成功", null);
        } else {
            return ResponseResult.error("密码修改失败");
        }
    }

}