import request from '../../utils/request'

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

export function generateScheduleByMode(scheduleId, startDate, endDate, modeId, configParams) {
  return request({
    url: '/duty/auto-schedule/generate-by-mode',
    method: 'post',
    data: { scheduleId, startDate, endDate, modeId, configParams }
  })
}

export function checkConflict(employeeId, dutyDate, dutyShift) {
  return request({
    url: '/duty/auto-schedule/check-conflict',
    method: 'get',
    params: { employeeId, dutyDate, dutyShift }
  })
}

export function getAvailableEmployees(dutyDate, dutyShift, excludeEmployeeId) {
  return request({
    url: '/duty/auto-schedule/available-employees',
    method: 'get',
    params: { dutyDate, dutyShift, excludeEmployeeId }
  })
}

export function getEmployeeMonthlyWorkHours(scheduleId, startDate, endDate) {
  return request({
    url: '/duty/auto-schedule/employee-monthly-work-hours',
    method: 'get',
    params: { scheduleId, startDate, endDate }
  })
}
