/**
 * 任务相关常量
 */

// 任务状态
export const TASK_STATUS = {
  NOT_STARTED: 0,
  IN_PROGRESS: 1,
  COMPLETED: 2,
  CANCELLED: 3
} as const;

// 任务状态名称映射
export const TASK_STATUS_MAP: Record<number, string> = {
  [TASK_STATUS.NOT_STARTED]: '未开始',
  [TASK_STATUS.IN_PROGRESS]: '进行中',
  [TASK_STATUS.COMPLETED]: '已完成',
  [TASK_STATUS.CANCELLED]: '已取消'
};

// 任务优先级
export const TASK_PRIORITY = {
  LOW: 0,
  MEDIUM: 1,
  HIGH: 2,
  URGENT: 3
} as const;

// 任务优先级名称映射
export const TASK_PRIORITY_MAP: Record<number, string> = {
  [TASK_PRIORITY.LOW]: '低',
  [TASK_PRIORITY.MEDIUM]: '中',
  [TASK_PRIORITY.HIGH]: '高',
  [TASK_PRIORITY.URGENT]: '紧急'
};

// 任务进度范围
export const TASK_PROGRESS_MIN = 0
export const TASK_PROGRESS_MAX = 100
