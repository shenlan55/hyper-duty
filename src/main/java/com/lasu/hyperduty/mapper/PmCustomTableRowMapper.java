package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmCustomTableRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmCustomTableRowMapper extends BaseMapper<PmCustomTableRow> {

    @Select("SELECT * FROM pm_custom_table_row WHERE table_id = #{tableId} ORDER BY COALESCE(sort_order, id) ASC")
    List<PmCustomTableRow> selectByTableId(@Param("tableId") Long tableId);
}
