import request from '../utils/request'

export function getProjectPage(params) {
  return request({
    url: '/pm/project/page',
    method: 'get',
    params
  })
}

export function getMyProjects(employeeId) {
  return request({
    url: `/pm/project/my/${employeeId}`,
    method: 'get'
  })
}

export function getProjectDetail(id) {
  return request({
    url: `/pm/project/${id}`,
    method: 'get'
  })
}

export function createProject(data) {
  return request({
    url: '/pm/project',
    method: 'post',
    data
  })
}

export function updateProject(data) {
  return request({
    url: '/pm/project',
    method: 'put',
    data
  })
}

export function archiveProject(id) {
  return request({
    url: `/pm/project/archive/${id}`,
    method: 'put'
  })
}

export function deleteProject(id) {
  return request({
    url: `/pm/project/${id}`,
    method: 'delete'
  })
}

export function getProjectsByStatus(status) {
  return request({
    url: `/pm/project/status/${status}`,
    method: 'get'
  })
}
