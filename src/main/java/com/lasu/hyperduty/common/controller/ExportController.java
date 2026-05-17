package com.lasu.hyperduty.common.controller;

import com.lasu.hyperduty.common.service.impl.ExportServiceImpl;
import com.lasu.hyperduty.pm.dto.TaskProgressReportQueryDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








@RestController
@RequestMapping("/duty/export")
public class ExportController {

    @Autowired
    private ExportServiceImpl exportService;

    @GetMapping("/assignments")
    public void exportDutyAssignments(HttpServletResponse response) throws IOException {
        exportService.exportDutyAssignments(response);
    }

    @GetMapping("/records")
    public void exportDutyRecords(HttpServletResponse response) throws IOException {
        exportService.exportDutyRecords(response);
    }

    @GetMapping("/leave-requests")
    public void exportLeaveRequests(HttpServletResponse response) throws IOException {
        exportService.exportLeaveRequests(response);
    }

    @GetMapping("/statistics")
    public void exportStatistics(HttpServletResponse response) throws IOException {
        exportService.exportStatistics(response);
    }

    @GetMapping("/gantt")
    public void exportGantt(HttpServletResponse response, @RequestParam Long projectId) throws IOException {
        exportService.exportGantt(response, projectId);
    }

    /**
     * 导出任务进展报告
     */
    @GetMapping("/task-progress-report")
    public void exportTaskProgressReport(
            HttpServletResponse response,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate taskStartDateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate taskStartDateTo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate taskEndDateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate taskEndDateTo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime progressUpdateTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime progressUpdateTimeTo) throws IOException {
        
        TaskProgressReportQueryDTO queryDTO = new TaskProgressReportQueryDTO();
        queryDTO.setProjectIds(projectIds);
        queryDTO.setTaskStartDateFrom(taskStartDateFrom);
        queryDTO.setTaskStartDateTo(taskStartDateTo);
        queryDTO.setTaskEndDateFrom(taskEndDateFrom);
        queryDTO.setTaskEndDateTo(taskEndDateTo);
        queryDTO.setProgressUpdateTimeFrom(progressUpdateTimeFrom);
        queryDTO.setProgressUpdateTimeTo(progressUpdateTimeTo);
        
        exportService.exportTaskProgressReport(response, queryDTO);
    }
}
