package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.service.DutyScheduleService;
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

    @GetMapping("/list")
    public ResponseResult<List<DutySchedule>> getAllSchedules() {
        List<DutySchedule> scheduleList = dutyScheduleService.list();
        return ResponseResult.success(scheduleList);
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

    @GetMapping("/{id}/leaders")
    public ResponseResult<List<Long>> getScheduleLeaders(@PathVariable Long id) {
        List<Long> leaderIds = dutyScheduleService.getLeaderIdsByScheduleId(id);
        return ResponseResult.success(leaderIds);
    }

    @PostMapping
    public ResponseResult<Void> addSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.save(dutySchedule);
        return ResponseResult.success();
    }

    @PutMapping
    public ResponseResult<Void> updateSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.updateById(dutySchedule);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/employees")
    public ResponseResult<Void> updateScheduleEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        dutyScheduleService.updateEmployees(id, employeeIds);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/employees-and-leaders")
    public ResponseResult<Void> updateScheduleEmployeesAndLeaders(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        List<Long> employeeIds = params.get("employeeIds");
        List<Long> leaderIds = params.get("leaderIds");
        dutyScheduleService.updateEmployeesAndLeaders(id, employeeIds, leaderIds);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/leaders")
    public ResponseResult<Void> updateScheduleLeaders(@PathVariable Long id, @RequestBody List<Long> leaderIds) {
        dutyScheduleService.updateLeaders(id, leaderIds);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteSchedule(@PathVariable Long id) {
        dutyScheduleService.removeById(id);
        return ResponseResult.success();
    }

}