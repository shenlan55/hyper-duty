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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private com.lasu.hyperduty.service.SysEmployeeService sysEmployeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
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

        // 生成JWT令牌
        String token;
        if (employee != null) {
            token = jwtUtil.generateToken(loginDTO.getUsername(), employee.getId(), employee.getEmployeeName());
        } else {
            token = jwtUtil.generateToken(loginDTO.getUsername());
        }

        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", loginDTO.getUsername());
        userInfo.put("token", token);
        if (employee != null) {
            userInfo.put("employeeId", employee.getId());
            userInfo.put("employeeName", employee.getEmployeeName());
        }

        return ResponseResult.success("登录成功", userInfo);
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout() {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        return ResponseResult.success();
    }

    @PostMapping("/change-password")
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