import request from '../../utils/request'

export function getOverallStatistics() {
  return request({
    url: '/duty/statistics/overall',
    method: 'get'
  })
}

export function getDeptStatistics(deptId) {
  return request({
    url: '/duty/statistics/dept',
    method: 'get',
    params: { deptId }
  })
}

export function getShiftDistribution() {
  return request({
    url: '/duty/statistics/shift-distribution',
    method: 'get'
  })
}

export function getMonthlyTrend() {
  return request({
    url: '/duty/statistics/monthly-trend',
    method: 'get'
  })
}

export function getAllStatistics() {
  return request({
    url: '/duty/statistics',
    method: 'get'
  })
}

export function exportStatisticsExcel(params) {
  return request({
    url: '/duty/export/statistics',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
