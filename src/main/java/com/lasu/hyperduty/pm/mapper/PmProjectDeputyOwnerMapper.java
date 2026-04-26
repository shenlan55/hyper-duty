package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmProjectDeputyOwner;
import com.lasu.hyperduty.pm.mapper.PmProjectDeputyOwnerMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;








@Mapper
public interface PmProjectDeputyOwnerMapper extends BaseMapper<PmProjectDeputyOwner> {

    /**
     * 根据项目ID获取代理负责人列表
     * @param projectId 项目ID
     * @return 代理负责人ID列表
     */
    @Select("SELECT employee_id FROM pm_project_deputy_owner WHERE project_id = #{projectId}")
    List<Long> selectDeputyOwnerIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID删除所有代理负责人关联
     * @param projectId 项目ID
     * @return 删除影响行数
     */
    @Delete("DELETE FROM pm_project_deputy_owner WHERE project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

}
