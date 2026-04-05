import request from '../utils/request'

export function getCustomTablePage(params) {
  return request({
    url: '/pm/custom-table/page',
    method: 'get',
    params
  })
}

export function getCustomTableDetail(id) {
  return request({
    url: `/pm/custom-table/${id}`,
    method: 'get'
  })
}

export function getCustomTableColumns(id) {
  return request({
    url: `/pm/custom-table/${id}/columns`,
    method: 'get'
  })
}

export function getCustomTableRows(id) {
  return request({
    url: `/pm/custom-table/${id}/rows`,
    method: 'get'
  })
}

export function createCustomTable(data) {
  return request({
    url: '/pm/custom-table',
    method: 'post',
    data
  })
}

export function updateCustomTable(data) {
  return request({
    url: '/pm/custom-table',
    method: 'put',
    data
  })
}

export function deleteCustomTable(id) {
  return request({
    url: `/pm/custom-table/${id}`,
    method: 'delete'
  })
}

export function createTableRow(tableId, rowData) {
  return request({
    url: `/pm/custom-table/${tableId}/row`,
    method: 'post',
    data: { rowData }
  })
}

export function updateTableRow(id, rowData) {
  return request({
    url: `/pm/custom-table/row/${id}`,
    method: 'put',
    data: { rowData }
  })
}

export function deleteTableRow(id) {
  return request({
    url: `/pm/custom-table/row/${id}`,
    method: 'delete'
  })
}

export function getTaskBindings(taskId) {
  return request({
    url: `/pm/custom-table/task/${taskId}/bindings`,
    method: 'get'
  })
}

export function bindCustomRow(taskId, tableId, rowId) {
  return request({
    url: `/pm/custom-table/task/${taskId}/bind`,
    method: 'post',
    data: { tableId, rowId }
  })
}

export function unbindCustomRow(taskId, bindingId) {
  return request({
    url: `/pm/custom-table/task/${taskId}/bind/${bindingId}`,
    method: 'delete'
  })
}
