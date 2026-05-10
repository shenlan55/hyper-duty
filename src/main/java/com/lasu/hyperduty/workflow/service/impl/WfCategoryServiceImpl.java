package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.entity.WfCategory;
import com.lasu.hyperduty.workflow.mapper.WfCategoryMapper;
import com.lasu.hyperduty.workflow.service.WfCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 流程分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfCategoryServiceImpl extends ServiceImpl<WfCategoryMapper, WfCategory> implements WfCategoryService {

    @Override
    public Page<WfCategory> pageList(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<WfCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(WfCategory::getSort)
                .orderByDesc(WfCategory::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfCategory createCategory(WfCategory category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        save(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfCategory updateCategory(WfCategory category) {
        WfCategory existing = getById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        WfCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        removeById(id);
    }

    @Override
    public WfCategory getCategoryDetail(Long id) {
        WfCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }
}
