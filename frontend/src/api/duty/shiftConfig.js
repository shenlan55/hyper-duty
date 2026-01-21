import request from '../../utils/request'

export function getShiftConfigList() {
  return request({
    url: '/duty/shift-config/list',
    method: 'get'
  })
}

export function getShiftConfigById(id) {
  return request({
    url: `/duty/shift-config/${id}`,
    method: 'get'
  })
}

export function addShiftConfig(data) {
  return request({
    url: '/duty/shift-config',
    method: 'post',
    data
  })
}

export function updateShiftConfig(data) {
  return request({
    url: '/duty/shift-config',
    method: 'put',
    data
  })
}

export function deleteShiftConfig(id) {
  return request({
    url: `/duty/shift-config/${id}`,
    method: 'delete'
  })
}

export function updateShiftConfigStatus(id, status) {
  return request({
    url: `/duty/shift-config/status/${id}`,
    method: 'put',
    params: { status }
  })
}

