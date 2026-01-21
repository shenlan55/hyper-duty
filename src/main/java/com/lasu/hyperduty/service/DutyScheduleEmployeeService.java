package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyScheduleEmployee;

import java.util.List;

public interface DutyScheduleEmployeeService extends IService<DutyScheduleEmployee> {

    List<Long> getEmployeeIdsByScheduleId(Long scheduleId);

    List<DutyScheduleEmployee> getEmployeesByScheduleId(Long scheduleId);

    List<Long> getLeaderIdsByScheduleId(Long scheduleId);

    boolean saveBatch(Long scheduleId, List<Long> employeeIds);

    boolean updateLeaders(Long scheduleId, List<Long> leaderIds);

    boolean deleteByScheduleId(Long scheduleId);
}
