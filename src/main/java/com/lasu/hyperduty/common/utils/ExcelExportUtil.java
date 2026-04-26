package com.lasu.hyperduty.common.utils;

import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.entity.DutyRecord;
import com.lasu.hyperduty.duty.entity.LeaveRequest;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.entity.PmTask;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;









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

    public static void exportGantt(HttpServletResponse response, PmProject project, List<PmTask> tasks) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        String sheetName = (project != null && project.getProjectName() != null) ? project.getProjectName() : "甘特图";
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 28) + "...";
        }
        XSSFSheet sheet = workbook.createSheet(sheetName);

        if (tasks == null || tasks.isEmpty()) {
            String[] headers = {"提示"};
            createHeaderRow(sheet, headers);
            sheet.createRow(1).createCell(0).setCellValue("暂无任务数据");
            autoSizeColumns(sheet);
            writeResponse(response, workbook, project);
            return;
        }

        LocalDate minDate = null;
        LocalDate maxDate = null;
        
        for (PmTask task : tasks) {
            if (task.getStartDate() != null) {
                if (minDate == null || task.getStartDate().isBefore(minDate)) {
                    minDate = task.getStartDate();
                }
            }
            if (task.getEndDate() != null) {
                if (maxDate == null || task.getEndDate().isAfter(maxDate)) {
                    maxDate = task.getEndDate();
                }
            }
        }

        if (minDate == null || maxDate == null) {
            String[] headers = {"提示"};
            createHeaderRow(sheet, headers);
            sheet.createRow(1).createCell(0).setCellValue("任务缺少日期信息");
            autoSizeColumns(sheet);
            writeResponse(response, workbook, project);
            return;
        }

        minDate = minDate.minusDays(3);
        maxDate = maxDate.plusDays(3);

        List<LocalDate> dateList = new java.util.ArrayList<>();
        LocalDate current = minDate;
        while (!current.isAfter(maxDate)) {
            dateList.add(current);
            current = current.plusDays(1);
        }

        int taskColCount = 4;
        int totalCols = taskColCount + dateList.size();

        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle dateHeaderStyle = createDateHeaderStyle(workbook);
        XSSFCellStyle weekendStyle = createWeekendStyle(workbook);
        XSSFCellStyle todayStyle = createTodayStyle(workbook);
        XSSFCellStyle taskNameStyle = createTaskNameStyle(workbook);

        XSSFRow headerRow = sheet.createRow(0);
        
        String[] taskHeaders = {"任务名称", "优先级", "状态", "进度"};
        for (int i = 0; i < taskHeaders.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(taskHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < dateList.size(); i++) {
            LocalDate date = dateList.get(i);
            XSSFCell cell = headerRow.createCell(taskColCount + i);
            cell.setCellValue(date.getMonthValue() + "/" + date.getDayOfMonth());
            
            if (date.getDayOfWeek().getValue() >= 6) {
                cell.setCellStyle(weekendStyle);
            } else if (date.equals(LocalDate.now())) {
                cell.setCellStyle(todayStyle);
            } else {
                cell.setCellStyle(dateHeaderStyle);
            }
        }

        XSSFCellStyle borderedStyle = createBorderedStyle(workbook);
        
        for (int i = 0; i < tasks.size(); i++) {
            PmTask task = tasks.get(i);
            XSSFRow row = sheet.createRow(i + 1);
            
            String taskNameWithIndent = "";
            if (task.getTaskLevel() != null) {
                for (int j = 1; j < task.getTaskLevel(); j++) {
                    taskNameWithIndent += "  ";
                }
            }
            taskNameWithIndent += task.getTaskName() != null ? task.getTaskName() : "";
            
            XSSFCell taskNameCell = row.createCell(0);
            taskNameCell.setCellValue(taskNameWithIndent);
            taskNameCell.setCellStyle(taskNameStyle);
            
            XSSFCell priorityCell = row.createCell(1);
            priorityCell.setCellValue(getPriorityName(task.getPriority()));
            priorityCell.setCellStyle(borderedStyle);
            
            XSSFCell statusCell = row.createCell(2);
            statusCell.setCellValue(getTaskStatusName(task.getStatus()));
            statusCell.setCellStyle(borderedStyle);
            
            XSSFCell progressCell = row.createCell(3);
            progressCell.setCellValue(task.getProgress() != null ? task.getProgress() + "%" : "0%");
            progressCell.setCellStyle(borderedStyle);
            
            if (task.getStartDate() != null && task.getEndDate() != null) {
                int startIndex = dateList.indexOf(task.getStartDate());
                int endIndex = dateList.indexOf(task.getEndDate());
                
                if (startIndex != -1 && endIndex != -1) {
                    for (int j = startIndex; j <= endIndex; j++) {
                        XSSFCell cell = row.createCell(taskColCount + j);
                        XSSFCellStyle style = createBarStyle(workbook, task.getStatus(), task.getProgress(), j == startIndex, j == endIndex);
                        cell.setCellStyle(style);
                    }
                    
                    if (task.getProgress() != null && task.getProgress() > 0) {
                        int progressEndIndex = startIndex + (int) Math.round((endIndex - startIndex + 1) * task.getProgress() / 100.0);
                        int actualProgressEnd = Math.min(progressEndIndex, endIndex);
                        
                        for (int j = startIndex; j <= actualProgressEnd; j++) {
                            XSSFCell cell = row.getCell(taskColCount + j);
                            if (cell != null) {
                                XSSFCellStyle style = createProgressStyle(workbook, task.getStatus(), task.getProgress(), j == startIndex, j == actualProgressEnd);
                                cell.setCellStyle(style);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < taskColCount; i++) {
            sheet.setColumnWidth(i, 4000);
        }
        for (int i = 0; i < dateList.size(); i++) {
            sheet.setColumnWidth(taskColCount + i, 800);
        }

        sheet.createFreezePane(taskColCount, 1);

        writeResponse(response, workbook, project);
    }

    private static void writeResponse(HttpServletResponse response, XSSFWorkbook workbook, PmProject project) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode((project != null ? project.getProjectName() : "项目") + "_甘特图.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static XSSFCellStyle createDateHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createWeekendStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createTodayStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static XSSFCellStyle createTaskNameStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static XSSFCellStyle createBorderedStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static XSSFCellStyle createBarStyle(XSSFWorkbook workbook, Integer status, Integer progress, boolean isStart, boolean isEnd) {
        XSSFCellStyle style = workbook.createCellStyle();
        IndexedColors color;
        if (status != null && status == 3) {
            color = IndexedColors.BRIGHT_GREEN;
        } else if (status != null && status == 4) {
            color = IndexedColors.GREY_50_PERCENT;
        } else {
            color = IndexedColors.GREY_25_PERCENT;
        }
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        if (isStart) {
            style.setBorderLeft(BorderStyle.THIN);
        }
        if (isEnd) {
            style.setBorderRight(BorderStyle.THIN);
        }
        return style;
    }

    private static XSSFCellStyle createProgressStyle(XSSFWorkbook workbook, Integer status, Integer progress, boolean isStart, boolean isEnd) {
        XSSFCellStyle style = workbook.createCellStyle();
        IndexedColors color;
        if (status != null && status == 3) {
            color = IndexedColors.BRIGHT_GREEN;
        } else if (status != null && status == 4) {
            color = IndexedColors.GREY_50_PERCENT;
        } else if (progress != null && progress >= 60) {
            color = IndexedColors.BRIGHT_GREEN;
        } else if (progress != null && progress >= 30) {
            color = IndexedColors.GOLD;
        } else {
            color = IndexedColors.RED;
        }
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        if (isStart) {
            style.setBorderLeft(BorderStyle.THIN);
        }
        if (isEnd) {
            style.setBorderRight(BorderStyle.THIN);
        }
        return style;
    }

    private static String getPriorityName(Integer priority) {
        if (priority == null) return "";
        switch (priority) {
            case 1: return "高";
            case 2: return "中";
            case 3: return "低";
            default: return "未知";
        }
    }

    private static String getTaskStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "未开始";
            case 2: return "进行中";
            case 3: return "已完成";
            case 4: return "已暂停";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}
