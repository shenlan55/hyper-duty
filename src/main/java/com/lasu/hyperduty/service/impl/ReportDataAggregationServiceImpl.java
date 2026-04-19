package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lasu.hyperduty.entity.PmProject;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.PmProjectMapper;
import com.lasu.hyperduty.mapper.PmTaskMapper;
import com.lasu.hyperduty.mapper.PmTaskProgressUpdateMapper;
import com.lasu.hyperduty.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.service.ReportDataAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataAggregationServiceImpl implements ReportDataAggregationService {

    private final PmProjectMapper pmProjectMapper;
    private final PmTaskMapper pmTaskMapper;
    private final PmTaskProgressUpdateMapper pmTaskProgressUpdateMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, String> aggregateDailyData(LocalDate reportDate, List<Long> projectIds) {
        Map<String, String> result = new HashMap<>();

        // 获取项目信息
        List<PmProject> projects = getProjects(projectIds);
        String projectInfo = buildProjectInfo(projects);
        result.put("projectInfo", projectInfo);

        // 获取该日期的任务更新
        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);
        String taskUpdates = buildTaskUpdates(projects, startOfDay, endOfDay);
        result.put("taskUpdates", taskUpdates);

        return result;
    }

    @Override
    public Map<String, String> aggregateWeeklyData(LocalDate startDate, LocalDate endDate, List<Long> projectIds) {
        Map<String, String> result = new HashMap<>();

        // 获取项目信息
        List<PmProject> projects = getProjects(projectIds);
        String projectInfo = buildProjectInfo(projects);
        result.put("projectInfo", projectInfo);

        // 获取该周的任务更新
        LocalDateTime startOfWeek = startDate.atStartOfDay();
        LocalDateTime endOfWeek = endDate.atTime(23, 59, 59);
        String weeklyTaskData = buildWeeklyTaskData(projects, startOfWeek, endOfWeek);
        result.put("weeklyTaskData", weeklyTaskData);

        return result;
    }

    private List<PmProject> getProjects(List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
            log.info("查询指定项目，projectIds={}", projectIds);
            return pmProjectMapper.selectProjectByIds(projectIds);
        } else {
            log.info("查询所有项目");
            return pmProjectMapper.selectProjectsWithOwner();
        }
    }

    private String buildProjectInfo(List<PmProject> projects) {
        StringBuilder sb = new StringBuilder();
        for (PmProject project : projects) {
            sb.append("- 项目名称：").append(project.getProjectName()).append("\n");
            sb.append("  项目编码：").append(project.getProjectCode() != null ? project.getProjectCode() : "").append("\n");
            sb.append("  负责人：").append(project.getOwnerName() != null ? project.getOwnerName() : "").append("\n");
            sb.append("  当前进度：").append(project.getProgress() != null ? project.getProgress() : 0).append("%\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildTaskUpdates(List<PmProject> projects, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sb = new StringBuilder();

        for (PmProject project : projects) {
            sb.append("【").append(project.getProjectName()).append("】\n");

            // 获取该项目在该时间段的任务
            LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(PmTask::getProjectId, project.getId());
            List<PmTask> tasks = pmTaskMapper.selectList(taskWrapper);

            if (tasks.isEmpty()) {
                sb.append("  暂无任务更新\n\n");
                continue;
            }

            // 区分重点任务和非重点任务
            List<PmTask> focusTasks = tasks.stream().filter(t -> t.getIsFocus() != null && t.getIsFocus() == 1).collect(Collectors.toList());
            List<PmTask> normalTasks = tasks.stream().filter(t -> t.getIsFocus() == null || t.getIsFocus() != 1).collect(Collectors.toList());

            // 获取这些任务在该时间段的进度更新
            Set<Long> taskIds = tasks.stream().map(PmTask::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<PmTaskProgressUpdate> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PmTaskProgressUpdate::getTaskId, taskIds);
            updateWrapper.ge(PmTaskProgressUpdate::getCreateTime, startTime);
            updateWrapper.le(PmTaskProgressUpdate::getCreateTime, endTime);
            updateWrapper.orderByDesc(PmTaskProgressUpdate::getCreateTime);
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateMapper.selectList(updateWrapper);

            // 按任务分组更新
            Map<Long, List<PmTaskProgressUpdate>> updatesByTask = updates.stream()
                    .collect(Collectors.groupingBy(PmTaskProgressUpdate::getTaskId));

            // 重点任务 - 更明显的标记
            if (!focusTasks.isEmpty()) {
                sb.append("\n=======================================\n");
                sb.append("★★★ 重点任务区域 ★★★\n");
                sb.append("=======================================\n");
                for (PmTask task : focusTasks) {
                    appendTaskInfo(sb, task, updatesByTask);
                }
            }

            // 非重点任务
            if (!normalTasks.isEmpty()) {
                sb.append("\n=======================================\n");
                sb.append("◆◆◆ 其他任务区域 ◆◆◆\n");
                sb.append("=======================================\n");
                for (PmTask task : normalTasks) {
                    appendTaskInfo(sb, task, updatesByTask);
                }
            }
        }

        return sb.toString();
    }

    private void appendTaskInfo(StringBuilder sb, PmTask task, Map<Long, List<PmTaskProgressUpdate>> updatesByTask) {
        sb.append("  - 任务名称：").append(task.getTaskName()).append("\n");
        sb.append("    当前状态：").append(getStatusText(task.getStatus())).append("\n");
        sb.append("    当前进度：").append(task.getProgress() != null ? task.getProgress() : 0).append("%\n");

        List<PmTaskProgressUpdate> taskUpdates = updatesByTask.get(task.getId());
        if (taskUpdates != null && !taskUpdates.isEmpty()) {
            sb.append("    今日更新：\n");
            for (PmTaskProgressUpdate update : taskUpdates) {
                String employeeName = getEmployeeName(update.getEmployeeId());
                sb.append("      - [").append(employeeName).append("] ");
                sb.append("进度更新至").append(update.getProgress()).append("%");
                if (update.getDescription() != null) {
                    sb.append("：").append(update.getDescription());
                }
                sb.append("\n");
            }
        }
        sb.append("\n");
    }

    private String buildWeeklyTaskData(List<PmProject> projects, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sb = new StringBuilder();

        for (PmProject project : projects) {
            sb.append("【").append(project.getProjectName()).append("】\n");

            // 获取该项目的任务
            LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(PmTask::getProjectId, project.getId());
            List<PmTask> tasks = pmTaskMapper.selectList(taskWrapper);

            if (tasks.isEmpty()) {
                sb.append("  暂无任务\n\n");
                continue;
            }

            // 区分重点任务和非重点任务
            List<PmTask> focusTasks = tasks.stream().filter(t -> t.getIsFocus() != null && t.getIsFocus() == 1).collect(Collectors.toList());
            List<PmTask> normalTasks = tasks.stream().filter(t -> t.getIsFocus() == null || t.getIsFocus() != 1).collect(Collectors.toList());

            // 获取该时间段的进度更新
            Set<Long> taskIds = tasks.stream().map(PmTask::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<PmTaskProgressUpdate> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PmTaskProgressUpdate::getTaskId, taskIds);
            updateWrapper.ge(PmTaskProgressUpdate::getCreateTime, startTime);
            updateWrapper.le(PmTaskProgressUpdate::getCreateTime, endTime);
            updateWrapper.orderByDesc(PmTaskProgressUpdate::getCreateTime);
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateMapper.selectList(updateWrapper);

            // 按任务分组
            Map<Long, List<PmTaskProgressUpdate>> updatesByTask = updates.stream()
                    .collect(Collectors.groupingBy(PmTaskProgressUpdate::getTaskId));

            // 按日期分组
            Map<LocalDate, List<PmTaskProgressUpdate>> updatesByDate = updates.stream()
                    .collect(Collectors.groupingBy(u -> u.getCreateTime().toLocalDate()));

            // 重点任务 - 更明显的标记
            if (!focusTasks.isEmpty()) {
                sb.append("\n=======================================\n");
                sb.append("★★★ 重点任务区域 ★★★\n");
                sb.append("=======================================\n");
                sb.append("  本周任务进度：\n");
                for (PmTask task : focusTasks) {
                    sb.append("    - ").append(task.getTaskName());
                    sb.append(" [").append(getStatusText(task.getStatus())).append("]");
                    sb.append(" 进度：").append(task.getProgress() != null ? task.getProgress() : 0).append("%\n");
                }
            }

            // 非重点任务
            if (!normalTasks.isEmpty()) {
                sb.append("\n=======================================\n");
                sb.append("◆◆◆ 其他任务区域 ◆◆◆\n");
                sb.append("=======================================\n");
                sb.append("  本周任务进度：\n");
                for (PmTask task : normalTasks) {
                    sb.append("    - ").append(task.getTaskName());
                    sb.append(" [").append(getStatusText(task.getStatus())).append("]");
                    sb.append(" 进度：").append(task.getProgress() != null ? task.getProgress() : 0).append("%\n");
                }
            }

            sb.append("\n  本周更新记录：\n");
            for (Map.Entry<LocalDate, List<PmTaskProgressUpdate>> entry : updatesByDate.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).toList()) {
                sb.append("    ").append(entry.getKey().format(DATE_FORMATTER)).append("：\n");
                for (PmTaskProgressUpdate update : entry.getValue()) {
                    PmTask task = tasks.stream().filter(t -> t.getId().equals(update.getTaskId())).findFirst().orElse(null);
                    String taskName = task != null ? task.getTaskName() : "未知任务";
                    String employeeName = getEmployeeName(update.getEmployeeId());
                    sb.append("      - [").append(employeeName).append("] [").append(taskName).append("] ");
                    sb.append("进度更新至").append(update.getProgress()).append("%");
                    if (update.getDescription() != null) {
                        sb.append("：").append(update.getDescription());
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待开始";
            case 2 -> "进行中";
            case 3 -> "已完成";
            case 4 -> "已暂停";
            default -> "未知";
        };
    }

    private String getEmployeeName(Long employeeId) {
        if (employeeId == null) return "未知";
        SysEmployee employee = sysEmployeeMapper.selectById(employeeId);
        return employee != null ? employee.getEmployeeName() : "未知";
    }
}
