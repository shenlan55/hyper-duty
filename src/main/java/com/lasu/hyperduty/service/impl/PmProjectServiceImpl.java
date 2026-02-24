package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.PmProject;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.PmProjectMapper;
import com.lasu.hyperduty.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.service.PmProjectEmployeeService;
import com.lasu.hyperduty.service.PmProjectService;
import com.lasu.hyperduty.service.PmTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmProjectServiceImpl extends ServiceImpl<PmProjectMapper, PmProject> implements PmProjectService {

    private final PmTaskService pmTaskService;
    private final PmProjectEmployeeService pmProjectEmployeeService;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public Page<PmProject> pageList(Integer pageNum, Integer pageSize, String projectName, Integer status, Long ownerId, Boolean showArchived) {
        Page<PmProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        
        // 只有当showArchived为true时才显示已归档的项目
        if (!Boolean.TRUE.equals(showArchived)) {
            wrapper.eq(PmProject::getArchived, 0);
        }
        
        if (StringUtils.hasText(projectName)) {
            wrapper.like(PmProject::getProjectName, projectName);
        }
        
        if (status != null) {
            wrapper.eq(PmProject::getStatus, status);
        }
        
        if (ownerId != null) {
            wrapper.eq(PmProject::getOwnerId, ownerId);
        }
        
        wrapper.orderByDesc(PmProject::getCreateTime);
        
        Page<PmProject> result = baseMapper.selectPage(page, wrapper);
        
        // 填充负责人名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (PmProject project : result.getRecords()) {
                if (project.getOwnerId() != null) {
                    SysEmployee employee = sysEmployeeMapper.selectById(project.getOwnerId());
                    if (employee != null) {
                        project.setOwnerName(employee.getEmployeeName());
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    public List<PmProject> getMyProjects(Long employeeId) {
        return baseMapper.selectByOwnerId(employeeId);
    }

    @Override
    public PmProject getProjectDetail(Long id) {
        PmProject project = baseMapper.selectProjectById(id);
        if (project != null) {
            Integer progress = pmTaskService.calculateProjectProgress(id);
            project.setProgress(progress);
            
            // 加载项目参与者
            List<Long> participantIds = pmProjectEmployeeService.getEmployeeIdsByProjectId(id);
            project.setParticipants(participantIds);
        }
        return project;
    }

    @Override
    public PmProject createProject(PmProject project) {
        project.setStatus(1);
        project.setProgress(0);
        project.setArchived(0);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        
        save(project);
        
        // 保存项目参与者
        if (project.getParticipants() != null && !project.getParticipants().isEmpty()) {
            pmProjectEmployeeService.saveProjectEmployees(project.getId(), project.getParticipants());
        }
        
        log.info("创建项目成功: {}", project.getProjectName());
        return project;
    }

    @Override
    public PmProject updateProject(PmProject project) {
        project.setUpdateTime(LocalDateTime.now());
        updateById(project);
        
        // 保存项目参与者
        pmProjectEmployeeService.saveProjectEmployees(project.getId(), project.getParticipants());
        
        log.info("更新项目成功: {}", project.getId());
        return project;
    }

    @Override
    public void archiveProject(Long id) {
        PmProject project = getById(id);
        if (project != null) {
            project.setArchived(1);
            project.setUpdateTime(LocalDateTime.now());
            updateById(project);
            log.info("归档项目成功: {}", id);
        }
    }

    @Override
    public void deleteProject(Long id) {
        removeById(id);
        log.info("删除项目成功: {}", id);
    }

    @Override
    public List<PmProject> getProjectsByStatus(Integer status) {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProject::getStatus, status)
               .eq(PmProject::getArchived, 0)
               .orderByDesc(PmProject::getCreateTime);
        return list(wrapper);
    }
}
