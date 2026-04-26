package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysRole;
import com.lasu.hyperduty.system.service.SysRoleService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 获取角色列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 角色列表
     */
    @GetMapping("/list")
    public ResponseResult<Page<SysRole>> listRole(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysRole> page = sysRoleService.getRoleList(pageNum, pageSize, keyword);
        return ResponseResult.success(page);
    }

    /**
     * 获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<SysRole> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        return ResponseResult.success(role);
    }

    /**
     * 保存角色
     * @param sysRole 角色信息
     * @return 是否保存成功
     */
    @PostMapping
    @RateLimit(window = 60, max = 20, message = "保存角色操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> saveRole(@RequestBody SysRole sysRole) {
        boolean result = sysRoleService.save(sysRole);
        return ResponseResult.success(result);
    }

    /**
     * 更新角色
     * @param sysRole 角色信息
     * @return 是否更新成功
     */
    @PutMapping
    @RateLimit(window = 60, max = 20, message = "更新角色操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> updateRole(@RequestBody SysRole sysRole) {
        boolean result = sysRoleService.updateById(sysRole);
        return ResponseResult.success(result);
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除角色操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> deleteRole(@PathVariable Long id) {
        boolean result = sysRoleService.removeById(id);
        return ResponseResult.success(result);
    }

    /**
     * 获取角色菜单
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @GetMapping("/menu/{roleId}")
    public ResponseResult<List<Long>> getRoleMenu(@PathVariable Long roleId) {
        List<Long> menuIds = sysRoleService.getMenuIdsByRoleId(roleId);
        return ResponseResult.success(menuIds);
    }

    /**
     * 保存角色菜单
     * @param request 请求参数，包含角色ID和菜单ID列表
     * @return 是否保存成功
     */
    @PostMapping("/menu")
    @RateLimit(window = 60, max = 20, message = "保存角色菜单操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> saveRoleMenu(@RequestBody Map<String, Object> request) {
        Long roleId = Long.parseLong(request.get("roleId").toString());
        List<Long> menuIds = ((List<?>) request.get("menuIds")).stream()
            .map(item -> Long.parseLong(item.toString()))
            .collect(Collectors.toList());
        boolean result = sysRoleService.saveRoleMenu(roleId, menuIds);
        return ResponseResult.success(result);
    }
    
    /**
     * 获取角色用户
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @GetMapping("/user/{roleId}")
    public ResponseResult<List<Long>> getRoleUser(@PathVariable("roleId") Long roleId) {
        List<Long> userIds = sysRoleService.getUserIdsByRoleId(roleId);
        return ResponseResult.success(userIds);
    }
    
    /**
     * 保存角色用户
     * @param request 请求参数，包含角色ID和用户ID列表
     * @return 是否保存成功
     */
    @PostMapping("/user")
    @RateLimit(window = 60, max = 20, message = "保存角色用户操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> saveRoleUser(@RequestBody Map<String, Object> request) {
        Long roleId = Long.parseLong(request.get("roleId").toString());
        List<Long> userIds = ((List<?>) request.get("userIds")).stream()
            .map(item -> Long.parseLong(item.toString()))
            .collect(Collectors.toList());
        boolean result = sysRoleService.saveRoleUser(roleId, userIds);
        return ResponseResult.success(result);
    }

}
