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
