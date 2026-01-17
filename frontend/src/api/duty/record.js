import request from '../../utils/request'

export function getRecordList() {
  return request({
    url: '/duty/record/list',
    method: 'get'
  })
}

export function getRecordsByEmployeeId(employeeId) {
  return request({
    url: `/duty/record/list/employee/${employeeId}`,
    method: 'get'
  })
}

export function getAvailableSubstitutes(recordId) {
  return request({
    url: `/duty/record/substitutes/${recordId}`,
    method: 'get'
  })
}

export function addRecord(data) {
  return request({
    url: '/duty/record',
    method: 'post',
    data
  })
}

export function checkIn(assignmentId, data) {
  return request({
    url: `/duty/record/check-in/${assignmentId}`,
    method: 'post',
    data
  })
}

export function checkOut(id, data) {
  return request({
    url: `/duty/record/check-out/${id}`,
    method: 'post',
    data
  })
}

export function updateRecord(data) {
  return request({
    url: '/duty/record',
    method: 'put',
    data
  })
}

export function deleteRecord(id) {
  return request({
    url: `/duty/record/${id}`,
    method: 'delete'
  })
}