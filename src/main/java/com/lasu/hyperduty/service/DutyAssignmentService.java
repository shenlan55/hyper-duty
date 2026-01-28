package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyAssignment;

public interface DutyAssignmentService extends IService<DutyAssignment> {

    void deleteByScheduleIdAndDateRange(Long scheduleId, String startDate, String endDate);
    
    void swapDutyAssignments(Long originalEmployeeId, Long targetEmployeeId, java.time.LocalDate swapDate, Integer swapShift);
}