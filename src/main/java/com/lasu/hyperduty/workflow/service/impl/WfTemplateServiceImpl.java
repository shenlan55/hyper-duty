package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.workflow.entity.WfTemplate;
import com.lasu.hyperduty.workflow.mapper.WfTemplateMapper;
import com.lasu.hyperduty.workflow.service.WfTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 流程模板 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfTemplateServiceImpl implements WfTemplateService {

    private final WfTemplateMapper mapper;

    @Override
    public Page<WfTemplate> pageTemplates(Integer pageNum, Integer pageSize, String category, Integer status) {
        Page<WfTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WfTemplate> qw = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) qw.eq(WfTemplate::getCategory, category);
        if (status != null) qw.eq(WfTemplate::getStatus, status);
        qw.eq(WfTemplate::getDeleted, 0);
        qw.orderByAsc(WfTemplate::getSortNo).orderByDesc(WfTemplate::getUseCount);
        return mapper.selectPage(page, qw);
    }

    @Override
    public List<WfTemplate> listActive(String category) {
        LambdaQueryWrapper<WfTemplate> qw = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) qw.eq(WfTemplate::getCategory, category);
        qw.eq(WfTemplate::getStatus, 1).eq(WfTemplate::getDeleted, 0);
        qw.orderByAsc(WfTemplate::getSortNo);
        return mapper.selectList(qw);
    }

    @Override
    public WfTemplate getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public WfTemplate getByKey(String templateKey) {
        LambdaQueryWrapper<WfTemplate> qw = new LambdaQueryWrapper<>();
        qw.eq(WfTemplate::getTemplateKey, templateKey).eq(WfTemplate::getDeleted, 0);
        return mapper.selectOne(qw);
    }

    @Override
    public void saveTemplate(WfTemplate template) {
        Date now = new Date();
        if (template.getId() == null) {
            if (template.getStatus() == null) template.setStatus(1);
            if (template.getUseCount() == null) template.setUseCount(0);
            if (template.getDeleted() == null) template.setDeleted(0);
            template.setCreateTime(now);
            template.setUpdateTime(now);
            mapper.insert(template);
        } else {
            template.setUpdateTime(now);
            mapper.updateById(template);
        }
    }

    @Override
    public void deleteTemplate(Long id) {
        WfTemplate t = new WfTemplate();
        t.setId(id);
        t.setDeleted(1);
        t.setUpdateTime(new Date());
        mapper.updateById(t);
    }

    @Override
    public void incrementUseCount(Long id) {
        mapper.incrementUseCount(id);
    }
}
