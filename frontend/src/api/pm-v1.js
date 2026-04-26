import request from '../utils/request'

/**
 * 项目管理 API v1
 * 使用 DTO 和统一的响应格式
 */

// ============================================
// 任务相关 API
// ============================================

export function getTaskPageV1(params) {
  return request({
    url: '/api/v1/pm/task/page',
    method: 'get',
    params
  })
}

export function getTaskDetailV1(id) {
  return request({
    url: `/api/v1/pm/task/${id}`,
    method: 'get'
  })
}

export function createTaskV1(data) {
  return request({
    url: '/api/v1/pm/task',
    method: 'post',
    data
  })
}

export function updateTaskV1(data) {
  return request({
    url: '/api/v1/pm/task',
    method: 'put',
    data
  })
}

export function deleteTaskV1(id) {
  return request({
    url: `/api/v1/pm/task/${id}`,
    method: 'delete'
  })
}

// ============================================
// 项目相关 API（可选扩展）
// ============================================

// 后续可以添加
