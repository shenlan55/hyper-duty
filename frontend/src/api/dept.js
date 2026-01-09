import request from '../utils/request'

// 获取部门列表
export function getDeptList() {
  return request({
    url: '/dept/list',
    method: 'get'
  })
}

// 获取部门树
export function getDeptTree() {
  return request({
    url: '/dept/tree',
    method: 'get'
  })
}

// 添加部门
export function addDept(data) {
  return request({
    url: '/dept',
    method: 'post',
    data
  })
}

// 修改部门
export function updateDept(data) {
  return request({
    url: '/dept',
    method: 'put',
    data
  })
}

// 删除部门
export function deleteDept(id) {
  return request({
    url: `/dept/${id}`,
    method: 'delete'
  })
}