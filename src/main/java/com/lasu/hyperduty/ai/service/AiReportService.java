package com.lasu.hyperduty.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.ai.entity.AiReport;
import com.lasu.hyperduty.ai.entity.AiReportConfig;
import com.lasu.hyperduty.ai.service.AiReportService;
import java.time.LocalDate;
import java.util.List;









public interface AiReportService {

    /**
     * 生成日报
     * @param reportDate 报告日期
     * @param projectIds 项目ID列表（可选）
     * @param configId 配置ID（可选）
     * @param employeeId 操作人ID
     * @return 生成的报告
     */
    AiReport generateDailyReport(LocalDate reportDate, List<Long> projectIds, Long configId, Long employeeId);

    /**
     * 生成周报
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param projectIds 项目ID列表（可选）
     * @param configId 配置ID（可选）
     * @param employeeId 操作人ID
     * @return 生成的报告
     */
    AiReport generateWeeklyReport(LocalDate startDate, LocalDate endDate, List<Long> projectIds, Long configId, Long employeeId);

    /**
     * 获取报告分页列表
     */
    Page<AiReport> getReportPage(Integer pageNum, Integer pageSize, String reportType, Long projectId);

    /**
     * 获取报告详情
     */
    AiReport getReportById(Long id);

    /**
     * 删除报告
     */
    void deleteReport(Long id);

    /**
     * 获取配置列表
     */
    List<AiReportConfig> getConfigList();

    /**
     * 获取配置详情
     */
    AiReportConfig getConfigById(Long id);

    /**
     * 保存配置
     */
    AiReportConfig saveConfig(AiReportConfig config);

    /**
     * 删除配置
     */
    void deleteConfig(Long id);
}
