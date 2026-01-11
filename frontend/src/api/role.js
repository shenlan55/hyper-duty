import request from '../utils/request'

// 角色管理相关API

// 获取角色列表
export function listRole(params) {
  return request({
    url: '/role/list',
    method: 'get',
    params
  })
}

// 获取角色详情
export function getRole(id) {
  return request({
    url: `/role/detail/${id}`,
    method: 'get'
  })
}

// 添加角色
export function addRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(data) {
  return request({
    url: '/role',
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

// 获取角色菜单
export function getRoleMenu(roleId) {
  return request({
    url: `/role/menu/${roleId}`,
    method: 'get'
  })
}

// 保存角色菜单
export function saveRoleMenu(data) {
  return request({
    url: '/role/menu',
    method: 'post',
    data
  })
}

// 获取角色用户
export function getRoleUser(roleId) {
  return request({
    url: `/role/user/${roleId}`,
    method: 'get'
  })
}

// 保存角色用户
export function saveRoleUser(roleId, userIds) {
  return request({
    url: '/role/user',
    method: 'post',
    params: { roleId },
    data: userIds
  })
}