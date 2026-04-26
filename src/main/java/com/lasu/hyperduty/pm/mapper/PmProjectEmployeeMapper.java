package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmProjectEmployee;
import com.lasu.hyperduty.pm.mapper.PmProjectEmployeeMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;








@Mapper
public interface PmProjectEmployeeMapper extends BaseMapper<PmProjectEmployee> {

    /**
     * 根据项目ID获取参与者列表
     * @param projectId 项目ID
     * @return 参与者ID列表
     */
    @Select("SELECT employee_id FROM pm_project_employee WHERE project_id = #{projectId}")
    List<Long> selectEmployeeIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和员工ID删除关联
     * @param projectId 项目ID
     * @param employeeId 员工ID
     * @return 删除影响行数
     */
    @Delete("DELETE FROM pm_project_employee WHERE project_id = #{projectId} AND employee_id = #{employeeId}")
    int deleteByProjectIdAndEmployeeId(@Param("projectId") Long projectId, @Param("employeeId") Long employeeId);

    /**
     * 根据项目ID删除所有关联
     * @param projectId 项目ID
     * @return 删除影响行数
     */
    @Delete("DELETE FROM pm_project_employee WHERE project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

}
