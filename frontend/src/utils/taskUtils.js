/**
 * 任务相关工具函数
 *
 * 业务规则：
 * - 本文件**不再**提供任务状态/优先级的 label 和 type 映射
 *   业务组件请使用 useDict('task_status').labelOf / tagTypeOf，
 *   或模板里直接 useDict().options 渲染
 * - 进度↔状态、排序、格式化 等业务逻辑函数仍保留在此（不属于枚举）
 */

// 进度对应的 Element Plus el-progress status
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

// 任务状态分组排序优先级（与字典 value 对齐）
// 1=未开始 2=进行中 3=已完成 4=已暂停 5=已取消
const statusGroupOrder = {
  1: 0, // 未开始 - 第一组
  2: 0, // 进行中 - 第一组
  3: 1, // 已完成 - 第二组
  4: 2, // 已暂停 - 第三组
  5: 3  // 已取消 - 第四组
}

// 任务优先级排序优先级（与字典 value 对齐）
// 1=高 2=中 3=低
const priorityOrder = {
  1: 0, // 高
  2: 1, // 中
  3: 2  // 低
}

// 任务排序函数
export const sortTasks = (tasks) => {
  return [...tasks].sort((a, b) => {
    // 1. 首先按置顶排序，置顶的任务排最前面
    const isPinnedA = a.isPinned === 1 ? 0 : 1
    const isPinnedB = b.isPinned === 1 ? 0 : 1
    if (isPinnedA !== isPinnedB) {
      return isPinnedA - isPinnedB
    }
    // 2. 置顶状态相同则按状态分组排序
    const statusGroupA = statusGroupOrder[a.status] ?? 999
    const statusGroupB = statusGroupOrder[b.status] ?? 999
    if (statusGroupA !== statusGroupB) {
      return statusGroupA - statusGroupB
    }
    // 3. 状态分组相同则按优先级排序
    const priorityA = priorityOrder[a.priority] ?? 999
    const priorityB = priorityOrder[b.priority] ?? 999
    return priorityA - priorityB
  })
}

// 根据进度获取对应的状态（与字典 task_status 的 value 对齐）
export const getStatusByProgress = (progress) => {
  if (progress >= 100) {
    return 3 // 已完成
  } else if (progress <= 0) {
    return 1 // 未开始
  } else {
    return 2 // 进行中
  }
}

// 根据状态获取对应的进度（保留原进度的情况返回null）
export const getProgressByStatus = (status, currentProgress = 0) => {
  switch (status) {
    case 1: // 未开始
      return 0
    case 3: // 已完成
      return 100
    case 2: // 进行中
    case 4: // 已暂停
    default:
      return null // 返回null表示不修改进度
  }
}
