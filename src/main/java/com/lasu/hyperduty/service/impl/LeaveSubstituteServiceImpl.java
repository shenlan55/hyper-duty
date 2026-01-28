package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.LeaveSubstitute;
import com.lasu.hyperduty.mapper.LeaveSubstituteMapper;
import com.lasu.hyperduty.service.LeaveSubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假顶岗信息Service实现
 */
@Service
public class LeaveSubstituteServiceImpl extends ServiceImpl<LeaveSubstituteMapper, LeaveSubstitute> implements LeaveSubstituteService {

    @Autowired
    private LeaveSubstituteMapper leaveSubstituteMapper;

    @Override
    public List<LeaveSubstitute> getByLeaveRequestId(Long leaveRequestId) {
        // 使用 MyBatis Plus 的 LambdaQueryWrapper 实现查询
        return this.lambdaQuery()
                .eq(LeaveSubstitute::getLeaveRequestId, leaveRequestId)
                .list();
    }

    @Override
    public boolean saveSubstitutes(Long leaveRequestId, Long originalEmployeeId, List<java.util.Map<String, Object>> substituteData) {
        if (substituteData == null || substituteData.isEmpty() || originalEmployeeId == null) {
            return false;
        }

        // 先删除该请假申请的所有顶岗信息
        this.lambdaUpdate()
                .eq(LeaveSubstitute::getLeaveRequestId, leaveRequestId)
                .remove();

        // 保存新的顶岗信息
        for (java.util.Map<String, Object> data : substituteData) {
            String date = (String) data.get("date");
            Long shiftId = ((Number) data.get("shiftId")).longValue();
            Long substituteEmployeeId = ((Number) data.get("substituteEmployeeId")).longValue();

            LeaveSubstitute substitute = new LeaveSubstitute();
            substitute.setLeaveRequestId(leaveRequestId);
            substitute.setOriginalEmployeeId(originalEmployeeId);
            substitute.setSubstituteEmployeeId(substituteEmployeeId);
            substitute.setDutyDate(LocalDate.parse(date));
            substitute.setShiftConfigId(shiftId);
            substitute.setStatus(1);
            substitute.setCreateTime(LocalDateTime.now());
            substitute.setUpdateTime(LocalDateTime.now());

            this.save(substitute);
        }

        return true;
    }

}
