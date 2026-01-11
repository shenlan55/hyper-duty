package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysUser;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectByUsername(String username);

    List<String> selectRoleCodesByUserId(Long userId);

    List<String> selectPermsByUserId(Long userId);

}