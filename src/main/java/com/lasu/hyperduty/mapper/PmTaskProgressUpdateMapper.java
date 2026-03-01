package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmTaskProgressUpdate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmTaskProgressUpdateMapper extends BaseMapper<PmTaskProgressUpdate> {

    @Select("SELECT p.*, e.employee_name as employee_name " +
            "FROM pm_task_progress_update p " +
            "LEFT JOIN sys_employee e ON p.employee_id = e.id " +
            "WHERE p.task_id = #{taskId} " +
            "ORDER BY p.create_time DESC")
    List<PmTaskProgressUpdate> selectByTaskId(Long taskId);

    @Select("SELECT p.*, e.employee_name as employee_name " +
            "FROM pm_task_progress_update p " +
            "LEFT JOIN sys_employee e ON p.employee_id = e.id " +
            "WHERE p.id = #{id}")
    PmTaskProgressUpdate selectByIdWithEmployeeName(Long id);
}
