package com.lasu.hyperduty.score.service;

import com.lasu.hyperduty.score.entity.ScoreSummary;
import java.util.List;
import java.util.Map;

/**
 * 积分汇总 Service
 */
public interface ScoreSummaryService {

    /**
     * 生成指定月份的汇总数据
     * @param year 年份
     * @param month 月份
     */
    void generateMonthlySummary(Integer year, Integer month);

    /**
     * 查询月度汇总排名
     * @param year 年份
     * @param month 月份
     * @return 带员工姓名的汇总列表
     */
    List<ScoreSummary> getMonthlySummary(Integer year, Integer month);

    /**
     * 查询季度/年度评选排名
     * @param periodType 周期类型 QUARTERLY/YEARLY
     * @param year 年份
     * @param periodIndex 季度1-4 或 年度1
     * @return 评选排名列表
     */
    List<Map<String, Object>> getEvaluationRanking(String periodType, Integer year, Integer periodIndex);
}