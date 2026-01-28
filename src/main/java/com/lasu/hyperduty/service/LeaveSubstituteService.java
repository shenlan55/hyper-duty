package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.LeaveSubstitute;

import java.util.List;

/**
 * 请假顶岗信息Service
 */
public interface LeaveSubstituteService extends IService<LeaveSubstitute> {

    /**
     * 根据请假申请ID查询顶岗信息
     * @param leaveRequestId 请假申请ID
     * @return 顶岗信息列表
     */
    List<LeaveSubstitute> getByLeaveRequestId(Long leaveRequestId);

    /**
     * 保存请假顶岗信息
     * @param leaveRequestId 请假申请ID
     * @param originalEmployeeId 原始员工ID
     * @param substituteData 顶岗数据列表
     * @return 是否保存成功
     */
    boolean saveSubstitutes(Long leaveRequestId, Long originalEmployeeId, List<java.util.Map<String, Object>> substituteData);

}
