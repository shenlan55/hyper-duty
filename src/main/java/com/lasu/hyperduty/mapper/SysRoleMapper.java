package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysRole;

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

}
