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

    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;

    // 使用构造函数注入
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, SysUserService sysUserService) {
        this.jwtUtil = jwtUtil;
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // 获取请求路径
        String requestPath = request.getServletPath();
        
        // 对于不需要认证的路径，直接放行
        if (requestPath.startsWith("/auth/login") || 
            requestPath.startsWith("/auth/logout") ||
            requestPath.startsWith("/doc.html") ||
            requestPath.startsWith("/swagger-ui/") ||
            requestPath.startsWith("/v3/api-docs/") ||
            requestPath.startsWith("/druid/") ||
            requestPath.startsWith("/static/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // 没有令牌，返回401
            ResponseUtil.sendUnauthorizedResponse(response, "Token not provided");
            return;
        }

        String jwt = authorizationHeader.substring(7);
        String username = null;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // JWT解析失败，可能是令牌无效或已过期
            ResponseUtil.sendUnauthorizedResponse(response, "Token invalid or expired");
            return;
        }

        if (username == null) {
            // 用户名为空，返回401
            ResponseUtil.sendUnauthorizedResponse(response, "Token invalid");
            return;
        }

        // 如果用户未认证
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
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