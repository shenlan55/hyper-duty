package com.lasu.hyperduty.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.workflow.entity.WfTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 流程模板 Mapper
 */
@Mapper
public interface WfTemplateMapper extends BaseMapper<WfTemplate> {

    /**
     * 增加使用次数（原子 +1）
     */
    @Update("UPDATE wf_template SET use_count = COALESCE(use_count, 0) + 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int incrementUseCount(Long id);
}
