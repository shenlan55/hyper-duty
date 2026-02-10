package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysEmployeeServiceImpl extends ServiceImpl<SysEmployeeMapper, SysEmployee> implements SysEmployeeService {

    @Override
    public List<SysEmployee> getAllEmployees() {
        return list();
    }

    @Override
    public List<SysEmployee> getEmployeesByDeptId(Long deptId) {
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEmployee::getDeptId, deptId);
        return list(queryWrapper);
    }

    @Override
    public boolean updateById(SysEmployee sysEmployee) {
        // 使用LambdaUpdateWrapper强制更新所有字段，包括null值
        LambdaUpdateWrapper<SysEmployee> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysEmployee::getId, sysEmployee.getId())
                .set(SysEmployee::getEmployeeName, sysEmployee.getEmployeeName())
                .set(SysEmployee::getEmployeeCode, sysEmployee.getEmployeeCode())
                .set(SysEmployee::getDeptId, sysEmployee.getDeptId())
                .set(SysEmployee::getPhone, sysEmployee.getPhone())
                .set(SysEmployee::getEmail, sysEmployee.getEmail())
                .set(SysEmployee::getGender, sysEmployee.getGender())
                .set(SysEmployee::getDictTypeId, sysEmployee.getDictTypeId())
                .set(SysEmployee::getDictDataId, sysEmployee.getDictDataId())
                .set(SysEmployee::getStatus, sysEmployee.getStatus())
                .set(SysEmployee::getUpdateTime, sysEmployee.getUpdateTime());
        return update(updateWrapper);
    }

}