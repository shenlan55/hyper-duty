package com.lasu.hyperduty.common.service.impl;

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
import com.lasu.hyperduty.common.utils.ExcelExportUtil;
import com.lasu.hyperduty.common.utils.ExcelExportUtil.TaskProgressReportData;
import com.lasu.hyperduty.common.utils.ExcelExportUtil.TaskOverviewItem;
import com.lasu.hyperduty.common.utils.ExcelExportUtil.ProgressHistoryItem;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;









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
        if (sourceTasks != null) {
            allTaskIds.addAll(sourceTasks.stream().map(PmTask::getId).collect(Collectors.toList()));
        }
        if (shadowTasks != null) {
            allTaskIds.addAll(shadowTasks.stream().map(PmTaskShadow::getSourceTaskId).collect(Collectors.toList()));
        }

        // 4. 查询进展历史
        List<PmTaskProgressUpdate> progressHistory = pmTaskProgressUpdateMapper.selectProgressHistoryForReport(
                allTaskIds.isEmpty() ? null : allTaskIds,
                queryDTO.getProgressUpdateTimeFrom(),
                queryDTO.getProgressUpdateTimeTo()
        );

        // 5. 构建报告数据
        List<TaskOverviewItem> taskOverviewList = new ArrayList<>();
        
        // 添加源任务
        if (sourceTasks != null) {
            for (PmTask task : sourceTasks) {
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
                        task.getLastProgressUpdateTime()
                );
                taskOverviewList.add(item);
            }
        }

        // 添加影子任务
        if (shadowTasks != null) {
            for (PmTaskShadow shadow : shadowTasks) {
                String taskName = shadow.getShadowAlias() != null && !shadow.getShadowAlias().isEmpty()
                        ? shadow.getShadowAlias()
                        : shadow.getSourceTaskName();
                
                TaskOverviewItem item = new TaskOverviewItem(
                        shadow.getTargetProjectName(),
                        "影子任务",
                        taskName,
                        shadow.getSourcePriority(),
                        shadow.getSourceStatus(),
                        shadow.getSourceProgress(),
                        shadow.getSourceOwnerName(),
                        shadow.getSourceStartDate(),
                        shadow.getSourceEndDate(),
                        shadow.getCreatedAt(),
                        shadow.getUpdatedAt(),
                        shadow.getLastProgressUpdateTime()
                );
                taskOverviewList.add(item);
            }
        }

        // 6. 构建进展历史数据
        List<ProgressHistoryItem> progressHistoryList = new ArrayList<>();
        if (progressHistory != null) {
            for (PmTaskProgressUpdate update : progressHistory) {
                ProgressHistoryItem item = new ProgressHistoryItem(
                        update.getProjectName(),
                        update.getTaskName(),
                        update.getEmployeeName(),
                        update.getCreateTime(),
                        update.getProgress(),
                        update.getDescription()
                );
                progressHistoryList.add(item);
            }
        }

        // 7. 构建报告数据对象
        TaskProgressReportData reportData = new TaskProgressReportData(
                taskOverviewList,
                progressHistoryList
        );

        // 8. 导出 Excel
        String reportName = "任务进展报告";
        ExcelExportUtil.exportTaskProgressReport(response, reportName, reportData);
    }
}
