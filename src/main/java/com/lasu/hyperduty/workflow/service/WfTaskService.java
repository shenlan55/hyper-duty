package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.workflow.dto.*;
import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

/**
 * 任务管理服务
 */
public interface WfTaskService {

    /**
     * 获取任务详情
     * @param taskId 任务ID
     * @return 任务
     */
    Task getTask(String taskId);

    /**
     * 分页查询待办任务
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<Task> pageTodoTask(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 分页查询已办任务
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<HistoricTaskDTO> pageDoneTask(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 查询用户的所有待办任务
     * @param userId 用户ID
     * @return 任务列表
     */
    List<Task> listTodoTasks(Long userId);

    /**
     * 获取任务变量
     * @param taskId 任务ID
     * @return 变量
     */
    Map<String, Object> getTaskVariables(String taskId);

    /**
     * 设置任务变量
     * @param taskId 任务ID
     * @param variables 变量
     */
    void setTaskVariables(String taskId, Map<String, Object> variables);

    /**
     * 完成任务
     * @param dto 完成参数
     */
    void completeTask(WfTaskCompleteDTO dto);

    /**
     * 转办任务
     * @param dto 转办参数
     */
    void reassignTask(WfTaskReassignDTO dto);

    /**
     * 委托任务
     * @param dto 委托参数
     */
    void delegateTask(WfTaskDelegateDTO dto);

    /**
     * 批量转办任务
     * @param dto 批量转办参数
     */
    void batchReassignTask(WfTaskBatchReassignDTO dto);

    /**
     * 认领任务
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void claimTask(String taskId, Long userId);

    /**
     * 取消认领任务
     * @param taskId 任务ID
     */
    void unclaimTask(String taskId);

    /**
     * 获取流程实例的历史任务
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    List<HistoricTaskDTO> listHistoryTasks(String processInstanceId);

    /**
     * 获取流程实例的历史流程记录
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例
     */
    HistoricProcessInstanceDTO getHistoryProcessInstance(String processInstanceId);

    // ====================== 驳回相关 ======================

    /**
     * 列出可驳回的目标节点（按结束时间倒序的历史 UserTask，排除当前任务）
     * @param taskId 当前任务ID
     * @return 目标节点列表
     */
    List<RejectTargetDTO> listRejectTargets(String taskId);

    /**
     * 驳回到上一 UserTask（自动按历史顺序找最近的结束过的 userTask）
     * @param dto 驳回参数
     */
    void rejectToPrevious(WfTaskRejectDTO dto);

    /**
     * 驳回到指定 activityId 的历史 UserTask（自由驳回）
     * @param dto 驳回参数（dto.targetActivityId 必填）
     */
    void rejectToActivity(WfTaskRejectDTO dto);

    /**
     * 驳回到发起人（删除当前 runtime 任务，流程回到 StartEvent）
     * @param dto 驳回参数
     */
    void rejectToInitiator(WfTaskRejectDTO dto);

    // ====================== 加签/减签/取回 ======================

    /**
     * 加签：把指定 userId 加入当前 task 的 candidateUsers（多人会签）
     * @param dto 加签参数
     */
    void addSign(WfTaskSignDTO dto);

    /**
     * 减签：从当前 task 的 candidateUsers 移除指定 userId
     * @param dto 减签参数
     */
    void removeSign(WfTaskSignDTO dto);

    /**
     * 取回：由发起人 / 上一节点审批人把流程抢回到自己
     * 触发条件：当前 task 处于 pending（runtime 存在），上一 userTask 已 finished
     * 行为：runtime jump 回上一节点的 taskDefinitionKey
     * @param taskId 当前任务ID
     * @param reason 取回原因
     */
    void recall(String taskId, String reason);

    /**
     * 自由跳转：管理员/特权角色把当前 task 跳到任意指定 activityId
     * @param dto 跳转参数
     */
    void jumpToActivity(WfTaskJumpDTO dto);

}
