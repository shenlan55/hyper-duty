import request from '../../utils/request'

/**
 * 获取首页统计数据
 * @returns {Promise}
 */
export function getDashboardStatistics() {
  return request({
    url: '/system/statistics/dashboard',
    method: 'get'
  })
}
