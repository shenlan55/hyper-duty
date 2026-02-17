import request from '../../utils/request'

// 获取待审批的加班申请
export function getPendingOvertimeApprovals(employeeId) {
  return request({
    url: `/duty/record/pending/${employeeId}`,
    method: 'get'
  })
}

// 获取加班申请分页列表
export function getOvertimeRequestsPage(
  employeeId,
  page,
  size,
  scheduleId,
  startDate,
  endDate
) {
  return request({
    url: `/duty/record/page/${employeeId}`,
    method: 'get',
    params: {
      page,
      size,
      scheduleId,
      startDate,
      endDate
    }
  })
}

// 审批加班申请
export function approveOvertimeRequest(id, approvalStatus, approvalRemark) {
  return request({
    url: `/duty/record/approve/${id}`,
    method: 'post',
    data: {
      approvalStatus,
      approvalRemark
    }
  })
}

// 获取加班申请详情
export function getOvertimeRequestDetail(id) {
  return request({
    url: `/duty/record/${id}`,
    method: 'get'
  })
}
