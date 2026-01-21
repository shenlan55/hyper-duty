package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.mapper.DutyAssignmentMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class DutyAssignmentServiceImpl extends ServiceImpl<DutyAssignmentMapper, DutyAssignment> implements DutyAssignmentService {

    @Override
    public void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate) {
        LambdaQueryWrapper<DutyAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyAssignment::getScheduleId, scheduleId)
                .ge(DutyAssignment::getDutyDate, startDate)
                .le(DutyAssignment::getDutyDate, endDate);
        remove(wrapper);
    }
}