package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmTaskCustomRow;
import com.lasu.hyperduty.pm.mapper.PmTaskCustomRowMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;








@Mapper
public interface PmTaskCustomRowMapper extends BaseMapper<PmTaskCustomRow> {

    @Select("SELECT * FROM pm_task_custom_row WHERE task_id = #{taskId}")
    List<PmTaskCustomRow> selectByTaskId(@Param("taskId") Long taskId);
}
