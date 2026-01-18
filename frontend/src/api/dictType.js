import request from '../utils/request'

export function listDictType(params) {
  return request({
    url: '/dict/type/list',
    method: 'get',
    params
  })
}

export function getDictType(id) {
  return request({
    url: `/dict/type/detail/${id}`,
    method: 'get'
  })
}

export function addDictType(data) {
  return request({
    url: '/dict/type',
    method: 'post',
    data
  })
}

export function updateDictType(data) {
  return request({
    url: '/dict/type',
    method: 'put',
    data
  })
}

export function deleteDictType(id) {
  return request({
    url: `/dict/type/${id}`,
    method: 'delete'
  })
}