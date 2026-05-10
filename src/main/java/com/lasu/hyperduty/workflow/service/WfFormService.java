package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.workflow.entity.WfForm;

/**
 * 流程表单服务
 */
public interface WfFormService extends IService<WfForm> {

    /**
     * 分页查询表单
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 表单名称
     * @return 分页结果
     */
    Page<WfForm> pageList(Integer pageNum, Integer pageSize, String name);

    /**
     * 创建表单
     * @param form 表单
     * @return 表单
     */
    WfForm createForm(WfForm form);

    /**
     * 更新表单
     * @param form 表单
     * @return 表单
     */
    WfForm updateForm(WfForm form);

    /**
     * 删除表单
     * @param id 表单ID
     */
    void deleteForm(Long id);

    /**
     * 获取表单详情
     * @param id 表单ID
     * @return 表单
     */
    WfForm getFormDetail(Long id);

}
