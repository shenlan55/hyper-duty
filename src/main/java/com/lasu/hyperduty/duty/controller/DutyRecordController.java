package com.lasu.hyperduty.duty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.duty.entity.DutyRecord;
import com.lasu.hyperduty.duty.service.DutyRecordService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 获取所有加班记录（支持分页和筛选）
     * @param pageRequestDTO 分页参数
     * @param scheduleId 值班表ID
     * @param keyword 搜索关键词
     * @param date 值班日期
     * @return 加班记录分页列表
     */
    @GetMapping("/list")
    public ResponseResult<PageResponseDTO<DutyRecord>> getAllRecords(@ModelAttribute PageRequestDTO pageRequestDTO,
                                                                     @RequestParam(required = false) Long scheduleId,
                                                                     @RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) String date) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (scheduleId != null) {
            params.put("scheduleId", scheduleId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", keyword);
        }
        if (date != null && !date.isEmpty()) {
            params.put("date", date);
        }
        
        // 调用服务方法进行分页查询
        PageResponseDTO<DutyRecord> recordPage = dutyRecordService.page(pageRequestDTO, params);
        return ResponseResult.success(recordPage);
    }

    /**
     * 根据员工ID获取加班记录（支持分页和筛选）
     * @param employeeId 员工ID
     * @param pageRequestDTO 分页参数
     * @param scheduleId 值班表ID
     * @param keyword 搜索关键词
     * @param date 值班日期
     * @return 加班记录分页列表
     */
    @GetMapping("/list/employee/{employeeId}")
    public ResponseResult<PageResponseDTO<DutyRecord>> getRecordsByEmployeeId(@PathVariable Long employeeId,
                                                                              @ModelAttribute PageRequestDTO pageRequestDTO,
                                                                              @RequestParam(required = false) Long scheduleId,
                                                                              @RequestParam(required = false) String keyword,
                                                                              @RequestParam(required = false) String date) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        if (scheduleId != null) {
            params.put("scheduleId", scheduleId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", keyword);
        }
        if (date != null && !date.isEmpty()) {
            params.put("date", date);
        }
        
        // 调用服务方法进行分页查询
        PageResponseDTO<DutyRecord> recordPage = dutyRecordService.page(pageRequestDTO, params);
        return ResponseResult.success(recordPage);
    }

    /**
     * 添加加班记录
     * @param dutyRecord 加班记录信息
     * @return 操作结果
     */
    @PostMapping
    @RateLimit(window = 60, max = 20, message = "添加加班记录过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 10, message = "签到操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 10, message = "签退操作过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "更新加班记录过于频繁，请60秒后再试")
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
    @RateLimit(window = 60, max = 20, message = "删除加班记录过于频繁，请60秒后再试")
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