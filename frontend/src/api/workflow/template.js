import http from '@/utils/request'

/** 流程模板市场 API（P3-1） */

export const pageTemplates = (params) => {
  return http.get('/wf/template/page', { params })
}

export const listActiveTemplates = (category) => {
  return http.get('/wf/template/list', { params: { category } })
}

export const getTemplate = (id) => {
  return http.get(`/wf/template/${id}`)
}

export const createTemplate = (data) => {
  return http.post('/wf/template', data)
}

export const updateTemplate = (data) => {
  return http.put('/wf/template', data)
}

export const deleteTemplate = (id) => {
  return http.delete(`/wf/template/${id}`)
}

export const useTemplate = (id) => {
  return http.post(`/wf/template/${id}/use`)
}

/** AI 辅助生成 BPMN（P3-1 占位） */
export const aiGenerateProcess = (prompt) => {
  return http.post('/wf/ai/generate', { prompt })
}
