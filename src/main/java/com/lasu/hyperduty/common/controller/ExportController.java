package com.lasu.hyperduty.common.controller;

import com.lasu.hyperduty.common.service.impl.ExportServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
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
}
