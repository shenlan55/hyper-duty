package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.PmProject;

import java.util.List;

public interface PmProjectService extends IService<PmProject> {

    Page<PmProject> pageList(Integer pageNum, Integer pageSize, String projectName, Integer status, Long ownerId, Boolean showArchived);

    List<PmProject> getMyProjects(Long employeeId);

    PmProject getProjectDetail(Long id);

    PmProject createProject(PmProject project);

    PmProject updateProject(PmProject project);

    void archiveProject(Long id);

    void deleteProject(Long id);

    List<PmProject> getProjectsByStatus(Integer status);
}
