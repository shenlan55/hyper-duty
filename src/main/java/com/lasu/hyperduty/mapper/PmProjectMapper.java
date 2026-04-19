package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmProjectMapper extends BaseMapper<PmProject> {

    @Select("SELECT p.*, e.employee_name as owner_name " +
            "FROM pm_project p " +
            "LEFT JOIN sys_employee e ON p.owner_id = e.id " +
            "WHERE p.archived = 0 " +
            "ORDER BY p.sort ASC, p.create_time DESC")
    List<PmProject> selectProjectsWithOwner();

    @Select("SELECT DISTINCT p.*, e.employee_name as owner_name, " +
            "(SELECT COUNT(*) FROM pm_task t WHERE t.project_id = p.id) as task_count, " +
            "(SELECT COUNT(*) FROM pm_task t WHERE t.project_id = p.id AND t.status = 3) as completed_task_count " +
            "FROM pm_project p " +
            "LEFT JOIN sys_employee e ON p.owner_id = e.id " +
            "LEFT JOIN pm_project_employee pe ON p.id = pe.project_id " +
            "LEFT JOIN pm_project_deputy_owner pdo ON p.id = pdo.project_id " +
            "WHERE (p.owner_id = #{employeeId} OR pdo.employee_id = #{employeeId} OR pe.employee_id = #{employeeId}) AND p.archived = 0 " +
            "ORDER BY p.sort ASC, p.create_time DESC")
    List<PmProject> selectByOwnerId(@Param("employeeId") Long employeeId);

    @Select("SELECT p.*, e.employee_name as owner_name " +
            "FROM pm_project p " +
            "LEFT JOIN sys_employee e ON p.owner_id = e.id " +
            "WHERE p.id = #{id}")
    PmProject selectProjectById(@Param("id") Long id);

    @Select("SELECT p.*, e.employee_name as owner_name " +
            "FROM pm_project p " +
            "LEFT JOIN sys_employee e ON p.owner_id = e.id " +
            "WHERE p.id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    List<PmProject> selectProjectByIds(@Param("ids") List<Long> ids);
}
