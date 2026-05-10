import request from '@/utils/request'

// 流程管理API
export function deployProcess(name, bpmnXml) {
  return request({
    url: '/api/workflow/process/deploy',
    method: 'post',
    params: { name },
    data: bpmnXml,
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}

export function deleteDeployment(deploymentId) {
  return request({
    url: `/api/workflow/process/deploy/${deploymentId}`,
    method: 'delete'
  })
}

export function pageProcessDefinition(pageNum, pageSize, processKey) {
  return request({
    url: '/api/workflow/process/definition/page',
    method: 'get',
    params: { pageNum, pageSize, processKey }
  })
}

export function listProcessDefinition() {
  return request({
    url: '/api/workflow/process/definition/list',
    method: 'get'
  })
}

export function getLatestProcessDefinition(processKey) {
  return request({
    url: `/api/workflow/process/definition/latest/${processKey}`,
    method: 'get'
  })
}

export function getProcessBpmnXml(processDefinitionId) {
  return request({
    url: `/api/workflow/process/definition/bpmn/${processDefinitionId}`,
    method: 'get'
  })
}

export function syncProcessDefinition() {
  return request({
    url: '/api/workflow/process/definition/sync',
    method: 'post'
  })
}

export function bindFormToProcess(params) {
  return request({
    url: '/api/workflow/process/definition/bind-form',
    method: 'post',
    params
  })
}

export function startProcess(data) {
  return request({
    url: '/api/workflow/process/start',
    method: 'post',
    data
  })
}

export function getProcessInstance(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/${processInstanceId}`,
    method: 'get'
  })
}

export function pageMyStartedProcess(pageNum, pageSize) {
  return request({
    url: '/api/workflow/process/instance/my/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getProcessVariables(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/variables/${processInstanceId}`,
    method: 'get'
  })
}

export function setProcessVariables(processInstanceId, variables) {
  return request({
    url: `/api/workflow/process/instance/variables/${processInstanceId}`,
    method: 'post',
    data: variables
  })
}

export function cancelProcess(processInstanceId, reason) {
  return request({
    url: `/api/workflow/process/instance/cancel/${processInstanceId}`,
    method: 'post',
    params: { reason }
  })
}

export function suspendProcess(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/suspend/${processInstanceId}`,
    method: 'post'
  })
}

export function activateProcess(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/activate/${processInstanceId}`,
    method: 'post'
  })
}

export function getHistoricActivityInstances(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/history/activities/${processInstanceId}`,
    method: 'get'
  })
}

export function getHistoricProcessInstance(processInstanceId) {
  return request({
    url: `/api/workflow/process/instance/history/${processInstanceId}`,
    method: 'get'
  })
}
