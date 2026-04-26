package com.lasu.hyperduty.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.ai.dto.AiReportDataDTO;
import com.lasu.hyperduty.ai.service.ReportDataAggregationService;
import com.lasu.hyperduty.pm.dto.TaskUpdateDTO;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.pm.mapper.PmProjectMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskProgressUpdateMapper;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;









@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataAggregationServiceImpl implements ReportDataAggregationService {

    private final PmProjectMapper pmProjectMapper;
    private final PmTaskMapper pmTaskMapper;
    private final PmTaskProgressUpdateMapper pmTaskProgressUpdateMapper;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public AiReportDataDTO aggregateDailyData(LocalDate reportDate, List<Long> projectIds) {
        List<PmProject> projects = getProjects(projectIds);
        List<AiReportDataDTO.ProjectInfoDTO> projectInfoList = buildProjectInfoList(projects);

        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);

        AiReportDataDTO.DailyTaskDataDTO dailyTaskData = buildDailyTaskData(projects, startOfDay, endOfDay);

        return AiReportDataDTO.builder()
                .projectInfo(projectInfoList)
                .dailyTaskData(dailyTaskData)
                .build();
    }

    @Override
    public AiReportDataDTO aggregateWeeklyData(LocalDate startDate, LocalDate endDate, List<Long> projectIds) {
        List<PmProject> projects = getProjects(projectIds);
        List<AiReportDataDTO.ProjectInfoDTO> projectInfoList = buildProjectInfoList(projects);

        LocalDateTime startOfWeek = startDate.atStartOfDay();
        LocalDateTime endOfWeek = endDate.atTime(23, 59, 59);

        AiReportDataDTO.WeeklyTaskDataDTO weeklyTaskData = buildWeeklyTaskData(projects, startOfWeek, endOfWeek);

        return AiReportDataDTO.builder()
                .projectInfo(projectInfoList)
                .weeklyTaskData(weeklyTaskData)
                .build();
    }

    @Override
    public String toJsonString(AiReportDataDTO data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("转换JSON失败", e);
            return "{}";
        }
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

    private List<AiReportDataDTO.ProjectInfoDTO> buildProjectInfoList(List<PmProject> projects) {
        return projects.stream()
                .map(project -> AiReportDataDTO.ProjectInfoDTO.builder()
                        .projectId(project.getId())
                        .projectName(project.getProjectName())
                        .projectCode(project.getProjectCode())
                        .ownerName(project.getOwnerName())
                        .progress(project.getProgress() != null ? project.getProgress() : 0)
                        .description(project.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private AiReportDataDTO.DailyTaskDataDTO buildDailyTaskData(List<PmProject> projects, LocalDateTime startTime, LocalDateTime endTime) {
        List<AiReportDataDTO.TaskDTO> allFocusTasks = new ArrayList<>();
        List<AiReportDataDTO.TaskDTO> allHighPriorityTasks = new ArrayList<>();

        for (PmProject project : projects) {
            LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(PmTask::getProjectId, project.getId());
            List<PmTask> tasks = pmTaskMapper.selectList(taskWrapper);

            if (tasks.isEmpty()) {
                continue;
            }

            List<PmTask> focusTasks = tasks.stream()
                    .filter(t -> t.getIsFocus() != null && t.getIsFocus() == 1)
                    .collect(Collectors.toList());

            List<PmTask> highPriorityNormalTasks = tasks.stream()
                    .filter(t -> (t.getIsFocus() == null || t.getIsFocus() != 1))
                    .filter(t -> t.getPriority() != null && t.getPriority() == 1)
                    .collect(Collectors.toList());

            Set<Long> displayTaskIds = new HashSet<>();
            displayTaskIds.addAll(focusTasks.stream().map(PmTask::getId).collect(Collectors.toSet()));
            displayTaskIds.addAll(highPriorityNormalTasks.stream().map(PmTask::getId).collect(Collectors.toSet()));

            LambdaQueryWrapper<PmTaskProgressUpdate> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PmTaskProgressUpdate::getTaskId, displayTaskIds);
            updateWrapper.ge(PmTaskProgressUpdate::getCreateTime, startTime);
            updateWrapper.le(PmTaskProgressUpdate::getCreateTime, endTime);
            updateWrapper.orderByDesc(PmTaskProgressUpdate::getCreateTime);
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateMapper.selectList(updateWrapper);

            Map<Long, List<PmTaskProgressUpdate>> updatesByTask = updates.stream()
                    .collect(Collectors.groupingBy(PmTaskProgressUpdate::getTaskId));

            for (PmTask task : focusTasks) {
                allFocusTasks.add(buildTaskDTO(task, updatesByTask));
            }
            for (PmTask task : highPriorityNormalTasks) {
                allHighPriorityTasks.add(buildTaskDTO(task, updatesByTask));
            }
        }

        return AiReportDataDTO.DailyTaskDataDTO.builder()
                .focusTasks(allFocusTasks)
                .highPriorityTasks(allHighPriorityTasks)
                .build();
    }

    private AiReportDataDTO.WeeklyTaskDataDTO buildWeeklyTaskData(List<PmProject> projects, LocalDateTime startTime, LocalDateTime endTime) {
        List<AiReportDataDTO.TaskDTO> allFocusTasks = new ArrayList<>();
        List<AiReportDataDTO.TaskDTO> allHighPriorityTasks = new ArrayList<>();
        List<AiReportDataDTO.DailyUpdateDTO> allDailyUpdates = new ArrayList<>();
        List<PmTaskProgressUpdate> allUpdates = new ArrayList<>();

        for (PmProject project : projects) {
            LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
            taskWrapper.eq(PmTask::getProjectId, project.getId());
            List<PmTask> tasks = pmTaskMapper.selectList(taskWrapper);

            if (tasks.isEmpty()) {
                continue;
            }

            List<PmTask> focusTasks = tasks.stream()
                    .filter(t -> t.getIsFocus() != null && t.getIsFocus() == 1)
                    .collect(Collectors.toList());

            List<PmTask> highPriorityNormalTasks = tasks.stream()
                    .filter(t -> (t.getIsFocus() == null || t.getIsFocus() != 1))
                    .filter(t -> t.getPriority() != null && t.getPriority() == 1)
                    .collect(Collectors.toList());

            Set<Long> displayTaskIds = new HashSet<>();
            displayTaskIds.addAll(focusTasks.stream().map(PmTask::getId).collect(Collectors.toSet()));
            displayTaskIds.addAll(highPriorityNormalTasks.stream().map(PmTask::getId).collect(Collectors.toSet()));

            LambdaQueryWrapper<PmTaskProgressUpdate> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PmTaskProgressUpdate::getTaskId, displayTaskIds);
            updateWrapper.ge(PmTaskProgressUpdate::getCreateTime, startTime);
            updateWrapper.le(PmTaskProgressUpdate::getCreateTime, endTime);
            updateWrapper.orderByDesc(PmTaskProgressUpdate::getCreateTime);
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateMapper.selectList(updateWrapper);

            allUpdates.addAll(updates);

            Map<Long, List<PmTaskProgressUpdate>> updatesByTask = updates.stream()
                    .collect(Collectors.groupingBy(PmTaskProgressUpdate::getTaskId));

            for (PmTask task : focusTasks) {
                allFocusTasks.add(buildTaskDTO(task, updatesByTask));
            }
            for (PmTask task : highPriorityNormalTasks) {
                allHighPriorityTasks.add(buildTaskDTO(task, updatesByTask));
            }
        }

        Map<LocalDate, List<PmTaskProgressUpdate>> updatesByDate = allUpdates.stream()
                .collect(Collectors.groupingBy(u -> u.getCreateTime().toLocalDate()));

        for (Map.Entry<LocalDate, List<PmTaskProgressUpdate>> entry : updatesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            List<AiReportDataDTO.TaskUpdateDTO> updateDTOs = entry.getValue().stream()
                    .map(this::buildTaskUpdateDTO)
                    .collect(Collectors.toList());
            allDailyUpdates.add(AiReportDataDTO.DailyUpdateDTO.builder()
                    .date(entry.getKey().format(DATE_FORMATTER))
                    .updates(updateDTOs)
                    .build());
        }

        return AiReportDataDTO.WeeklyTaskDataDTO.builder()
                .focusTasks(allFocusTasks)
                .highPriorityTasks(allHighPriorityTasks)
                .dailyUpdates(allDailyUpdates)
                .build();
    }

    private AiReportDataDTO.TaskDTO buildTaskDTO(PmTask task, Map<Long, List<PmTaskProgressUpdate>> updatesByTask) {
        List<AiReportDataDTO.TaskUpdateDTO> updateDTOs = new ArrayList<>();
        List<PmTaskProgressUpdate> updates = updatesByTask.get(task.getId());
        if (updates != null) {
            updateDTOs = updates.stream()
                    .map(this::buildTaskUpdateDTO)
                    .collect(Collectors.toList());
        }

        return AiReportDataDTO.TaskDTO.builder()
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .status(task.getStatus())
                .statusText(getStatusText(task.getStatus()))
                .progress(task.getProgress() != null ? task.getProgress() : 0)
                .priority(task.getPriority())
                .isFocus(task.getIsFocus())
                .description(task.getDescription())
                .updates(updateDTOs)
                .build();
    }

    private AiReportDataDTO.TaskUpdateDTO buildTaskUpdateDTO(PmTaskProgressUpdate update) {
        return AiReportDataDTO.TaskUpdateDTO.builder()
                .updateId(update.getId())
                .taskId(update.getTaskId())
                .employeeId(update.getEmployeeId())
                .employeeName(getEmployeeName(update.getEmployeeId()))
                .progress(update.getProgress())
                .description(update.getDescription())
                .updateTime(update.getCreateTime().format(DATETIME_FORMATTER))
                .build();
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
