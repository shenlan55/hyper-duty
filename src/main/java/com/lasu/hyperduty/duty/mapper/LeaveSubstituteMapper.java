package com.lasu.hyperduty.duty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.duty.entity.LeaveSubstitute;
import com.lasu.hyperduty.duty.mapper.LeaveSubstituteMapper;
import java.util.List;








/**
 * 请假顶岗信息Mapper
 */
public interface LeaveSubstituteMapper extends BaseMapper<LeaveSubstitute> {

    /**
     * 根据请假申请ID查询顶岗信息
     * @param leaveRequestId 请假申请ID
     * @return 顶岗信息列表
     */
    List<LeaveSubstitute> selectByLeaveRequestId(Long leaveRequestId);

}
