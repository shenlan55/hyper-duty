import request from '@/utils/request'

export const generateDailyReport = (data) => {
  return request({
    url: '/ai/report/daily',
    method: 'post',
    params: data,
    timeout: 120000 // 2分钟超时
  })
}

export const generateWeeklyReport = (data) => {
  return request({
    url: '/ai/report/weekly',
    method: 'post',
    params: data,
    timeout: 120000 // 2分钟超时
  })
}

export const getReportPage = (params) => {
  return request({
    url: '/ai/report/page',
    method: 'get',
    params
  })
}

export const getReportById = (id) => {
  return request({
    url: `/ai/report/${id}`,
    method: 'get'
  })
}

export const deleteReport = (id) => {
  return request({
    url: `/ai/report/${id}`,
    method: 'delete'
  })
}

export const getConfigList = () => {
  return request({
    url: '/ai/report/config/list',
    method: 'get'
  })
}

export const getConfigById = (id) => {
  return request({
    url: `/ai/report/config/${id}`,
    method: 'get'
  })
}

export const saveConfig = (data) => {
  return request({
    url: '/ai/report/config',
    method: 'post',
    data
  })
}

export const deleteConfig = (id) => {
  return request({
    url: `/ai/report/config/${id}`,
    method: 'delete'
  })
}
