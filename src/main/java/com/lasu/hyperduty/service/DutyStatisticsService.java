package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyStatistics;

import java.util.List;
import java.util.Map;

public interface DutyStatisticsService extends IService<DutyStatistics> {

    Map<String, Object> getOverallStatistics();

    List<Map<String, Object>> getDeptStatistics(Long deptId);

    List<Map<String, Object>> getShiftDistribution();

    List<Map<String, Object>> getMonthlyTrend();

    List<Map<String, Object>> getEmployeeStatistics(Integer year, Integer month);
}
