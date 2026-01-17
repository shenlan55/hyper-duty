package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
import com.lasu.hyperduty.service.LeaveRequestService;
import com.lasu.hyperduty.service.DutyStatisticsService;
import com.lasu.hyperduty.util.ExcelExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("overall", dutyStatisticsService.getOverallStatistics());
        statistics.put("dept", dutyStatisticsService.getDeptStatistics(null));
        statistics.put("shift", dutyStatisticsService.getShiftDistribution());
        statistics.put("trend", dutyStatisticsService.getMonthlyTrend());
        ExcelExportUtil.exportStatistics(response, statistics);
    }
}
