package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 获取角色菜单列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 保存角色菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean saveRoleMenu(Long roleId, List<Long> menuIds);
    
    /**
     * 获取角色用户列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByRoleId(Long roleId);
    
    /**
     * 保存角色用户
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean saveRoleUser(Long roleId, List<Long> userIds);

}
