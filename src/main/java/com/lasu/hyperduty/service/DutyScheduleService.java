package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutySchedule;

import java.util.List;

public interface DutyScheduleService extends IService<DutySchedule> {

    List<Long> getEmployeeIdsByScheduleId(Long scheduleId);

    List<Long> getLeaderIdsByScheduleId(Long scheduleId);

    boolean updateEmployees(Long scheduleId, List<Long> employeeIds);

    boolean updateLeaders(Long scheduleId, List<Long> leaderIds);
 
    boolean updateEmployeesAndLeaders(Long scheduleId, List<Long> employeeIds, List<Long> leaderIds);
}