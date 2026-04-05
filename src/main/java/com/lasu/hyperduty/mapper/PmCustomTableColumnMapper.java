package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmCustomTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmCustomTableColumnMapper extends BaseMapper<PmCustomTableColumn> {

    @Select("SELECT * FROM pm_custom_table_column WHERE table_id = #{tableId} ORDER BY sort_order ASC")
    List<PmCustomTableColumn> selectByTableId(@Param("tableId") Long tableId);
}
