package com.lasu.hyperduty.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.config.KKFileViewConfig;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.entity.PmTaskProgressUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final KKFileViewConfig kkFileViewConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 确保任务的附件数据包含正确的previewUrl
     */
    public PmTask ensureAttachmentsHavePreviewUrls(PmTask task) {
        if (task == null || task.getAttachments() == null || task.getAttachments().trim().isEmpty()) {
            return task;
        }

        try {
            List<Map<String, Object>> attachments = objectMapper.readValue(
                task.getAttachments(),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            boolean needUpdate = false;
            int updatedCount = 0;
            for (Map<String, Object> attachment : attachments) {
                String filePath = (String) attachment.get("filePath");
                String name = (String) attachment.get("name");
                String currentPreviewUrl = (String) attachment.get("previewUrl");

                // 如果previewUrl缺失或为空，重新生成
                if (filePath != null && !filePath.trim().isEmpty() && 
                    (currentPreviewUrl == null || currentPreviewUrl.trim().isEmpty())) {
                    String previewUrl = generatePreviewUrl(filePath, name);
                    attachment.put("previewUrl", previewUrl);
                    needUpdate = true;
                    updatedCount++;
                }
            }

            if (needUpdate) {
                task.setAttachments(objectMapper.writeValueAsString(attachments));
                log.debug("为任务 {} 的 {} 个附件重新生成了previewUrl", task.getId(), updatedCount);
            }
        } catch (Exception e) {
            log.error("处理附件previewUrl失败: {}", e.getMessage());
        }

        return task;
    }

    /**
     * 生成预览URL
     */
    private String generatePreviewUrl(String filePath, String fileName) {
        try {
            String fullFileUrl;
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null || backendUrl.isEmpty()) {
                // 本地开发环境，用 host.docker.internal（启动 KKFileView 时加了 --add-host）
                backendUrl = "http://host.docker.internal:8080";
            }

            // 加上 &fullfilename 参数明确告诉 KKFileView 文件名！
            fullFileUrl = backendUrl + "/api/file/preview?filePath=" +
                java.net.URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString()) +
                "&fullfilename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

            // KKFileView 4.4.0 用 URL 安全 Base64，直接传递
            String base64Url = Base64.getUrlEncoder().encodeToString(fullFileUrl.getBytes(StandardCharsets.UTF_8));
            String finalPreviewUrl = kkFileViewConfig.getEndpoint() + kkFileViewConfig.getPreviewPath() + "?url=" + base64Url;

            return finalPreviewUrl;
        } catch (Exception e) {
            log.error("生成预览URL失败: {}", e.getMessage(), e);
            return filePath;
        }
    }

    /**
     * 批量处理任务列表的附件
     */
    public List<PmTask> ensureAttachmentsForTaskList(List<PmTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return tasks;
        }
        List<PmTask> result = new ArrayList<>();
        for (PmTask task : tasks) {
            result.add(ensureAttachmentsHavePreviewUrls(task));
        }
        return result;
    }

    /**
     * 确保进度更新的附件数据包含正确的previewUrl
     */
    public PmTaskProgressUpdate ensureProgressUpdateAttachmentsHavePreviewUrls(PmTaskProgressUpdate update) {
        if (update == null || update.getAttachments() == null || update.getAttachments().trim().isEmpty()) {
            return update;
        }

        try {
            List<Map<String, Object>> attachments = objectMapper.readValue(
                update.getAttachments(),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            boolean needUpdate = false;
            int updatedCount = 0;
            for (Map<String, Object> attachment : attachments) {
                // 进度更新的附件用url字段，需要从中提取filePath
                String url = (String) attachment.get("url");
                String name = (String) attachment.get("name");
                String currentPreviewUrl = (String) attachment.get("previewUrl");

                String filePath = extractFilePathFromUrl(url);

                // 如果previewUrl缺失或为空，重新生成
                if (filePath != null && !filePath.trim().isEmpty() && 
                    (currentPreviewUrl == null || currentPreviewUrl.trim().isEmpty())) {
                    String previewUrl = generatePreviewUrl(filePath, name);
                    attachment.put("previewUrl", previewUrl);
                    needUpdate = true;
                    updatedCount++;
                }
            }

            if (needUpdate) {
                update.setAttachments(objectMapper.writeValueAsString(attachments));
                log.debug("为进度更新 {} 的 {} 个附件重新生成了previewUrl", update.getId(), updatedCount);
            }
        } catch (Exception e) {
            log.error("处理进度更新附件previewUrl失败: {}", e.getMessage());
        }

        return update;
    }

    /**
     * 批量处理进度更新列表的附件
     */
    public List<PmTaskProgressUpdate> ensureAttachmentsForProgressUpdateList(List<PmTaskProgressUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return updates;
        }
        List<PmTaskProgressUpdate> result = new ArrayList<>();
        for (PmTaskProgressUpdate update : updates) {
            result.add(ensureProgressUpdateAttachmentsHavePreviewUrls(update));
        }
        return result;
    }

    /**
     * 从URL中提取filePath
     */
    private String extractFilePathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // URL格式类似: /api/file/preview?filePath=xxx
        // 或者: http://localhost:8080/api/file/preview?filePath=xxx
        try {
            int filePathIndex = url.indexOf("filePath=");
            if (filePathIndex != -1) {
                String filePathPart = url.substring(filePathIndex + 9);
                int ampIndex = filePathPart.indexOf("&");
                if (ampIndex != -1) {
                    filePathPart = filePathPart.substring(0, ampIndex);
                }
                // URL解码
                return java.net.URLDecoder.decode(filePathPart, StandardCharsets.UTF_8.toString());
            }
        } catch (Exception e) {
            log.warn("从URL提取filePath失败: {}", url, e);
        }
        return null;
    }
}
