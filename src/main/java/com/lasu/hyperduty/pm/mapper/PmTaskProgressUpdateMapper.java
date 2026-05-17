package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmTaskProgressUpdate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 查询指定任务ID列表的进展历史
     */
    @Select("<script>" +
            "SELECT p.*, e.employee_name as employee_name, t.task_name, pj.project_name " +
            "FROM pm_task_progress_update p " +
            "LEFT JOIN sys_employee e ON p.employee_id = e.id " +
            "LEFT JOIN pm_task t ON p.task_id = t.id " +
            "LEFT JOIN pm_project pj ON t.project_id = pj.id " +
            "<where>" +
            "<if test='taskIds != null and taskIds.size() > 0'> AND p.task_id IN " +
            "<foreach collection='taskIds' item='taskId' open='(' separator=',' close=')'>#{taskId}</foreach>" +
            "</if>" +
            "<if test='progressUpdateTimeFrom != null'> AND p.create_time &gt;= #{progressUpdateTimeFrom}</if>" +
            "<if test='progressUpdateTimeTo != null'> AND p.create_time &lt;= #{progressUpdateTimeTo}</if>" +
            "</where>" +
            "ORDER BY pj.project_name, p.create_time DESC" +
            "</script>")
    List<PmTaskProgressUpdate> selectProgressHistoryForReport(
            @Param("taskIds") List<Long> taskIds,
            @Param("progressUpdateTimeFrom") java.time.LocalDateTime progressUpdateTimeFrom,
            @Param("progressUpdateTimeTo") java.time.LocalDateTime progressUpdateTimeTo);
}
