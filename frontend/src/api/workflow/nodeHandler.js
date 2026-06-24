import request from '@/utils/request'

/**
 * 节点处理人配置 API
 * - 与 BPMN 设计师属性面板联动
 * - 在流程部署时按 processDefinitionId 批量保存
 */
export function saveNodeHandler(data) {
  return request({
    url: '/workflow/node-handler/save',
    method: 'post',
    data
  })
}

export function saveBatchNodeHandler(processDefinitionId, processDefinitionKey, handlers) {
  return request({
    url: '/workflow/node-handler/save-batch',
    method: 'post',
    params: { processDefinitionId, processDefinitionKey },
    data: handlers
  })
}

export function listByDefinition(processDefinitionId) {
  return request({
    url: '/workflow/node-handler/list-by-definition',
    method: 'get',
    params: { processDefinitionId }
  })
}

export function listByKey(processDefinitionKey) {
  return request({
    url: '/workflow/node-handler/list-by-key',
    method: 'get',
    params: { processDefinitionKey }
  })
}

export function deleteNodeHandler(id) {
  return request({
    url: `/workflow/node-handler/${id}`,
    method: 'delete'
  })
}

export function deleteByDefinition(processDefinitionId) {
  return request({
    url: '/workflow/node-handler/delete-by-definition',
    method: 'delete',
    params: { processDefinitionId }
  })
}
