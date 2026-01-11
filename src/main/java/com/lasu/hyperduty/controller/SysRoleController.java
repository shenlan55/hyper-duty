package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysRole;
import com.lasu.hyperduty.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @return 角色列表
     */
    @GetMapping("/list")
    public ResponseResult<Page<SysRole>> listRole(@RequestParam(defaultValue = "1") Integer pageNum, 
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        Page<SysRole> rolePage = sysRoleService.page(page);
        return ResponseResult.success(rolePage);
    }

    /**
     * 获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
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
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否保存成功
     */
    @PostMapping("/menu")
    public ResponseResult<Boolean> saveRoleMenu(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        boolean result = sysRoleService.saveRoleMenu(roleId, menuIds);
        return ResponseResult.success(result);
    }

}
