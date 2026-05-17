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
        
        // 1. 查询源任务
        List<PmTask> sourceTasks = pmTaskMapper.selectTasksForReport(
                queryDTO.getProjectIds(),
                queryDTO.getTaskStartDateFrom(),
                queryDTO.getTaskStartDateTo(),
                queryDTO.getTaskEndDateFrom(),
                queryDTO.getTaskEndDateTo()
        );

        // 2. 查询影子任务
        List<PmTaskShadow> shadowTasks = pmTaskShadowMapper.selectShadowTasksForReport(
                queryDTO.getProjectIds(),
                queryDTO.getTaskStartDateFrom(),
                queryDTO.getTaskStartDateTo(),
                queryDTO.getTaskEndDateFrom(),
                queryDTO.getTaskEndDateTo()
        );

        // 3. 收集所有任务ID
        List<Long> allTaskIds = new ArrayList<>();
        Map<Long, PmTask> sourceTaskMap = new HashMap<>();
        if (sourceTasks != null) {
            for (PmTask task : sourceTasks) {
                sourceTaskMap.put(task.getId(), task);
                allTaskIds.add(task.getId());
            }
        }
        
        Map<Long, List<PmTaskShadow>> shadowTaskMap = new HashMap<>();
        if (shadowTasks != null) {
            for (PmTaskShadow shadow : shadowTasks) {
                allTaskIds.add(shadow.getSourceTaskId());
                shadowTaskMap.computeIfAbsent(shadow.getSourceTaskId(), k -> new ArrayList<>()).add(shadow);
            }
        }

        // 4. 查询进展历史 - 查询所有相关任务的所有进展，不限制时间
        List<PmTaskProgressUpdate> progressHistory = pmTaskProgressUpdateMapper.selectProgressHistoryForReport(
                allTaskIds.isEmpty() ? null : allTaskIds,
                null, // 不限制开始时间
                null  // 不限制结束时间
        );

        // 5. 按任务ID分组进展历史
        Map<Long, List<PmTaskProgressUpdate>> progressMap = new HashMap<>();
        if (progressHistory != null) {
            for (PmTaskProgressUpdate update : progressHistory) {
                progressMap.computeIfAbsent(update.getTaskId(), k -> new ArrayList<>()).add(update);
            }
        }

        // 6. 构建报告数据
        List<TaskOverviewItem> taskOverviewList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Pattern htmlPattern = Pattern.compile("<[^>]*>");
        
        // 添加源任务
        if (sourceTasks != null) {
            for (PmTask task : sourceTasks) {
                // 获取该任务的进展历史
                List<PmTaskProgressUpdate> updates = progressMap.getOrDefault(task.getId(), new ArrayList<>());
                // 按时间倒序排列
                updates.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                
                // 构建进展详情
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
                        progressDetails.toString()
                );
                taskOverviewList.add(item);
                
                // 添加该任务的影子任务
                List<PmTaskShadow> shadows = shadowTaskMap.getOrDefault(task.getId(), new ArrayList<>());
                for (PmTaskShadow shadow : shadows) {
                    String shadowTaskName = shadow.getShadowAlias() != null && !shadow.getShadowAlias().isEmpty()
                            ? shadow.getShadowAlias()
                            : task.getTaskName();
                    
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
                            progressDetails.toString() // 影子任务使用源任务的进展历史
                    );
                    taskOverviewList.add(shadowItem);
                }
            }
        }

        // 7. 构建报告数据对象 - 不再需要单独的进展历史列表
        TaskProgressReportData reportData = new TaskProgressReportData(
                taskOverviewList,
                new ArrayList<>()
        );

        // 8. 导出 Excel
        String reportName = "任务进展报告";
        ExcelExportUtil.exportTaskProgressReport(response, reportName, reportData);
    }
}
