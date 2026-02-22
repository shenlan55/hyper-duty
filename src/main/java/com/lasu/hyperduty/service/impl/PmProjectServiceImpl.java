package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.PmProject;
import com.lasu.hyperduty.mapper.PmProjectMapper;
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

    @Override
    public Page<PmProject> pageList(Integer pageNum, Integer pageSize, String projectName, Integer status, Long ownerId) {
        Page<PmProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(PmProject::getArchived, 0);
        
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
        
        return baseMapper.selectPage(page, wrapper);
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
        
        log.info("创建项目成功: {}", project.getProjectName());
        return project;
    }

    @Override
    public PmProject updateProject(PmProject project) {
        project.setUpdateTime(LocalDateTime.now());
        updateById(project);
        
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
