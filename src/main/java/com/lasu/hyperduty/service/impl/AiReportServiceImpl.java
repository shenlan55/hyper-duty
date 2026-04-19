package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.entity.AiReport;
import com.lasu.hyperduty.entity.AiReportConfig;
import com.lasu.hyperduty.entity.PmProject;
import com.lasu.hyperduty.mapper.AiReportConfigMapper;
import com.lasu.hyperduty.mapper.AiReportMapper;
import com.lasu.hyperduty.mapper.PmProjectMapper;
import com.lasu.hyperduty.service.AiReportService;
import com.lasu.hyperduty.service.ReportDataAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 获取配置
        AiReportConfig config = getConfig(configId, "daily");

        // 聚合数据
        Map<String, String> data = reportDataAggregationService.aggregateDailyData(reportDate, projectIds);

        // 构建提示词
        String prompt = buildPrompt(config.getPromptTemplate(), data);

        // 调用AI生成报告
        String reportContent = callAi(prompt);

        // 保存报告
        AiReport report = new AiReport();
        report.setReportTitle(reportDate.format(DATE_FORMATTER) + " 日报");
        report.setReportType("daily");
        report.setReportDate(reportDate);
        // 设置项目信息（只保存第一个项目ID用于筛选，项目名称保存多个）
        if (projectIds != null && !projectIds.isEmpty()) {
            report.setProjectId(projectIds.get(0));
            List<PmProject> projects = pmProjectMapper.selectProjectByIds(projectIds);
            String projectNames = projects.stream().map(PmProject::getProjectName).collect(Collectors.joining(", "));
            report.setProjectName(projectNames);
        }
        report.setReportContent(reportContent);
        report.setConfigId(config.getId());
        report.setCreateBy(employeeId);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        save(report);
        return report;
    }

    @Override
    public AiReport generateWeeklyReport(LocalDate startDate, LocalDate endDate, List<Long> projectIds, Long configId, Long employeeId) {
        // 获取配置
        AiReportConfig config = getConfig(configId, "weekly");

        // 聚合数据
        Map<String, String> data = reportDataAggregationService.aggregateWeeklyData(startDate, endDate, projectIds);

        // 构建提示词
        String prompt = buildPrompt(config.getPromptTemplate(), data);

        // 调用AI生成报告
        String reportContent = callAi(prompt);

        // 保存报告
        AiReport report = new AiReport();
        report.setReportTitle(startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER) + " 周报");
        report.setReportType("weekly");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        // 设置项目信息（只保存第一个项目ID用于筛选，项目名称保存多个）
        if (projectIds != null && !projectIds.isEmpty()) {
            report.setProjectId(projectIds.get(0));
            List<PmProject> projects = pmProjectMapper.selectProjectByIds(projectIds);
            String projectNames = projects.stream().map(PmProject::getProjectName).collect(Collectors.joining(", "));
            report.setProjectName(projectNames);
        }
        report.setReportContent(reportContent);
        report.setConfigId(config.getId());
        report.setCreateBy(employeeId);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        save(report);
        return report;
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
    public List<AiReportConfig> getConfigList() {
        LambdaQueryWrapper<AiReportConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiReportConfig::getStatus, 1);
        wrapper.orderByAsc(AiReportConfig::getId);
        return aiReportConfigMapper.selectList(wrapper);
    }

    @Override
    public AiReportConfig getConfigById(Long id) {
        return aiReportConfigMapper.selectById(id);
    }

    @Override
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
            // 如果没有配置，使用默认提示词
            config = new AiReportConfig();
            if ("daily".equals(reportType)) {
                config.setPromptTemplate("请基于以下项目任务数据，按项目分组，生成一份结构化的日报：\n\n【项目信息】\n{projectInfo}\n\n【今日任务更新】\n{taskUpdates}\n\n重要要求：\n1. 按项目分组，每个项目独立成一个大章节\n2. 每个项目下分\"今日已完成\"\"今日进行中\"\"明日计划\"三部分\n3. 严格按照\"一、二、三\"\"1.2.3.\"\"①②③\"的三级层级输出\n4. 语言简洁，突出重点\n5. 【非常重要】：任务数据中明确标记了\"★★★ 重点任务区域 ★★★\"和\"◆◆◆ 其他任务区域 ◆◆◆\"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！\n6. 只展示你选择的项目的内容，不要展示其他项目的内容！");
            } else {
                config.setPromptTemplate("请基于以下一周的任务数据，按项目分组，生成一份结构化的周报：\n\n【项目信息】\n{projectInfo}\n\n【本周任务汇总】\n{weeklyTaskData}\n\n重要要求：\n1. 按项目分组，每个项目独立成一个大章节\n2. 包含\"一、周报基本信息\"\"二、本周核心成果（按项目）\"\"三、本周工作进度复盘\"\"四、项目风险与问题\"\"五、下周工作计划（按项目）\"\n3. 严格按照\"一、二、三\"\"1.2.3.\"\"①②③\"的三级层级输出\n4. 突出亮点和关键里程碑\n5. 【非常重要】：任务数据中明确标记了\"★★★ 重点任务区域 ★★★\"和\"◆◆◆ 其他任务区域 ◆◆◆\"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！\n6. 只展示你选择的项目的内容，不要展示其他项目的内容！");
            }
            config.setId(0L);
        }
        return config;
    }

    private String buildPrompt(String template, Map<String, String> data) {
        String prompt = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            prompt = prompt.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return prompt;
    }

    private String callAi(String prompt) {
        try {
            if (!StringUtils.hasText(zhipuApiKey)) {
                log.warn("智谱 AI API Key 未配置，返回模拟数据");
                return generateMockReport(prompt);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(zhipuApiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuModel);
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);
            requestBody.put("messages", messages);

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
            return "AI 调用失败，请稍后重试";
        } catch (Exception e) {
            log.error("调用 AI 失败", e);
            return "AI 调用异常：" + e.getMessage() + "\n\n原始数据：\n" + prompt;
        }
    }

    private String generateMockReport(String prompt) {
        return "【模拟生成的报告】\n\n" +
               "由于 AI API Key 未配置，这是一个模拟报告。\n\n" +
               "请在配置文件中配置 ai.zhipu.api-key 以使用真实的 AI 功能。\n\n" +
               "以下是原始数据预览：\n" +
               prompt.substring(0, Math.min(500, prompt.length())) + "...";
    }
}
