package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单关联Mapper接口
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色ID删除角色菜单关联
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(Long roleId);

    /**
     * 批量插入角色菜单关联
     * @param roleMenuList 角色菜单关联列表
     * @return 插入数量
     */
    int batchInsert(List<SysRoleMenu> roleMenuList);

}
