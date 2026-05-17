package com.lasu.hyperduty.common.service.impl;

import com.lasu.hyperduty.common.utils.ExcelExportUtil;
import com.lasu.hyperduty.common.utils.ExcelExportUtil.TaskOverviewItem;
import com.lasu.hyperduty.common.utils.ExcelExportUtil.TaskProgressReportData;
import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.entity.DutyRecord;
import com.lasu.hyperduty.duty.entity.LeaveRequest;
import com.lasu.hyperduty.duty.service.DutyAssignmentService;
import com.lasu.hyperduty.duty.service.DutyRecordService;
import com.lasu.hyperduty.duty.service.DutyStatisticsService;
import com.lasu.hyperduty.duty.service.LeaveRequestService;
import com.lasu.hyperduty.pm.dto.TaskProgressReportQueryDTO;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import com.lasu.hyperduty.pm.mapper.PmTaskMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskProgressUpdateMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskShadowMapper;
import com.lasu.hyperduty.pm.service.PmProjectService;
import com.lasu.hyperduty.pm.service.PmTaskService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;









@Service
public class ExportServiceImpl {

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyRecordService dutyRecordService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private DutyStatisticsService dutyStatisticsService;

    @Autowired
    private PmProjectService pmProjectService;

    @Autowired
    private PmTaskService pmTaskService;

    @Autowired
    private PmTaskMapper pmTaskMapper;

    @Autowired
    private PmTaskShadowMapper pmTaskShadowMapper;

    @Autowired
    private PmTaskProgressUpdateMapper pmTaskProgressUpdateMapper;

    public void exportDutyAssignments(HttpServletResponse response) throws IOException {
        List<DutyAssignment> assignments = dutyAssignmentService.list();
        ExcelExportUtil.exportDutyAssignments(response, assignments);
    }

    public void exportDutyRecords(HttpServletResponse response) throws IOException {
        List<DutyRecord> records = dutyRecordService.list();
        ExcelExportUtil.exportDutyRecords(response, records);
    }

    public void exportLeaveRequests(HttpServletResponse response) throws IOException {
        List<LeaveRequest> requests = leaveRequestService.list();
        ExcelExportUtil.exportLeaveRequests(response, requests);
    }

    public void exportStatistics(HttpServletResponse response) throws IOException {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("overall", dutyStatisticsService.getOverallStatistics());
        statistics.put("dept", dutyStatisticsService.getDeptStatistics(null));
        statistics.put("shift", dutyStatisticsService.getShiftDistribution());
        statistics.put("trend", dutyStatisticsService.getMonthlyTrend());
        ExcelExportUtil.exportStatistics(response, statistics);
    }

    public void exportGantt(HttpServletResponse response, Long projectId) throws IOException {
        PmProject project = pmProjectService.getById(projectId);
        List<PmTask> tasks = pmTaskService.getProjectTasks(projectId);
        ExcelExportUtil.exportGantt(response, project, tasks);
    }

    /**
     * 导出任务进展报告
     */
    public void exportTaskProgressReport(
            HttpServletResponse response,
            TaskProgressReportQueryDTO queryDTO) throws IOException {
        
        // 1. 查询用户选择项目下面的源任务
        List<PmTask> projectSourceTasks = pmTaskMapper.selectTasksForReport(
                queryDTO.getProjectIds(),
                queryDTO.getTaskStartDateFrom(),
                queryDTO.getTaskStartDateTo(),
                queryDTO.getTaskEndDateFrom(),
                queryDTO.getTaskEndDateTo()
        );

        // 2. 查询用户选择项目下面的影子任务
        List<PmTaskShadow> projectShadowTasks = pmTaskShadowMapper.selectShadowTasksForReport(
                queryDTO.getProjectIds(),
                queryDTO.getTaskStartDateFrom(),
                queryDTO.getTaskStartDateTo(),
                queryDTO.getTaskEndDateFrom(),
                queryDTO.getTaskEndDateTo()
        );

        // 3. 收集所有需要查询的源任务ID（包括影子任务对应的源任务）
        List<Long> sourceTaskIdsToQuery = new ArrayList<>();
        Map<Long, PmTaskShadow> shadowTaskByIdMap = new HashMap<>();
        if (projectShadowTasks != null) {
            for (PmTaskShadow shadow : projectShadowTasks) {
                sourceTaskIdsToQuery.add(shadow.getSourceTaskId());
                shadowTaskByIdMap.put(shadow.getId(), shadow);
            }
        }
        if (projectSourceTasks != null) {
            for (PmTask task : projectSourceTasks) {
                sourceTaskIdsToQuery.add(task.getId());
            }
        }

        // 4. 查询所有需要的源任务（不限项目）
        Map<Long, PmTask> allSourceTaskMap = new HashMap<>();
        if (!sourceTaskIdsToQuery.isEmpty()) {
            List<PmTask> allSourceTasks = pmTaskMapper.selectTasksByIds(sourceTaskIdsToQuery);
            if (allSourceTasks != null) {
                for (PmTask task : allSourceTasks) {
                    allSourceTaskMap.put(task.getId(), task);
                }
            }
        }

        // 5. 收集所有任务ID（用于查询进展历史）
        List<Long> allTaskIds = new ArrayList<>(allSourceTaskMap.keySet());

        // 6. 查询进展历史 - 使用用户选择的时间范围
        List<PmTaskProgressUpdate> progressHistory = pmTaskProgressUpdateMapper.selectProgressHistoryForReport(
                allTaskIds.isEmpty() ? null : allTaskIds,
                queryDTO.getProgressUpdateTimeFrom(),
                queryDTO.getProgressUpdateTimeTo()
        );

        // 7. 按任务ID分组进展历史
        Map<Long, List<PmTaskProgressUpdate>> progressMap = new HashMap<>();
        if (progressHistory != null) {
            for (PmTaskProgressUpdate update : progressHistory) {
                progressMap.computeIfAbsent(update.getTaskId(), k -> new ArrayList<>()).add(update);
            }
        }

        // 8. 构建报告数据
        List<TaskOverviewItem> taskOverviewList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Pattern htmlPattern = Pattern.compile("<[^>]*>");
        
        // 8.1 添加用户选择项目下面的源任务（只添加在指定时间范围内有进展的任务）
        if (projectSourceTasks != null) {
            for (PmTask task : projectSourceTasks) {
                addTaskToReport(taskOverviewList, task, progressMap, dateFormatter, htmlPattern,
                        queryDTO.getProgressUpdateTimeFrom(), queryDTO.getProgressUpdateTimeTo());
            }
        }

        // 8.2 添加用户选择项目下面的影子任务（只添加在指定时间范围内有进展的任务）
        if (projectShadowTasks != null) {
            for (PmTaskShadow shadow : projectShadowTasks) {
                PmTask sourceTask = allSourceTaskMap.get(shadow.getSourceTaskId());
                if (sourceTask != null) {
                    addShadowTaskToReport(taskOverviewList, shadow, sourceTask, progressMap, dateFormatter, htmlPattern,
                            queryDTO.getProgressUpdateTimeFrom(), queryDTO.getProgressUpdateTimeTo());
                }
            }
        }

        // 9. 构建报告数据对象
        TaskProgressReportData reportData = new TaskProgressReportData(
                taskOverviewList,
                new ArrayList<>()
        );

        // 10. 导出 Excel
        String reportName = "任务进展报告";
        ExcelExportUtil.exportTaskProgressReport(response, reportName, reportData);
    }

    /**
     * 将源任务添加到报告中
     */
    private void addTaskToReport(
            List<TaskOverviewItem> taskOverviewList,
            PmTask task,
            Map<Long, List<PmTaskProgressUpdate>> progressMap,
            DateTimeFormatter dateFormatter,
            Pattern htmlPattern,
            java.time.LocalDateTime progressUpdateTimeFrom,
            java.time.LocalDateTime progressUpdateTimeTo) {
        
        // 获取该任务的进展历史
        List<PmTaskProgressUpdate> updates = progressMap.getOrDefault(task.getId(), new ArrayList<>());
        
        // 如果没有指定时间范围，或者在指定时间范围内有进展，才添加到报告
        boolean shouldAdd = true;
        if (progressUpdateTimeFrom != null || progressUpdateTimeTo != null) {
            shouldAdd = !updates.isEmpty();
        }
        
        if (!shouldAdd) {
            return;
        }
        
        // 按时间倒序排列
        updates.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 构建进展详情
        String progressDetails = buildProgressDetails(updates, dateFormatter, htmlPattern);
        
        TaskOverviewItem item = new TaskOverviewItem(
                task.getProjectName(),
                "源任务",
                task.getTaskName(),
                task.getPriority(),
                task.getStatus(),
                task.getProgress(),
                task.getOwnerName(),
                task.getStartDate(),
                task.getEndDate(),
                task.getCreateTime(),
                task.getUpdateTime(),
                task.getLastProgressUpdateTime(),
                progressDetails
        );
        taskOverviewList.add(item);
    }

    /**
     * 将影子任务添加到报告中
     */
    private void addShadowTaskToReport(
            List<TaskOverviewItem> taskOverviewList,
            PmTaskShadow shadow,
            PmTask sourceTask,
            Map<Long, List<PmTaskProgressUpdate>> progressMap,
            DateTimeFormatter dateFormatter,
            Pattern htmlPattern,
            java.time.LocalDateTime progressUpdateTimeFrom,
            java.time.LocalDateTime progressUpdateTimeTo) {
        
        // 获取源任务的进展历史
        List<PmTaskProgressUpdate> updates = progressMap.getOrDefault(sourceTask.getId(), new ArrayList<>());
        
        // 如果没有指定时间范围，或者在指定时间范围内有进展，才添加到报告
        boolean shouldAdd = true;
        if (progressUpdateTimeFrom != null || progressUpdateTimeTo != null) {
            shouldAdd = !updates.isEmpty();
        }
        
        if (!shouldAdd) {
            return;
        }
        
        // 按时间倒序排列
        updates.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 构建进展详情
        String progressDetails = buildProgressDetails(updates, dateFormatter, htmlPattern);
        
        // 影子任务名称
        String shadowTaskName = shadow.getShadowAlias() != null && !shadow.getShadowAlias().isEmpty()
                ? shadow.getShadowAlias()
                : sourceTask.getTaskName();
        
        TaskOverviewItem shadowItem = new TaskOverviewItem(
                shadow.getTargetProjectName(),
                "影子任务",
                shadowTaskName,
                shadow.getSourcePriority(),
                shadow.getSourceStatus(),
                shadow.getSourceProgress(),
                shadow.getSourceOwnerName(),
                shadow.getSourceStartDate(),
                shadow.getSourceEndDate(),
                shadow.getCreatedAt(),
                shadow.getUpdatedAt(),
                shadow.getLastProgressUpdateTime(),
                progressDetails
        );
        taskOverviewList.add(shadowItem);
    }

    /**
     * 构建进展详情文本
     */
    private String buildProgressDetails(
            List<PmTaskProgressUpdate> updates,
            DateTimeFormatter dateFormatter,
            Pattern htmlPattern) {
        
        StringBuilder progressDetails = new StringBuilder();
        for (PmTaskProgressUpdate update : updates) {
            String cleanedDescription = update.getDescription();
            if (cleanedDescription != null) {
                cleanedDescription = htmlPattern.matcher(cleanedDescription).replaceAll("");
            }
            
            progressDetails.append(update.getCreateTime().format(dateFormatter))
                    .append(" ")
                    .append(update.getEmployeeName() != null ? update.getEmployeeName() : "")
                    .append(" 进度更新至 ")
                    .append(update.getProgress() != null ? update.getProgress() : 0)
                    .append("%");
            
            if (cleanedDescription != null && !cleanedDescription.isEmpty()) {
                progressDetails.append(" ").append(cleanedDescription);
            }
            
            progressDetails.append("\n");
        }
        return progressDetails.toString();
    }
}
