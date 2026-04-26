package com.lasu.hyperduty.duty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.duty.entity.DutyScheduleEmployee;
import com.lasu.hyperduty.duty.service.DutyScheduleEmployeeService;
import java.util.List;
import java.util.Map;








public interface DutyScheduleEmployeeService extends IService<DutyScheduleEmployee> {

    List<Long> getEmployeeIdsByScheduleId(Long scheduleId);

    List<DutyScheduleEmployee> getEmployeesByScheduleId(Long scheduleId);

    List<Long> getLeaderIdsByScheduleId(Long scheduleId);

    List<Map<String, Object>> getScheduleEmployeesWithLeaderInfo(Long scheduleId);

    List<Map<String, Object>> getScheduleEmployeesWithDetails(Long scheduleId);

    boolean saveBatch(Long scheduleId, List<Long> employeeIds);

    boolean updateLeaders(Long scheduleId, List<Long> leaderIds);

    boolean deleteByScheduleId(Long scheduleId);
}
