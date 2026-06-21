
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
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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
     * 统一的任务信息（用于构建父任务链）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TaskInfo {
        private Long id;
        private Integer isShadow; // 0=真实任务，1=影子任务
        private Long realTaskId; // 如果是影子任务，这个是源任务ID
        private Long parentId;
        private String taskName;
        private String projectName;
        private Integer priority;
        private Integer status;
        private Integer progress;
        private String ownerName;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime lastProgressUpdateTime;
        private PmTaskShadow shadow; // 影子任务原始对象（如果是影子）
        private PmTask realTask; // 真实任务原始对象（如果是真实）
    }

    /**
     * 构建带父任务链的任务名称
     */
    private String buildFullTaskName(TaskInfo task, Map<Long, TaskInfo> allTaskMap) {
        List<String> taskNames = new ArrayList<>();
        TaskInfo currentTask = task;
        int maxDepth = 10; // 最大深度限制，防止死循环
        int currentDepth = 0;

        // 从当前任务开始，向上追溯所有父任务
        while (currentTask != null && currentDepth < maxDepth) {
            taskNames.add(0, currentTask.getTaskName()); // 添加到列表开头
            if (currentTask.getParentId() == null || currentTask.getParentId() == 0) {
                break;
            }
            currentTask = allTaskMap.get(currentTask.getParentId());
            currentDepth++;
        }

        // 用连字符连接所有任务名称
        return String.join("-", taskNames);
    }

    /**
     * 导出任务进展报告
     */
    public void exportTaskProgressReport(
            HttpServletResponse response,
            TaskProgressReportQueryDTO queryDTO) throws IOException {

        // 0. 查询在指定时间范围内有进展更新的所有任务ID
        List<Long> taskIdsWithProgress = pmTaskProgressUpdateMapper.selectTaskIdsWithProgressInTimeRange(
                queryDTO.getProgressUpdateTimeFrom(),
                queryDTO.getProgressUpdateTimeTo());

        // 1. 为每个项目收集所有任务（真实任务 + 影子任务）
        Map<Long, TaskInfo> allTaskMap = new HashMap<>();
        Map<Long, List<TaskInfo>> projectTaskMap = new HashMap<>();
        
        if (queryDTO.getProjectIds() != null && !queryDTO.getProjectIds().isEmpty()) {
            for (Long projectId : queryDTO.getProjectIds()) {
                // 查询该项目的所有任务（真实 + 影子）
                List<ShadowTaskVO> shadowTasks = pmTaskShadowMapper.selectAllRootTasksWithShadows(projectId);
                
                List<TaskInfo> taskInfos = new ArrayList<>();
                for (ShadowTaskVO vo : shadowTasks) {
                    TaskInfo info = new TaskInfo();
                    info.setId(vo.getId());
                    info.setIsShadow(vo.getIsShadow());
                    info.setParentId(vo.getParentId());
                    info.setTaskName(vo.getTaskName());
                    info.setProjectName(vo.getProjectName());
                    info.setPriority(vo.getPriority());
                    info.setStatus(vo.getStatus());
                    info.setProgress(vo.getProgress());
                    info.setOwnerName(vo.getOwnerName());
                    info.setStartDate(vo.getStartDate());
                    info.setEndDate(vo.getEndDate());
                    info.setCreateTime(vo.getCreateTime());
                    info.setUpdateTime(vo.getUpdateTime());
                    info.setLastProgressUpdateTime(vo.getLastProgressUpdateTime());
                    
                    if (vo.getIsShadow() == 1) {
                        info.setRealTaskId(vo.getSourceTaskId());
                    } else {
                        info.setRealTaskId(vo.getId());
                    }
                    
                    taskInfos.add(info);
                    allTaskMap.put(vo.getId(), info);
                }
                
                projectTaskMap.put(projectId, taskInfos);
            }
        }

        // 2. 查询所有真实任务的详细信息（用于获取额外字段）
        List<Long> allRealTaskIds = allTaskMap.values().stream()
                .map(TaskInfo::getRealTaskId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, PmTask> realTaskMap = new HashMap<>();
        if (!allRealTaskIds.isEmpty()) {
            List<PmTask> realTasks = pmTaskMapper.selectTasksByIds(allRealTaskIds);
            if (realTasks != null) {
                for (PmTask task : realTasks) {
                    realTaskMap.put(task.getId(), task);
                }
            }
        }

        // 3. 查询所有影子任务的详细信息
        List<Long> allShadowIds = allTaskMap.values().stream()
                .filter(t -> t.getIsShadow() == 1)
                .map(TaskInfo::getId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, PmTaskShadow> shadowMap = new HashMap<>();
        if (!allShadowIds.isEmpty()) {
            for (Long shadowId : allShadowIds) {
                PmTaskShadow shadow = pmTaskShadowMapper.selectById(shadowId);
                if (shadow != null) {
                    shadowMap.put(shadowId, shadow);
                }
            }
        }

        // 4. 查询进展历史
        Map<Long, List<PmTaskProgressUpdate>> progressMap = new HashMap<>();
        if (!allRealTaskIds.isEmpty()) {
            List<PmTaskProgressUpdate> progressHistory = pmTaskProgressUpdateMapper.selectProgressHistoryForReport(
                    allRealTaskIds,
                    queryDTO.getProgressUpdateTimeFrom(),
                    queryDTO.getProgressUpdateTimeTo());
            
            if (progressHistory != null) {
                for (PmTaskProgressUpdate update : progressHistory) {
                    progressMap.computeIfAbsent(update.getTaskId(), k -> new ArrayList<>()).add(update);
                }
            }
        }

        // 5. 构建报告数据
        List<TaskOverviewItem> taskOverviewList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Pattern htmlPattern = Pattern.compile("<[^>]*>");

        // 遍历所有项目的任务
        for (Map.Entry<Long, List<TaskInfo>> entry : projectTaskMap.entrySet()) {
            Long projectId = entry.getKey();
            List<TaskInfo> taskInfos = entry.getValue();
            
            for (TaskInfo info : taskInfos) {
                // 检查是否在指定时间范围内有进展
                boolean shouldAdd = true;
                if (queryDTO.getProgressUpdateTimeFrom() != null || queryDTO.getProgressUpdateTimeTo() != null) {
                    shouldAdd = taskIdsWithProgress.contains(info.getRealTaskId());
                }
                
                if (!shouldAdd) {
                    continue;
                }
                
                // 检查任务日期范围
                if ((queryDTO.getTaskStartDateFrom() != null && info.getStartDate() != null && 
                     info.getStartDate().isBefore(queryDTO.getTaskStartDateFrom())) ||
                    (queryDTO.getTaskStartDateTo() != null && info.getStartDate() != null && 
                     info.getStartDate().isAfter(queryDTO.getTaskStartDateTo())) ||
                    (queryDTO.getTaskEndDateFrom() != null && info.getEndDate() != null && 
                     info.getEndDate().isBefore(queryDTO.getTaskEndDateFrom())) ||
                    (queryDTO.getTaskEndDateTo() != null && info.getEndDate() != null && 
                     info.getEndDate().isAfter(queryDTO.getTaskEndDateTo()))) {
                    continue;
                }
                
                // 构建带父任务链的任务名称
                String fullTaskName = buildFullTaskName(info, allTaskMap);
                
                // 获取该任务的进展历史
                List<PmTaskProgressUpdate> updates = progressMap.getOrDefault(info.getRealTaskId(), new ArrayList<>());
                updates.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                
                // 构建进展详情
                String progressDetails = buildProgressDetails(updates, dateFormatter, htmlPattern);
                
                // 添加到报告
                if (info.getIsShadow() == 1) {
                    // 影子任务
                    PmTaskShadow shadow = shadowMap.get(info.getId());
                    if (shadow != null) {
                        // 构建影子任务的完整名称（带父任务链）
                        // 先构建当前任务的名称（用别名或源任务名）
                        String currentTaskName = shadow.getShadowAlias() != null && !shadow.getShadowAlias().isEmpty() 
                                ? shadow.getShadowAlias() 
                                : info.getTaskName();
                        
                        // 现在重新构建完整任务链，把当前任务名替换进去
                        List<String> taskNames = new ArrayList<>();
                        TaskInfo currentTask = info;
                        int maxDepth = 10;
                        int currentDepth = 0;
                        
                        while (currentTask != null && currentDepth < maxDepth) {
                            if (currentDepth == 0) {
                                // 当前任务，用别名替换
                                taskNames.add(0, currentTaskName);
                            } else {
                                // 父任务，用原名称
                                taskNames.add(0, currentTask.getTaskName());
                            }
                            
                            if (currentTask.getParentId() == null || currentTask.getParentId() == 0) {
                                break;
                            }
                            currentTask = allTaskMap.get(currentTask.getParentId());
                            currentDepth++;
                        }
                        
                        String displayName = String.join("-", taskNames);
                        
                        TaskOverviewItem item = new TaskOverviewItem(
                                info.getProjectName(),
                                "影子任务",
                                displayName,
                                info.getPriority(),
                                info.getStatus(),
                                info.getProgress(),
                                info.getOwnerName(),
                                info.getStartDate(),
                                info.getEndDate(),
                                info.getCreateTime(),
                                info.getUpdateTime(),
                                info.getLastProgressUpdateTime(),
                                progressDetails);
                        taskOverviewList.add(item);
                    }
                } else {
                    // 真实任务
                    PmTask realTask = realTaskMap.get(info.getRealTaskId());
                    if (realTask != null) {
                        TaskOverviewItem item = new TaskOverviewItem(
                                info.getProjectName(),
                                "源任务",
                                fullTaskName,
                                info.getPriority(),
                                info.getStatus(),
                                info.getProgress(),
                                info.getOwnerName(),
                                info.getStartDate(),
                                info.getEndDate(),
                                info.getCreateTime(),
                                info.getUpdateTime(),
                                info.getLastProgressUpdateTime(),
                                progressDetails);
                        taskOverviewList.add(item);
                    }
                }
            }
        }

        // 6. 构建报告数据对象
        TaskProgressReportData reportData = new TaskProgressReportData(
                taskOverviewList,
                new ArrayList<>());

        // 7. 导出 Excel
        String reportName = "任务进展报告";
        ExcelExportUtil.exportTaskProgressReport(response, reportName, reportData);
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

