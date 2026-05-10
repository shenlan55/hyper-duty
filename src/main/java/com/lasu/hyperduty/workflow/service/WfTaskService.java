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

}
