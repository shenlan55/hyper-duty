package com.lasu.hyperduty.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.mapper.SysDictDataMapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;








public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectByDictTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 批量查询字典数据（按 dict_type_id 集合）
     * 用于前端按 dict_code 批量取值
     * @param dictTypeIds 字典类型 ID 集合
     * @return 字典数据列表（含全部 typeId 的数据）
     */
    List<SysDictData> selectByDictTypeIds(@Param("dictTypeIds") Collection<Long> dictTypeIds);
}