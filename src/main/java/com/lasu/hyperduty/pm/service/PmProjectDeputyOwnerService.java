package com.lasu.hyperduty.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.pm.entity.PmProjectDeputyOwner;
import com.lasu.hyperduty.pm.service.PmProjectDeputyOwnerService;
import java.util.List;








public interface PmProjectDeputyOwnerService extends IService<PmProjectDeputyOwner> {

    /**
     * 保存项目代理负责人关联
     * @param projectId 项目ID
     * @param employeeIds 代理负责人ID列表
     */
    void saveDeputyOwners(Long projectId, List<Long> employeeIds);

    /**
     * 根据项目ID获取代理负责人列表
     * @param projectId 项目ID
     * @return 代理负责人ID列表
     */
    List<Long> getDeputyOwnerIdsByProjectId(Long projectId);

    /**
     * 根据项目ID删除所有代理负责人关联
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);

}
