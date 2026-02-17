package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;

import java.time.LocalDate;
import java.util.List;

/**
 * 加班记录服务
 * 处理加班记录相关的业务逻辑
 */
public interface DutyRecordService extends IService<DutyRecord> {

    /**
     * 根据加班记录ID获取可用的替补人员列表
     * @param recordId 加班记录ID
     * @return 替补人员列表
     */
    List<SysEmployee> getAvailableSubstitutes(Long recordId);

    /**
     * 根据员工ID、加班日期和班次获取可用的替补人员列表
     * @param employeeId 员工ID
     * @param dutyDate 加班日期
     * @param dutyShift 班次
     * @return 替补人员列表
     */
    List<SysEmployee> getAvailableSubstitutes(Long employeeId, LocalDate dutyDate, Integer dutyShift);
}