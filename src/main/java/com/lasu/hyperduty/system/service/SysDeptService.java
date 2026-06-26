package com.lasu.hyperduty.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.system.entity.SysDept;
import com.lasu.hyperduty.system.service.SysDeptService;
import java.util.List;








public interface SysDeptService extends IService<SysDept> {

    /**
     * 获取所有部门（包括禁用），用于系统管理部门管理
     * 调用方：Dept.vue / Employee.vue / SystemStatisticsController
     */
    List<SysDept> getAllDepts();

    /**
     * 获取部门树（包括禁用），用于系统管理部门管理
     * 调用方：Dept.vue / Employee.vue
     */
    List<SysDept> getDeptTree();

    /**
     * 获取启用部门列表（status=1），用于业务模块选人/选部门
     * 调用方：PersonSelector / EmployeeSelector / DutyAssignment / DutyStatisticsServiceImpl
     */
    List<SysDept> getActiveDepts();

    /**
     * 获取启用部门树（status=1），用于业务模块选人/选部门
     * 调用方：PersonSelector
     */
    List<SysDept> getActiveDeptTree();

}