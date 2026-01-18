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

}