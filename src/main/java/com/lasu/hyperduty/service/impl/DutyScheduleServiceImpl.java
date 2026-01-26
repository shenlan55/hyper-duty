package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.DutyScheduleEmployee;
import com.lasu.hyperduty.mapper.DutyScheduleMapper;
import com.lasu.hyperduty.service.DutyScheduleEmployeeService;
import com.lasu.hyperduty.service.DutyScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DutyScheduleServiceImpl extends ServiceImpl<DutyScheduleMapper, DutySchedule> implements DutyScheduleService {

    @Autowired
    private DutyScheduleEmployeeService scheduleEmployeeService;

    @Override
    public List<Long> getEmployeeIdsByScheduleId(Long scheduleId) {
        return scheduleEmployeeService.getEmployeeIdsByScheduleId(scheduleId);
    }

    @Override
    public List<Long> getLeaderIdsByScheduleId(Long scheduleId) {
        return scheduleEmployeeService.getLeaderIdsByScheduleId(scheduleId);
    }

    @Override
    public List<Map<String, Object>> getScheduleEmployeesWithLeaderInfo(Long scheduleId) {
        return scheduleEmployeeService.getScheduleEmployeesWithLeaderInfo(scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployees(Long scheduleId, List<Long> employeeIds) {
        scheduleEmployeeService.deleteByScheduleId(scheduleId);
        return scheduleEmployeeService.saveBatch(scheduleId, employeeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLeaders(Long scheduleId, List<Long> leaderIds) {
        return scheduleEmployeeService.updateLeaders(scheduleId, leaderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployeesAndLeaders(Long scheduleId, List<Long> employeeIds, List<Long> leaderIds) {
        List<DutyScheduleEmployee> employees = new ArrayList<>();
        for (int i = 0; i < employeeIds.size(); i++) {
            DutyScheduleEmployee item = new DutyScheduleEmployee();
            item.setScheduleId(scheduleId);
            item.setEmployeeId(employeeIds.get(i));
            item.setIsLeader(leaderIds != null && leaderIds.contains(employeeIds.get(i)) ? 1 : 0);
            item.setSortOrder(i);
            employees.add(item);
        }
        
        scheduleEmployeeService.deleteByScheduleId(scheduleId);
        return scheduleEmployeeService.saveBatch(employees);
    }
}