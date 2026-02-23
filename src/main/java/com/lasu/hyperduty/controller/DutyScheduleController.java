package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.DutyScheduleShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/duty/schedule")
public class DutyScheduleController {

    @Autowired
    private DutyScheduleService dutyScheduleService;
    
    @Autowired
    private DutyScheduleShiftService dutyScheduleShiftService;

    @GetMapping("/list")
    public ResponseResult<Page<DutySchedule>> getAllSchedules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        Page<DutySchedule> page = dutyScheduleService.getScheduleList(pageNum, pageSize, keyword, sortField, sortOrder);
        return ResponseResult.success(page);
    }

    @GetMapping("/all")
    public ResponseResult<List<DutySchedule>> getAllSchedules() {
        List<DutySchedule> schedules = dutyScheduleService.getAllSchedules();
        return ResponseResult.success(schedules);
    }

    @GetMapping("/{id}")
    public ResponseResult<DutySchedule> getScheduleById(@PathVariable Long id) {
        DutySchedule schedule = dutyScheduleService.getById(id);
        return ResponseResult.success(schedule);
    }

    @GetMapping("/{id}/employees")
    public ResponseResult<List<Long>> getScheduleEmployees(@PathVariable Long id) {
        List<Long> employeeIds = dutyScheduleService.getEmployeeIdsByScheduleId(id);
        return ResponseResult.success(employeeIds);
    }

    @GetMapping("/{id}/employees-with-leader-info")
    public ResponseResult<List<Map<String, Object>>> getScheduleEmployeesWithLeaderInfo(@PathVariable Long id) {
        List<Map<String, Object>> employees = dutyScheduleService.getScheduleEmployeesWithLeaderInfo(id);
        return ResponseResult.success(employees);
    }

    @GetMapping("/{id}/leaders")
    public ResponseResult<List<Long>> getScheduleLeaders(@PathVariable Long id) {
        List<Long> leaderIds = dutyScheduleService.getLeaderIdsByScheduleId(id);
        return ResponseResult.success(leaderIds);
    }

    @GetMapping("/{id}/shifts")
    public ResponseResult<List<Long>> getScheduleShifts(@PathVariable Long id) {
        List<Long> shiftConfigIds = dutyScheduleShiftService.getShiftConfigIdsByScheduleId(id);
        return ResponseResult.success(shiftConfigIds);
    }

    @PostMapping
    @RateLimit(window = 60, max = 20, message = "添加值班表操作过于频繁，请60秒后再试")
    public ResponseResult<Void> addSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.save(dutySchedule);
        return ResponseResult.success();
    }

    @PutMapping
    @RateLimit(window = 60, max = 20, message = "修改值班表操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.updateById(dutySchedule);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/employees")
    @RateLimit(window = 60, max = 20, message = "更新值班表员工操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateScheduleEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        dutyScheduleService.updateEmployees(id, employeeIds);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/employees-and-leaders")
    @RateLimit(window = 60, max = 20, message = "更新值班表员工和领导操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateScheduleEmployeesAndLeaders(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        List<Long> employeeIds = params.get("employeeIds");
        List<Long> leaderIds = params.get("leaderIds");
        dutyScheduleService.updateEmployeesAndLeaders(id, employeeIds, leaderIds);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/leaders")
    @RateLimit(window = 60, max = 20, message = "更新值班表领导操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateScheduleLeaders(@PathVariable Long id, @RequestBody List<Long> leaderIds) {
        dutyScheduleService.updateLeaders(id, leaderIds);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/shifts")
    @RateLimit(window = 60, max = 20, message = "更新值班表班次操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateScheduleShifts(@PathVariable Long id, @RequestBody List<Long> shiftConfigIds) {
        dutyScheduleShiftService.saveScheduleShifts(id, shiftConfigIds);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除值班表操作过于频繁，请60秒后再试")
    public ResponseResult<Void> deleteSchedule(@PathVariable Long id) {
        dutyScheduleService.removeById(id);
        return ResponseResult.success();
    }

}