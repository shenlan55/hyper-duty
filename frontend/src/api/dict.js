/**
 * 字典 API
 *
 * 设计原则：
 * - 业务枚举（任务状态/班次/审批/是否/启用禁用/性别 等）的 label、value、tagClass
 *   统一通过字典 API 返回，**禁止前端硬编码中文标签**
 * - 前端通过 dict_code（如 'task_status'）批量取值，并在 Pinia 缓存
 *
 * 后端接口：
 * - GET /dict/data/byCodes?codes=task_status,task_priority
 *   返回 { code: { records: [...] } }（响应拦截器已 unwrap）
 */
import request from '../utils/request'

/**
 * 按 dict_code 批量查询字典数据
 * @param {string|string[]} codes 字典编码，单个或数组
 * @returns {Promise<Object<string, Array<{dictLabel:string,dictValue:string,listClass:string,...}>>>}
 *          Map<dictCode, List<SysDictData>>
 */
export function getDictDataByCodes(codes) {
  const codeStr = Array.isArray(codes) ? codes.join(',') : String(codes || '')
  return request({
    url: '/dict/data/byCodes',
    method: 'get',
    params: { codes: codeStr }
  })
}
