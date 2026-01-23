import request from '../../utils/request'

/**
 * 班次配置API
 */
export function shiftConfigApi() {
  return {
    /**
     * 获取班次配置列表
     * @returns {Promise}
     */
    getShiftConfigList() {
      return request({
        url: '/duty/shift-config/list',
        method: 'get'
      })
    },

    /**
     * 根据ID获取班次配置
     * @param {number} id 班次配置ID
     * @returns {Promise}
     */
    getShiftConfigById(id) {
      return request({
        url: `/duty/shift-config/${id}`,
        method: 'get'
      })
    },

    /**
     * 新增班次配置
     * @param {Object} data 班次配置信息
     * @returns {Promise}
     */
    addShiftConfig(data) {
      return request({
        url: '/duty/shift-config',
        method: 'post',
        data
      })
    },

    /**
     * 更新班次配置
     * @param {Object} data 班次配置信息
     * @returns {Promise}
     */
    updateShiftConfig(data) {
      return request({
        url: '/duty/shift-config',
        method: 'put',
        data
      })
    },

    /**
     * 删除班次配置
     * @param {number} id 班次配置ID
     * @returns {Promise}
     */
    deleteShiftConfig(id) {
      return request({
        url: `/duty/shift-config/${id}`,
        method: 'delete'
      })
    },

    /**
     * 更新班次配置状态
     * @param {number} id 班次配置ID
     * @param {number} status 状态：0-禁用，1-启用
     * @returns {Promise}
     */
    updateShiftConfigStatus(id, status) {
      return request({
        url: `/duty/shift-config/status/${id}`,
        method: 'put',
        params: { status }
      })
    },

    /**
     * 获取启用的班次配置
     * @returns {Promise}
     */
    getEnabledShifts() {
      return request({
        url: '/duty/shift-config/enabled',
        method: 'get'
      })
    }
  }
}

