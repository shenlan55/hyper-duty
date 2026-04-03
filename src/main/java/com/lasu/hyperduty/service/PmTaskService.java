package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.PmTask;

import java.util.List;

public interface PmTaskService extends IService<PmTask> {

    Page<PmTask> pageList(Integer pageNum, Integer pageSize, Long projectId, Long assigneeId, Integer status, Integer priority);

    List<PmTask> getProjectTasks(Long projectId);

    List<PmTask> getSubTasks(Long parentId);

    List<PmTask> getMyTasks(Long employeeId);

    List<PmTask> getMyTasksByProject(Long employeeId, Long projectId);

    PmTask getTaskDetail(Long id);

    PmTask createTask(PmTask task);

    PmTask updateTask(PmTask task);

    void updateProgress(Long taskId, Integer progress);

    void deleteTask(Long id);

    List<PmTask> getUpcomingTasks();

    List<PmTask> getTasksByStatus(Integer status);

    void pinTask(Long taskId, boolean pinned);

    Integer calculateProjectProgress(Long projectId);

    void recalculateAllProjectProgress();

    boolean hasTaskPermission(Long taskId, Long employeeId);

    boolean hasTaskDeletePermission(Long taskId, Long employeeId);
}
