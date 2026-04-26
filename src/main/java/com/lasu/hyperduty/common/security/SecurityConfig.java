package com.lasu.hyperduty.common.security;

import com.lasu.hyperduty.duty.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;







@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    // 从环境变量读取允许的源
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
    private String corsAllowedOrigins;

    // 使用构造函数注入
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Autowired
    private EmployeeUserDetailsService employeeUserDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(employeeUserDetailsService).passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 添加CORS配置
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
                    // 从环境变量读取允许的源并添加
                    for (String origin : corsAllowedOrigins.split(",")) {
                        if (origin.trim().equals("*")) {
                            // 使用allowedOriginPatterns支持通配符
                            configuration.addAllowedOriginPattern("*");
                        } else {
                            configuration.addAllowedOrigin(origin.trim());
                        }
                    }
                    configuration.addAllowedMethod("*");
                    configuration.addAllowedHeader("*");
                    configuration.setAllowCredentials(true);
                    return configuration;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // 认证接口放行
                    .requestMatchers("/auth/login", "/auth/logout", "/auth/refresh-token").permitAll()
                    // 邮件配置相关接口（获取配置和发送验证码）放行
                    .requestMatchers("/mail-config/current", "/mail-config/send-code").permitAll()
                    // Swagger文档放行
                    .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // Druid监控放行
                    .requestMatchers("/druid/**").permitAll()
                    // 文件相关接口放行
                    .requestMatchers("/file/**").permitAll()
                    // 只保护需要认证的接口
                    .requestMatchers("/employee/**", "/system/**", "/duty/**", "/pm/**", "/notification/**", "/export/**", "/operation-log/**").authenticated()
                    // 其他所有请求都放行（包括前端静态资源和SPA路由）
                    .anyRequest().permitAll())
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}