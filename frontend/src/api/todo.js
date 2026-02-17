import request from './utils/request'

// 获取待办事项列表
export function getTodoList() {
  return request({
    url: '/todo/list',
    method: 'get'
  })
}

// 获取待办事项数量
export function getTodoCount() {
  return request({
    url: '/todo/count',
    method: 'get'
  })
}
