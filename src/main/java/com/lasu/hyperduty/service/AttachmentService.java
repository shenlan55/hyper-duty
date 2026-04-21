package com.lasu.hyperduty.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.config.KKFileViewConfig;
import com.lasu.hyperduty.entity.PmTask;
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
                    log.info("为附件 {} 重新生成previewUrl: {}", name, previewUrl);
                }
            }

            if (needUpdate) {
                task.setAttachments(objectMapper.writeValueAsString(attachments));
            }
        } catch (Exception e) {
            log.error("处理附件previewUrl失败: {}", e.getMessage(), e);
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
                backendUrl = "http://backend:8080/api";
            }
            fullFileUrl = backendUrl + "/file/preview?filePath=" + 
                java.net.URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString());
            if (fileName != null && !fileName.isEmpty()) {
                fullFileUrl += "&fileName=" + 
                    java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            }
            byte[] urlBytes = fullFileUrl.getBytes(StandardCharsets.UTF_8);
            String base64Url = Base64.getEncoder().encodeToString(urlBytes);
            // 对Base64编码后的字符串进行URI编码，符合KKFileView接入规范
            String encodedBase64Url = java.net.URLEncoder.encode(base64Url, StandardCharsets.UTF_8.toString());
            return kkFileViewConfig.getEndpoint() + kkFileViewConfig.getPreviewPath() + "?url=" + encodedBase64Url;
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
}
