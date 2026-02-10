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

export function getScheduleEmployeesWithLeaderInfo(id) {
  return request({
    url: `/duty/schedule/${id}/employees-with-leader-info`,
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

export function getScheduleShifts(id) {
  return request({
    url: `/duty/schedule/${id}/shifts`,
    method: 'get'
  })
}

export function updateScheduleShifts(id, shiftConfigIds) {
  return request({
    url: `/duty/schedule/${id}/shifts`,
    method: 'put',
    data: shiftConfigIds
  })
}

export function deleteSchedule(id) {
  return request({
    url: `/duty/schedule/${id}`,
    method: 'delete'
  })
}

export function generateAutoSchedule(scheduleId, startDate, endDate, ruleId) {
  return request({
    url: '/duty/auto-schedule/generate',
    method: 'post',
    params: { scheduleId, startDate, endDate, ruleId }
  })
}

export function generateAutoScheduleByWorkHours(scheduleId, startDate, endDate, ruleId, employeeId) {
  return request({
    url: '/duty/auto-schedule/generate-by-work-hours',
    method: 'post',
    params: { scheduleId, startDate, endDate, ruleId, employeeId }
  })
}

export function getEmployeeMonthlyWorkHours(scheduleId, startDate, endDate) {
  return request({
    url: '/duty/auto-schedule/employee-monthly-work-hours',
    method: 'get',
    params: { scheduleId, startDate, endDate }
  })
}

export function getScheduleModeList() {
  return request({
    url: '/duty/schedule-mode/list',
    method: 'get'
  })
}

export function generateScheduleByMode(scheduleId, startDate, endDate, modeId, configParams) {
  return request({
    url: '/duty/auto-schedule/generate-by-mode',
    method: 'post',
    data: { scheduleId, startDate, endDate, modeId, configParams }
  })
}