package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmTaskMapper extends BaseMapper<PmTask> {

    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "<where>" +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if>" +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if>" +
            "<if test='status != null'> AND t.status = #{status}</if>" +
            "<if test='priority != null'> AND t.priority = #{priority}</if>" +
            "</where>" +
            "ORDER BY t.is_pinned DESC, t.priority ASC, t.end_date ASC" +
            "</script>")
    List<PmTask> selectTaskPage(@Param("projectId") Long projectId,
                                  @Param("assigneeId") Long assigneeId,
                                  @Param("status") Integer status,
                                  @Param("priority") Integer priority);

    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM pm_task t " +
            "<where>" +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if>" +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if>" +
            "<if test='status != null'> AND t.status = #{status}</if>" +
            "<if test='priority != null'> AND t.priority = #{priority}</if>" +
            "</where>" +
            "</script>")
    Long selectTaskCount(@Param("projectId") Long projectId,
                         @Param("assigneeId") Long assigneeId,
                         @Param("status") Integer status,
                         @Param("priority") Integer priority);

    @Select("SELECT t.*, e.employee_name as owner_name, p.project_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "WHERE t.project_id = #{projectId} AND t.parent_id = 0 " +
            "ORDER BY t.is_pinned DESC, t.priority ASC, t.end_date ASC")
    List<PmTask> selectByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.parent_id = #{parentId} " +
            "ORDER BY t.priority ASC, t.end_date ASC")
    List<PmTask> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id) as sub_task_count, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id AND sub.status = 3) as completed_sub_task_count " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "WHERE t.assignee_id = #{employeeId} AND t.status != 3 " +
            "ORDER BY t.is_pinned DESC, t.priority ASC, t.end_date ASC")
    List<PmTask> selectMyTasks(@Param("employeeId") Long employeeId);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.id = #{id}")
    PmTask selectTaskById(@Param("id") Long id);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.status = #{status} " +
            "ORDER BY t.is_pinned DESC, t.priority ASC, t.end_date ASC")
    List<PmTask> selectByStatus(@Param("status") Integer status);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.end_date <= CURRENT_DATE + INTERVAL '3 days' AND t.status != 3 " +
            "ORDER BY t.end_date ASC, t.priority ASC")
    List<PmTask> selectUpcomingTasks();
}
