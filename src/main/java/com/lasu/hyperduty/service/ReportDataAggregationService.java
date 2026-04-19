package com.lasu.hyperduty.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportDataAggregationService {

    /**
     * 聚合日报数据
     * @param reportDate 报告日期
     * @param projectIds 项目ID列表（可选，为null或空则聚合所有项目）
     * @return 聚合后的数据Map，包含projectInfo和taskUpdates
     */
    Map<String, String> aggregateDailyData(LocalDate reportDate, List<Long> projectIds);

    /**
     * 聚合周报数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param projectIds 项目ID列表（可选，为null或空则聚合所有项目）
     * @return 聚合后的数据Map，包含projectInfo和weeklyTaskData
     */
    Map<String, String> aggregateWeeklyData(LocalDate startDate, LocalDate endDate, List<Long> projectIds);
}
