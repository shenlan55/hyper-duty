import request from '../../utils/request'

export function getLeaveRequestList() {
  return request({
    url: '/duty/leave-request/list',
    method: 'get'
  })
}

export function getMyLeaveRequests(employeeId) {
  return request({
    url: `/duty/leave-request/my/${employeeId}`,
    method: 'get'
  })
}

export function getPendingApprovals(approverId) {
  return request({
    url: `/duty/leave-request/pending/${approverId}`,
    method: 'get'
  })
}

export function getPendingApprovalsByScheduleId(scheduleId) {
  return request({
    url: `/duty/leave-request/pending/schedule/${scheduleId}`,
    method: 'get'
  })
}

export function getApprovedApprovals(approverId) {
  return request({
    url: `/duty/leave-request/approved/${approverId}`,
    method: 'get'
  })
}

export function getApprovedApprovalsByScheduleId(scheduleId) {
  return request({
    url: `/duty/leave-request/approved/schedule/${scheduleId}`,
    method: 'get'
  })
}

export function getLeaveRequestById(id) {
  return request({
    url: `/duty/leave-request/${id}`,
    method: 'get'
  })
}

export function submitLeaveRequest(data) {
  return request({
    url: '/duty/leave-request',
    method: 'post',
    data
  })
}

export function approveLeaveRequest(requestId, approverId, approvalStatus, opinion, scheduleAction, scheduleType, scheduleDateRange) {
  return request({
    url: `/duty/leave-request/approve/${requestId}`,
    method: 'put',
    params: { approverId, approvalStatus, opinion, scheduleAction, scheduleType, scheduleDateRange }
  })
}

export function deleteLeaveRequest(id) {
  return request({
    url: `/duty/leave-request/${id}`,
    method: 'delete'
  })
}

export function getApprovalRecords(requestId) {
  return request({
    url: `/duty/leave-request/approval-records/${requestId}`,
    method: 'get'
  })
}

export function checkEmployeeSchedule(employeeId, startDate, endDate) {
  return request({
    url: '/duty/leave-request/check-schedule',
    method: 'get',
    params: { employeeId, startDate, endDate }
  })
}

export function confirmScheduleCompletion(requestId, approverId) {
  return request({
    url: `/duty/leave-request/confirm-schedule/${requestId}`,
    method: 'put',
    params: { approverId }
  })
}

export function getEmployeeLeaveInfo(employeeIds, startDate, endDate) {
  return request({
    url: '/duty/leave-request/employee-leave-info',
    method: 'post',
    params: { employeeIds: employeeIds.join(','), startDate, endDate }
  })
}
