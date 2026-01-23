import request from '@/utils/request'

/**
 * 节假日相关API
 */
export function holidayApi() {
  return {
    /**
     * 判断指定日期是否为节假日
     * @param {string} date - 日期，格式：YYYY-MM-DD
     * @returns {Promise}
     */
    isHoliday(date) {
      return request({
        url: `/duty/holiday/is-holiday`,
        method: 'get',
        params: { date }
      })
    },

    /**
     * 判断指定日期是否为工作日
     * @param {string} date - 日期，格式：YYYY-MM-DD
     * @returns {Promise}
     */
    isWorkday(date) {
      return request({
        url: `/duty/holiday/is-workday`,
        method: 'get',
        params: { date }
      })
    },

    /**
     * 获取指定日期范围内的节假日列表
     * @param {string} startDate - 开始日期，格式：YYYY-MM-DD
     * @param {string} endDate - 结束日期，格式：YYYY-MM-DD
     * @returns {Promise}
     */
    getHolidaysInRange(startDate, endDate) {
      return request({
        url: `/duty/holiday/range`,
        method: 'get',
        params: { startDate, endDate }
      })
    },

    /**
     * 获取指定日期范围内的工作日列表
     * @param {string} startDate - 开始日期，格式：YYYY-MM-DD
     * @param {string} endDate - 结束日期，格式：YYYY-MM-DD
     * @returns {Promise}
     */
    getWorkdaysInRange(startDate, endDate) {
      return request({
        url: `/duty/holiday/workdays`,
        method: 'get',
        params: { startDate, endDate }
      })
    },

    /**
     * 获取指定日期范围内的非工作日列表
     * @param {string} startDate - 开始日期，格式：YYYY-MM-DD
     * @param {string} endDate - 结束日期，格式：YYYY-MM-DD
     * @returns {Promise}
     */
    getNonWorkdaysInRange(startDate, endDate) {
      return request({
        url: `/duty/holiday/non-workdays`,
        method: 'get',
        params: { startDate, endDate }
      })
    },

    /**
     * 批量导入节假日数据
     * @param {Array} holidays - 节假日列表
     * @returns {Promise}
     */
    importHolidays(holidays) {
      return request({
        url: `/duty/holiday/import`,
        method: 'post',
        data: holidays
      })
    }
  }
}
