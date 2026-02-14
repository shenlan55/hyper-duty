package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.DutyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/duty/record")
public class DutyRecordController {

    @Autowired
    private DutyRecordService dutyRecordService;

    @GetMapping("/substitutes/{recordId}")
    public ResponseResult<List<SysEmployee>> getAvailableSubstitutes(@PathVariable Long recordId) {
        List<SysEmployee> substitutes = dutyRecordService.getAvailableSubstitutes(recordId);
        return ResponseResult.success(substitutes);
    }

    @GetMapping("/list")
    public ResponseResult<List<DutyRecord>> getAllRecords() {
        List<DutyRecord> recordList = dutyRecordService.list();
        return ResponseResult.success(recordList);
    }

    @GetMapping("/list/employee/{employeeId}")
    public ResponseResult<List<DutyRecord>> getRecordsByEmployeeId(@PathVariable Long employeeId) {
        List<DutyRecord> recordList = dutyRecordService.lambdaQuery()
                .eq(DutyRecord::getEmployeeId, employeeId)
                .list();
        return ResponseResult.success(recordList);
    }

    @PostMapping
    public ResponseResult<Void> addRecord(@Validated @RequestBody DutyRecord dutyRecord) {
        dutyRecordService.save(dutyRecord);
        return ResponseResult.success();
    }

    @PostMapping("/check-in/{assignmentId}")
    public ResponseResult<Void> checkIn(@PathVariable Long assignmentId, @RequestBody DutyRecord dutyRecord) {
        // 检查是否已经存在该assignmentId的记录
        DutyRecord existingRecord = dutyRecordService.lambdaQuery()
                .eq(DutyRecord::getAssignmentId, assignmentId)
                .one();
        
        if (existingRecord != null) {
            // 已存在记录，更新签到信息
            existingRecord.setEmployeeId(dutyRecord.getEmployeeId());
            // 优先使用前端传递的时间值，如果没有则使用当前时间
            if (dutyRecord.getCheckInTime() != null) {
                existingRecord.setCheckInTime(dutyRecord.getCheckInTime());
            } else {
                existingRecord.setCheckInTime(LocalDateTime.now());
            }
            existingRecord.setCheckInRemark(dutyRecord.getCheckInRemark());
            existingRecord.setDutyStatus(1);
            dutyRecordService.updateById(existingRecord);
        } else {
            // 不存在记录，创建新记录
            dutyRecord.setAssignmentId(assignmentId);
            // 优先使用前端传递的时间值，如果没有则使用当前时间
            if (dutyRecord.getCheckInTime() == null) {
                dutyRecord.setCheckInTime(LocalDateTime.now());
            }
            dutyRecord.setDutyStatus(1);
            dutyRecordService.save(dutyRecord);
        }
        return ResponseResult.success();
    }

    @PostMapping("/check-out/{id}")
    public ResponseResult<Void> checkOut(@PathVariable Long id, @RequestBody DutyRecord dutyRecord) {
        DutyRecord existingRecord = dutyRecordService.getById(id);
        if (existingRecord != null) {
            // 优先使用前端传递的时间值，如果没有则使用当前时间
            if (dutyRecord.getCheckOutTime() != null) {
                existingRecord.setCheckOutTime(dutyRecord.getCheckOutTime());
            } else {
                existingRecord.setCheckOutTime(LocalDateTime.now());
            }
            existingRecord.setDutyStatus(2);
            existingRecord.setCheckOutRemark(dutyRecord.getCheckOutRemark());
            existingRecord.setOvertimeHours(dutyRecord.getOvertimeHours());
            dutyRecordService.updateById(existingRecord);
        }
        return ResponseResult.success();
    }

    @PutMapping
    public ResponseResult<Void> updateRecord(@Validated @RequestBody DutyRecord dutyRecord) {
        dutyRecordService.updateById(dutyRecord);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteRecord(@PathVariable Long id) {
        dutyRecordService.removeById(id);
        return ResponseResult.success();
    }

}