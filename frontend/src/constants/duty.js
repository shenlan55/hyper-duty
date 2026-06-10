/**
 * 值班管理相关常量
 *
 * 业务规则：
 * - 数字编码的枚举（task_status、shift_type 等）→ 走 sys_dict + useDict
 * - 字符串编码的字段（如 duty.approvalStatus 存的是 'pending'/'approved'/'rejected'）
 *   保持字符串编码映射，并集中在本文件统一管理，避免散落在页面里
 * - 字段值与后端 POJO 保持一致，任何修改必须同步后端代码
 */

/**
 * 审批状态（字符串编码，与后端 DutyRecord.approvalStatus 字段值一致）
 * 后端字段为 String，业务约定：
 *   pending   = 待审批
 *   approved  = 已批准
 *   rejected  = 已驳回
 *   withdrawn = 已撤回
 *   cancelled = 已取消（调班申请等场景使用）
 */
export const APPROVAL_STATUS = Object.freeze({
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  WITHDRAWN: 'withdrawn',
  CANCELLED: 'cancelled'
})

/** 审批状态 → 中文 label */
export const APPROVAL_STATUS_LABEL = Object.freeze({
  [APPROVAL_STATUS.PENDING]: '待审批',
  [APPROVAL_STATUS.APPROVED]: '已批准',
  [APPROVAL_STATUS.REJECTED]: '已驳回',
  [APPROVAL_STATUS.WITHDRAWN]: '已撤回',
  [APPROVAL_STATUS.CANCELLED]: '已取消'
})

/** 审批状态 → Element Plus tag type */
export const APPROVAL_STATUS_TAG = Object.freeze({
  [APPROVAL_STATUS.PENDING]: 'warning',
  [APPROVAL_STATUS.APPROVED]: 'success',
  [APPROVAL_STATUS.REJECTED]: 'danger',
  [APPROVAL_STATUS.WITHDRAWN]: 'info',
  [APPROVAL_STATUS.CANCELLED]: 'info'
})

/**
 * 班次类型 → 备用中文 label（fallback）
 * 完整班次以 sys_shift_config 表为准；
 * 字典 sys_dict.shift_type 提供的是"基础班次类型"，value=0..5（白/早/中/晚/全/夜），
 * 与 sys_shift_config.shiftType 字段值一致。GOC/SRE 班次等业务扩展班次
 * value>=6 不在字典范围内，落到此处 fallback。
 */
export const SHIFT_TYPE_FALLBACK_LABEL = Object.freeze({
  0: '白班',
  1: '早班',
  2: '中班',
  3: '晚班',
  4: '全天',
  5: '夜班'
})
