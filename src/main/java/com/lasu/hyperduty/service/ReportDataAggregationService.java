package com.lasu.hyperduty.service;

import com.lasu.hyperduty.dto.AiReportDataDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportDataAggregationService {

    /**
     * 聚合日报数据（结构化格式）
     * @param reportDate 报告日期
     * @param projectIds 项目ID列表（可选，为null或空则聚合所有项目）
     * @return 聚合后的结构化数据
     */
    AiReportDataDTO aggregateDailyData(LocalDate reportDate, List<Long> projectIds);

    /**
     * 聚合周报数据（结构化格式）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param projectIds 项目ID列表（可选，为null或空则聚合所有项目）
     * @return 聚合后的结构化数据
     */
    AiReportDataDTO aggregateWeeklyData(LocalDate startDate, LocalDate endDate, List<Long> projectIds);

    /**
     * 将结构化数据转换为AI友好的JSON字符串
     * @param data 结构化数据
     * @return JSON字符串
     */
    String toJsonString(AiReportDataDTO data);
}
