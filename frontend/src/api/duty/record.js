import request from '../../utils/request'

// 获取所有值班记录列表
export function getRecordList() {
  return request({
    url: '/duty/record/list',
    method: 'get'
  })
}

// 根据员工ID获取值班记录
export function getRecordsByEmployeeId(employeeId) {
  return request({
    url: `/duty/record/list/employee/${employeeId}`,
    method: 'get'
  })
}

// 添加值班记录
export function addRecord(data) {
  return request({
    url: '/duty/record',
    method: 'post',
    data
  })
}

// 签到
export function checkIn(assignmentId, data) {
  return request({
    url: `/duty/record/check-in/${assignmentId}`,
    method: 'post',
    data
  })
}

// 签退
export function checkOut(id, data) {
  return request({
    url: `/duty/record/check-out/${id}`,
    method: 'post',
    data
  })
}

// 修改值班记录
export function updateRecord(data) {
  return request({
    url: '/duty/record',
    method: 'put',
    data
  })
}

// 删除值班记录
export function deleteRecord(id) {
  return request({
    url: `/duty/record/${id}`,
    method: 'delete'
  })
}