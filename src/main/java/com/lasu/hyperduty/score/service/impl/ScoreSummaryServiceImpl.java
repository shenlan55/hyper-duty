package com.lasu.hyperduty.score.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lasu.hyperduty.score.entity.ScorePeriodConfig;
import com.lasu.hyperduty.score.entity.ScoreRecord;
import com.lasu.hyperduty.score.entity.ScoreSummary;
import com.lasu.hyperduty.score.mapper.ScorePeriodConfigMapper;
import com.lasu.hyperduty.score.mapper.ScoreRecordMapper;
import com.lasu.hyperduty.score.mapper.ScoreSummaryMapper;
import com.lasu.hyperduty.score.service.ScoreSummaryService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分汇总 Service 实现
 */
@Service
public class ScoreSummaryServiceImpl implements ScoreSummaryService {

    @Autowired
    private ScoreRecordMapper scoreRecordMapper;

    @Autowired
    private ScoreSummaryMapper scoreSummaryMapper;

    @Autowired
    private ScorePeriodConfigMapper scorePeriodConfigMapper;

    @Autowired
    private SysEmployeeMapper sysEmployeeMapper;

    /** 默认积分权重 */
    private static final BigDecimal DEFAULT_POINT_WEIGHT = new BigDecimal("0.60");

    /** 默认工时权重 */
    private static final BigDecimal DEFAULT_HOUR_WEIGHT = new BigDecimal("0.40");

    @Override
    @Transactional
    public void generateMonthlySummary(Integer year, Integer month) {
        // 获取当前周期配置的权重
        ScorePeriodConfig config = getCurrentPeriodConfig(year, month);
        BigDecimal pointWeight = (config != null) ? config.getPointWeight() : DEFAULT_POINT_WEIGHT;
        BigDecimal hourWeight = (config != null) ? config.getHourWeight() : DEFAULT_HOUR_WEIGHT;

        // 查询所有正常状态的员工（status=1）
        List<Long> employeeIds = getAllActiveEmployeeIds();

        for (Long employeeId : employeeIds) {
            // 汇总积分
            Integer totalScore = scoreRecordMapper.sumScoreByEmployeeAndMonth(employeeId, year, month);
            if (totalScore == null) totalScore = 0;

            // 汇总工时
            BigDecimal workHours = scoreSummaryMapper.sumOvertimeHoursByEmployeeAndMonth(employeeId, year, month);
            if (workHours == null) workHours = BigDecimal.ZERO;

            // 计算综合评分
            BigDecimal comprehensiveScore = BigDecimal.valueOf(totalScore)
                    .multiply(pointWeight)
                    .add(workHours.multiply(hourWeight))
                    .setScale(2, RoundingMode.HALF_UP);

            // 查询是否已存在
            ScoreSummary existing = scoreSummaryMapper.selectOne(
                    new LambdaQueryWrapper<ScoreSummary>()
                            .eq(ScoreSummary::getEmployeeId, employeeId)
                            .eq(ScoreSummary::getPeriodYear, year)
                            .eq(ScoreSummary::getPeriodMonth, month)
            );

            ScoreSummary summary = new ScoreSummary();
            summary.setEmployeeId(employeeId);
            summary.setPeriodYear(year);
            summary.setPeriodMonth(month);
            summary.setTotalScore(totalScore);
            summary.setWorkHours(workHours);
            summary.setComprehensiveScore(comprehensiveScore);

            if (existing != null) {
                summary.setId(existing.getId());
                summary.setCreateTime(existing.getCreateTime());
                scoreSummaryMapper.updateById(summary);
            } else {
                summary.setCreateTime(LocalDateTime.now());
                scoreSummaryMapper.insert(summary);
            }
        }
    }

    @Override
    public List<ScoreSummary> getMonthlySummary(Integer year, Integer month) {
        // 2026-06-27 修复：只展示启用员工（status=1）
        // 历史 score_summary 中如果员工已被禁用，不应在月度汇总中显示
        List<ScoreSummary> list = scoreSummaryMapper.selectList(
                new LambdaQueryWrapper<ScoreSummary>()
                        .eq(ScoreSummary::getPeriodYear, year)
                        .eq(ScoreSummary::getPeriodMonth, month)
                        .orderByDesc(ScoreSummary::getComprehensiveScore)
        );

        if (list.isEmpty()) {
            return list;
        }

        // 一次性查询所有相关员工，缩小为启用员工
        Set<Long> employeeIds = list.stream()
                .map(ScoreSummary::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> activeEmployeeNameMap = sysEmployeeMapper.selectList(
                        new LambdaQueryWrapper<SysEmployee>()
                                .in(SysEmployee::getId, employeeIds)
                                .eq(SysEmployee::getStatus, 1)
                                .select(SysEmployee::getId, SysEmployee::getEmployeeName)
                )
                .stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getEmployeeName));

        // 过滤掉禁用员工 + 填充员工姓名
        return list.stream()
                .filter(summary -> activeEmployeeNameMap.containsKey(summary.getEmployeeId()))
                .peek(summary -> summary.setEmployeeName(activeEmployeeNameMap.get(summary.getEmployeeId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getEvaluationRanking(String periodType, Integer year, Integer periodIndex) {
        // 确定月份范围
        int startMonth, endMonth;
        if ("QUARTERLY".equals(periodType)) {
            startMonth = (periodIndex - 1) * 3 + 1;
            endMonth = startMonth + 2;
        } else {
            startMonth = 1;
            endMonth = 12;
        }

        // 查询期间内所有月度汇总，按员工聚合
        List<ScoreSummary> allSummaries = scoreSummaryMapper.selectList(
                new LambdaQueryWrapper<ScoreSummary>()
                        .eq(ScoreSummary::getPeriodYear, year)
                        .ge(ScoreSummary::getPeriodMonth, startMonth)
                        .le(ScoreSummary::getPeriodMonth, endMonth)
        );

        // 按员工ID聚合数据
        Map<Long, Map<String, Object>> employeeMap = new HashMap<>();
        Map<Long, Integer> monthCountMap = new HashMap<>();

        for (ScoreSummary s : allSummaries) {
            Long empId = s.getEmployeeId();
            Map<String, Object> data = employeeMap.computeIfAbsent(empId, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("employeeId", empId);
                m.put("employeeName", "");
                m.put("totalScore", 0);
                m.put("totalWorkHours", BigDecimal.ZERO);
                m.put("totalComprehensiveScore", BigDecimal.ZERO);
                return m;
            });
            data.put("totalScore", (Integer) data.get("totalScore") + s.getTotalScore());
            data.put("totalWorkHours", ((BigDecimal) data.get("totalWorkHours")).add(s.getWorkHours()));
            data.put("totalComprehensiveScore", ((BigDecimal) data.get("totalComprehensiveScore")).add(s.getComprehensiveScore()));
            monthCountMap.put(empId, monthCountMap.getOrDefault(empId, 0) + 1);
        }

        // 2026-06-27 修复：批量查询启用员工姓名，禁用员工直接排除
        Set<Long> employeeIds = employeeMap.keySet();
        Map<Long, String> activeEmployeeNameMap = sysEmployeeMapper.selectList(
                        new LambdaQueryWrapper<SysEmployee>()
                                .in(SysEmployee::getId, employeeIds)
                                .eq(SysEmployee::getStatus, 1)
                                .select(SysEmployee::getId, SysEmployee::getEmployeeName)
                )
                .stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getEmployeeName));

        // 填充员工姓名并计算平均综合评分（只保留启用员工）
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> entry : employeeMap.entrySet()) {
            Long empId = entry.getKey();
            // 禁用员工直接跳过
            if (!activeEmployeeNameMap.containsKey(empId)) {
                continue;
            }
            Map<String, Object> data = entry.getValue();
            data.put("employeeName", activeEmployeeNameMap.get(empId));
            // 平均综合评分
            int months = monthCountMap.get(empId);
            BigDecimal avgScore = ((BigDecimal) data.get("totalComprehensiveScore"))
                    .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
            data.put("avgComprehensiveScore", avgScore);
            result.add(data);
        }

        // 按平均综合评分降序排列
        result.sort((a, b) -> {
            BigDecimal bScore = (BigDecimal) b.get("avgComprehensiveScore");
            BigDecimal aScore = (BigDecimal) a.get("avgComprehensiveScore");
            return bScore.compareTo(aScore);
        });

        return result;
    }

    @Override
    public Map<String, Object> getCurrentEvaluationConfig(String periodType, Integer year, Integer periodIndex) {
        // 查周期配置（status=1 进行中）
        ScorePeriodConfig config = scorePeriodConfigMapper.selectOne(
                new LambdaQueryWrapper<ScorePeriodConfig>()
                        .eq(ScorePeriodConfig::getPeriodType, periodType)
                        .eq(ScorePeriodConfig::getPeriodYear, year)
                        .eq(ScorePeriodConfig::getPeriodIndex, periodIndex)
                        .eq(ScorePeriodConfig::getStatus, 1)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("periodType", periodType);
        result.put("year", year);
        result.put("periodIndex", periodIndex);
        if (config == null) {
            // 无配置时返回默认值
            result.put("pointWeight", DEFAULT_POINT_WEIGHT);
            result.put("hourWeight", DEFAULT_HOUR_WEIGHT);
            result.put("source", "default");
        } else {
            result.put("pointWeight", config.getPointWeight());
            result.put("hourWeight", config.getHourWeight());
            result.put("source", "config");
        }
        return result;
    }

    /**
     * 获取当前周期配置
     */
    private ScorePeriodConfig getCurrentPeriodConfig(Integer year, Integer month) {
        // 查找当前月份所属的季度配置
        int quarterIndex = (month - 1) / 3 + 1;
        ScorePeriodConfig config = scorePeriodConfigMapper.selectOne(
                new LambdaQueryWrapper<ScorePeriodConfig>()
                        .eq(ScorePeriodConfig::getPeriodType, "QUARTERLY")
                        .eq(ScorePeriodConfig::getPeriodYear, year)
                        .eq(ScorePeriodConfig::getPeriodIndex, quarterIndex)
                        .eq(ScorePeriodConfig::getStatus, 1)
        );
        if (config == null) {
            // 查年度配置
            config = scorePeriodConfigMapper.selectOne(
                    new LambdaQueryWrapper<ScorePeriodConfig>()
                            .eq(ScorePeriodConfig::getPeriodType, "YEARLY")
                            .eq(ScorePeriodConfig::getPeriodYear, year)
                            .eq(ScorePeriodConfig::getStatus, 1)
            );
        }
        return config;
    }

    /**
     * 获取所有正常状态的员工ID列表（status=1）
     */
    private List<Long> getAllActiveEmployeeIds() {
        List<SysEmployee> employees = sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getStatus, 1)
                        .select(SysEmployee::getId)
                        .orderByAsc(SysEmployee::getId)
        );

        List<Long> employeeIds = new ArrayList<>();
        for (SysEmployee employee : employees) {
            employeeIds.add(employee.getId());
        }
        return employeeIds;
    }
}