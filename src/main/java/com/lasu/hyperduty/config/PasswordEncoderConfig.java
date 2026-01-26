package com.lasu.hyperduty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类
 * 将PasswordEncoder的Bean定义移到单独的配置类中，避免循环依赖
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 配置BCrypt密码编码器
     * @return PasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}