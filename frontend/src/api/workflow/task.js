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
