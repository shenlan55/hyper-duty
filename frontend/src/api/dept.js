import request from '../utils/request'

// 获取部门列表（全量，包括禁用）—— 系统管理用
export function getDeptList() {
  return request({
    url: '/dept/list',
    method: 'get'
  })
}

// 获取部门树（全量，包括禁用）—— 系统管理用
export function getDeptTree() {
  return request({
    url: '/dept/tree',
    method: 'get'
  })
}

// 获取启用部门列表（status=1）—— 业务模块选人/选部门用
// 2026-06-27 新增：双接口方案
export function getActiveDeptList() {
  return request({
    url: '/dept/active-list',
    method: 'get'
  })
}

// 获取启用部门树（status=1）—— 业务模块选人用
// 2026-06-27 新增：双接口方案
export function getActiveDeptTree() {
  return request({
    url: '/dept/active-tree',
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