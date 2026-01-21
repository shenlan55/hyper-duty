import request from '../../utils/request'

export function getOperationLogList(params) {
  return request({
    url: '/system/operation-log/list',
    method: 'get',
    params
  })
}

export function getOperationLogById(id) {
  return request({
    url: `/system/operation-log/${id}`,
    method: 'get'
  })
}

export function deleteOperationLog(id) {
  return request({
    url: `/system/operation-log/${id}`,
    method: 'delete'
  })
}
