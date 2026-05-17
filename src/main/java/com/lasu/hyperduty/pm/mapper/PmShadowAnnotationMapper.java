package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.dto.PmShadowAnnotationVO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationWithProjectVO;
import com.lasu.hyperduty.pm.entity.PmShadowAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 影子任务批注Mapper
 */
@Mapper
public interface PmShadowAnnotationMapper extends BaseMapper<PmShadowAnnotation> {

    /**
     * 查询影子任务的批注列表（包含创建人姓名）
     */
    List<PmShadowAnnotationVO> selectByShadowId(@Param("shadowId") Long shadowId);

    /**
     * 查询源任务的所有影子批注（包含影子项目信息）
     */
    List<ShadowAnnotationWithProjectVO> selectBySourceTaskId(@Param("sourceTaskId") Long sourceTaskId);

    /**
     * 统计影子任务的批注数量
     */
    Integer countByShadowId(@Param("shadowId") Long shadowId);
}
