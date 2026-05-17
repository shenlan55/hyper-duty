import request from '../utils/request'

/**
 * 项目管理 API
 * 使用 DTO 和统一的响应格式
 */

// ============================================
// 任务相关 API
// ============================================

export function getTaskPage(params) {
  return request({
    url: '/pm/task/page',
    method: 'get',
    params
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/pm/task/detail/${id}`,
    method: 'get'
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

export function getMyTasks(employeeId, taskName) {
  return request({
    url: `/pm/task/my/${employeeId}`,
    method: 'get',
    params: { taskName }
  })
}

export function getMyTasksByProject(employeeId, projectId, taskName) {
  return request({
    url: `/pm/task/my/${employeeId}/project/${projectId}`,
    method: 'get',
    params: { taskName }
  })
}

export function getUpcomingTasks(employeeId) {
  return request({
    url: `/pm/task/upcoming/${employeeId}`,
    method: 'get'
  })
}

export function getTasksByStatus(status) {
  return request({
    url: `/pm/task/status/${status}`,
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

export function batchCreateTasks(data) {
  return request({
    url: '/pm/task/batch',
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

export function pinTask(taskId, pinned) {
  return request({
    url: `/pm/task/pin/${taskId}`,
    method: 'put',
    params: { pinned }
  })
}

export function deleteTask(id) {
  return request({
    url: `/pm/task/${id}`,
    method: 'delete'
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

export function recalculateAllProjectProgress() {
  return request({
    url: '/pm/task/recalculate-project-progress',
    method: 'post'
  })
}

export function getWorkloadPage(params) {
  return request({
    url: '/pm/task/workload/page',
    method: 'get',
    params
  })
}

// ============================================
// 任务评论相关 API
// ============================================

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

// ============================================
// 任务进展更新相关 API
// ============================================

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

// ============================================
// 甘特图导出 API
// ============================================

export function exportGantt(projectId) {
  return request({
    url: '/duty/export/gantt',
    method: 'get',
    params: { projectId },
    responseType: 'blob'
  })
}

// ============================================
// 影子任务相关 API
// ============================================

export function getTaskListWithShadows(projectId) {
  return request({
    url: `/pm/shadow/project/${projectId}`,
    method: 'get'
  })
}

export function createShadowTask(data) {
  return request({
    url: '/pm/shadow',
    method: 'post',
    data
  })
}

export function updateShadowTask(data) {
  return request({
    url: `/pm/shadow/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteShadowTask(id) {
  return request({
    url: `/pm/shadow/${id}`,
    method: 'delete'
  })
}

export function getShadowTaskDetail(id) {
  return request({
    url: `/pm/shadow/${id}`,
    method: 'get'
  })
}

export function getShadowTaskBySource(sourceTaskId, targetProjectId) {
  return request({
    url: `/pm/shadow/source-task/${sourceTaskId}`,
    method: 'get'
  })
}

export function getShadowAnnotations(shadowId) {
  return request({
    url: `/pm/shadow/annotation/shadow/${shadowId}`,
    method: 'get'
  })
}

export function addShadowAnnotation(data) {
  return request({
    url: '/pm/shadow/annotation',
    method: 'post',
    data
  })
}

export function deleteShadowAnnotation(annotationId) {
  return request({
    url: `/pm/shadow/annotation/${annotationId}`,
    method: 'delete'
  })
}

export function getShadowAnnotationsBySource(sourceTaskId) {
    return request({
        url: `/pm/shadow/annotation/source-task/${sourceTaskId}`,
        method: 'get'
    });
}

/**
 * 导出任务进展报告
 */
export function exportTaskProgressReport(params) {
    return request({
        url: '/duty/export/task-progress-report',
        method: 'get',
        params,
        responseType: 'blob'
    });
}