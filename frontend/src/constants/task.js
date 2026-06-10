/**
 * 任务相关常量
 *
 * 业务规则：
 * - 任务状态、优先级的 **value** 是和后端 sys_dict_data 约定的业务编码（不可变）
 * - **label**（中文名）必须从字典 API 获取，**禁止前端硬编码**
 *   - 业务组件请使用 useDict('task_status') / useDict('task_priority')
 *   - store 路径：@/stores/dict 中的 DICT_CODES
 * - 进度等业务范围类常量继续保留在此处（不属于枚举）
 */
import { DICT_CODES } from '@/stores/dict'

/**
 * 任务状态（dict_value 与后端 sys_dict_data.task_status 对齐）
 * 含义：1=未开始 2=进行中 3=已完成 4=已暂停
 */
export const TASK_STATUS = Object.freeze({
  NOT_STARTED: 1,
  IN_PROGRESS: 2,
  COMPLETED: 3,
  PAUSED: 4
})

/**
 * 任务优先级（dict_value 与后端 sys_dict_data.task_priority 对齐）
 * 含义：1=高 2=中 3=低
 */
export const TASK_PRIORITY = Object.freeze({
  HIGH: 1,
  MEDIUM: 2,
  LOW: 3
})

/** 字典 code 引用（业务组件请通过 DICT_CODES.TASK_STATUS 取 code） */
export { DICT_CODES }

// 任务进度范围
export const TASK_PROGRESS_MIN = 0
export const TASK_PROGRESS_MAX = 100
