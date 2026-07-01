package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.workflow.dto.WfDefinitionDiffDTO;
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
     * 分页查询我已完成的流程（历史）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<HistoricProcessInstance> pageMyCompletedProcess(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 获取流程变量
     * @param processInstanceId 流程实例ID
     * @return 流程变量
     */
    Map<String, Object> getProcessVariables(String processInstanceId);

    /**
     * ====================== P1-9 流程版本管理 ======================
     */

    /**
     * 分页查询指定流程 KEY 的所有历史版本
     * @param processKey 流程定义 KEY
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 历史版本分页（按 version 倒序）
     */
    Page<ProcessDefinition> pageProcessDefinitionVersions(String processKey, Integer pageNum, Integer pageSize);

    /**
     * 回滚到指定 deploymentId 的历史版本（用其 XML 重新部署为新版本）
     * @param deploymentId 历史部署 ID
     * @return 新部署结果
     */
    Deployment rollbackToVersion(String deploymentId);

    /**
     * 对比两个版本的 BPMN XML（按节点 + 连线维度）
     * @param deploymentIdA 版本 A
     * @param deploymentIdB 版本 B
     * @return 差异结果
     */
    WfDefinitionDiffDTO compareVersions(String deploymentIdA, String deploymentIdB);

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

    /**
     * 发起人撤回流程
     * <p>仅当流程未结束、当前用户是发起人、且尚无审批完成的节点时允许撤回。
     * @param processInstanceId 流程实例ID
     * @param reason 撤回原因
     */
    void withdrawProcess(String processInstanceId, String reason);

    /**
     * 获取流程跟踪图数据：含 BPMN XML 和已完成/当前节点列表
     * @param processInstanceId 流程实例ID
     * @return 流程跟踪数据
     */
    java.util.Map<String, Object> getProcessTraceData(String processInstanceId);

}
