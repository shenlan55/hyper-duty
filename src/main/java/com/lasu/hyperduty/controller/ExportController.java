package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.service.impl.ExportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
}
