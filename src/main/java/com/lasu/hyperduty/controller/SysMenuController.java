package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysMenu;
import com.lasu.hyperduty.service.SysMenuService;
import com.lasu.hyperduty.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有菜单列表（树形结构）
     * @return 菜单列表
     */
    @GetMapping("/list")
    public ResponseResult<List<SysMenu>> getAllMenus() {
        List<SysMenu> menus = sysMenuService.getAllMenus();
        return ResponseResult.success(menus);
    }

    /**
     * 获取菜单详情
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    public ResponseResult<SysMenu> getMenuById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        return ResponseResult.success(menu);
    }

    /**
     * 保存菜单
     * @param sysMenu 菜单信息
     * @return 是否保存成功
     */
    @PostMapping
    public ResponseResult<Boolean> saveMenu(@RequestBody SysMenu sysMenu) {
        boolean result = sysMenuService.saveMenu(sysMenu);
        return ResponseResult.success(result);
    }

    /**
     * 更新菜单
     * @param sysMenu 菜单信息
     * @return 是否更新成功
     */
    @PutMapping
    public ResponseResult<Boolean> updateMenu(@RequestBody SysMenu sysMenu) {
        boolean result = sysMenuService.updateMenu(sysMenu);
        return ResponseResult.success(result);
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> deleteMenu(@PathVariable Long id) {
        boolean result = sysMenuService.deleteMenu(id);
        return ResponseResult.success(result);
    }

    /**
     * 获取菜单树形结构（用于选择父菜单）
     * @return 菜单树形结构
     */
    @GetMapping("/tree")
    public ResponseResult<List<SysMenu>> getMenuTree() {
        List<SysMenu> menus = sysMenuService.list();
        List<SysMenu> menuTree = sysMenuService.buildMenuTree(menus);
        return ResponseResult.success(menuTree);
    }
    
    /**
     * 根据用户ID获取菜单列表（树形结构）
     * @return 菜单列表
     */
    @GetMapping("/user")
    public ResponseResult<List<SysMenu>> getMenusByUserId() {
        // 从SecurityContext中获取当前用户
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        String username = null;
        
        // 处理不同类型的principal
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            // 如果是UserDetails对象
            org.springframework.security.core.userdetails.User userDetails = 
                    (org.springframework.security.core.userdetails.User) principal;
            username = userDetails.getUsername();
        } else if (principal instanceof String) {
            // 如果是String对象
            username = (String) principal;
        } else {
            // 其他类型，返回错误
            return ResponseResult.error("无法获取当前用户信息");
        }
        
        // 从数据库中根据用户名查询用户ID
        com.lasu.hyperduty.entity.SysUser sysUser = sysUserService.getByUsername(username);
        if (sysUser == null) {
            return ResponseResult.error("用户不存在");
        }
        
        Long userId = sysUser.getId();
        List<SysMenu> menus = sysMenuService.getMenusByUserId(userId);
        return ResponseResult.success(menus);
    }
}
