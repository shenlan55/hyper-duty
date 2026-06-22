package com.lasu.hyperduty.score.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.score.entity.ScoreSummary;
import com.lasu.hyperduty.score.service.ScoreSummaryService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分汇总与评选 Controller
 */
@RestController
@RequestMapping("/score")
public class ScoreSummaryController {

    @Autowired
    private ScoreSummaryService scoreSummaryService;

    /**
     * 查询月度汇总排名
     */
    @GetMapping("/summary/monthly")
    public ResponseResult<List<ScoreSummary>> getMonthlySummary(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        List<ScoreSummary> list = scoreSummaryService.getMonthlySummary(year, month);
        return ResponseResult.success(list);
    }

    /**
     * 手动触发生成月度汇总
     */
    @PostMapping("/summary/generate")
    public ResponseResult<String> generateMonthlySummary(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        scoreSummaryService.generateMonthlySummary(year, month);
        return ResponseResult.success("汇总生成成功");
    }

    /**
     * 季度/年度评选排名
     */
    @GetMapping("/evaluation")
    public ResponseResult<List<Map<String, Object>>> getEvaluationRanking(
            @RequestParam String periodType,
            @RequestParam Integer year,
            @RequestParam Integer periodIndex) {
        List<Map<String, Object>> list = scoreSummaryService.getEvaluationRanking(periodType, year, periodIndex);
        return ResponseResult.success(list);
    }

    /**
     * 查询当前评选周期的权重配置（用于前端"?"悬浮提示公示排名规则）
     * 2026-06-22 新增：动态读取 score_period_config 中 status=1 的配置
     *              无配置时返回默认权重 0.6/0.4，source=default
     */
    @GetMapping("/evaluation/config")
    public ResponseResult<Map<String, Object>> getCurrentEvaluationConfig(
            @RequestParam String periodType,
            @RequestParam Integer year,
            @RequestParam Integer periodIndex) {
        Map<String, Object> config = scoreSummaryService.getCurrentEvaluationConfig(periodType, year, periodIndex);
        return ResponseResult.success(config);
    }
}