package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysRole;
import com.lasu.hyperduty.entity.SysRoleMenu;
import com.lasu.hyperduty.entity.SysUserRole;
import com.lasu.hyperduty.mapper.SysRoleMapper;
import com.lasu.hyperduty.mapper.SysRoleMenuMapper;
import com.lasu.hyperduty.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

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
            
            return sysRoleMenuMapper.batchInsert(roleMenuList) > 0;
        }
        
        return true;
    }
    
    @Override
    @Cacheable(value = "role", key = "'user_ids_' + #roleId")
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return baseMapper.selectUserIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleUser(Long roleId, List<Long> userIds) {
        // 删除原有角色用户关联
        baseMapper.deleteUserByRoleId(roleId);
        
        // 添加新的角色用户关联
        if (userIds != null && !userIds.isEmpty()) {
            List<SysUserRole> userRoleList = userIds.stream().map(userId -> {
                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(roleId);
                userRole.setUserId(userId);
                return userRole;
            }).collect(java.util.stream.Collectors.toList());
            
            return baseMapper.batchInsertUserRoles(userRoleList) > 0;
        }
        
        return true;
    }

}
