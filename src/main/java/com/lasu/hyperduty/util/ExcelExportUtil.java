package com.lasu.hyperduty.util;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.entity.SysEmployee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExcelExportUtil {

    public static void exportDutyAssignments(HttpServletResponse response, List<DutyAssignment> assignments) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("值班安排");

        String[] headers = {"ID", "值班表ID", "值班日期", "班次", "值班人员", "状态", "备注", "创建时间"};
        String[] fields = {"id", "scheduleId", "dutyDate", "dutyShift", "employeeId", "status", "remark", "createTime"};

        createHeaderRow(sheet, headers);

        int rowNum = 1;
        for (DutyAssignment assignment : assignments) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(assignment.getId());
            row.createCell(1).setCellValue(assignment.getScheduleId());
            row.createCell(2).setCellValue(assignment.getDutyDate() != null ? assignment.getDutyDate().toString() : "");
            row.createCell(3).setCellValue(getShiftName(assignment.getDutyShift()));
            row.createCell(4).setCellValue(assignment.getEmployeeId() != null ? assignment.getEmployeeId().toString() : "");
            row.createCell(5).setCellValue(assignment.getStatus() != null ? assignment.getStatus().toString() : "");
            row.createCell(6).setCellValue(assignment.getRemark() != null ? assignment.getRemark() : "");
            row.createCell(7).setCellValue(assignment.getCreateTime() != null ? assignment.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }

        autoSizeColumns(sheet);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("值班安排.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public static void exportDutyRecords(HttpServletResponse response, List<DutyRecord> records) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("值班记录");

        String[] headers = {"ID", "值班安排ID", "值班人员", "签到时间", "签退时间", "值班状态", "签到备注", "签退备注", "加班时长", "审批状态", "创建时间"};
        String[] fields = {"id", "assignmentId", "employeeId", "checkInTime", "checkOutTime", "dutyStatus", "checkInRemark", "checkOutRemark", "overtimeHours", "approvalStatus", "createTime"};

        createHeaderRow(sheet, headers);

        int rowNum = 1;
        for (DutyRecord record : records) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getId());
            row.createCell(1).setCellValue(record.getAssignmentId());
            row.createCell(2).setCellValue(record.getEmployeeId() != null ? record.getEmployeeId().toString() : "");
            row.createCell(3).setCellValue(record.getCheckInTime() != null ? record.getCheckInTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            row.createCell(4).setCellValue(record.getCheckOutTime() != null ? record.getCheckOutTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            row.createCell(5).setCellValue(getDutyStatusName(record.getDutyStatus()));
            row.createCell(6).setCellValue(record.getCheckInRemark() != null ? record.getCheckInRemark() : "");
            row.createCell(7).setCellValue(record.getCheckOutRemark() != null ? record.getCheckOutRemark() : "");
            row.createCell(8).setCellValue(record.getOvertimeHours() != null ? record.getOvertimeHours().toString() : "");
            row.createCell(9).setCellValue(record.getApprovalStatus() != null ? record.getApprovalStatus() : "");
            row.createCell(10).setCellValue(record.getCreateTime() != null ? record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }

        autoSizeColumns(sheet);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("值班记录.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public static void exportLeaveRequests(HttpServletResponse response, List<LeaveRequest> requests) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("请假申请");

        String[] headers = {"ID", "申请编号", "申请人", "请假类型", "开始日期", "结束日期", "请假时长", "请假原因", "审批状态", "替补人员", "创建时间"};
        String[] fields = {"id", "requestNo", "employeeId", "leaveType", "startDate", "endDate", "totalHours", "reason", "approvalStatus", "substituteEmployeeId", "createTime"};

        createHeaderRow(sheet, headers);

        int rowNum = 1;
        for (LeaveRequest request : requests) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(request.getId());
            row.createCell(1).setCellValue(request.getRequestNo() != null ? request.getRequestNo() : "");
            row.createCell(2).setCellValue(request.getEmployeeId() != null ? request.getEmployeeId().toString() : "");
            row.createCell(3).setCellValue(getLeaveTypeName(request.getLeaveType()));
            row.createCell(4).setCellValue(request.getStartDate() != null ? request.getStartDate().toString() : "");
            row.createCell(5).setCellValue(request.getEndDate() != null ? request.getEndDate().toString() : "");
            row.createCell(6).setCellValue(request.getTotalHours() != null ? request.getTotalHours().toString() : "");
            row.createCell(7).setCellValue(request.getReason() != null ? request.getReason() : "");
            row.createCell(8).setCellValue(request.getApprovalStatus() != null ? request.getApprovalStatus() : "");
            row.createCell(9).setCellValue(request.getSubstituteEmployeeId() != null ? request.getSubstituteEmployeeId().toString() : "");
            row.createCell(10).setCellValue(request.getCreateTime() != null ? request.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }

        autoSizeColumns(sheet);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("请假申请.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public static void exportStatistics(HttpServletResponse response, Map<String, Object> statistics) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        XSSFSheet overviewSheet = workbook.createSheet("总体统计");
        String[] overviewHeaders = {"指标", "数值"};
        createHeaderRow(overviewSheet, overviewHeaders);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> overall = (Map<String, Object>) statistics.get("overall");
        if (overall != null) {
            int rowNum = 1;
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("总排班数");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("totalSchedules") != null ? overall.get("totalSchedules").toString() : "0");
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("总值班安排");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("totalAssignments") != null ? overall.get("totalAssignments").toString() : "0");
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("总值班记录");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("totalRecords") != null ? overall.get("totalRecords").toString() : "0");
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("日均时长(小时)");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("avgDailyHours") != null ? overall.get("avgDailyHours").toString() : "0");
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("总加班时长(小时)");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("totalOvertimeHours") != null ? overall.get("totalOvertimeHours").toString() : "0");
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("请假申请数");
            overviewSheet.getRow(rowNum - 1).createCell(1).setCellValue(overall.get("totalLeaveRequests") != null ? overall.get("totalLeaveRequests").toString() : "0");
        }
        
        autoSizeColumns(overviewSheet);
        
        XSSFSheet shiftSheet = workbook.createSheet("班次分布");
        String[] shiftHeaders = {"班次名称", "数量"};
        createHeaderRow(shiftSheet, shiftHeaders);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> shiftData = (List<Map<String, Object>>) statistics.get("shift");
        if (shiftData != null) {
            int rowNum = 1;
            for (Map<String, Object> shift : shiftData) {
                XSSFRow row = shiftSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(shift.get("shiftName") != null ? shift.get("shiftName").toString() : "");
                row.createCell(1).setCellValue(shift.get("count") != null ? shift.get("count").toString() : "0");
            }
        }
        
        autoSizeColumns(shiftSheet);
        
        XSSFSheet trendSheet = workbook.createSheet("月度趋势");
        String[] trendHeaders = {"月份", "出勤时长", "加班时长"};
        createHeaderRow(trendSheet, trendHeaders);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trendData = (List<Map<String, Object>>) statistics.get("trend");
        if (trendData != null) {
            int rowNum = 1;
            for (Map<String, Object> trend : trendData) {
                XSSFRow row = trendSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(trend.get("month") != null ? trend.get("month").toString() : "");
                row.createCell(1).setCellValue(trend.get("hours") != null ? trend.get("hours").toString() : "0");
                row.createCell(2).setCellValue(trend.get("overtime") != null ? trend.get("overtime").toString() : "0");
            }
        }
        
        autoSizeColumns(trendSheet);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("排班统计.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private static void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static String getShiftName(Integer shift) {
        if (shift == null) return "";
        switch (shift) {
            case 1: return "早班";
            case 2: return "中班";
            case 3: return "晚班";
            case 4: return "全天";
            case 5: return "夜班";
            default: return "未知";
        }
    }

    private static String getDutyStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "正常";
            case 2: return "迟到";
            case 3: return "早退";
            case 4: return "缺勤";
            case 5: return "请假";
            default: return "未知";
        }
    }

    private static String getLeaveTypeName(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "事假";
            case 2: return "病假";
            case 3: return "年假";
            case 4: return "调休";
            case 5: return "其他";
            default: return "未知";
        }
    }
}
