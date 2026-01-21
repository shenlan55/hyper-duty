package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyScheduleEmployee;
import com.lasu.hyperduty.mapper.DutyScheduleEmployeeMapper;
import com.lasu.hyperduty.service.DutyScheduleEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DutyScheduleEmployeeServiceImpl extends ServiceImpl<DutyScheduleEmployeeMapper, DutyScheduleEmployee> implements DutyScheduleEmployeeService {

    @Override
    public List<Long> getEmployeeIdsByScheduleId(Long scheduleId) {
        return baseMapper.getEmployeeIdsByScheduleId(scheduleId);
    }

    @Override
    public List<DutyScheduleEmployee> getEmployeesByScheduleId(Long scheduleId) {
        return baseMapper.getEmployeesByScheduleId(scheduleId);
    }

    @Override
    public List<Long> getLeaderIdsByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<DutyScheduleEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyScheduleEmployee::getScheduleId, scheduleId)
               .eq(DutyScheduleEmployee::getIsLeader, 1);
        List<DutyScheduleEmployee> leaders = list(wrapper);
        return leaders.stream().map(DutyScheduleEmployee::getEmployeeId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Long scheduleId, List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return true;
        }

        List<DutyScheduleEmployee> list = new ArrayList<>();
        for (int i = 0; i < employeeIds.size(); i++) {
            DutyScheduleEmployee item = new DutyScheduleEmployee();
            item.setScheduleId(scheduleId);
            item.setEmployeeId(employeeIds.get(i));
            item.setIsLeader(0);
            item.setSortOrder(i);
            list.add(item);
        }

        return saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLeaders(Long scheduleId, List<Long> leaderIds) {
        LambdaQueryWrapper<DutyScheduleEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DutyScheduleEmployee::getScheduleId, scheduleId);
        
        List<DutyScheduleEmployee> employees = list(wrapper);
        for (DutyScheduleEmployee employee : employees) {
            employee.setIsLeader(leaderIds != null && leaderIds.contains(employee.getEmployeeId()) ? 1 : 0);
        }
        
        return updateBatchById(employees);
    }

    @Override
    public boolean deleteByScheduleId(Long scheduleId) {
        return baseMapper.deleteByScheduleId(scheduleId) > 0;
    }
}
