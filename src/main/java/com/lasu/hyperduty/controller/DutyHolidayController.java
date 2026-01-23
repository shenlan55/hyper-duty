package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.entity.DutyHoliday;
import com.lasu.hyperduty.service.DutyHolidayService;
import com.lasu.hyperduty.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 节假日管理控制器
 */
@RestController
@RequestMapping("/duty/holiday")
public class DutyHolidayController {

    @Autowired
    private DutyHolidayService dutyHolidayService;

    /**
     * 判断指定日期是否为节假日
     * @param date 日期
     * @return 是否为节假日
     */
    @GetMapping("/is-holiday")
    public ResponseResult<Boolean> isHoliday(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        boolean result = dutyHolidayService.isHoliday(date);
        return ResponseResult.success(result);
    }

    /**
     * 判断指定日期是否为工作日
     * @param date 日期
     * @return 是否为工作日
     */
    @GetMapping("/is-workday")
    public ResponseResult<Boolean> isWorkday(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        boolean result = dutyHolidayService.isWorkday(date);
        return ResponseResult.success(result);
    }

    /**
     * 获取指定日期范围内的节假日列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日列表
     */
    @GetMapping("/range")
    public ResponseResult<List<DutyHoliday>> getHolidaysInRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<DutyHoliday> holidays = dutyHolidayService.getHolidaysInRange(startDate, endDate);
        return ResponseResult.success(holidays);
    }

    /**
     * 获取指定日期范围内的工作日列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日列表
     */
    @GetMapping("/workdays")
    public ResponseResult<List<LocalDate>> getWorkdaysInRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<LocalDate> workdays = dutyHolidayService.getWorkdaysInRange(startDate, endDate);
        return ResponseResult.success(workdays);
    }

    /**
     * 获取指定日期范围内的非工作日列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 非工作日列表
     */
    @GetMapping("/non-workdays")
    public ResponseResult<List<LocalDate>> getNonWorkdaysInRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<LocalDate> nonWorkdays = dutyHolidayService.getNonWorkdaysInRange(startDate, endDate);
        return ResponseResult.success(nonWorkdays);
    }

    /**
     * 批量导入节假日数据
     * @param holidays 节假日列表
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseResult<Boolean> importHolidays(
            @RequestBody List<DutyHoliday> holidays) {
        boolean result = dutyHolidayService.importHolidays(holidays);
        return ResponseResult.success(result);
    }
}
