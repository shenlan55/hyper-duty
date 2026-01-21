package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 节假日服务接口
 */
public interface DutyHolidayService extends IService<DutyHoliday> {

    /**
     * 判断指定日期是否为节假日
     * @param date 日期
     * @return 是否为节假日
     */
    boolean isHoliday(LocalDate date);

    /**
     * 判断指定日期是否为工作日（包括调休）
     * @param date 日期
     * @return 是否为工作日
     */
    boolean isWorkday(LocalDate date);

    /**
     * 获取指定日期范围内的节假日列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日列表
     */
    List<DutyHoliday> getHolidaysInRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获取指定日期范围内的工作日列表（包括调休）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日列表
     */
    List<LocalDate> getWorkdaysInRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获取指定日期范围内的非工作日列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 非工作日列表
     */
    List<LocalDate> getNonWorkdaysInRange(LocalDate startDate, LocalDate endDate);

    /**
     * 批量导入节假日数据
     * @param holidays 节假日列表
     * @return 导入结果
     */
    boolean importHolidays(List<DutyHoliday> holidays);
}