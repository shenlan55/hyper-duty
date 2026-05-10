import request from '@/utils/request'

// 委托管理API
export function pageDelegate(pageNum, pageSize) {
  return request({
    url: '/api/workflow/delegate/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function createDelegate(data) {
  return request({
    url: '/api/workflow/delegate',
    method: 'post',
    data
  })
}

export function updateDelegate(id, data) {
  return request({
    url: `/api/workflow/delegate/${id}`,
    method: 'put',
    data
  })
}

export function deleteDelegate(id) {
  return request({
    url: `/api/workflow/delegate/${id}`,
    method: 'delete'
  })
}

export function enableDelegate(id) {
  return request({
    url: `/api/workflow/delegate/enable/${id}`,
    method: 'post'
  })
}

export function disableDelegate(id) {
  return request({
    url: `/api/workflow/delegate/disable/${id}`,
    method: 'post'
  })
}
