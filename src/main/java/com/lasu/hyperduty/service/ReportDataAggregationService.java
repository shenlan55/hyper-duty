package com.lasu.hyperduty.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReportDataAggregationService {

    /**
     * 聚合日报数据
     * @param reportDate 报告日期
     * @param projectId 项目ID（可选，为null则聚合所有项目）
     * @return 聚合后的数据Map，包含projectInfo和taskUpdates
     */
    Map<String, String> aggregateDailyData(LocalDate reportDate, Long projectId);

    /**
     * 聚合周报数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param projectId 项目ID（可选，为null则聚合所有项目）
     * @return 聚合后的数据Map，包含projectInfo和weeklyTaskData
     */
    Map<String, String> aggregateWeeklyData(LocalDate startDate, LocalDate endDate, Long projectId);
}
