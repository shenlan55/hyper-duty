package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.workflow.entity.WfTemplate;

import java.util.List;

/**
 * 流程模板 Service
 */
public interface WfTemplateService {

    /**
     * 分页查询模板（带分类/状态过滤）
     */
    Page<WfTemplate> pageTemplates(Integer pageNum, Integer pageSize, String category, Integer status);

    /**
     * 列出所有启用中的模板（供模板市场页面下拉）
     */
    List<WfTemplate> listActive(String category);

    /**
     * 详情
     */
    WfTemplate getById(Long id);

    /**
     * 根据 KEY 获取
     */
    WfTemplate getByKey(String templateKey);

    /**
     * 新建/更新模板
     */
    void saveTemplate(WfTemplate template);

    /**
     * 删除（逻辑删）
     */
    void deleteTemplate(Long id);

    /**
     * 使用模板（useCount+1）
     */
    void incrementUseCount(Long id);
}
