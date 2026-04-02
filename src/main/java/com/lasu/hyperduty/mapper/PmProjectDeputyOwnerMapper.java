package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.PmProjectDeputyOwner;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
