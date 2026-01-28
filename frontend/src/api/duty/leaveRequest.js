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

export function approveLeaveRequest(requestId, approverId, approvalStatus, opinion, scheduleAction, substituteData) {
  return request({
    url: `/duty/leave-request/approve/${requestId}`,
    method: 'put',
    params: { approverId, approvalStatus, opinion, scheduleAction },
    data: substituteData
  })
}

export function getAvailableSubstitutes(scheduleId, startDate, endDate, leaveEmployeeId) {
  return request({
    url: '/duty/leave-request/available-substitutes',
    method: 'get',
    params: { scheduleId, startDate, endDate, leaveEmployeeId }
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

/**
 * 分页获取我的请假申请
 */
export function getMyLeaveRequestsPage(employeeId, page, size, leaveType, approvalStatus, startDate, endDate) {
  return request({
    url: `/duty/leave-request/my/page/${employeeId}`,
    method: 'get',
    params: { page, size, leaveType, approvalStatus, startDate, endDate }
  })
}

/**
 * 分页获取待审批请假申请
 */
export function getPendingApprovalsPage(approverId, page, size, scheduleId, leaveType, startDate, endDate) {
  return request({
    url: `/duty/leave-request/pending/page/${approverId}`,
    method: 'get',
    params: { page, size, scheduleId, leaveType, startDate, endDate }
  })
}

/**
 * 分页获取已审批请假申请
 */
export function getApprovedApprovalsPage(approverId, page, size, scheduleId, leaveType, approvalStatus, startDate, endDate) {
  return request({
    url: `/duty/leave-request/approved/page/${approverId}`,
    method: 'get',
    params: { page, size, scheduleId, leaveType, approvalStatus, startDate, endDate }
  })
}

/**
 * 获取请假顶岗信息
 */
export function getLeaveSubstitutes(leaveRequestId) {
  return request({
    url: `/duty/leave-request/substitutes/${leaveRequestId}`,
    method: 'get'
  })
}

/**
 * 根据员工ID和日期范围获取顶岗信息
 */
export function getSubstitutesByEmployees(employeeIds, startDate, endDate) {
  return request({
    url: '/duty/leave-request/substitutes-by-employees',
    method: 'get',
    params: { employeeIds: employeeIds.join(','), startDate, endDate }
  })
}
