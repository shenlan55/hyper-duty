package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.entity.WfForm;
import com.lasu.hyperduty.workflow.mapper.WfFormMapper;
import com.lasu.hyperduty.workflow.service.WfFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 流程表单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfFormServiceImpl extends ServiceImpl<WfFormMapper, WfForm> implements WfFormService {

    @Override
    public Page<WfForm> pageList(Integer pageNum, Integer pageSize, String name) {
        LambdaQueryWrapper<WfForm> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(WfForm::getName, name);
        }
        wrapper.orderByDesc(WfForm::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfForm createForm(WfForm form) {
        form.setVersion(1);
        form.setCreateTime(LocalDateTime.now());
        form.setUpdateTime(LocalDateTime.now());
        if (form.getStatus() == null) {
            form.setStatus(1);
        }
        if (form.getFormType() == null) {
            form.setFormType("dynamic");
        }
        save(form);
        return form;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfForm updateForm(WfForm form) {
        WfForm existing = getById(form.getId());
        if (existing == null) {
            throw new BusinessException("表单不存在");
        }
        form.setVersion(existing.getVersion() + 1);
        form.setUpdateTime(LocalDateTime.now());
        updateById(form);
        return form;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForm(Long id) {
        WfForm form = getById(id);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }
        removeById(id);
    }

    @Override
    public WfForm getFormDetail(Long id) {
        WfForm form = getById(id);
        if (form == null) {
            throw new BusinessException("表单不存在");
        }
        return form;
    }
}
