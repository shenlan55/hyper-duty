import request from '@/utils/request'

/**
 * 流程抄送 API
 */
export function pageMineCc(pageNum, pageSize, readStatus) {
  return request({
    url: '/workflow/cc/mine/page',
    method: 'get',
    params: { pageNum, pageSize, readStatus }
  })
}

export function createCc(data) {
  return request({
    url: '/workflow/cc',
    method: 'post',
    data
  })
}

export function markRead(id) {
  return request({
    url: `/workflow/cc/read/${id}`,
    method: 'post'
  })
}

export function markAllRead() {
  return request({
    url: '/workflow/cc/read-all',
    method: 'post'
  })
}

export function batchCcForNode(data) {
  return request({
    url: '/workflow/cc/batch-for-node',
    method: 'post',
    data
  })
}
