package com.lasu.hyperduty.system.controller;

import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.entity.SysMailConfig;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import com.lasu.hyperduty.system.service.SysMailConfigService;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;








/**
 * 邮件服务配置Controller
 */
@RestController
@RequestMapping("/mail-config")
public class SysMailConfigController {

    @Autowired
    private SysMailConfigService mailConfigService;

    @Autowired
    private SysEmployeeService employeeService;

    /**
     * 获取当前邮件配置
     */
    @GetMapping("/current")
    public ResponseResult<SysMailConfig> getCurrentConfig() {
        SysMailConfig config = mailConfigService.getActiveConfig();
        if (config != null) {
            // 不返回密码
            config.setAuthPassword(null);
        }
        return ResponseResult.success(config);
    }

    /**
     * 保存或更新邮件配置
     */
    @PostMapping("/save")
    public ResponseResult<String> saveConfig(@RequestBody SysMailConfig config) {
        boolean success = mailConfigService.saveOrUpdateConfig(config);
        if (success) {
            return ResponseResult.success("保存成功");
        } else {
            return ResponseResult.error("保存失败");
        }
    }

    /**
     * 发送登录验证码
     */
    @PostMapping("/send-code")
    @RateLimit(window = 60, max = 3, message = "验证码发送过于频繁，请60秒后再试")
    public ResponseResult<Map<String, Object>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        // 根据用户名查询用户邮箱
        SysEmployee employee = employeeService.lambdaQuery()
                .eq(SysEmployee::getUsername, username)
                .one();

        if (employee == null || employee.getEmail() == null || employee.getEmail().isEmpty()) {
            return ResponseResult.error("用户不存在或未配置邮箱");
        }

        // 检查是否启用了邮件验证码登录
        SysMailConfig config = mailConfigService.getActiveConfig();
        if (config == null || config.getEnableEmailLogin() == null || config.getEnableEmailLogin() != 1) {
            return ResponseResult.error("邮件验证码登录未启用");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        boolean sent = mailConfigService.sendVerificationCode(employee.getEmail(), code);

        Map<String, Object> result = new HashMap<>();
        result.put("sent", sent);
        result.put("email", maskEmail(employee.getEmail()));

        if (sent) {
            return ResponseResult.success("验证码已发送", result);
        } else {
            return ResponseResult.error("验证码发送失败", result);
        }
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.substring(0, 2) + "***@" + domain;
    }

    /**
     * 测试邮件连接
     */
    @PostMapping("/test-connection")
    public ResponseResult<String> testConnection(@RequestBody SysMailConfig config) {
        boolean success = mailConfigService.testConnection(config);
        if (success) {
            return ResponseResult.success("邮件连接测试成功");
        } else {
            return ResponseResult.error("邮件连接测试失败");
        }
    }
}
