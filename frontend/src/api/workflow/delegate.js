import request from '@/utils/request'

// 委托管理API
export function pageDelegate(pageNum, pageSize) {
  return request({
    url: '/workflow/delegate/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function createDelegate(data) {
  return request({
    url: '/workflow/delegate',
    method: 'post',
    data
  })
}

export function updateDelegate(id, data) {
  return request({
    url: `/workflow/delegate/${id}`,
    method: 'put',
    data
  })
}

export function deleteDelegate(id) {
  return request({
    url: `/workflow/delegate/${id}`,
    method: 'delete'
  })
}

export function enableDelegate(id) {
  return request({
    url: `/workflow/delegate/enable/${id}`,
    method: 'post'
  })
}

export function disableDelegate(id) {
  return request({
    url: `/workflow/delegate/disable/${id}`,
    method: 'post'
  })
}
