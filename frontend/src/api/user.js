import { pageQuery, post, put, del } from '../utils/api'

// 获取用户列表（分页）
export function getUserList(pageNum = 1, pageSize = 10, keyword = '', deptId = null) {
  // 确保空字符串的deptId转换为null
  const params = {
    pageNum,
    pageSize,
    keyword
  }
  
  // 只有当deptId不为空字符串且不为null时才添加到参数中
  if (deptId && deptId !== '') {
    params.deptId = deptId
  }
  
  return pageQuery('/user/list', params)
}

// 添加用户
export function addUser(data) {
  return post('/user', data)
}

// 修改用户
export function updateUser(data) {
  return put('/user', data)
}

// 删除用户
export function deleteUser(id) {
  return del(`/user/${id}`)
}
