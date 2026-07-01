import request from '@/utils/request'

/**
 * 发起催办
 * @param {object} data { taskId, toUserId, content }
 */
export function urge(data) {
  return request({
    url: '/workflow/urge',
    method: 'post',
    data
  })
}

/**
 * 我发起的催办（分页）
 */
export function pageSentUrge(params) {
  return request({
    url: '/workflow/urge/page/sent',
    method: 'get',
    params
  })
}

/**
 * 我接收的催办（分页）
 */
export function pageReceivedUrge(params) {
  return request({
    url: '/workflow/urge/page/received',
    method: 'get',
    params
  })
}
