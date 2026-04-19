package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.AiReport;
import com.lasu.hyperduty.entity.AiReportConfig;
import com.lasu.hyperduty.service.AiReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/ai/report")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    /**
     * 生成日报 - 异步处理
     */
    @PostMapping("/daily")
    public ResponseResult<Map<String, Object>> generateDailyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reportDate,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) Long configId,
            @RequestParam(required = false) Long employeeId) {
        Long finalEmployeeId = employeeId != null ? employeeId : 1L;
        
        // 异步生成报告
        CompletableFuture.runAsync(() -> {
            try {
                aiReportService.generateDailyReport(reportDate, projectIds, configId, finalEmployeeId);
            } catch (Exception e) {
                log.error("生成日报失败", e);
            }
        });
        
        // 立即返回确认信息
        Map<String, Object> result = new HashMap<>();
        result.put("message", "日报生成中，请稍后在历史报告中查看");
        return ResponseResult.success(result);
    }

    /**
     * 生成周报 - 异步处理
     */
    @PostMapping("/weekly")
    public ResponseResult<Map<String, Object>> generateWeeklyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) Long configId,
            @RequestParam(required = false) Long employeeId) {
        Long finalEmployeeId = employeeId != null ? employeeId : 1L;
        
        // 异步生成报告
        CompletableFuture.runAsync(() -> {
            try {
                aiReportService.generateWeeklyReport(startDate, endDate, projectIds, configId, finalEmployeeId);
            } catch (Exception e) {
                log.error("生成周报失败", e);
            }
        });
        
        // 立即返回确认信息
        Map<String, Object> result = new HashMap<>();
        result.put("message", "周报生成中，请稍后在历史报告中查看");
        return ResponseResult.success(result);
    }

    /**
     * 获取报告分页列表
     */
    @GetMapping("/page")
    public ResponseResult<Page<AiReport>> getReportPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Long projectId) {
        Page<AiReport> page = aiReportService.getReportPage(pageNum, pageSize, reportType, projectId);
        return ResponseResult.success(page);
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public ResponseResult<AiReport> getReportById(@PathVariable Long id) {
        AiReport report = aiReportService.getReportById(id);
        return ResponseResult.success(report);
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteReport(@PathVariable Long id) {
        aiReportService.deleteReport(id);
        return ResponseResult.success();
    }

    /**
     * 获取配置列表
     */
    @GetMapping("/config/list")
    public ResponseResult<List<AiReportConfig>> getConfigList() {
        List<AiReportConfig> configs = aiReportService.getConfigList();
        return ResponseResult.success(configs);
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/config/{id}")
    public ResponseResult<AiReportConfig> getConfigById(@PathVariable Long id) {
        AiReportConfig config = aiReportService.getConfigById(id);
        return ResponseResult.success(config);
    }

    /**
     * 保存配置
     */
    @PostMapping("/config")
    public ResponseResult<AiReportConfig> saveConfig(@RequestBody AiReportConfig config) {
        AiReportConfig savedConfig = aiReportService.saveConfig(config);
        return ResponseResult.success(savedConfig);
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/config/{id}")
    public ResponseResult<Void> deleteConfig(@PathVariable Long id) {
        aiReportService.deleteConfig(id);
        return ResponseResult.success();
    }
}
