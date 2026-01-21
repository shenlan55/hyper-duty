package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyHoliday;
import com.lasu.hyperduty.mapper.DutyHolidayMapper;
import com.lasu.hyperduty.service.DutyHolidayService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 节假日服务实现类
 */
@Service
public class DutyHolidayServiceImpl extends ServiceImpl<DutyHolidayMapper, DutyHoliday> implements DutyHolidayService {

    @Override
    public boolean isHoliday(LocalDate date) {
        QueryWrapper<DutyHoliday> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("holiday_date", date);
        DutyHoliday holiday = this.getOne(queryWrapper);
        
        // 如果是节假日且不是调休工作日
        return holiday != null && holiday.getIsWorkday() != 1;
    }

    @Override
    public boolean isWorkday(LocalDate date) {
        QueryWrapper<DutyHoliday> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("holiday_date", date);
        DutyHoliday holiday = this.getOne(queryWrapper);
        
        if (holiday != null) {
            // 如果是调休工作日
            return holiday.getIsWorkday() == 1;
        } else {
            // 非节假日的周一到周五是工作日
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
        }
    }

    @Override
    public List<DutyHoliday> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<DutyHoliday> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("holiday_date", startDate, endDate);
        return this.list(queryWrapper);
    }

    @Override
    public List<LocalDate> getWorkdaysInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> workdays = new ArrayList<>();
        List<DutyHoliday> holidays = getHolidaysInRange(startDate, endDate);
        
        // 转换为日期Map，键为日期，值为是否是调休工作日
        Map<LocalDate, Integer> holidayMap = holidays.stream()
                .collect(Collectors.toMap(DutyHoliday::getHolidayDate, DutyHoliday::getIsWorkday));
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            Integer isWorkday = holidayMap.get(currentDate);
            
            if (isWorkday != null) {
                // 节假日记录中标记为工作日的调休日
                if (isWorkday == 1) {
                    workdays.add(currentDate);
                }
            } else {
                // 非节假日的周一到周五是工作日
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    workdays.add(currentDate);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return workdays;
    }

    @Override
    public List<LocalDate> getNonWorkdaysInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> nonWorkdays = new ArrayList<>();
        List<DutyHoliday> holidays = getHolidaysInRange(startDate, endDate);
        
        // 转换为日期Map
        Set<LocalDate> holidayDates = holidays.stream()
                .map(DutyHoliday::getHolidayDate)
                .collect(Collectors.toSet());
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // 周六周日或者节假日（且不是调休工作日）
            if ((dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) || 
                holidayDates.contains(currentDate) && !isWorkday(currentDate)) {
                nonWorkdays.add(currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return nonWorkdays;
    }

    @Override
    public boolean importHolidays(List<DutyHoliday> holidays) {
        // 批量导入前先删除已存在的同日期数据
        List<LocalDate> dates = holidays.stream()
                .map(DutyHoliday::getHolidayDate)
                .collect(Collectors.toList());
        
        if (!dates.isEmpty()) {
            QueryWrapper<DutyHoliday> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("holiday_date", dates);
            this.remove(queryWrapper);
        }
        
        // 批量插入新数据
        return this.saveBatch(holidays);
    }
}