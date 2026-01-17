import request from '../../utils/request'

// 获取值班表列表
export function getScheduleList() {
  return request({
    url: '/duty/schedule/list',
    method: 'get'
  })
}

// 根据ID获取值班表详情
export function getScheduleById(id) {
  return request({
    url: `/duty/schedule/${id}`,
    method: 'get'
  })
}

// 添加值班表
export function addSchedule(data) {
  return request({
    url: '/duty/schedule',
    method: 'post',
    data
  })
}

// 修改值班表
export function updateSchedule(data) {
  return request({
    url: '/duty/schedule',
    method: 'put',
    data
  })
}

// 删除值班表
export function deleteSchedule(id) {
  return request({
    url: `/duty/schedule/${id}`,
    method: 'delete'
  })
}