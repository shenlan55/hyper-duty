package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.workflow.entity.WfNodeHandler;
import com.lasu.hyperduty.workflow.dto.WfNodeHandlerDTO;

import java.util.List;

/**
 * 节点处理人配置服务
 */
public interface WfNodeHandlerService extends IService<WfNodeHandler> {

    /**
     * 保存/更新节点处理人配置（按 processDefinitionId + nodeId 唯一约束）
     * @param dto 节点处理人配置
     * @return 节点处理人配置
     */
    WfNodeHandler saveNodeHandler(WfNodeHandlerDTO dto);

    /**
     * 批量保存节点处理人配置（部署时使用）
     * @param processDefinitionId 流程定义ID
     * @param processDefinitionKey 流程定义KEY
     * @param handlers 节点处理人配置列表
     */
    void saveBatch(String processDefinitionId, String processDefinitionKey, List<WfNodeHandlerDTO> handlers);

    /**
     * 根据流程定义ID查询所有节点处理人配置
     * @param processDefinitionId 流程定义ID
     * @return 节点处理人配置列表
     */
    List<WfNodeHandler> listByProcessDefinitionId(String processDefinitionId);

    /**
     * 根据流程定义KEY查询最新版本的所有节点处理人配置
     * @param processDefinitionKey 流程定义KEY
     * @return 节点处理人配置列表
     */
    List<WfNodeHandler> listByProcessDefinitionKey(String processDefinitionKey);

    /**
     * 删除节点处理人配置
     * @param id 主键
     */
    void deleteById(Long id);

    /**
     * 删除流程的所有节点处理人配置
     * @param processDefinitionId 流程定义ID
     */
    void deleteByProcessDefinitionId(String processDefinitionId);
}
