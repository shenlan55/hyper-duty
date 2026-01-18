import request from '../utils/request'

export function listDictData(params) {
  return request({
    url: '/dict/data/list',
    method: 'get',
    params
  })
}

export function getDictDataByType(dictTypeId) {
  return request({
    url: `/dict/data/byType/${dictTypeId}`,
    method: 'get'
  })
}

export function getDictData(id) {
  return request({
    url: `/dict/data/detail/${id}`,
    method: 'get'
  })
}

export function addDictData(data) {
  return request({
    url: '/dict/data',
    method: 'post',
    data
  })
}

export function updateDictData(data) {
  return request({
    url: '/dict/data',
    method: 'put',
    data
  })
}

export function deleteDictData(id) {
  return request({
    url: `/dict/data/${id}`,
    method: 'delete'
  })
}