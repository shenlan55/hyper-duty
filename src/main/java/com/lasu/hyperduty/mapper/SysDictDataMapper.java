package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.SysDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectByDictTypeId(@Param("dictTypeId") Long dictTypeId);
}