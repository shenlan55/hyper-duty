package com.lasu.hyperduty.security;

import com.lasu.hyperduty.common.ResponseUtil;
import com.lasu.hyperduty.service.SysUserService;
import com.lasu.hyperduty.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;
    private SysUserService sysUserService;

    // 使用构造函数注入，避免循环依赖
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, SysUserService sysUserService) {
        this.jwtUtil = jwtUtil;
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 从Authorization头中提取JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // JWT解析失败，可能是令牌无效或已过期
                ResponseUtil.sendUnauthorizedResponse(response, "Token invalid or expired");
                return;
            }
        }

        // 如果令牌有效且用户未认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.sysUserService.loadUserByUsername(username);

            try {
                // 验证令牌有效性
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    // 令牌无效，返回401
                    ResponseUtil.sendUnauthorizedResponse(response, "Token invalid");
                    return;
                }
            } catch (Exception e) {
                // 验证失败，返回401
                ResponseUtil.sendUnauthorizedResponse(response, "Token validation failed");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}