package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.WfNodeHandlerDTO;
import com.lasu.hyperduty.workflow.entity.WfNodeHandler;
import com.lasu.hyperduty.workflow.mapper.WfNodeHandlerMapper;
import com.lasu.hyperduty.workflow.service.WfNodeHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 节点处理人配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfNodeHandlerServiceImpl extends ServiceImpl<WfNodeHandlerMapper, WfNodeHandler> implements WfNodeHandlerService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfNodeHandler saveNodeHandler(WfNodeHandlerDTO dto) {
        validate(dto);
        WfNodeHandler entity = new WfNodeHandler();
        copyDtoToEntity(dto, entity);

        // 唯一约束：processDefinitionId + nodeId
        WfNodeHandler existing = baseMapper.selectByNode(entity.getProcessDefinitionId(), entity.getNodeId());
        if (existing != null && !Objects.equals(existing.getId(), dto.getId())) {
            throw new BusinessException("节点处理人配置已存在：processDefinitionId=" + entity.getProcessDefinitionId() + ", nodeId=" + entity.getNodeId());
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
            entity.setUpdateTime(LocalDateTime.now());
            updateById(entity);
            return entity;
        }

        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(String processDefinitionId, String processDefinitionKey, List<WfNodeHandlerDTO> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        // 先清空同流程的旧配置（按 processDefinitionId 唯一）
        baseMapper.deleteByProcessDefinitionId(processDefinitionId);
        int seq = 0;
        for (WfNodeHandlerDTO dto : handlers) {
            dto.setProcessDefinitionId(processDefinitionId);
            dto.setProcessDefinitionKey(processDefinitionKey);
            dto.setId(null);
            if (dto.getSeq() == null) {
                dto.setSeq(seq++);
            }
            WfNodeHandler entity = new WfNodeHandler();
            copyDtoToEntity(dto, entity);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            save(entity);
        }
    }

    @Override
    public List<WfNodeHandler> listByProcessDefinitionId(String processDefinitionId) {
        return baseMapper.selectByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public List<WfNodeHandler> listByProcessDefinitionKey(String processDefinitionKey) {
        return baseMapper.selectByProcessDefinitionKey(processDefinitionKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProcessDefinitionId(String processDefinitionId) {
        baseMapper.deleteByProcessDefinitionId(processDefinitionId);
    }

    /**
     * 字段校验
     */
    private void validate(WfNodeHandlerDTO dto) {
        if (dto == null) {
            throw new BusinessException("节点处理人配置不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(dto.getProcessDefinitionId())) {
            throw new BusinessException("流程定义ID不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(dto.getNodeId())) {
            throw new BusinessException("节点ID不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(dto.getHandlerType())) {
            throw new BusinessException("处理人类型不能为空");
        }
    }

    /**
     * DTO → Entity 字段拷贝
     */
    private void copyDtoToEntity(WfNodeHandlerDTO dto, WfNodeHandler entity) {
        entity.setProcessDefinitionId(dto.getProcessDefinitionId());
        entity.setProcessDefinitionKey(dto.getProcessDefinitionKey());
        entity.setNodeId(dto.getNodeId());
        entity.setNodeName(dto.getNodeName());
        entity.setHandlerType(dto.getHandlerType());
        entity.setHandlerConfig(dto.getHandlerConfig());
        entity.setMultiInstanceType(dto.getMultiInstanceType());
        entity.setMultiInstanceConfig(dto.getMultiInstanceConfig());
        entity.setCcConfig(dto.getCcConfig());
        entity.setSeq(dto.getSeq());
    }
}
