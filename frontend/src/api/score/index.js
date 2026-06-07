import request from '../../utils/request'

// ========== 积分事件 ==========

/** 查询积分事件列表 */
export function getScoreEvents() {
  return request({ url: '/score/events', method: 'get' })
}

/** 新增积分事件 */
export function createScoreEvent(data) {
  return request({ url: '/score/events', method: 'post', data })
}

/** 修改积分事件 */
export function updateScoreEvent(id, data) {
  return request({ url: `/score/events/${id}`, method: 'put', data })
}

/** 删除积分事件 */
export function deleteScoreEvent(id) {
  return request({ url: `/score/events/${id}`, method: 'delete' })
}

// ========== 积分记录 ==========

/** 分页查询积分记录 */
export function getScoreRecords(params) {
  return request({ url: '/score/records', method: 'get', params })
}

/** 录入积分记录 */
export function createScoreRecord(data) {
  return request({ url: '/score/records', method: 'post', data })
}

/** 删除积分记录 */
export function deleteScoreRecord(id) {
  return request({ url: `/score/records/${id}`, method: 'delete' })
}

// ========== 汇总与评选 ==========

/** 查询月度汇总 */
export function getMonthlySummary(params) {
  return request({ url: '/score/summary/monthly', method: 'get', params })
}

/** 生成月度汇总 */
export function generateMonthlySummary(params) {
  return request({ url: '/score/summary/generate', method: 'post', params })
}

/** 评选排名 */
export function getEvaluationRanking(params) {
  return request({ url: '/score/evaluation', method: 'get', params })
}