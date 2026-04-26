package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.pm.entity.PmProjectDeputyOwner;
import com.lasu.hyperduty.pm.mapper.PmProjectDeputyOwnerMapper;
import com.lasu.hyperduty.pm.service.PmProjectDeputyOwnerService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;








@Slf4j
@Service
@RequiredArgsConstructor
public class PmProjectDeputyOwnerServiceImpl extends ServiceImpl<PmProjectDeputyOwnerMapper, PmProjectDeputyOwner> implements PmProjectDeputyOwnerService {

    private final PmProjectDeputyOwnerMapper pmProjectDeputyOwnerMapper;

    @Override
    @Transactional
    public void saveDeputyOwners(Long projectId, List<Long> employeeIds) {
        // 先删除该项目的所有代理负责人关联
        pmProjectDeputyOwnerMapper.deleteByProjectId(projectId);
        
        // 再保存新的代理负责人关联
        if (employeeIds != null && !employeeIds.isEmpty()) {
            for (Long employeeId : employeeIds) {
                PmProjectDeputyOwner deputyOwner = new PmProjectDeputyOwner();
                deputyOwner.setProjectId(projectId);
                deputyOwner.setEmployeeId(employeeId);
                deputyOwner.setCreateTime(LocalDateTime.now());
                deputyOwner.setUpdateTime(LocalDateTime.now());
                save(deputyOwner);
            }
            log.info("保存项目代理负责人成功: projectId={}, employeeCount={}", projectId, employeeIds.size());
        }
    }

    @Override
    public List<Long> getDeputyOwnerIdsByProjectId(Long projectId) {
        List<Long> ids = pmProjectDeputyOwnerMapper.selectDeputyOwnerIdsByProjectId(projectId);
        return ids != null ? ids : new ArrayList<>();
    }

    @Override
    @Transactional
    public void deleteByProjectId(Long projectId) {
        int count = pmProjectDeputyOwnerMapper.deleteByProjectId(projectId);
        log.info("删除项目代理负责人关联成功: projectId={}, count={}", projectId, count);
    }

}
