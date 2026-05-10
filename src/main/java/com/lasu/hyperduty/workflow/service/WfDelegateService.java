package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.workflow.dto.WfDelegateConfigDTO;
import com.lasu.hyperduty.workflow.entity.WfDelegate;

/**
 * 委托代理服务
 */
public interface WfDelegateService extends IService<WfDelegate> {

    /**
     * 分页查询委托配置
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param delegatorId 委托人ID
     * @return 分页结果
     */
    Page<WfDelegate> pageList(Integer pageNum, Integer pageSize, Long delegatorId);

    /**
     * 创建委托配置
     * @param dto 委托参数
     * @return 委托配置
     */
    WfDelegate createDelegate(WfDelegateConfigDTO dto);

    /**
     * 更新委托配置
     * @param id ID
     * @param dto 委托参数
     * @return 委托配置
     */
    WfDelegate updateDelegate(Long id, WfDelegateConfigDTO dto);

    /**
     * 删除委托配置
     * @param id ID
     */
    void deleteDelegate(Long id);

    /**
     * 启用/禁用委托配置
     * @param id ID
     * @param status 状态
     */
    void toggleDelegate(Long id, Integer status);

    /**
     * 获取用户当前有效的委托配置
     * @param delegatorId 委托人ID
     * @param processDefinitionKey 流程定义KEY
     * @return 委托配置
     */
    WfDelegate getActiveDelegate(Long delegatorId, String processDefinitionKey);

}
