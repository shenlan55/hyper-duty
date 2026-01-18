import request from '../../utils/request'

// 获取所有值班安排列表
export function getAssignmentList() {
  return request({
    url: '/duty/assignment/list',
    method: 'get'
  })
}

// 根据值班表ID获取值班安排
export function getAssignmentsByScheduleId(scheduleId) {
  return request({
    url: `/duty/assignment/list/${scheduleId}`,
    method: 'get'
  })
}

// 添加值班安排
export function addAssignment(data) {
  return request({
    url: '/duty/assignment',
    method: 'post',
    data
  })
}

// 批量添加值班安排
export function addBatchAssignments(data) {
  return request({
    url: '/duty/assignment/batch',
    method: 'post',
    data
  })
}

// 修改值班安排
export function updateAssignment(data) {
  return request({
    url: '/duty/assignment',
    method: 'put',
    data
  })
}

// 删除值班安排
export function deleteAssignment(id) {
  return request({
    url: `/duty/assignment/${id}`,
    method: 'delete'
  })
}