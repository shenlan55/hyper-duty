import { pageQuery, get, post, put, del } from '../utils/api'

// 获取人员列表（分页）
export function getEmployeeList(pageNum = 1, pageSize = 10, keyword = '', deptId = null) {
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
  
  return pageQuery('/employee/list', params)
}

// 根据部门ID获取人员列表
export function getEmployeesByDeptId(deptId) {
  return get(`/employee/list/${deptId}`)
}

// 添加人员
export function addEmployee(data) {
  return post('/employee', data)
}

// 修改人员
export function updateEmployee(data) {
  return put('/employee', data)
}

// 删除人员
export function deleteEmployee(id) {
  return del(`/employee/${id}`)
}