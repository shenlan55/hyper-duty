package com.lasu.hyperduty.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.pm.entity.PmProjectEmployee;
import com.lasu.hyperduty.pm.service.PmProjectEmployeeService;
import java.util.List;








public interface PmProjectEmployeeService extends IService<PmProjectEmployee> {

    /**
     * 保存项目参与者关联
     * @param projectId 项目ID
     * @param employeeIds 参与者ID列表
     */
    void saveProjectEmployees(Long projectId, List<Long> employeeIds);

    /**
     * 根据项目ID获取参与者列表
     * @param projectId 项目ID
     * @return 参与者ID列表
     */
    List<Long> getEmployeeIdsByProjectId(Long projectId);

    /**
     * 根据项目ID删除所有参与者关联
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);

}
