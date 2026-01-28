import request from '../../utils/request'

export function getMySwapRequests(employeeId) {
  return request({
    url: `/duty/swap-request/my/${employeeId}`,
    method: 'get'
  })
}

export function submitSwapRequest(data) {
  return request({
    url: '/duty/swap-request',
    method: 'post',
    data
  })
}

export function confirmSwapRequest(requestId, employeeId) {
  return request({
    url: `/duty/swap-request/confirm/${requestId}`,
    method: 'put',
    params: { employeeId }
  })
}

export function deleteSwapRequest(id) {
  return request({
    url: `/duty/swap-request/${id}`,
    method: 'delete'
  })
}

export function getMySwapRequestsPage(employeeId, page, size, approvalStatus, startDate, endDate) {
  return request({
    url: `/duty/swap-request/my/page/${employeeId}`,
    method: 'get',
    params: {
      page,
      size,
      approvalStatus,
      startDate,
      endDate
    }
  })
}
