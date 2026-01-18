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

export function approveLeaveRequest(requestId, approverId, approvalStatus, opinion) {
  return request({
    url: `/duty/leave-request/approve/${requestId}`,
    method: 'put',
    params: { approverId, approvalStatus, opinion }
  })
}

export function deleteLeaveRequest(id) {
  return request({
    url: `/duty/leave-request/${id}`,
    method: 'delete'
  })
}
