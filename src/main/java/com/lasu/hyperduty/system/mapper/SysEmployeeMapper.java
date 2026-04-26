package com.lasu.hyperduty.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import java.util.List;








public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    List<String> selectRoleCodesByUserId(Long userId);

    List<String> selectPermsByUserId(Long userId);

}