package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectByUsername(String username);

}