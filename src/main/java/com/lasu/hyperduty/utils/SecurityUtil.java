package com.lasu.hyperduty.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 * 提供获取当前登录用户等安全相关操作
 */
@Component
public class SecurityUtil {

    private static SysEmployeeService sysEmployeeService;

    /**
     * 注入SysEmployeeService
     * 静态注入，确保静态方法能够使用
     */
    @Autowired
    public void setSysEmployeeService(SysEmployeeService sysEmployeeService) {
        SecurityUtil.sysEmployeeService = sysEmployeeService;
    }

    /**
     * 获取当前登录用户
     * @return 当前用户信息，如果未登录则返回null
     */
    public static SysEmployee getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String username = authentication.getName();
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getUsername, username);
        return sysEmployeeService.getOne(wrapper);
    }

    /**
     * 获取当前登录用户ID
     * @return 当前用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        SysEmployee user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     * @return 当前用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 判断用户是否已登录
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
