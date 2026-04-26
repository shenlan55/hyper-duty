package com.lasu.hyperduty.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.mapper.SysDictDataMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;








public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectByDictTypeId(@Param("dictTypeId") Long dictTypeId);
}