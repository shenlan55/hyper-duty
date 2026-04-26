package com.lasu.hyperduty.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.system.entity.SysMailConfig;
import com.lasu.hyperduty.system.mapper.SysMailConfigMapper;
import org.apache.ibatis.annotations.Mapper;







/**
 * 邮件服务配置Mapper接口
 */
@Mapper
public interface SysMailConfigMapper extends BaseMapper<SysMailConfig> {
}
