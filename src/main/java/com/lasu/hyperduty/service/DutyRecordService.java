package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;

import java.time.LocalDate;
import java.util.List;

public interface DutyRecordService extends IService<DutyRecord> {

    List<SysEmployee> getAvailableSubstitutes(Long recordId);

    List<SysEmployee> getAvailableSubstitutes(Long employeeId, LocalDate dutyDate, Integer dutyShift);
}