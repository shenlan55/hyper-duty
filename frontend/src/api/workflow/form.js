import request from '@/utils/request'

// 表单管理API
export function pageForm(params) {
  return request({
    url: '/workflow/form/page',
    method: 'get',
    params
  })
}

export function createForm(data) {
  return request({
    url: '/workflow/form',
    method: 'post',
    data
  })
}

export function updateForm(data) {
  return request({
    url: '/workflow/form',
    method: 'put',
    data
  })
}

export function deleteForm(id) {
  return request({
    url: `/workflow/form/${id}`,
    method: 'delete'
  })
}

export function getForm(id) {
  return request({
    url: `/workflow/form/${id}`,
    method: 'get'
  })
}

export function listForm(params) {
  return request({
    url: '/workflow/form/page',
    method: 'get',
    params
  })
}

export function getFormDetail(id) {
  return request({
    url: `/workflow/form/${id}`,
    method: 'get'
  })
}
