package com.lasu.hyperduty.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.workflow.entity.WfCc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 流程抄送 Mapper
 */
@Mapper
public interface WfCcMapper extends BaseMapper<WfCc> {

    /**
     * 标记为已读
     * @param id 主键
     * @param ccUserId 当前抄送人（防止误标记他人）
     * @return 影响行数
     */
    @Update("UPDATE public.wf_cc SET read_status = 1, read_time = CURRENT_TIMESTAMP WHERE id = #{id} AND cc_user_id = #{ccUserId} AND read_status = 0")
    int markRead(@Param("id") Long id, @Param("ccUserId") Long ccUserId);

    /**
     * 全部标记为已读
     */
    @Update("UPDATE public.wf_cc SET read_status = 1, read_time = CURRENT_TIMESTAMP WHERE cc_user_id = #{ccUserId} AND read_status = 0")
    int markAllRead(@Param("ccUserId") Long ccUserId);
}
