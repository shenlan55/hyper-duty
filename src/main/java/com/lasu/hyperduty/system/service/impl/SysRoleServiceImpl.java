package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.system.entity.SysRole;
import com.lasu.hyperduty.system.entity.SysRoleMenu;
import com.lasu.hyperduty.system.entity.SysUserRole;
import com.lasu.hyperduty.system.mapper.SysRoleMapper;
import com.lasu.hyperduty.system.mapper.SysRoleMenuMapper;
import com.lasu.hyperduty.system.service.SysRoleService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;








/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends CacheableServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    @Cacheable(value = "role", key = "'menu_ids_' + #roleId")
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenu(Long roleId, List<Long> menuIds) {
        // 删除原有角色菜单关联
        sysRoleMenuMapper.deleteByRoleId(roleId);
        
        // 添加新的角色菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenuList = menuIds.stream().map(menuId -> {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            }).collect(java.util.stream.Collectors.toList());
            
            boolean result = sysRoleMenuMapper.batchInsert(roleMenuList) > 0;
            if (result) {
                // 清除缓存
                CacheUtil.delete("role::menu_ids_" + roleId);
                // 清除与该角色相关联的所有用户的菜单缓存
                List<Long> userIds = baseMapper.selectUserIdsByRoleId(roleId);
                for (Long userId : userIds) {
                    CacheUtil.delete("menu::user_" + userId);
                }
            }
            return result;
        } else {
            // 清除缓存
            CacheUtil.delete("role::menu_ids_" + roleId);
            // 清除与该角色相关联的所有用户的菜单缓存
            List<Long> userIds = baseMapper.selectUserIdsByRoleId(roleId);
            for (Long userId : userIds) {
                CacheUtil.delete("menu::user_" + userId);
            }
            return true;
        }
    }
    
    @Override
    @Cacheable(value = "role", key = "'user_ids_' + #roleId")
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return baseMapper.selectUserIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleUser(Long roleId, List<Long> userIds) {
        // 获取原有用户ID列表
        List<Long> oldUserIds = baseMapper.selectUserIdsByRoleId(roleId);
        
        // 删除原有角色用户关联
        baseMapper.deleteUserByRoleId(roleId);
        
        // 添加新的角色用户关联
        if (userIds != null && !userIds.isEmpty()) {
            List<SysUserRole> userRoleList = userIds.stream().map(userId -> {
                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(roleId);
                userRole.setEmployeeId(userId);
                return userRole;
            }).collect(java.util.stream.Collectors.toList());
            
            boolean result = baseMapper.batchInsertUserRoles(userRoleList) > 0;
            if (result) {
                // 清除缓存
                CacheUtil.delete("role::user_ids_" + roleId);
                
                // 清除所有相关用户的菜单缓存
                // 包括原有用户和新添加的用户
                for (Long userId : oldUserIds) {
                    CacheUtil.delete("menu::user_" + userId);
                }
                for (Long userId : userIds) {
                    CacheUtil.delete("menu::user_" + userId);
                }
            }
            return result;
        } else {
            // 清除缓存
            CacheUtil.delete("role::user_ids_" + roleId);
            
            // 清除原有用户的菜单缓存
            for (Long userId : oldUserIds) {
                CacheUtil.delete("menu::user_" + userId);
            }
            return true;
        }
    }

    @Override
    protected void clearCache(SysRole entity) {
        // 清除角色相关的所有缓存
        if (entity != null && entity.getId() != null) {
            CacheUtil.delete("role::menu_ids_" + entity.getId());
            CacheUtil.delete("role::user_ids_" + entity.getId());
        }
    }

    @Override
    public Page<SysRole> getRoleList(Integer pageNum, Integer pageSize, String keyword) {
        // 创建分页对象
        Page<SysRole> pagination = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like("role_name", keyword)
                       .or().like("role_code", keyword)
                       .or().like("description", keyword)
            );
        }
        
        // 按排序字段升序排列
        queryWrapper.orderByAsc("sort")
                .orderByDesc("create_time");
        
        // 执行分页查询
        return baseMapper.selectPage(pagination, queryWrapper);
    }

}
