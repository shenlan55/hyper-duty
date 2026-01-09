import request from '../utils/request'

// 获取人员列表
export function getEmployeeList() {
  return request({
    url: '/employee/list',
    method: 'get'
  })
}

// 根据部门ID获取人员列表
export function getEmployeesByDeptId(deptId) {
  return request({
    url: `/employee/list/${deptId}`,
    method: 'get'
  })
}

// 添加人员
export function addEmployee(data) {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}

// 修改人员
export function updateEmployee(data) {
  return request({
    url: '/employee',
    method: 'put',
    data
  })
}

// 删除人员
export function deleteEmployee(id) {
  return request({
    url: `/employee/${id}`,
    method: 'delete'
  })
}