/**
 * 角色相关 API
 *
 * 业务规则：
 * - 拉取全量角色（如 BPMN 选组弹窗）走分页接口，pageSize 取 1000 即可
 * - 不再提供前端 mock 兜底；后端不可用时把异常抛出，由调用方决定是否提示
 */
import request from '@/utils/request'

/**
 * 拉取全量可用角色（用于 BPMN 设计器选组弹窗等）
 * 实现思路：复用现有 /role/list 分页接口，传入大 pageSize 取所有角色
 * @returns {Promise<Array<Object>>} 角色列表
 */
export const listAllRoles = async () => {
  // 响应拦截器已 unwrap，业务数据直接是分页对象 { records, total, ... }
  const page = await request({
    url: '/role/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000 }
  })
  const records = (page && Array.isArray(page.records)) ? page.records : []
  return records
}
