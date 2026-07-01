import request from '@/utils/request'

// 任务管理API
export function getTask(taskId) {
  return request({
    url: `/workflow/task/${taskId}`,
    method: 'get'
  })
}

export function pageTodoTask(pageNum, pageSize) {
  return request({
    url: '/workflow/task/todo/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function pageDoneTask(pageNum, pageSize) {
  return request({
    url: '/workflow/task/done/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getTaskVariables(taskId) {
  return request({
    url: `/workflow/task/variables/${taskId}`,
    method: 'get'
  })
}

export function setTaskVariables(taskId, variables) {
  return request({
    url: `/workflow/task/variables/${taskId}`,
    method: 'post',
    data: variables
  })
}

export function completeTask(data) {
  return request({
    url: '/workflow/task/complete',
    method: 'post',
    data
  })
}

export function reassignTask(data) {
  return request({
    url: '/workflow/task/reassign',
    method: 'post',
    data
  })
}

export function delegateTask(data) {
  return request({
    url: '/workflow/task/delegate',
    method: 'post',
    data
  })
}

export function batchReassignTask(data) {
  return request({
    url: '/workflow/task/batch-reassign',
    method: 'post',
    data
  })
}

export function claimTask(taskId) {
  return request({
    url: `/workflow/task/claim/${taskId}`,
    method: 'post'
  })
}

export function unclaimTask(taskId) {
  return request({
    url: `/workflow/task/unclaim/${taskId}`,
    method: 'post'
  })
}

export function listHistoryTasks(processInstanceId) {
  return request({
    url: `/workflow/task/history/list/${processInstanceId}`,
    method: 'get'
  })
}

export function getHistoryProcessInstance(processInstanceId) {
  return request({
    url: `/workflow/task/history/process/${processInstanceId}`,
    method: 'get'
  })
}

// ====================== 驳回相关 API ======================

/**
 * 列出可驳回的目标节点（按历史 UserTask 倒序）
 * @param {string} taskId 当前任务 ID
 */
export function listRejectTargets(taskId) {
  return request({
    url: `/workflow/task/reject-targets/${taskId}`,
    method: 'get'
  })
}

/**
 * 驳回到上一 UserTask（后端按历史顺序自动找）
 * @param {object} data { taskId, reason }
 */
export function rejectToPrevious(data) {
  return request({
    url: '/workflow/task/reject/previous',
    method: 'post',
    data
  })
}

/**
 * 驳回到指定 activityId 的历史 UserTask
 * @param {object} data { taskId, targetActivityId, reason }
 */
export function rejectToActivity(data) {
  return request({
    url: '/workflow/task/reject/activity',
    method: 'post',
    data
  })
}

/**
 * 驳回到发起人（runtime 跳回 startActivityId）
 * @param {object} data { taskId, reason }
 */
export function rejectToInitiator(data) {
  return request({
    url: '/workflow/task/reject/initiator',
    method: 'post',
    data
  })
}

// ====================== 加签/减签/取回 API ======================

/**
 * 加签：把 userId 加入 task 的 candidateUsers（多人会签）
 * @param {object} data { taskId, userId, reason }
 */
export function addSign(data) {
  return request({
    url: '/workflow/task/sign/add',
    method: 'post',
    data
  })
}

/**
 * 减签：从 task 的 candidateUsers 移除 userId
 * @param {object} data { taskId, userId, reason }
 */
export function removeSign(data) {
  return request({
    url: '/workflow/task/sign/remove',
    method: 'post',
    data
  })
}

/**
 * 取回：发起人/上一节点审批人把流程抢回
 * @param {object} data { taskId, reason }
 */
export function recallTask(data) {
  return request({
    url: '/workflow/task/recall',
    method: 'post',
    data
  })
}

/**
 * 自由跳转：管理员把当前 task 跳到任意指定 activityId
 * @param {object} data { taskId, targetActivityId, reason }
 */
export function jumpActivity(data) {
  return request({
    url: '/workflow/task/jump',
    method: 'post',
    data
  })
}
