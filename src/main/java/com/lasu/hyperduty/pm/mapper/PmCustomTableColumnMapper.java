package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmCustomTableColumn;
import com.lasu.hyperduty.pm.mapper.PmCustomTableColumnMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;








@Mapper
public interface PmCustomTableColumnMapper extends BaseMapper<PmCustomTableColumn> {

    @Select("SELECT * FROM pm_custom_table_column WHERE table_id = #{tableId} ORDER BY sort_order ASC")
    List<PmCustomTableColumn> selectByTableId(@Param("tableId") Long tableId);
}
