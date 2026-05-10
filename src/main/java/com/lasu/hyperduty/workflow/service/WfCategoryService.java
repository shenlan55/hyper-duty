package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.workflow.entity.WfCategory;

/**
 * 流程分类服务
 */
public interface WfCategoryService extends IService<WfCategory> {

    /**
     * 分页查询分类
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<WfCategory> pageList(Integer pageNum, Integer pageSize);

    /**
     * 创建分类
     * @param category 分类
     * @return 分类
     */
    WfCategory createCategory(WfCategory category);

    /**
     * 更新分类
     * @param category 分类
     * @return 分类
     */
    WfCategory updateCategory(WfCategory category);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类
     */
    WfCategory getCategoryDetail(Long id);

}
