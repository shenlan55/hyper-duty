/**
 * 字典 Pinia Store
 *
 * 业务枚举（任务状态/班次/审批/是否/启用禁用/性别 等）的统一数据源：
 * - 一次请求按 dict_code 批量加载并缓存到内存
 * - 组件通过 useDict(dictCode) 直接拿到 options/label/tagClass
 *
 * 使用：
 *   import { useDictStore } from '@/stores/dict'
 *   const dictStore = useDictStore()
 *   await dictStore.loadDict(['task_status', 'task_priority'])
 *   const taskStatusOptions = dictStore.getOptions('task_status')
 *   const label = dictStore.getLabel('task_status', 1)   // '未开始'
 *   const tagType = dictStore.getTagType('task_status', 1) // 'info'
 */
import { defineStore } from 'pinia'
import { getDictDataByCodes } from '@/api/dict'

/**
 * 业务字典 code 统一常量（前端唯一标识字典）
 * 命名规则：snake_case，与后端 sys_dict_type.dict_code 保持一致
 */
export const DICT_CODES = Object.freeze({
  COMMON_STATUS: 'common_status',      // 通用启用/禁用
  TASK_STATUS: 'task_status',          // 任务状态
  TASK_PRIORITY: 'task_priority',      // 任务优先级
  APPROVAL_STATUS: 'approval_status',  // 审批状态
  SHIFT_TYPE: 'shift_type',            // 班次类型
  YES_NO: 'yes_no',                    // 是否
  GENDER: 'gender',                    // 性别
  FORM_TYPE: 'form_type',              // 表单类型
  DEPLOY_STATUS: 'deploy_status',      // 流程部署状态
  POSITION: 'Position',                // 职位（已有）
  ITEM_TYPE: 'ItemType'                // 事项类型（已有）
})

export const useDictStore = defineStore('dict', {
  state: () => ({
    // Map<dictCode, Array<SysDictData>> —— 缓存的字典数据
    dictMap: {},
    // 加载状态：Set<dictCode>，避免重复请求
    loadingSet: new Set()
  }),
  getters: {
    /**
     * 获取指定字典的数据列表
     * @param {string} code 字典编码
     * @returns {Array}
     */
    getDictList: (state) => (code) => {
      return state.dictMap[code] || []
    }
  },
  actions: {
    /**
     * 批量加载字典（已加载的会自动跳过）
     * @param {string|string[]} codes
     */
    async loadDict(codes) {
      const codeList = (Array.isArray(codes) ? codes : [codes])
        .filter(c => c && !this.dictMap[c])
      if (codeList.length === 0) return

      // 避免并发重复请求
      const toFetch = codeList.filter(c => !this.loadingSet.has(c))
      toFetch.forEach(c => this.loadingSet.add(c))
      try {
        const res = await getDictDataByCodes(codeList)
        // 响应拦截器已 unwrap，业务数据直接是 { task_status: [...], ... }
        if (res && typeof res === 'object') {
          for (const code of codeList) {
            this.dictMap[code] = Array.isArray(res[code]) ? res[code] : []
          }
        }
      } catch (e) {
        // 加载失败时移除 loading 标记，允许重试
        console.error('[dict] loadDict failed:', codeList, e)
      } finally {
        toFetch.forEach(c => this.loadingSet.delete(c))
      }
    },

    /**
     * 获取 el-select 风格的下拉选项
     * 防御性兜底：即便后端未排序，前端也按 dictSort 升序 + id 升序排列，保证展示顺序与字典表一致
     * @param {string} code
     * @returns {Array<{label:string, value:any, isDefault:boolean, raw:Object}>}
     */
    getOptions(code) {
      const list = this.dictMap[code] || []
      // 排序：先按 dictSort 升序，再按 id 升序（兜底）
      const sorted = [...list].sort((a, b) => {
        const sa = a.dictSort ?? 0
        const sb = b.dictSort ?? 0
        if (sa !== sb) return sa - sb
        return (a.id ?? 0) - (b.id ?? 0)
      })
      return sorted.map(d => ({
        label: d.dictLabel,
        value: d.dictValue,  // 保持字符串，业务侧按需转 Number
        isDefault: d.isDefault === 1,  // 暴露默认项标记，供组件初始化使用
        raw: d
      }))
    },

    /**
     * 获取字典中标记为"默认项"的值（is_default = 1）
     * 若字典未加载完成 / 未设置默认项，返回 null
     * @param {string} code
     * @returns {string|null}
     */
    getDefaultValue(code) {
      const list = this.dictMap[code] || []
      const found = list.find(d => d.isDefault === 1)
      return found ? found.dictValue : null
    },

    /**
     * 获取字典中标记为"默认项"的完整 option
     * @param {string} code
     * @returns {{label:string, value:any, isDefault:boolean, raw:Object}|null}
     */
    getDefaultOption(code) {
      const list = this.dictMap[code] || []
      const found = list.find(d => d.isDefault === 1)
      if (!found) return null
      return {
        label: found.dictLabel,
        value: found.dictValue,
        isDefault: true,
        raw: found
      }
    },

    /**
     * 根据 value 取出 label，找不到返回 fallback 或原 value
     * @param {string} code
     * @param {any} value
     * @param {string} [fallback]
     */
    getLabel(code, value, fallback) {
      const list = this.dictMap[code] || []
      // 兼容 value 类型（String/Number）—— 后端 dict_value 统一为 String
      const v = value === null || value === undefined ? '' : String(value)
      const found = list.find(d => String(d.dictValue) === v)
      if (found) return found.dictLabel
      return fallback !== undefined ? fallback : (value ?? '')
    },

    /**
     * 根据 value 取出 el-tag 的 type（primary/success/warning/danger/info）
     * 找不到返回 'info'
     * @param {string} code
     * @param {any} value
     */
    getTagType(code, value) {
      const list = this.dictMap[code] || []
      const v = value === null || value === undefined ? '' : String(value)
      const found = list.find(d => String(d.dictValue) === v)
      return (found && found.listClass) ? found.listClass : 'info'
    },

    /**
     * 强制刷新（清空后重新加载），用于字典后台更新后
     * @param {string|string[]} codes
     */
    async refreshDict(codes) {
      const codeList = Array.isArray(codes) ? codes : [codes]
      codeList.forEach(c => delete this.dictMap[c])
      return this.loadDict(codeList)
    }
  }
})
