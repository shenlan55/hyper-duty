import request from '../utils/request'

/**
 * 影子任务 API (v2)
 */

// ========================================
// 影子任务 CRUD
// ========================================

export function createShadowTask(data) {
  return request({
    url: '/pm/shadow',
    method: 'post',
    data
  })
}

export function updateShadowTask(shadowId, data) {
  return request({
    url: `/pm/shadow/${shadowId}`,
    method: 'put',
    data
  })
}

export function deleteShadowTask(shadowId) {
  return request({
    url: `/pm/shadow/${shadowId}`,
    method: 'delete'
  })
}

// ========================================
// 查询
// ========================================

/**
 * 查询：真实任务 + 影子任务（UNION ALL）
 * @param projectId 项目ID
 */
export function getTaskListWithShadows(projectId) {
  return request({
    url: `/pm/shadow/project/${projectId}`,
    method: 'get'
  })
}

export function getShadowTaskDetail(shadowId) {
  return request({
    url: `/pm/shadow/${shadowId}`,
    method: 'get'
  })
}

export function getShadowTasksBySourceTask(sourceTaskId) {
  return request({
    url: `/pm/shadow/source-task/${sourceTaskId}`,
    method: 'get'
  })
}

// ========================================
// 批注 CRUD
// ========================================

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

export function getShadowAnnotations(shadowId) {
  return request({
    url: `/pm/shadow/annotation/shadow/${shadowId}`,
    method: 'get'
  })
}
