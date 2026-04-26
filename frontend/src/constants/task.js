/**
 * 任务相关常量
 */

// 任务状态
export const TASK_STATUS = {
  NOT_STARTED: 1,
  IN_PROGRESS: 2,
  COMPLETED: 3,
  PAUSED: 4
}

// 任务状态名称映射
export const TASK_STATUS_MAP = {
  [TASK_STATUS.NOT_STARTED]: '未开始',
  [TASK_STATUS.IN_PROGRESS]: '进行中',
  [TASK_STATUS.COMPLETED]: '已完成',
  [TASK_STATUS.PAUSED]: '已暂停'
}

// 任务优先级
export const TASK_PRIORITY = {
  HIGH: 1,
  MEDIUM: 2,
  LOW: 3
}

// 任务优先级名称映射
export const TASK_PRIORITY_MAP = {
  [TASK_PRIORITY.HIGH]: '高',
  [TASK_PRIORITY.MEDIUM]: '中',
  [TASK_PRIORITY.LOW]: '低'
}

// 任务进度范围
export const TASK_PROGRESS_MIN = 0
export const TASK_PROGRESS_MAX = 100
