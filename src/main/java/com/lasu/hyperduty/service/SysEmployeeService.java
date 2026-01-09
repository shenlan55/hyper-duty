package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysEmployee;

import java.util.List;

public interface SysEmployeeService extends IService<SysEmployee> {

    List<SysEmployee> getAllEmployees();

    List<SysEmployee> getEmployeesByDeptId(Long deptId);

}