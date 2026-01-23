package com.lasu.hyperduty.service.impl;

import com.lasu.hyperduty.entity.DutyHoliday;
import com.lasu.hyperduty.service.DutyHolidayService;
import com.lasu.hyperduty.util.HolidayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.List;

/**
 * 节假日任务执行器
 * 用于定时获取节假日信息
 */
@Slf4j
@Component
public class HolidayTaskExecutor {

    @Autowired
    private DutyHolidayService dutyHolidayService;

    /**
     * 执行节假日信息获取任务
     */
    public void execute() {
        log.info("开始执行节假日信息获取任务");
        
        try {
            // 获取当前年份
            int currentYear = Year.now().getValue();
            
            // 获取当前年份和下一年的节假日信息
            List<DutyHoliday> currentYearHolidays = HolidayUtil.getHolidays(currentYear);
            List<DutyHoliday> nextYearHolidays = HolidayUtil.getHolidays(currentYear + 1);
            
            // 合并节假日列表
            currentYearHolidays.addAll(nextYearHolidays);
            
            // 导入节假日数据
            boolean result = dutyHolidayService.importHolidays(currentYearHolidays);
            
            if (result) {
                log.info("节假日信息获取任务执行成功，共导入 {} 条节假日数据", currentYearHolidays.size());
            } else {
                log.warn("节假日信息获取任务执行失败");
            }
        } catch (Exception e) {
            log.error("执行节假日信息获取任务时发生异常", e);
        }
    }
}
