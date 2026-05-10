import request from '@/utils/request'

const BASE_URL = '/api/workflow/category'

export function pageCategory(params) {
  return request({
    url: BASE_URL + '/page',
    method: 'get',
    params
  })
}

export function listCategory(params) {
  return request({
    url: BASE_URL + '/list',
    method: 'get',
    params
  })
}

export function getCategory(id) {
  return request({
    url: BASE_URL + '/' + id,
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: BASE_URL,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: BASE_URL + '/' + id,
    method: 'delete'
  })
}
