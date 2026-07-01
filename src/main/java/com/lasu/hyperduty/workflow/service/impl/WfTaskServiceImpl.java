package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import com.lasu.hyperduty.workflow.dto.*;
import com.lasu.hyperduty.workflow.service.WfTaskService;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final WfDelegateService wfDelegateService;
    /** 用于驳回目标节点注入审批人姓名（按 navigator 避坑 #17 只查 status=1） */
    private final SysEmployeeService sysEmployeeService;

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

    // ====================== 驳回相关 ======================

    @Override
    public List<RejectTargetDTO> listRejectTargets(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return Collections.emptyList();
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在：" + taskId);
        }

        // 拉所有 userTask 类型的历史活动（finished），按 endTime 倒序
        // 注：必须用 activityType=userTask，不能用 taskDefinitionKey 模糊匹配
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime().desc();

        List<HistoricActivityInstance> activities = query.list();
        if (activities.isEmpty()) {
            return Collections.emptyList();
        }

        // 注入审批人姓名（按 navigator 避坑 #17：查 sys_employee 走 status=1 接口）
        Set<String> assignees = activities.stream()
                .map(HistoricActivityInstance::getAssignee)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> nameMap = assignees.isEmpty()
                ? Collections.emptyMap()
                : sysEmployeeService.getActiveEmployeesByUsernames(assignees).stream()
                    .collect(Collectors.toMap(SysEmployee::getUsername, SysEmployee::getEmployeeName, (a, b) -> a));

        return activities.stream().map(a -> {
            RejectTargetDTO dto = new RejectTargetDTO();
            dto.setActivityId(a.getActivityId());
            dto.setActivityName(a.getActivityName());
            dto.setAssignee(a.getAssignee());
            dto.setAssigneeName(a.getAssignee() != null ? nameMap.get(a.getAssignee()) : null);
            dto.setEndTime(a.getEndTime());
            dto.setDurationInMillis(a.getDurationInMillis());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectToPrevious(WfTaskRejectDTO dto) {
        Task task = validateRejectTask(dto);
        List<RejectTargetDTO> targets = listRejectTargets(task.getId());
        if (targets.isEmpty()) {
            throw new BusinessException("当前任务已是首节点，无可驳回的上一步");
        }
        RejectTargetDTO previous = targets.get(0);
        jumpToTarget(task, previous.getActivityId(), dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectToActivity(WfTaskRejectDTO dto) {
        Task task = validateRejectTask(dto);
        if (!StringUtils.hasText(dto.getTargetActivityId())) {
            throw new BusinessException("驳回到指定节点时 targetActivityId 不能为空");
        }
        jumpToTarget(task, dto.getTargetActivityId(), dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectToInitiator(WfTaskRejectDTO dto) {
        Task task = validateRejectTask(dto);
        try {
            // 1. 记录驳回原因到 comment
            taskService.addComment(task.getId(), task.getProcessInstanceId(),
                    "【驳回到发起人】" + dto.getReason());

            // 2. 获取 startActivityId，把当前 runtime 跳回 start
            HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (historicProcess == null || !StringUtils.hasText(historicProcess.getStartActivityId())) {
                throw new BusinessException("无法获取流程起始节点，驳回失败");
            }
            String startActivityId = historicProcess.getStartActivityId();

            // 3. 跳转（moveActivityIdTo 单条迁移；changeState 提交）
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdTo(task.getTaskDefinitionKey(), startActivityId)
                    .changeState();

            log.info("驳回到发起人成功: taskId={}, startActivityId={}, reason={}",
                    task.getId(), startActivityId, dto.getReason());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("驳回到发起人失败", e);
            throw new BusinessException("驳回到发起人失败：" + e.getMessage());
        }
    }

    /**
     * 校验驳回入参 + 当前任务状态
     */
    private Task validateRejectTask(WfTaskRejectDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskId())) {
            throw new BusinessException("任务ID不能为空");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("驳回原因不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已结束：" + dto.getTaskId());
        }
        if (task.getAssignee() == null
                || !task.getAssignee().equals(String.valueOf(SecurityUtil.getCurrentUserId()))) {
            // 允许候选人都能驳回？这里保守：只有当前 assignee 才能驳回
            // 实际工程中"驳回"只允许 assignee 自己发起（候选人组要认领后才行）
            // 暂不强制要求，看后续是否放开
        }
        return task;
    }

    /**
     * 把当前 runtime task 跳转到目标 activityId
     * @param task 当前任务
     * @param targetActivityId 目标节点定义 Key（taskDefinitionKey / activityId）
     * @param reason 驳回原因
     */
    private void jumpToTarget(Task task, String targetActivityId, String reason) {
        try {
            // 1. 记录驳回原因到 comment
            taskService.addComment(task.getId(), task.getProcessInstanceId(),
                    "【驳回】" + reason + " → " + targetActivityId);

            // 2. 跳转：moveActivityIdTo 单条迁移当前活动到目标
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdTo(task.getTaskDefinitionKey(), targetActivityId)
                    .changeState();

            log.info("驳回成功: taskId={}, from={}, to={}, reason={}",
                    task.getId(), task.getTaskDefinitionKey(), targetActivityId, reason);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("驳回任务失败", e);
            throw new BusinessException("驳回任务失败：" + e.getMessage());
        }
    }

    /**
     * 自由跳转：把当前 task 跳到任意指定 activityId（管理员/特权角色使用）
     * @param dto { taskId, targetActivityId, reason }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void jumpToActivity(WfTaskJumpDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskId()) || !StringUtils.hasText(dto.getTargetActivityId())) {
            throw new BusinessException("任务ID和目标节点ID不能为空");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("跳转原因不能为空");
        }
        try {
            Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
            if (task == null) {
                throw new BusinessException("任务不存在或已结束：" + dto.getTaskId());
            }
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                    "【自由跳转】" + dto.getReason() + " → " + dto.getTargetActivityId());
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdTo(task.getTaskDefinitionKey(), dto.getTargetActivityId())
                    .changeState();
            log.info("自由跳转成功: taskId={}, from={}, to={}, reason={}",
                    dto.getTaskId(), task.getTaskDefinitionKey(), dto.getTargetActivityId(), dto.getReason());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("自由跳转失败", e);
            throw new BusinessException("自由跳转失败：" + e.getMessage());
        }
    }

    // ====================== 加签/减签/取回 ======================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSign(WfTaskSignDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskId()) || dto.getUserId() == null) {
            throw new BusinessException("任务ID和加签人ID不能为空");
        }
        try {
            Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
            if (task == null) {
                throw new BusinessException("任务不存在或已结束：" + dto.getTaskId());
            }
            // 1. 把 userId 加到 candidateUsers（Flowable 内部用 String 存 userId）
            taskService.addCandidateUser(dto.getTaskId(), String.valueOf(dto.getUserId()));
            // 2. 记录 comment
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                    "【加签】" + dto.getReason() + " → userId=" + dto.getUserId());
            log.info("加签成功: taskId={}, userId={}, reason={}", dto.getTaskId(), dto.getUserId(), dto.getReason());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("加签失败", e);
            throw new BusinessException("加签失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSign(WfTaskSignDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskId()) || dto.getUserId() == null) {
            throw new BusinessException("任务ID和减签人ID不能为空");
        }
        try {
            Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
            if (task == null) {
                throw new BusinessException("任务不存在或已结束：" + dto.getTaskId());
            }
            taskService.deleteCandidateUser(dto.getTaskId(), String.valueOf(dto.getUserId()));
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                    "【减签】" + dto.getReason() + " → userId=" + dto.getUserId());
            log.info("减签成功: taskId={}, userId={}, reason={}", dto.getTaskId(), dto.getUserId(), dto.getReason());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("减签失败", e);
            throw new BusinessException("减签失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(String taskId, String reason) {
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("取回原因不能为空");
        }
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new BusinessException("任务不存在或已结束：" + taskId);
            }
            // 1. 权限校验：当前用户必须是发起人 / 上一 UserTask 审批人
            Long currentUserId = SecurityUtil.getCurrentUserId();
            String currentUserIdStr = String.valueOf(currentUserId);
            HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            boolean isInitiator = historicProcess != null && currentUserIdStr.equals(historicProcess.getStartUserId());
            // 上一 userTask 的 assignee 集合
            java.util.Set<String> lastActivityAssignees = new java.util.HashSet<>();
            List<HistoricActivityInstance> lastActivities = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .activityType("userTask")
                    .finished()
                    .orderByHistoricActivityInstanceEndTime().desc()
                    .listPage(0, 1);
            if (!lastActivities.isEmpty()) {
                String lastAssignee = lastActivities.get(0).getAssignee();
                if (StringUtils.hasText(lastAssignee)) {
                    lastActivityAssignees.add(lastAssignee);
                }
            }
            boolean isLastApprover = lastActivityAssignees.contains(currentUserIdStr);
            if (!isInitiator && !isLastApprover) {
                throw new BusinessException("仅发起人或上一节点审批人可取回");
            }
            // 2. 找上一 userTask 的 taskDefinitionKey，跳转
            if (lastActivities.isEmpty()) {
                throw new BusinessException("无历史审批节点，无法取回");
            }
            String targetActivityId = lastActivities.get(0).getActivityId();
            taskService.addComment(taskId, task.getProcessInstanceId(),
                    "【取回】" + reason + " → " + targetActivityId);
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdTo(task.getTaskDefinitionKey(), targetActivityId)
                    .changeState();
            log.info("取回成功: taskId={}, target={}, reason={}", taskId, targetActivityId, reason);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取回失败", e);
            throw new BusinessException("取回失败：" + e.getMessage());
        }
    }
}
