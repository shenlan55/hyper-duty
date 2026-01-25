import request from '@/utils/request'

/**
 * 定时任务相关API
 */
export function scheduleJobApi() {
  return {
    /**
     * 获取定时任务列表
     * @returns {Promise}
     */
    getJobList() {
      return request({
        url: '/sys/schedule/job/list',
        method: 'get'
      })
    },

    /**
     * 获取定时任务详情
     * @param {number} jobId - 任务ID
     * @returns {Promise}
     */
    getJobDetail(jobId) {
      return request({
        url: `/sys/schedule/job/detail/${jobId}`,
        method: 'get'
      })
    },

    /**
     * 新增定时任务
     * @param {Object} job - 定时任务
     * @returns {Promise}
     */
    addJob(job) {
      return request({
        url: '/sys/schedule/job/add',
        method: 'post',
        data: job
      })
    },

    /**
     * 更新定时任务
     * @param {Object} job - 定时任务
     * @returns {Promise}
     */
    updateJob(job) {
      return request({
        url: '/sys/schedule/job/update',
        method: 'put',
        data: job
      })
    },

    /**
     * 删除定时任务
     * @param {number} jobId - 任务ID
     * @returns {Promise}
     */
    deleteJob(jobId) {
      return request({
        url: `/sys/schedule/job/delete/${jobId}`,
        method: 'delete'
      })
    },

    /**
     * 暂停定时任务
     * @param {number} jobId - 任务ID
     * @returns {Promise}
     */
    pauseJob(jobId) {
      return request({
        url: `/sys/schedule/job/pause/${jobId}`,
        method: 'post'
      })
    },

    /**
     * 恢复定时任务
     * @param {number} jobId - 任务ID
     * @returns {Promise}
     */
    resumeJob(jobId) {
      return request({
        url: `/sys/schedule/job/resume/${jobId}`,
        method: 'post'
      })
    },

    /**
     * 立即执行定时任务
     * @param {number} jobId - 任务ID
     * @returns {Promise}
     */
    runJob(jobId) {
      return request({
        url: `/sys/schedule/job/run/${jobId}`,
        method: 'post'
      })
    },

    /**
     * 获取定时任务日志列表
     * @param {number} jobId - 任务ID（可选）
     * @returns {Promise}
     */
    getLogList(jobId) {
      return request({
        url: '/sys/schedule/log/list',
        method: 'get',
        params: { jobId }
      })
    },

    /**
     * 清理定时任务日志
     * @param {number} days - 天数
     * @returns {Promise}
     */
    cleanLogs(days) {
      return request({
        url: '/sys/schedule/log/clean',
        method: 'post',
        params: { days }
      })
    }
  }
}
