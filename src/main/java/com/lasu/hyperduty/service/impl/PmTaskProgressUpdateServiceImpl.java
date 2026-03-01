package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.mapper.PmTaskProgressUpdateMapper;
import com.lasu.hyperduty.service.PmTaskProgressUpdateService;
import com.lasu.hyperduty.service.PmTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PmTaskProgressUpdateServiceImpl extends ServiceImpl<PmTaskProgressUpdateMapper, PmTaskProgressUpdate> implements PmTaskProgressUpdateService {

    private final PmTaskProgressUpdateMapper pmTaskProgressUpdateMapper;
    private final PmTaskService pmTaskService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PmTaskProgressUpdate createProgressUpdate(PmTaskProgressUpdate update) {
        try {
            // 设置创建时间
            update.setCreateTime(LocalDateTime.now());
            
            // 保存任务进展更新
            pmTaskProgressUpdateMapper.insert(update);
            
            // 更新任务的进度
            pmTaskService.updateProgress(update.getTaskId(), update.getProgress());
            
            // 解析附件JSON字符串为对象列表
            if (update.getAttachments() != null && !update.getAttachments().trim().isEmpty()) {
                try {
                    List<PmTaskProgressUpdate.Attachment> attachments = objectMapper.readValue(
                            update.getAttachments(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, PmTaskProgressUpdate.Attachment.class)
                    );
                    update.setAttachmentList(attachments);
                } catch (Exception e) {
                    log.warn("解析附件JSON失败，attachments: {}", update.getAttachments(), e);
                    // 解析失败时不影响主流程
                }
            }
            
            return update;
        } catch (Exception e) {
            log.error("创建任务进展更新失败", e);
            throw new RuntimeException("创建任务进展更新失败", e);
        }
    }

    @Override
    public List<PmTaskProgressUpdate> getTaskProgressUpdates(Long taskId) {
        try {
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateMapper.selectByTaskId(taskId);
            
            // 解析附件JSON字符串为对象列表
            for (PmTaskProgressUpdate update : updates) {
                if (update.getAttachments() != null && !update.getAttachments().trim().isEmpty()) {
                    try {
                        List<PmTaskProgressUpdate.Attachment> attachments = objectMapper.readValue(
                                update.getAttachments(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, PmTaskProgressUpdate.Attachment.class)
                        );
                        update.setAttachmentList(attachments);
                    } catch (Exception e) {
                        log.warn("解析附件JSON失败，attachments: {}", update.getAttachments(), e);
                        // 解析失败时不影响主流程
                    }
                }
            }
            
            return updates;
        } catch (Exception e) {
            log.error("获取任务进展更新失败", e);
            throw new RuntimeException("获取任务进展更新失败", e);
        }
    }

    @Override
    public PmTaskProgressUpdate getProgressUpdateDetail(Long id) {
        try {
            PmTaskProgressUpdate update = pmTaskProgressUpdateMapper.selectByIdWithEmployeeName(id);
            
            // 解析附件JSON字符串为对象列表
            if (update != null && update.getAttachments() != null && !update.getAttachments().trim().isEmpty()) {
                try {
                    List<PmTaskProgressUpdate.Attachment> attachments = objectMapper.readValue(
                            update.getAttachments(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, PmTaskProgressUpdate.Attachment.class)
                    );
                    update.setAttachmentList(attachments);
                } catch (Exception e) {
                    log.warn("解析附件JSON失败，attachments: {}", update.getAttachments(), e);
                    // 解析失败时不影响主流程
                }
            }
            
            return update;
        } catch (Exception e) {
            log.error("获取任务进展更新详情失败", e);
            throw new RuntimeException("获取任务进展更新详情失败", e);
        }
    }
}
