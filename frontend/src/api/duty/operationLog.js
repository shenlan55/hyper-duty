import request from '../../utils/request'

export function getOperationLogList() {
  return request({
    url: '/duty/operation-log/list',
    method: 'get'
  })
}

export function getOperationLogById(id) {
  return request({
    url: `/duty/operation-log/${id}`,
    method: 'get'
  })
}

export function deleteOperationLog(id) {
  return request({
    url: `/duty/operation-log/${id}`,
    method: 'delete'
  })
}
