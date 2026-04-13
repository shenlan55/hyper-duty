package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.PmTask;

import java.util.List;

public interface PmTaskService extends IService<PmTask> {

    Page<PmTask> pageList(Integer pageNum, Integer pageSize, Long projectId, Long assigneeId, Integer status, Integer priority, String taskName, String assigneeName);

    List<PmTask> getProjectTasks(Long projectId);

    List<PmTask> getSubTasks(Long parentId);

    List<PmTask> getMyTasks(Long employeeId, String taskName);

    List<PmTask> getMyTasksByProject(Long employeeId, Long projectId, String taskName);

    PmTask getTaskDetail(Long id);

    PmTask createTask(PmTask task);

    PmTask updateTask(PmTask task);

    void updateProgress(Long taskId, Integer progress);

    void deleteTask(Long id);

    List<PmTask> getUpcomingTasks(Long employeeId);

    List<PmTask> getTasksByStatus(Integer status);

    void pinTask(Long taskId, boolean pinned);

    Integer calculateProjectProgress(Long projectId);

    void recalculateAllProjectProgress();

    boolean hasTaskPermission(Long taskId, Long employeeId);

    boolean hasTaskDeletePermission(Long taskId, Long employeeId);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.lasu.hyperduty.dto.WorkloadDTO> getWorkloadPage(Integer pageNum, Integer pageSize, Long projectId, String taskName, Long assigneeId, java.time.LocalDate taskStartDate, java.time.LocalDate taskEndDate, java.time.LocalDateTime bindStartTime, java.time.LocalDateTime bindEndTime, String orderNo, String title);
}
