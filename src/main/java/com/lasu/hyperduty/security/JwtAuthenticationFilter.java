package com.lasu.hyperduty.security;

import com.lasu.hyperduty.common.ResponseUtil;
import com.lasu.hyperduty.security.EmployeeUserDetailsService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器
 * 用于验证请求中的 JWT 令牌并进行身份认证
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final EmployeeUserDetailsService employeeUserDetailsService;
    private final AntPathMatcher pathMatcher;
    
    // 不需要认证的路径列表
    private final List<String> excludePaths;

    // 使用构造函数注入
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, EmployeeUserDetailsService employeeUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.employeeUserDetailsService = employeeUserDetailsService;
        this.pathMatcher = new AntPathMatcher();
        
        // 初始化不需要认证的路径列表
        this.excludePaths = new ArrayList<>();
        this.excludePaths.add("/auth/login");
        this.excludePaths.add("/auth/logout");
        this.excludePaths.add("/auth/refresh-token");
        this.excludePaths.add("/doc.html");
        this.excludePaths.add("/swagger-ui/**");
        this.excludePaths.add("/v3/api-docs/**");
        this.excludePaths.add("/druid/**");
        this.excludePaths.add("/static/**");
        this.excludePaths.add("/file/preview");
        this.excludePaths.add("/file/download");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // 获取请求路径并处理前缀
        String requestPath = getProcessedRequestPath(request);
        String originalPath = request.getServletPath();
        String fullUrl = request.getRequestURL().toString();
        
        log.debug("请求URL: {}, ServletPath: {}, 处理后路径: {}", fullUrl, originalPath, requestPath);
        
        // 对于不需要认证的路径，直接放行
        if (isExcludePathWithFallback(requestPath, originalPath)) {
            log.debug("路径匹配白名单，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        // 提取并验证 JWT 令牌
        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            ResponseUtil.sendUnauthorizedResponse(response, "Token not provided");
            return;
        }

        // 解析并验证 JWT 令牌
        String username = parseAndValidateJwt(jwt, response);
        if (username == null) {
            return;
        }

        // 进行身份认证
        authenticateUser(username, jwt, request, response);

        filterChain.doFilter(request, response);
    }

    /**
     * 获取处理后的请求路径
     * @param request HTTP请求对象
     * @return 处理后的请求路径
     */
    private String getProcessedRequestPath(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        // 移除/api前缀，统一处理路径
        return requestPath.replace("/api", "");
    }

    /**
     * 判断路径是否为不需要认证的路径
     * @param requestPath 请求路径
     * @return 是否为不需要认证的路径
     */
    private boolean isExcludePath(String requestPath) {
        for (String excludePath : excludePaths) {
            if (pathMatcher.match(excludePath, requestPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断路径是否为不需要认证的路径（同时检查原始路径）
     * @param requestPath 处理后的请求路径
     * @param originalPath 原始请求路径
     * @return 是否为不需要认证的路径
     */
    private boolean isExcludePathWithFallback(String requestPath, String originalPath) {
        for (String excludePath : excludePaths) {
            if (pathMatcher.match(excludePath, requestPath)) {
                log.debug("匹配到白名单路径（处理后）: {} -> {}", requestPath, excludePath);
                return true;
            }
            if (pathMatcher.match(excludePath, originalPath)) {
                log.debug("匹配到白名单路径（原始）: {} -> {}", originalPath, excludePath);
                return true;
            }
            if (pathMatcher.match("/api" + excludePath, originalPath)) {
                log.debug("匹配到白名单路径（带/api前缀）: {} -> /api{}", originalPath, excludePath);
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中提取 JWT 令牌
     * @param request HTTP请求对象
     * @return JWT 令牌，如果不存在则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 解析并验证 JWT 令牌
     * @param jwt JWT 令牌
     * @param response HTTP响应对象
     * @return 用户名，如果验证失败则返回null
     */
    private String parseAndValidateJwt(String jwt, HttpServletResponse response) {
        try {
            String username = jwtUtil.extractUsername(jwt);
            if (username == null) {
                ResponseUtil.sendUnauthorizedResponse(response, "Token invalid");
                return null;
            }
            return username;
        } catch (Exception e) {
            // JWT解析失败，可能是令牌无效或已过期
            ResponseUtil.sendUnauthorizedResponse(response, "Token invalid or expired");
            return null;
        }
    }

    /**
     * 对用户进行身份认证
     * @param username 用户名
     * @param jwt JWT 令牌
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    private void authenticateUser(String username, String jwt, HttpServletRequest request, HttpServletResponse response) {
        // 如果用户未认证
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.employeeUserDetailsService.loadUserByUsername(username);
                
                // 验证令牌有效性
                if (jwtUtil.validateAccessToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authenticationToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    // 令牌无效，返回401
                    ResponseUtil.sendUnauthorizedResponse(response, "Token invalid");
                }
            } catch (Exception e) {
                // 验证失败，返回401
                ResponseUtil.sendUnauthorizedResponse(response, "Token validation failed");
            }
        }
    }

}