import request from '../utils/request'

// 获取所有菜单列表
export function getMenuList() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}

// 获取菜单详情
export function getMenuById(id) {
  return request({
    url: `/menu/${id}`,
    method: 'get'
  })
}

// 获取菜单树形结构
export function getMenuTree() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}

// 添加菜单
export function addMenu(data) {
  return request({
    url: '/menu',
    method: 'post',
    data
  })
}

// 更新菜单
export function updateMenu(data) {
  return request({
    url: '/menu',
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenu(id) {
  return request({
    url: `/menu/${id}`,
    method: 'delete'
  })
}
