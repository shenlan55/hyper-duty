package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.workflow.dto.WfProcessStartDTO;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * 流程管理服务
 */
public interface WfProcessService {

    /**
     * 部署流程
     * @param name 流程名称
     * @param bpmnXml BPMN XML
     * @return 部署结果
     */
    Deployment deployProcess(String name, String bpmnXml);

    /**
     * 删除部署
     * @param deploymentId 部署ID
     */
    void deleteDeployment(String deploymentId);

    /**
     * 分页查询流程定义
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param processKey 流程KEY
     * @return 分页结果
     */
    Page<ProcessDefinition> pageProcessDefinition(Integer pageNum, Integer pageSize, String processKey);

    /**
     * 获取流程定义列表
     * @return 流程定义列表
     */
    List<ProcessDefinition> listProcessDefinition();

    /**
     * 根据KEY获取最新版本流程定义
     * @param processKey 流程KEY
     * @return 流程定义
     */
    ProcessDefinition getLatestProcessDefinition(String processKey);

    /**
     * 获取BPMN XML
     * @param processDefinitionId 流程定义ID
     * @return BPMN XML
     */
    String getProcessBpmnXml(String processDefinitionId);

    /**
     * 启动流程
     * @param dto 启动参数
     * @return 流程实例
     */
    ProcessInstance startProcess(WfProcessStartDTO dto);

    /**
     * 根据实例ID获取流程实例
     * @param processInstanceId 流程实例ID
     * @return 流程实例
     */
    ProcessInstance getProcessInstance(String processInstanceId);

    /**
     * 根据业务KEY获取流程实例
     * @param processDefinitionKey 流程定义KEY
     * @param businessKey 业务KEY
     * @return 流程实例
     */
    ProcessInstance getProcessInstanceByBusinessKey(String processDefinitionKey, String businessKey);

    /**
     * 分页查询我发起的流程
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<ProcessInstance> pageMyStartedProcess(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 获取流程变量
     * @param processInstanceId 流程实例ID
     * @return 流程变量
     */
    Map<String, Object> getProcessVariables(String processInstanceId);

    /**
     * 设置流程变量
     * @param processInstanceId 流程实例ID
     * @param variables 变量
     */
    void setProcessVariables(String processInstanceId, Map<String, Object> variables);

    /**
     * 作废流程
     * @param processInstanceId 流程实例ID
     * @param reason 原因
     */
    void cancelProcess(String processInstanceId, String reason);

    /**
     * 挂起流程
     * @param processInstanceId 流程实例ID
     */
    void suspendProcess(String processInstanceId);

    /**
     * 激活流程
     * @param processInstanceId 流程实例ID
     */
    void activateProcess(String processInstanceId);

    /**
     * 获取历史流程活动实例（用于流程跟踪）
     * @param processInstanceId 流程实例ID
     * @return 活动实例列表
     */
    List<HistoricActivityInstance> getHistoricActivityInstances(String processInstanceId);

    /**
     * 获取历史流程实例
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例
     */
    HistoricProcessInstance getHistoricProcessInstance(String processInstanceId);

}
