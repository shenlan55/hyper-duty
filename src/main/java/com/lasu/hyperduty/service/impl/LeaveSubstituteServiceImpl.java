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
        try {
            this.lambdaUpdate()
                    .eq(LeaveSubstitute::getLeaveRequestId, leaveRequestId)
                    .remove();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // 保存新的顶岗信息
        try {
            for (java.util.Map<String, Object> data : substituteData) {
                String date = (String) data.get("date");
                if (date == null) continue;
                
                Object shiftIdObj = data.get("shiftId");
                if (shiftIdObj == null) continue;
                Long shiftId = null;
                if (shiftIdObj instanceof Number) {
                    shiftId = ((Number) shiftIdObj).longValue();
                } else if (shiftIdObj instanceof String) {
                    try {
                        shiftId = Long.parseLong((String) shiftIdObj);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                if (shiftId == null) continue;
                
                Object substituteEmployeeIdObj = data.get("substituteEmployeeId");
                if (substituteEmployeeIdObj == null) continue;
                Long substituteEmployeeId = null;
                if (substituteEmployeeIdObj instanceof Number) {
                    substituteEmployeeId = ((Number) substituteEmployeeIdObj).longValue();
                } else if (substituteEmployeeIdObj instanceof String) {
                    try {
                        substituteEmployeeId = Long.parseLong((String) substituteEmployeeIdObj);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                if (substituteEmployeeId == null) continue;
                
                try {
                    LocalDate dutyDate = LocalDate.parse(date);
                    LeaveSubstitute substitute = new LeaveSubstitute();
                    substitute.setLeaveRequestId(leaveRequestId);
                    substitute.setOriginalEmployeeId(originalEmployeeId);
                    substitute.setSubstituteEmployeeId(substituteEmployeeId);
                    substitute.setDutyDate(dutyDate);
                    substitute.setShiftConfigId(shiftId);
                    substitute.setStatus(1);
                    substitute.setCreateTime(LocalDateTime.now());
                    substitute.setUpdateTime(LocalDateTime.now());

                    this.save(substitute);
                } catch (Exception e) {
                    // 日期解析或其他异常，跳过当前记录
                    e.printStackTrace();
                    continue;
                }
            }
            return true;
        } catch (Exception e) {
            // 捕获所有异常，确保审批流程不会中断
            e.printStackTrace();
            return false;
        }
    }

}
