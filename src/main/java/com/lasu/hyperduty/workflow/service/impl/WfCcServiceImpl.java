package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.WfCcDTO;
import com.lasu.hyperduty.workflow.entity.WfCc;
import com.lasu.hyperduty.workflow.mapper.WfCcMapper;
import com.lasu.hyperduty.workflow.service.WfCcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 流程抄送服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfCcServiceImpl extends ServiceImpl<WfCcMapper, WfCc> implements WfCcService {

    @Override
    public Page<WfCc> pageMine(Long userId, Integer pageNum, Integer pageSize, Integer readStatus) {
        LambdaQueryWrapper<WfCc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfCc::getCcUserId, userId);
        if (readStatus != null) {
            wrapper.eq(WfCc::getReadStatus, readStatus);
        }
        wrapper.orderByDesc(WfCc::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfCc createCc(WfCcDTO dto) {
        if (dto == null || dto.getCcUserId() == null) {
            throw new BusinessException("抄送人不能为空");
        }
        WfCc entity = new WfCc();
        copyDto(dto, entity);
        entity.setCreateTime(LocalDateTime.now());
        if (entity.getReadStatus() == null) {
            entity.setReadStatus(0);
        }
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id, Long userId) {
        WfCc existing = getById(id);
        if (existing == null) {
            throw new BusinessException("抄送记录不存在");
        }
        if (!Objects.equals(existing.getCcUserId(), userId)) {
            throw new BusinessException("无权操作该抄送");
        }
        if (existing.getReadStatus() != null && existing.getReadStatus() == 1) {
            return;
        }
        baseMapper.markRead(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllRead(Long userId) {
        return baseMapper.markAllRead(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateForNode(String processInstanceId, String processDefinitionId, String processName,
                                   String nodeId, String nodeName,
                                   List<Long> userIds,
                                   Long fromUserId, String fromUserName,
                                   String title, String content) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            WfCc entity = new WfCc();
            entity.setProcessInstanceId(processInstanceId);
            entity.setProcessDefinitionId(processDefinitionId);
            entity.setProcessName(processName);
            entity.setNodeId(nodeId);
            entity.setNodeName(nodeName);
            entity.setCcUserId(userId);
            entity.setFromUserId(fromUserId);
            entity.setFromUserName(fromUserName);
            entity.setTitle(title != null ? title : processName);
            entity.setContent(content);
            entity.setReadStatus(0);
            entity.setCreateTime(LocalDateTime.now());
            save(entity);
        }
    }

    /**
     * DTO → Entity 字段拷贝
     */
    private void copyDto(WfCcDTO dto, WfCc entity) {
        entity.setProcessInstanceId(dto.getProcessInstanceId());
        entity.setProcessDefinitionId(dto.getProcessDefinitionId());
        entity.setProcessName(dto.getProcessName());
        entity.setNodeId(dto.getNodeId());
        entity.setNodeName(dto.getNodeName());
        entity.setCcUserId(dto.getCcUserId());
        entity.setCcUserName(dto.getCcUserName());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setFromUserId(dto.getFromUserId());
        entity.setFromUserName(dto.getFromUserName());
    }
}
