package com.lasu.hyperduty.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.ai.dto.AiReportDataDTO;
import com.lasu.hyperduty.ai.entity.AiReport;
import com.lasu.hyperduty.ai.entity.AiReportConfig;
import com.lasu.hyperduty.ai.mapper.AiReportConfigMapper;
import com.lasu.hyperduty.ai.mapper.AiReportMapper;
import com.lasu.hyperduty.ai.service.AiReportService;
import com.lasu.hyperduty.ai.service.ReportDataAggregationService;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.mapper.PmProjectMapper;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;









@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportServiceImpl extends ServiceImpl<AiReportMapper, AiReport> implements AiReportService {

    private final AiReportConfigMapper aiReportConfigMapper;
    private final ReportDataAggregationService reportDataAggregationService;
    private final PmProjectMapper pmProjectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.zhipu.api-key:}")
    private String zhipuApiKey;

    @Value("${ai.zhipu.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String zhipuApiUrl;

    @Value("${ai.zhipu.model:glm-4-flash}")
    private String zhipuModel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public AiReport generateDailyReport(LocalDate reportDate, List<Long> projectIds, Long configId, Long employeeId) {
        AiReportConfig config = getConfig(configId, "daily");
        AiReportDataDTO data = reportDataAggregationService.aggregateDailyData(reportDate, projectIds);
        String reportContent = generateReportWithRetry(config, data, "daily");
        return saveReport(reportContent, reportDate, null, projectIds, "daily", config.getId(), employeeId);
    }

    @Override
    public AiReport generateWeeklyReport(LocalDate startDate, LocalDate endDate, List<Long> projectIds, Long configId, Long employeeId) {
        AiReportConfig config = getConfig(configId, "weekly");
        AiReportDataDTO data = reportDataAggregationService.aggregateWeeklyData(startDate, endDate, projectIds);
        String reportContent = generateReportWithRetry(config, data, "weekly");
        return saveReport(reportContent, startDate, endDate, projectIds, "weekly", config.getId(), employeeId);
    }

    private String generateReportWithRetry(AiReportConfig config, AiReportDataDTO data, String reportType) {
        int maxRetries = config.getMaxRetries() != null ? config.getMaxRetries() : 3;
        String lastError = "";

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("生成报告尝试 {}/{}", attempt, maxRetries);
                String prompt = buildPrompt(config, data, reportType, attempt > 1, lastError);
                String output = callAi(config, prompt);

                if (isValidOutput(output)) {
                    log.info("报告生成成功");
                    return output;
                } else {
                    lastError = "输出格式不符合要求，缺少关键结构或内容太短";
                    log.warn("第{}次尝试失败：{}", attempt, lastError);
                }
            } catch (Exception e) {
                lastError = e.getMessage();
                log.error("第{}次尝试异常", attempt, e);
            }
        }

        log.error("所有重试都失败，返回降级报告");
        return generateFallbackReport(data, reportType);
    }

    private String buildPrompt(AiReportConfig config, AiReportDataDTO data, String reportType, boolean isRetry, String lastError) {
        String dataJson = reportDataAggregationService.toJsonString(data);
        String userPrompt = config.getPromptTemplate()
                .replace("{projectInfo}", "") // 占位符已不需要，数据全在JSON里
                .replace("{taskUpdates}", "")
                .replace("{weeklyTaskData}", "");

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("【项目数据】\n").append(dataJson).append("\n\n");
        promptBuilder.append("【报告要求】\n").append(userPrompt).append("\n\n");

        if (isRetry && StringUtils.hasText(lastError)) {
            promptBuilder.append("【反馈信息】\n上次生成失败，原因：").append(lastError).append("\n请重新生成，确保输出格式正确且内容完整。\n\n");
        }

        return promptBuilder.toString();
    }

    private String callAi(AiReportConfig config, String prompt) {
        if (!StringUtils.hasText(zhipuApiKey)) {
            log.warn("智谱AI API Key未配置，返回模拟数据");
            return generateMockReport(prompt);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(zhipuApiKey);

            List<Map<String, Object>> messages = new ArrayList<>();

            String systemPrompt = config.getSystemPrompt();
            if (!StringUtils.hasText(systemPrompt)) {
                systemPrompt = getDefaultSystemPrompt();
            }
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            Map<String, Object> requestBody = new HashMap<>();
            String modelName = StringUtils.hasText(config.getModelName()) ? config.getModelName() : zhipuModel;
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);

            if (config.getTemperature() != null) {
                requestBody.put("temperature", config.getTemperature());
            }
            if (config.getMaxTokens() != null) {
                requestBody.put("max_tokens", config.getMaxTokens());
            }
            if (config.getTopP() != null) {
                requestBody.put("top_p", config.getTopP());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(zhipuApiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode messageNode = choices.get(0).path("message");
                    return messageNode.path("content").asText();
                }
            }
            throw new RuntimeException("AI返回无效响应");
        } catch (Exception e) {
            log.error("调用AI失败", e);
            throw new RuntimeException("AI调用异常: " + e.getMessage());
        }
    }

    private boolean isValidOutput(String output) {
        if (!StringUtils.hasText(output)) return false;
        if (output.length() < 100) return false;
        return output.contains("一、") || output.contains("1.") || output.contains("#") || output.contains("★");
    }

    private String generateFallbackReport(AiReportDataDTO data, String reportType) {
        StringBuilder sb = new StringBuilder();
        sb.append("【报告生成说明】\n由于AI服务暂时不可用，以下是基于原始数据的简要报告。\n\n");

        if (data.getProjectInfo() != null) {
            sb.append("一、项目概览\n");
            for (AiReportDataDTO.ProjectInfoDTO project : data.getProjectInfo()) {
                sb.append("• ").append(project.getProjectName());
                sb.append(" (进度: ").append(project.getProgress()).append("%)\n");
            }
            sb.append("\n");
        }

        if ("daily".equals(reportType) && data.getDailyTaskData() != null) {
            AiReportDataDTO.DailyTaskDataDTO daily = data.getDailyTaskData();
            if (daily.getFocusTasks() != null && !daily.getFocusTasks().isEmpty()) {
                sb.append("二、重点任务\n");
                for (AiReportDataDTO.TaskDTO task : daily.getFocusTasks()) {
                    sb.append("• ").append(task.getTaskName());
                    sb.append(" [").append(task.getStatusText()).append("] ");
                    sb.append(task.getProgress()).append("%\n");
                }
                sb.append("\n");
            }
        } else if ("weekly".equals(reportType) && data.getWeeklyTaskData() != null) {
            AiReportDataDTO.WeeklyTaskDataDTO weekly = data.getWeeklyTaskData();
            if (weekly.getFocusTasks() != null && !weekly.getFocusTasks().isEmpty()) {
                sb.append("二、本周重点任务\n");
                for (AiReportDataDTO.TaskDTO task : weekly.getFocusTasks()) {
                    sb.append("• ").append(task.getTaskName());
                    sb.append(" [").append(task.getStatusText()).append("] ");
                    sb.append(task.getProgress()).append("%\n");
                }
                sb.append("\n");
            }
        }

        sb.append("请检查AI配置后重试生成高质量报告。");
        return sb.toString();
    }

    private String getDefaultSystemPrompt() {
        return "你是一位专业的项目管理报告专家，擅长从项目任务数据中提炼有价值的洞察。\n" +
                "你的职责是：\n" +
                "1. 理解并分析提供的JSON格式项目数据\n" +
                "2. 生成结构清晰、内容详实的项目报告\n" +
                "3. 突出重点任务和关键里程碑\n" +
                "4. 语言精炼专业，避免简单罗列\n" +
                "5. 严格按照用户要求的格式输出\n\n" +
                "报告风格要求：\n" +
                "• 使用清晰的层级结构（一、二、三 → 1.2.3. → (1)(2)(3)）\n" +
                "• 重点内容用★标记\n" +
                "• 保持客观专业的语气";
    }

    private AiReport saveReport(String content, LocalDate startDate, LocalDate endDate, List<Long> projectIds, String type, Long configId, Long employeeId) {
        AiReport report = new AiReport();
        if ("daily".equals(type)) {
            report.setReportTitle(startDate.format(DATE_FORMATTER) + " 日报");
            report.setReportDate(startDate);
        } else {
            report.setReportTitle(startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER) + " 周报");
            report.setStartDate(startDate);
            report.setEndDate(endDate);
        }
        report.setReportType(type);
        report.setReportContent(content);
        report.setConfigId(configId);
        report.setCreateBy(employeeId);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        if (projectIds != null && !projectIds.isEmpty()) {
            report.setProjectId(projectIds.get(0));
            List<PmProject> projects = pmProjectMapper.selectProjectByIds(projectIds);
            String projectNames = projects.stream().map(PmProject::getProjectName).collect(Collectors.joining(", "));
            report.setProjectName(projectNames);
        }

        save(report);
        return report;
    }

    private String generateMockReport(String prompt) {
        return "【模拟生成的报告】\n\n" +
                "由于AI API Key未配置，这是一个模拟报告。\n\n" +
                "请在配置文件中配置 ai.zhipu.api-key 以使用真实的AI功能。\n\n" +
                "提示词预览：\n" + prompt.substring(0, Math.min(500, prompt.length())) + "...";
    }

    @Override
    public Page<AiReport> getReportPage(Integer pageNum, Integer pageSize, String reportType, Long projectId) {
        Page<AiReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiReport> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(reportType)) {
            wrapper.eq(AiReport::getReportType, reportType);
        }
        if (projectId != null) {
            wrapper.eq(AiReport::getProjectId, projectId);
        }

        wrapper.orderByDesc(AiReport::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public AiReport getReportById(Long id) {
        return getById(id);
    }

    @Override
    public void deleteReport(Long id) {
        removeById(id);
    }

    @Override
    @Cacheable(value = "aiReport", key = "'configList'")
    public List<AiReportConfig> getConfigList() {
        LambdaQueryWrapper<AiReportConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiReportConfig::getStatus, 1);
        wrapper.orderByAsc(AiReportConfig::getId);
        return aiReportConfigMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(value = "aiReport", key = "'config:' + #id")
    public AiReportConfig getConfigById(Long id) {
        return aiReportConfigMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "aiReport", allEntries = true)
    public AiReportConfig saveConfig(AiReportConfig config) {
        if (config.getId() == null) {
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            aiReportConfigMapper.insert(config);
        } else {
            config.setUpdateTime(LocalDateTime.now());
            aiReportConfigMapper.updateById(config);
        }
        return config;
    }

    @Override
    @CacheEvict(value = "aiReport", allEntries = true)
    public void deleteConfig(Long id) {
        aiReportConfigMapper.deleteById(id);
    }

    private AiReportConfig getConfig(Long configId, String reportType) {
        AiReportConfig config;
        if (configId != null) {
            config = aiReportConfigMapper.selectById(configId);
        } else {
            LambdaQueryWrapper<AiReportConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiReportConfig::getReportType, reportType);
            wrapper.eq(AiReportConfig::getStatus, 1);
            wrapper.last("LIMIT 1");
            config = aiReportConfigMapper.selectOne(wrapper);
        }

        if (config == null) {
            config = createDefaultConfig(reportType);
        }

        if (config.getTemperature() == null) config.setTemperature(0.7);
        if (config.getMaxTokens() == null) config.setMaxTokens(4000);
        if (config.getTopP() == null) config.setTopP(0.9);
        if (config.getMaxRetries() == null) config.setMaxRetries(3);
        if (!StringUtils.hasText(config.getSystemPrompt())) {
            config.setSystemPrompt(getDefaultSystemPrompt());
        }

        return config;
    }

    private AiReportConfig createDefaultConfig(String reportType) {
        AiReportConfig config = new AiReportConfig();
        config.setId(0L);
        config.setReportType(reportType);

        if ("daily".equals(reportType)) {
            config.setPromptTemplate("请基于上方的JSON项目数据，生成一份高质量的日报。\n\n" +
                    "报告要求：\n" +
                    "1. 按项目分组，每个项目独立章节\n" +
                    "2. 包含「今日完成」、「进行中」、「明日计划」三个部分\n" +
                    "3. 重点任务（isFocus=1）用★特别标注\n" +
                    "4. 不要简单罗列，要有总结提炼\n" +
                    "5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)\n");
        } else {
            config.setPromptTemplate("请基于上方的JSON项目数据，生成一份高质量的周报。\n\n" +
                    "报告要求：\n" +
                    "1. 按项目分组，每个项目独立章节\n" +
                    "2. 包含「周报概览」、「本周成果」、「进度复盘」、「下周计划」\n" +
                    "3. 重点任务（isFocus=1）用★特别标注\n" +
                    "4. 要有数据洞察，不要流水账\n" +
                    "5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)\n");
        }

        return config;
    }
}
