package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.service.DutyScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duty/schedule")
public class DutyScheduleController {

    @Autowired
    private DutyScheduleService dutyScheduleService;

    /**
     * 获取所有值班表列表
     */
    @GetMapping("/list")
    public ResponseResult<List<DutySchedule>> getAllSchedules() {
        List<DutySchedule> scheduleList = dutyScheduleService.list();
        return ResponseResult.success(scheduleList);
    }

    /**
     * 根据ID获取值班表详情
     */
    @GetMapping("/{id}")
    public ResponseResult<DutySchedule> getScheduleById(@PathVariable Long id) {
        DutySchedule schedule = dutyScheduleService.getById(id);
        return ResponseResult.success(schedule);
    }

    /**
     * 添加值班表
     */
    @PostMapping
    public ResponseResult<Void> addSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.save(dutySchedule);
        return ResponseResult.success();
    }

    /**
     * 修改值班表
     */
    @PutMapping
    public ResponseResult<Void> updateSchedule(@Validated @RequestBody DutySchedule dutySchedule) {
        dutyScheduleService.updateById(dutySchedule);
        return ResponseResult.success();
    }

    /**
     * 删除值班表
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteSchedule(@PathVariable Long id) {
        dutyScheduleService.removeById(id);
        return ResponseResult.success();
    }

}