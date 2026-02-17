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

/**
 * 加班记录控制器
 * 处理加班记录相关的API请求
 */
@RestController
@RequestMapping("/duty/record")
public class DutyRecordController {

    @Autowired
    private DutyRecordService dutyRecordService;

    /**
     * 获取可用的替补人员列表
     * @param recordId 加班记录ID
     * @return 替补人员列表
     */
    @GetMapping("/substitutes/{recordId}")
    public ResponseResult<List<SysEmployee>> getAvailableSubstitutes(@PathVariable Long recordId) {
        List<SysEmployee> substitutes = dutyRecordService.getAvailableSubstitutes(recordId);
        return ResponseResult.success(substitutes);
    }

    /**
     * 获取所有加班记录
     * @return 加班记录列表
     */
    @GetMapping("/list")
    public ResponseResult<List<DutyRecord>> getAllRecords() {
        List<DutyRecord> recordList = dutyRecordService.list();
        return ResponseResult.success(recordList);
    }

    /**
     * 根据员工ID获取加班记录
     * @param employeeId 员工ID
     * @return 加班记录列表
     */
    @GetMapping("/list/employee/{employeeId}")
    public ResponseResult<List<DutyRecord>> getRecordsByEmployeeId(@PathVariable Long employeeId) {
        List<DutyRecord> recordList = dutyRecordService.lambdaQuery()
                .eq(DutyRecord::getEmployeeId, employeeId)
                .list();
        return ResponseResult.success(recordList);
    }

    /**
     * 添加加班记录
     * @param dutyRecord 加班记录信息
     * @return 操作结果
     */
    @PostMapping
    public ResponseResult<Void> addRecord(@Validated @RequestBody DutyRecord dutyRecord) {
        dutyRecordService.save(dutyRecord);
        return ResponseResult.success();
    }

    /**
     * 加班签到
     * @param assignmentId 值班安排ID
     * @param dutyRecord 签到信息
     * @return 操作结果
     */
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

    /**
     * 加班签退
     * @param id 加班记录ID
     * @param dutyRecord 签退信息
     * @return 操作结果
     */
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

    /**
     * 更新加班记录
     * @param dutyRecord 加班记录信息
     * @return 操作结果
     */
    @PutMapping
    public ResponseResult<Void> updateRecord(@Validated @RequestBody DutyRecord dutyRecord) {
        dutyRecordService.updateById(dutyRecord);
        return ResponseResult.success();
    }

    /**
     * 删除加班记录
     * @param id 加班记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteRecord(@PathVariable Long id) {
        dutyRecordService.removeById(id);
        return ResponseResult.success();
    }

    /**
     * 获取待审批的加班记录
     * @param employeeId 审批人ID
     * @return 待审批的加班记录列表
     */
    @GetMapping("/pending/{employeeId}")
    public ResponseResult<List<DutyRecord>> getPendingApprovals(@PathVariable Long employeeId) {
        List<DutyRecord> recordList = dutyRecordService.getPendingApprovals(employeeId);
        return ResponseResult.success(recordList);
    }

}