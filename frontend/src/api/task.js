import request from '../utils/request'

export function getTaskPage(params) {
  return request({
    url: '/pm/task/page',
    method: 'get',
    params
  })
}

export function getProjectTasks(projectId) {
  return request({
    url: `/pm/task/project/${projectId}`,
    method: 'get'
  })
}

export function getSubTasks(parentId) {
  return request({
    url: `/pm/task/sub/${parentId}`,
    method: 'get'
  })
}

export function getMyTasks(employeeId) {
  return request({
    url: `/pm/task/my/${employeeId}`,
    method: 'get'
  })
}

export function getMyTasksByProject(employeeId, projectId) {
  return request({
    url: `/pm/task/my/${employeeId}/project/${projectId}`,
    method: 'get'
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/pm/task/${id}`,
    method: 'get'
  })
}

export function createTask(data) {
  return request({
    url: '/pm/task',
    method: 'post',
    data
  })
}

export function updateTask(data) {
  return request({
    url: '/pm/task',
    method: 'put',
    data
  })
}

export function updateProgress(taskId, progress) {
  return request({
    url: `/pm/task/progress/${taskId}`,
    method: 'put',
    params: { progress }
  })
}

export function deleteTask(id) {
  return request({
    url: `/pm/task/${id}`,
    method: 'delete'
  })
}

export function getUpcomingTasks() {
  return request({
    url: '/pm/task/upcoming',
    method: 'get'
  })
}

export function getTasksByStatus(status) {
  return request({
    url: `/pm/task/status/${status}`,
    method: 'get'
  })
}

export function pinTask(taskId, pinned) {
  return request({
    url: `/pm/task/pin/${taskId}`,
    method: 'put',
    params: { pinned }
  })
}

export function getTaskComments(taskId) {
  return request({
    url: `/pm/task/comment/list`,
    method: 'get',
    params: { taskId }
  })
}

export function addTaskComment(data) {
  return request({
    url: `/pm/task/comment/add`,
    method: 'post',
    data
  })
}

// 任务进展更新相关API
export function createProgressUpdate(data) {
  return request({
    url: '/pm/task/progress/update',
    method: 'post',
    data
  })
}

export function getTaskProgressUpdates(taskId) {
  return request({
    url: `/pm/task/progress/update/task/${taskId}`,
    method: 'get'
  })
}

export function getProgressUpdateDetail(id) {
  return request({
    url: `/pm/task/progress/update/${id}`,
    method: 'get'
  })
}

export function hasTaskPermission(taskId, employeeId) {
  return request({
    url: `/pm/task/permission/${taskId}/${employeeId}`,
    method: 'get'
  })
}

export function hasTaskDeletePermission(taskId, employeeId) {
  return request({
    url: `/pm/task/permission/delete/${taskId}/${employeeId}`,
    method: 'get'
  })
}

export function exportGantt(projectId) {
  return request({
    url: '/duty/export/gantt',
    method: 'get',
    params: { projectId },
    responseType: 'blob'
  })
}

export function getWorkloadPage(params) {
  return request({
    url: '/pm/task/workload/page',
    method: 'get',
    params
  })
}
