package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmTaskCustomRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmTaskCustomRowMapper extends BaseMapper<PmTaskCustomRow> {

    @Select("SELECT * FROM pm_task_custom_row WHERE task_id = #{taskId}")
    List<PmTaskCustomRow> selectByTaskId(@Param("taskId") Long taskId);
}
