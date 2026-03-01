// 任务状态相关工具函数
export const taskStatusMap = {
  1: { text: '未开始', type: 'info' },
  2: { text: '进行中', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已暂停', type: 'warning' },
  5: { text: '已取消', type: 'danger' }
}

// 任务优先级相关工具函数
export const taskPriorityMap = {
  1: { text: '高', type: 'danger' },
  2: { text: '中', type: 'warning' },
  3: { text: '低', type: 'info' }
}

// 获取任务状态文本
export const getTaskStatusText = (status) => {
  return taskStatusMap[status]?.text || '未知'
}

// 获取任务状态类型
export const getTaskStatusType = (status) => {
  return taskStatusMap[status]?.type || 'info'
}

// 获取任务优先级文本
export const getTaskPriorityText = (priority) => {
  return taskPriorityMap[priority]?.text || '未知'
}

// 获取任务优先级类型
export const getTaskPriorityType = (priority) => {
  return taskPriorityMap[priority]?.type || 'info'
}

// 获取进度状态
export const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 60) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

// 格式化日期时间
export const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
