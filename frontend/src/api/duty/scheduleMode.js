import request from '@/utils/request'

/**
 * 排班模式API
 */
export function scheduleModeApi() {
  return {
    /**
     * 获取所有启用的排班模式
     * @returns {Promise}
     */
    getEnabledModes() {
      return request({
        url: '/duty/schedule-mode/list',
        method: 'get'
      })
    },

    /**
     * 获取所有排班模式（包括禁用的）
     * @returns {Promise}
     */
    getAllModes() {
      return request({
        url: '/duty/schedule-mode/all',
        method: 'get'
      })
    },

    /**
     * 根据ID获取排班模式
     * @param {number} id 排班模式ID
     * @returns {Promise}
     */
    getById(id) {
      return request({
        url: '/duty/schedule-mode/getById',
        method: 'get',
        params: { id }
      })
    },

    /**
     * 新增排班模式
     * @param {Object} mode 排班模式信息
     * @returns {Promise}
     */
    add(mode) {
      return request({
        url: '/duty/schedule-mode',
        method: 'post',
        data: mode
      })
    },

    /**
     * 编辑排班模式
     * @param {Object} mode 排班模式信息
     * @returns {Promise}
     */
    update(mode) {
      return request({
        url: '/duty/schedule-mode',
        method: 'put',
        data: mode
      })
    },

    /**
     * 删除排班模式
     * @param {number} id 排班模式ID
     * @returns {Promise}
     */
    delete(id) {
      return request({
        url: `/duty/schedule-mode/${id}`,
        method: 'delete'
      })
    }
  }
}
