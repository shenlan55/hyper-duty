import request from '@/utils/request'

// 流程管理API
export function deployProcess(name, bpmnXml) {
  return request({
    url: '/workflow/process/deploy',
    method: 'post',
    data: { name, bpmnXml }
  })
}

export function deleteDeployment(deploymentId) {
  return request({
    url: `/workflow/process/deploy/${deploymentId}`,
    method: 'delete'
  })
}

export function pageProcessDefinition(pageNum, pageSize, processKey) {
  return request({
    url: '/workflow/process/definition/page',
    method: 'get',
    params: { pageNum, pageSize, processKey }
  })
}

export function listProcessDefinition() {
  return request({
    url: '/workflow/process/definition/list',
    method: 'get'
  })
}

export function getLatestProcessDefinition(processKey) {
  return request({
    url: `/workflow/process/definition/latest/${processKey}`,
    method: 'get'
  })
}

export function getProcessBpmnXml(processDefinitionId) {
  // GET 方式，processDefinitionId 放在 query 中（避免路径中冒号转义问题）
  // rawText=true 跳过响应拦截器的 HTML 实体解码（XML 中合法实体不能被解码）
  return request({
    url: '/workflow/process/definition/bpmn-xml',
    method: 'get',
    params: { processDefinitionId },
    rawText: true
  })
}

export function syncProcessDefinition() {
  return request({
    url: '/workflow/process/definition/sync',
    method: 'post'
  })
}

export function bindFormToProcess(data) {
  return request({
    url: '/workflow/process/definition/bind-form',
    method: 'post',
    data
  })
}

export function startProcess(data) {
  return request({
    url: '/workflow/process/start',
    method: 'post',
    data
  })
}

export function getProcessInstance(processInstanceId) {
  return request({
    url: `/workflow/process/instance/${processInstanceId}`,
    method: 'get'
  })
}

export function pageMyStartedProcess(pageNum, pageSize) {
  return request({
    url: '/workflow/process/instance/my/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function pageMyCompletedProcess(pageNum, pageSize) {
  return request({
    url: '/workflow/process/instance/my/completed/page',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getProcessVariables(processInstanceId) {
  return request({
    url: `/workflow/process/instance/variables/${processInstanceId}`,
    method: 'get'
  })
}

export function setProcessVariables(processInstanceId, variables) {
  return request({
    url: `/workflow/process/instance/variables/${processInstanceId}`,
    method: 'post',
    data: variables
  })
}

export function cancelProcess(processInstanceId, reason) {
  return request({
    url: `/workflow/process/instance/cancel/${processInstanceId}`,
    method: 'post',
    params: { reason }
  })
}

export function suspendProcess(processInstanceId) {
  return request({
    url: `/workflow/process/instance/suspend/${processInstanceId}`,
    method: 'post'
  })
}

export function activateProcess(processInstanceId) {
  return request({
    url: `/workflow/process/instance/activate/${processInstanceId}`,
    method: 'post'
  })
}

export function getHistoricActivityInstances(processInstanceId) {
  return request({
    url: `/workflow/process/instance/history/activities/${processInstanceId}`,
    method: 'get'
  })
}

export function getHistoricProcessInstance(processInstanceId) {
  return request({
    url: `/workflow/process/instance/history/${processInstanceId}`,
    method: 'get'
  })
}
