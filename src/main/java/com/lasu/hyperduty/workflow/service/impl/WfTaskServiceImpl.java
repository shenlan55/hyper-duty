package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.*;
import com.lasu.hyperduty.workflow.service.WfTaskService;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfTaskServiceImpl implements WfTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final IdentityService identityService;
    private final WfDelegateService wfDelegateService;

    /**
     * 将Flowable HistoricTaskInstance转换为DTO
     */
    private HistoricTaskDTO convertToHistoricTaskDTO(HistoricTaskInstance historicTask) {
        HistoricTaskDTO dto = new HistoricTaskDTO();
        dto.setId(historicTask.getId());
        dto.setName(historicTask.getName());
        dto.setDescription(historicTask.getDescription());
        dto.setTaskDefinitionKey(historicTask.getTaskDefinitionKey());
        dto.setProcessInstanceId(historicTask.getProcessInstanceId());
        dto.setProcessDefinitionId(historicTask.getProcessDefinitionId());
        dto.setExecutionId(historicTask.getExecutionId());
        dto.setAssignee(historicTask.getAssignee());
        dto.setStartTime(historicTask.getStartTime());
        dto.setEndTime(historicTask.getEndTime());
        dto.setDurationInMillis(historicTask.getDurationInMillis());
        dto.setDeleteReason(historicTask.getDeleteReason());
        dto.setPriority(historicTask.getPriority());
        dto.setDueDate(historicTask.getDueDate());
        dto.setFormKey(historicTask.getFormKey());
        return dto;
    }

    /**
     * 将Flowable HistoricProcessInstance转换为DTO
     */
    private HistoricProcessInstanceDTO convertToHistoricProcessDTO(HistoricProcessInstance historicProcess) {
        HistoricProcessInstanceDTO dto = new HistoricProcessInstanceDTO();
        dto.setId(historicProcess.getId());
        dto.setBusinessKey(historicProcess.getBusinessKey());
        dto.setProcessDefinitionId(historicProcess.getProcessDefinitionId());
        dto.setProcessDefinitionKey(historicProcess.getProcessDefinitionKey());
        dto.setProcessDefinitionName(historicProcess.getProcessDefinitionName());
        dto.setProcessDefinitionVersion(historicProcess.getProcessDefinitionVersion());
        dto.setDeploymentId(historicProcess.getDeploymentId());
        dto.setStartTime(historicProcess.getStartTime());
        dto.setEndTime(historicProcess.getEndTime());
        dto.setDurationInMillis(historicProcess.getDurationInMillis());
        dto.setStartUserId(historicProcess.getStartUserId());
        dto.setStartActivityId(historicProcess.getStartActivityId());
        dto.setDeleteReason(historicProcess.getDeleteReason());
        dto.setSuperProcessInstanceId(historicProcess.getSuperProcessInstanceId());
        return dto;
    }

    @Override
    public Task getTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public Page<Task> pageTodoTask(Integer pageNum, Integer pageSize, Long userId) {
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(userId.toString())
                .orderByTaskCreateTime().desc();

        long total = query.count();
        List<Task> list = query.listPage((pageNum - 1) * pageSize, pageSize);

        Page<Task> page = new Page<>(pageNum, pageSize);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<HistoricTaskDTO> pageDoneTask(Integer pageNum, Integer pageSize, Long userId) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId.toString())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();

        long total = query.count();
        List<HistoricTaskInstance> historicTasks = query.listPage((pageNum - 1) * pageSize, pageSize);
        List<HistoricTaskDTO> list = historicTasks.stream()
                .map(this::convertToHistoricTaskDTO)
                .collect(Collectors.toList());

        Page<HistoricTaskDTO> page = new Page<>(pageNum, pageSize);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<Task> listTodoTasks(Long userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId.toString())
                .orderByTaskCreateTime().desc()
                .list();
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    @Override
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(WfTaskCompleteDTO dto) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            identityService.setAuthenticatedUserId(userId.toString());

            if (StringUtils.hasText(dto.getComment())) {
                Task task = getTask(dto.getTaskId());
                taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), dto.getComment());
            }

            if (dto.getVariables() != null) {
                taskService.complete(dto.getTaskId(), dto.getVariables());
            } else {
                taskService.complete(dto.getTaskId());
            }
        } catch (Exception e) {
            log.error("完成任务失败", e);
            throw new BusinessException("完成任务失败：" + e.getMessage());
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reassignTask(WfTaskReassignDTO dto) {
        try {
            taskService.setAssignee(dto.getTaskId(), dto.getUserId().toString());
        } catch (Exception e) {
            log.error("转办任务失败", e);
            throw new BusinessException("转办任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WfTaskDelegateDTO dto) {
        try {
            taskService.delegateTask(dto.getTaskId(), dto.getUserId().toString());
        } catch (Exception e) {
            log.error("委托任务失败", e);
            throw new BusinessException("委托任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReassignTask(WfTaskBatchReassignDTO dto) {
        try {
            List<Task> tasks;
            if (CollectionUtils.isEmpty(dto.getTaskIds())) {
                tasks = listTodoTasks(dto.getFromUserId());
            } else {
                tasks = taskService.createTaskQuery()
                        .taskIds(dto.getTaskIds())
                        .taskAssignee(dto.getFromUserId().toString())
                        .list();
            }

            for (Task task : tasks) {
                taskService.setAssignee(task.getId(), dto.getToUserId().toString());
            }
        } catch (Exception e) {
            log.error("批量转办任务失败", e);
            throw new BusinessException("批量转办任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(String taskId, Long userId) {
        try {
            taskService.claim(taskId, userId.toString());
        } catch (Exception e) {
            log.error("认领任务失败", e);
            throw new BusinessException("认领任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unclaimTask(String taskId) {
        try {
            taskService.unclaim(taskId);
        } catch (Exception e) {
            log.error("取消认领任务失败", e);
            throw new BusinessException("取消认领任务失败：" + e.getMessage());
        }
    }

    @Override
    public List<HistoricTaskDTO> listHistoryTasks(String processInstanceId) {
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        return historicTasks.stream()
                .map(this::convertToHistoricTaskDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HistoricProcessInstanceDTO getHistoryProcessInstance(String processInstanceId) {
        HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (historicProcess == null) {
            return null;
        }
        return convertToHistoricProcessDTO(historicProcess);
    }
}
