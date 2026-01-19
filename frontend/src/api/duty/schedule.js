import request from '../../utils/request'

export function getScheduleList() {
  return request({
    url: '/duty/schedule/list',
    method: 'get'
  })
}

export function getScheduleById(id) {
  return request({
    url: `/duty/schedule/${id}`,
    method: 'get'
  })
}

export function getScheduleEmployees(id) {
  return request({
    url: `/duty/schedule/${id}/employees`,
    method: 'get'
  })
}

export function getScheduleLeaders(id) {
  return request({
    url: `/duty/schedule/${id}/leaders`,
    method: 'get'
  })
}

export function addSchedule(data) {
  return request({
    url: '/duty/schedule',
    method: 'post',
    data
  })
}

export function updateSchedule(data) {
  return request({
    url: '/duty/schedule',
    method: 'put',
    data
  })
}

export function updateScheduleEmployees(id, employeeIds) {
  return request({
    url: `/duty/schedule/${id}/employees`,
    method: 'put',
    data: employeeIds
  })
}

export function updateScheduleEmployeesAndLeaders(id, employeeIds, leaderIds) {
  return request({
    url: `/duty/schedule/${id}/employees-and-leaders`,
    method: 'put',
    data: { employeeIds, leaderIds }
  })
}

export function updateScheduleLeaders(id, leaderIds) {
  return request({
    url: `/duty/schedule/${id}/leaders`,
    method: 'put',
    data: leaderIds
  })
}

export function deleteSchedule(id) {
  return request({
    url: `/duty/schedule/${id}`,
    method: 'delete'
  })
}