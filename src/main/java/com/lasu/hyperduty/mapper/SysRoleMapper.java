package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysRole;
import com.lasu.hyperduty.entity.SysUserRole;

import java.util.List;

/**
 * 角色Mapper接口
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色ID查询角色菜单
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);
    
    /**
     * 根据角色ID查询角色用户
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(Long roleId);
    
    /**
     * 根据角色ID删除角色用户关联
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteUserByRoleId(Long roleId);
    
    /**
     * 批量插入角色用户关联
     * @param userRoles 用户角色关联列表
     * @return 插入数量
     */
    int batchInsertUserRoles(List<SysUserRole> userRoles);

}
