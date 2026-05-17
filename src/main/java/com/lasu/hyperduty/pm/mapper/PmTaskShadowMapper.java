package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 影子任务 Mapper (v2)
 */
@Mapper
public interface PmTaskShadowMapper extends BaseMapper<PmTaskShadow> {

    /**
     * 查询：真实任务 + 影子任务（UNION ALL）
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<ShadowTaskVO> selectTaskListWithShadows(@Param("projectId") Long projectId);

    /**
     * 查询：影子任务详情
     * @param shadowId 影子ID
     * @return 影子任务 VO
     */
    ShadowTaskVO selectShadowById(@Param("shadowId") Long shadowId);

    /**
     * 查询：源任务的所有影子
     * @param sourceTaskId 源任务ID
     * @return 影子列表
     */
    List<PmTaskShadow> selectShadowsBySourceTaskId(@Param("sourceTaskId") Long sourceTaskId);
}
