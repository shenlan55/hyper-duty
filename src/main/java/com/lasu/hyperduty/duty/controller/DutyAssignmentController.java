package com.lasu.hyperduty.duty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.service.DutyAssignmentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








@RestController
@RequestMapping("/duty/assignment")
public class DutyAssignmentController {

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    /**
     * 获取所有值班安排列表
     */
    @GetMapping("/list")
    public ResponseResult<List<DutyAssignment>> getAllAssignments() {
        List<DutyAssignment> assignmentList = dutyAssignmentService.list();
        return ResponseResult.success(assignmentList);
    }

    /**
     * 根据值班表ID获取值班安排
     */
    @GetMapping("/list/{scheduleId}")
    public ResponseResult<List<DutyAssignment>> getAssignmentsByScheduleId(@PathVariable Long scheduleId) {
        List<DutyAssignment> assignmentList = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getScheduleId, scheduleId)
                .list();
        return ResponseResult.success(assignmentList);
    }

    /**
     * 添加值班安排
     */
    @PostMapping
    public ResponseResult<Void> addAssignment(@Validated @RequestBody DutyAssignment dutyAssignment) {
        dutyAssignmentService.save(dutyAssignment);
        return ResponseResult.success();
    }

    /**
     * 批量添加值班安排
     */
    @PostMapping("/batch")
    public ResponseResult<Void> addBatchAssignments(@Validated @RequestBody List<DutyAssignment> dutyAssignments) {
        dutyAssignmentService.saveBatch(dutyAssignments);
        return ResponseResult.success();
    }

    /**
     * 修改值班安排
     */
    @PutMapping
    public ResponseResult<Void> updateAssignment(@Validated @RequestBody DutyAssignment dutyAssignment) {
        dutyAssignmentService.updateById(dutyAssignment);
        return ResponseResult.success();
    }

    /**
     * 删除值班安排
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteAssignment(@PathVariable Long id) {
        dutyAssignmentService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除值班安排
     */
    @DeleteMapping("/batch-delete")
    public ResponseResult<Void> deleteBatchAssignments(
            @RequestParam Long scheduleId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        dutyAssignmentService.deleteByScheduleIdAndDateRange(scheduleId, startDate, endDate);
        return ResponseResult.success();
    }

    /**
     * 获取值班人员在特定值班表中的排班日期列表
     */
    @GetMapping("/dates/{scheduleId}/{employeeId}")
    public ResponseResult<List<String>> getEmployeeDutyDates(
            @PathVariable Long scheduleId,
            @PathVariable Long employeeId) {
        List<String> dutyDates = dutyAssignmentService.getEmployeeDutyDates(scheduleId, employeeId);
        return ResponseResult.success(dutyDates);
    }

    /**
     * 获取值班人员在特定日期的排班班次
     */
    @GetMapping("/shifts/{scheduleId}/{employeeId}/{date}")
    public ResponseResult<List<Integer>> getEmployeeDutyShifts(
            @PathVariable Long scheduleId,
            @PathVariable Long employeeId,
            @PathVariable String date) {
        List<Integer> dutyShifts = dutyAssignmentService.getEmployeeDutyShifts(scheduleId, employeeId, date);
        return ResponseResult.success(dutyShifts);
    }

    /**
     * 批量排班（传统方式）
     */
    @PostMapping("/batch-schedule")
    public ResponseResult<Integer> batchSchedule(@RequestBody BatchScheduleRequest request) {
        int successCount = dutyAssignmentService.batchSchedule(request);
        return ResponseResult.success(successCount);
    }

    // 批量排班请求参数
    public static class BatchScheduleRequest {
        private Long scheduleId;
        private String startDate;
        private String endDate;
        private Integer scheduleType; // 1=轮换排班，2=固定排班
        private Integer shiftEmployeeCount;
        private Integer dutyShift;
        private List<Long> employeeIds;
        private List<String> dateType;
        private List<String> filteredDates;
        private String remark;

        // Getters and Setters
        public Long getScheduleId() { return scheduleId; }
        public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Integer getScheduleType() { return scheduleType; }
        public void setScheduleType(Integer scheduleType) { this.scheduleType = scheduleType; }
        public Integer getShiftEmployeeCount() { return shiftEmployeeCount; }
        public void setShiftEmployeeCount(Integer shiftEmployeeCount) { this.shiftEmployeeCount = shiftEmployeeCount; }
        public Integer getDutyShift() { return dutyShift; }
        public void setDutyShift(Integer dutyShift) { this.dutyShift = dutyShift; }
        public List<Long> getEmployeeIds() { return employeeIds; }
        public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }
        public List<String> getDateType() { return dateType; }
        public void setDateType(List<String> dateType) { this.dateType = dateType; }
        public List<String> getFilteredDates() { return filteredDates; }
        public void setFilteredDates(List<String> filteredDates) { this.filteredDates = filteredDates; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

}