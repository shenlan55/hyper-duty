package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysEmployee;

import java.util.List;

public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    List<String> selectRoleCodesByUserId(Long userId);

    List<String> selectPermsByUserId(Long userId);

}