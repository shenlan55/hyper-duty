package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysMenu;
import com.lasu.hyperduty.mapper.SysMenuMapper;
import com.lasu.hyperduty.service.SysMenuService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单Service实现类
 */
@Service
public class SysMenuServiceImpl extends CacheableServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    /**
     * 根据用户ID查询菜单列表（树形结构）
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    @Cacheable(value = "menu", key = "'user_' + #userId")
    public List<SysMenu> getMenusByUserId(Long userId) {
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus);
    }

    /**
     * 根据角色ID查询菜单列表
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Override
    @Cacheable(value = "menu", key = "'role_' + #roleId")
    public List<SysMenu> getMenusByRoleId(Long roleId) {
        return sysMenuMapper.selectMenusByRoleId(roleId);
    }

    /**
     * 查询所有菜单列表（树形结构）
     * @return 菜单列表
     */
    @Override
    @Cacheable(value = "menu", key = "'all'")
    public List<SysMenu> getAllMenus() {
        List<SysMenu> menus = sysMenuMapper.selectAllMenus();
        return buildMenuTree(menus);
    }

    /**
     * 构建菜单树形结构
     * @param menus 菜单列表
     * @return 菜单树形结构
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> treeMenus = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == 0) {
                // 根节点
                treeMenus.add(findChildren(menu, menus));
            }
        }
        // 对根节点菜单按照sort字段升序排序
        treeMenus.sort((m1, m2) -> Integer.compare(m1.getSort(), m2.getSort()));
        return treeMenus;
    }

    /**
     * 递归查找子菜单
     * @param menu 当前菜单
     * @param menus 所有菜单列表
     * @return 包含子菜单的菜单
     */
    private SysMenu findChildren(SysMenu menu, List<SysMenu> menus) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu subMenu : menus) {
            if (subMenu.getParentId().equals(menu.getId())) {
                // 为子菜单添加父菜单的路径前缀
                String parentPath = menu.getPath();
                String childPath = subMenu.getPath();
                
                // 处理系统管理菜单的子菜单
                if (menu.getMenuName().equals("系统管理") && childPath != null && !childPath.isEmpty() && !childPath.startsWith("/system")) {
                    subMenu.setPath("/system" + (childPath.startsWith("/") ? "" : "/") + childPath);
                }
                // 处理值班管理菜单的子菜单
                if (menu.getMenuName().equals("值班管理") && childPath != null && !childPath.isEmpty() && !childPath.startsWith("/duty")) {
                    subMenu.setPath("/duty" + (childPath.startsWith("/") ? "" : "/") + childPath);
                }
                
                children.add(findChildren(subMenu, menus));
            }
        }
        // 对子菜单按照sort字段升序排序
        children.sort((m1, m2) -> Integer.compare(m1.getSort(), m2.getSort()));
        menu.setChildren(children);
        return menu;
    }

    /**
     * 保存菜单
     * @param sysMenu 菜单信息
     * @return 是否保存成功
     */
    @Override
    public boolean saveMenu(SysMenu sysMenu) {
        boolean result = super.save(sysMenu);
        if (result) {
            // 清除所有菜单缓存
            clearAllMenuCache();
        }
        return result;
    }

    /**
     * 更新菜单
     * @param sysMenu 菜单信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateMenu(SysMenu sysMenu) {
        boolean result = super.updateById(sysMenu);
        if (result) {
            // 清除所有菜单缓存
            clearAllMenuCache();
        }
        return result;
    }

    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMenu(Long menuId) {
        // 检查是否有子菜单
        List<SysMenu> menus = this.list();
        for (SysMenu menu : menus) {
            if (menu.getParentId().equals(menuId)) {
                // 有子菜单，不能删除
                return false;
            }
        }
        boolean result = super.removeById(menuId);
        if (result) {
            // 清除所有菜单缓存
            clearAllMenuCache();
        }
        return result;
    }

    /**
     * 清除所有菜单缓存
     */
    private void clearAllMenuCache() {
        // 清除所有菜单缓存
        CacheUtil.deleteByPattern("menu::*");
    }

    @Override
    protected void clearCache(SysMenu entity) {
        // 清除所有菜单缓存
        clearAllMenuCache();
    }

}
