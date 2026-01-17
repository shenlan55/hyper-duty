package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.service.DutyStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/duty/statistics")
public class DutyStatisticsController {

    @Autowired
    private DutyStatisticsService dutyStatisticsService;

    @GetMapping("/overall")
    public ResponseResult<Map<String, Object>> getOverallStatistics() {
        Map<String, Object> statistics = dutyStatisticsService.getOverallStatistics();
        return ResponseResult.success(statistics);
    }

    @GetMapping("/dept")
    public ResponseResult<List<Map<String, Object>>> getDeptStatistics(@RequestParam(required = false) Long deptId) {
        List<Map<String, Object>> statistics = dutyStatisticsService.getDeptStatistics(deptId);
        return ResponseResult.success(statistics);
    }

    @GetMapping("/shift-distribution")
    public ResponseResult<List<Map<String, Object>>> getShiftDistribution() {
        List<Map<String, Object>> distribution = dutyStatisticsService.getShiftDistribution();
        return ResponseResult.success(distribution);
    }

    @GetMapping("/monthly-trend")
    public ResponseResult<List<Map<String, Object>>> getMonthlyTrend() {
        List<Map<String, Object>> trend = dutyStatisticsService.getMonthlyTrend();
        return ResponseResult.success(trend);
    }

    @GetMapping
    public ResponseResult<Map<String, Object>> getAllStatistics() {
        Map<String, Object> allStatistics = new java.util.HashMap<>();
        allStatistics.put("overall", dutyStatisticsService.getOverallStatistics());
        allStatistics.put("dept", dutyStatisticsService.getDeptStatistics(null));
        allStatistics.put("shift", dutyStatisticsService.getShiftDistribution());
        allStatistics.put("trend", dutyStatisticsService.getMonthlyTrend());
        return ResponseResult.success(allStatistics);
    }
}
