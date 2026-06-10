/**
 * 员工相关 API
 *
 * 业务规则：
 * - 拉取全量员工（如 BPMN 选人弹窗）走分页接口，pageSize 取 1000 即可
 * - 不再提供前端 mock 兜底；后端不可用时把异常抛出，由调用方决定是否提示
 */
import request from '@/utils/request'

/**
 * 拉取全量可用人员（用于 BPMN 设计器选人弹窗等）
 * 实现思路：复用现有 /employee/list 分页接口，传入大 pageSize 取所有人员
 * 解析时跳过 password 字段，避免敏感信息泄露
 * @returns {Promise<Array<Object>>} 员工列表（不含 password）
 */
export const listAllEmployees = async () => {
  // 响应拦截器已 unwrap，业务数据直接是分页对象 { records, total, ... }
  const page = await request({
    url: '/employee/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000 }
  })
  const records = (page && Array.isArray(page.records)) ? page.records : []
  return records.map((e) => {
    if (e && 'password' in e) delete e.password
    return e
  })
}
