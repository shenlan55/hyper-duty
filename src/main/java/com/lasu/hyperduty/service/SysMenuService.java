package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysMenu;

import java.util.List;
import java.util.Map;

/**
 * 菜单Service接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询菜单列表（树形结构）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> getMenusByUserId(Long userId);

    /**
     * 根据角色ID查询菜单列表
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<SysMenu> getMenusByRoleId(Long roleId);

    /**
     * 查询所有菜单列表（树形结构）
     * @return 菜单列表
     */
    List<SysMenu> getAllMenus();

    /**
     * 构建菜单树形结构
     * @param menus 菜单列表
     * @return 菜单树形结构
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     * 保存菜单
     * @param sysMenu 菜单信息
     * @return 是否保存成功
     */
    boolean saveMenu(SysMenu sysMenu);

    /**
     * 更新菜单
     * @param sysMenu 菜单信息
     * @return 是否更新成功
     */
    boolean updateMenu(SysMenu sysMenu);

    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    boolean deleteMenu(Long menuId);
}
