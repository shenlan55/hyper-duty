package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.pm.entity.PmProjectEmployee;
import com.lasu.hyperduty.pm.mapper.PmProjectEmployeeMapper;
import com.lasu.hyperduty.pm.service.PmProjectEmployeeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;








@Slf4j
@Service
@RequiredArgsConstructor
public class PmProjectEmployeeServiceImpl extends ServiceImpl<PmProjectEmployeeMapper, PmProjectEmployee> implements PmProjectEmployeeService {

    private final PmProjectEmployeeMapper pmProjectEmployeeMapper;

    @Override
    @Transactional
    public void saveProjectEmployees(Long projectId, List<Long> employeeIds) {
        // 先删除该项目的所有参与者关联
        pmProjectEmployeeMapper.deleteByProjectId(projectId);
        
        // 再保存新的参与者关联
        if (employeeIds != null && !employeeIds.isEmpty()) {
            for (Long employeeId : employeeIds) {
                PmProjectEmployee projectEmployee = new PmProjectEmployee();
                projectEmployee.setProjectId(projectId);
                projectEmployee.setEmployeeId(employeeId);
                projectEmployee.setCreatedAt(LocalDateTime.now());
                projectEmployee.setUpdatedAt(LocalDateTime.now());
                save(projectEmployee);
            }
            log.info("保存项目参与者成功: projectId={}, employeeCount={}", projectId, employeeIds.size());
        }
    }

    @Override
    public List<Long> getEmployeeIdsByProjectId(Long projectId) {
        return pmProjectEmployeeMapper.selectEmployeeIdsByProjectId(projectId);
    }

    @Override
    @Transactional
    public void deleteByProjectId(Long projectId) {
        int count = pmProjectEmployeeMapper.deleteByProjectId(projectId);
        log.info("删除项目参与者关联成功: projectId={}, count={}", projectId, count);
    }

}
