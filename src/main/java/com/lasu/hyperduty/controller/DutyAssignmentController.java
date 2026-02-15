package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.service.DutyAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}