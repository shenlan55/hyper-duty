package com.lasu.hyperduty.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.service.PmProjectService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








@Slf4j
@RestController
@RequestMapping("/pm/project")
@RequiredArgsConstructor
public class PmProjectController {

    private final PmProjectService pmProjectService;

    @GetMapping("/page")
    public ResponseResult<Page<PmProject>> getProjectPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false, defaultValue = "false") Boolean showArchived) {
        Page<PmProject> page = pmProjectService.pageList(pageNum, pageSize, projectName, status, ownerId, showArchived);
        return ResponseResult.success(page);
    }

    @GetMapping("/my/{employeeId}")
    public ResponseResult<List<PmProject>> getMyProjects(@PathVariable Long employeeId) {
        List<PmProject> projects = pmProjectService.getMyProjects(employeeId);
        return ResponseResult.success(projects);
    }

    @GetMapping("/{id}")
    public ResponseResult<PmProject> getProjectDetail(@PathVariable Long id) {
        PmProject project = pmProjectService.getProjectDetail(id);
        return ResponseResult.success(project);
    }

    @PostMapping
    public ResponseResult<PmProject> createProject(@RequestBody PmProject project) {
        PmProject created = pmProjectService.createProject(project);
        return ResponseResult.success(created);
    }

    @PutMapping
    public ResponseResult<PmProject> updateProject(@RequestBody PmProject project) {
        PmProject updated = pmProjectService.updateProject(project);
        return ResponseResult.success(updated);
    }

    @PutMapping("/archive/{id}")
    public ResponseResult<Void> archiveProject(@PathVariable Long id) {
        pmProjectService.archiveProject(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteProject(@PathVariable Long id) {
        pmProjectService.deleteProject(id);
        return ResponseResult.success();
    }

    @GetMapping("/status/{status}")
    public ResponseResult<List<PmProject>> getProjectsByStatus(@PathVariable Integer status) {
        List<PmProject> projects = pmProjectService.getProjectsByStatus(status);
        return ResponseResult.success(projects);
    }
}
