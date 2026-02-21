import request from './request'

/**
 * 通用API调用方法
 * @param {Object} config 请求配置
 * @returns {Promise} 请求结果
 */
export function apiCall(config) {
  return request(config)
}

/**
 * 通用分页查询方法
 * @param {string} url API地址
 * @param {Object} params 查询参数
 * @param {number} params.pageNum 页码
 * @param {number} params.pageSize 每页大小
 * @param {string} params.keyword 搜索关键字
 * @param {Object} options 其他选项
 * @returns {Promise} 分页查询结果
 */
export async function pageQuery(url, params = {}, options = {}) {
  // 确保分页参数存在
  const pageParams = {
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    ...params
  }
  
  try {
    const response = await request({
      url,
      method: 'get',
      params: pageParams,
      ...options
    })
    
    // 确保返回的数据结构正确
    if (!response) {
      return {
        records: [],
        total: 0,
        current: 1,
        size: 10,
        pages: 0
      }
    }
    
    // 确保records是数组
    if (!Array.isArray(response.records)) {
      response.records = []
    }
    
    return response
  } catch (error) {
    console.error('分页查询失败:', error)
    return {
      records: [],
      total: 0,
      current: 1,
      size: 10,
      pages: 0
    }
  }
}

/**
 * 通用POST请求方法
 * @param {string} url API地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求结果
 */
export function post(url, data = {}, options = {}) {
  return request({
    url,
    method: 'post',
    data,
    ...options
  })
}

/**
 * 通用PUT请求方法
 * @param {string} url API地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求结果
 */
export function put(url, data = {}, options = {}) {
  return request({
    url,
    method: 'put',
    data,
    ...options
  })
}

/**
 * 通用DELETE请求方法
 * @param {string} url API地址
 * @param {Object} options 其他选项
 * @returns {Promise} 请求结果
 */
export function del(url, options = {}) {
  return request({
    url,
    method: 'delete',
    ...options
  })
}

/**
 * 通用GET请求方法
 * @param {string} url API地址
 * @param {Object} params 查询参数
 * @param {Object} options 其他选项
 * @returns {Promise} 请求结果
 */
export function get(url, params = {}, options = {}) {
  return request({
    url,
    method: 'get',
    params,
    ...options
  })
}

// 导出通用API方法
export default {
  get,
  post,
  put,
  delete: del,
  pageQuery,
  apiCall
}
