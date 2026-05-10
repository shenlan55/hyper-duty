package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.WfDelegateConfigDTO;
import com.lasu.hyperduty.workflow.entity.WfDelegate;
import com.lasu.hyperduty.workflow.mapper.WfDelegateMapper;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 委托代理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfDelegateServiceImpl extends ServiceImpl<WfDelegateMapper, WfDelegate> implements WfDelegateService {

    @Override
    public Page<WfDelegate> pageList(Integer pageNum, Integer pageSize, Long delegatorId) {
        LambdaQueryWrapper<WfDelegate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(delegatorId != null, WfDelegate::getDelegatorId, delegatorId);
        wrapper.orderByDesc(WfDelegate::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfDelegate createDelegate(WfDelegateConfigDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        String userName = SecurityUtil.getCurrentUsername();

        WfDelegate delegate = new WfDelegate();
        delegate.setDelegatorId(userId);
        delegate.setDelegatorName(userName);
        delegate.setAttorneyId(dto.getAttorneyId());
        delegate.setAttorneyName(dto.getAttorneyName());
        delegate.setProcessDefinitionKey(dto.getProcessDefinitionKey());
        delegate.setStartTime(dto.getStartTime());
        delegate.setEndTime(dto.getEndTime());
        delegate.setRemark(dto.getRemark());
        delegate.setStatus(1);
        delegate.setCreateTime(LocalDateTime.now());
        delegate.setUpdateTime(LocalDateTime.now());

        save(delegate);
        return delegate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfDelegate updateDelegate(Long id, WfDelegateConfigDTO dto) {
        WfDelegate delegate = getById(id);
        if (delegate == null) {
            throw new BusinessException("委托配置不存在");
        }

        delegate.setAttorneyId(dto.getAttorneyId());
        delegate.setAttorneyName(dto.getAttorneyName());
        delegate.setProcessDefinitionKey(dto.getProcessDefinitionKey());
        delegate.setStartTime(dto.getStartTime());
        delegate.setEndTime(dto.getEndTime());
        delegate.setRemark(dto.getRemark());
        delegate.setUpdateTime(LocalDateTime.now());

        updateById(delegate);
        return delegate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDelegate(Long id) {
        WfDelegate delegate = getById(id);
        if (delegate == null) {
            throw new BusinessException("委托配置不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleDelegate(Long id, Integer status) {
        WfDelegate delegate = getById(id);
        if (delegate == null) {
            throw new BusinessException("委托配置不存在");
        }
        delegate.setStatus(status);
        delegate.setUpdateTime(LocalDateTime.now());
        updateById(delegate);
    }

    @Override
    public WfDelegate getActiveDelegate(Long delegatorId, String processDefinitionKey) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<WfDelegate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfDelegate::getDelegatorId, delegatorId);
        wrapper.eq(WfDelegate::getStatus, 1);
        wrapper.and(w -> w.eq(WfDelegate::getProcessDefinitionKey, processDefinitionKey)
                .or().isNull(WfDelegate::getProcessDefinitionKey));
        wrapper.le(WfDelegate::getStartTime, now);
        wrapper.ge(WfDelegate::getEndTime, now);
        wrapper.orderByDesc(WfDelegate::getCreateTime);
        wrapper.last("LIMIT 1");

        return getOne(wrapper);
    }
}
