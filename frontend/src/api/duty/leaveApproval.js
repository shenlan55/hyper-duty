import request from '../../utils/request'

export function getPendingApprovals(approverId) {
  return request({
    url: `/duty/leave-request/pending/${approverId}`,
    method: 'get'
  })
}

export function approveLeaveRequest(requestId, approverId, approvalStatus, opinion) {
  return request({
    url: `/duty/leave-request/approve/${requestId}`,
    method: 'put',
    params: { approverId, approvalStatus, opinion }
  })
}
