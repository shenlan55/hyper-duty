/**
 * useDict - 业务字典 composable
 *
 * 让组件用更少的样板代码消费字典：
 *   const { options, labelOf, tagTypeOf, loadDict } = useDict('task_status')
 *   await loadDict()                    // 在 onMounted 或 watch 中触发
 *   options.value                       // -> [{label:'未开始',value:'1',raw:{...}}, ...]
 *   labelOf(1)                          // -> '未开始'
 *   tagTypeOf(1)                        // -> 'info'
 *
 * 多字典：
 *   const { loadDict } = useDict(['task_status','task_priority'])
 *   await loadDict()
 *   // 选项取 task_statusOptions / taskPriorityOptions
 */
import { computed } from 'vue'
import { useDictStore } from '@/stores/dict'

export function useDict(codes) {
  const dictStore = useDictStore()
  const codeList = Array.isArray(codes) ? codes : [codes]

  // 触发加载
  const loadDict = () => dictStore.loadDict(codeList)

  // 为每个 code 暴露对应的 computed options
  const optionsMap = {}
  const labelFnMap = {}
  const tagTypeFnMap = {}

  for (const code of codeList) {
    const camelKey = code.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
    optionsMap[camelKey] = computed(() => dictStore.getOptions(code))
    labelFnMap[camelKey] = (value, fallback) => dictStore.getLabel(code, value, fallback)
    tagTypeFnMap[camelKey] = (value) => dictStore.getTagType(code, value)
  }

  // 直接挂到返回值上：options / labelOf / tagTypeOf
  // - 单字典时：options = 单个 computed
  // - 多字典时：options = { taskStatus: computed(...), taskPriority: computed(...) }
  const result = {
    loadDict,
    refreshDict: () => dictStore.refreshDict(codeList)
  }

  if (codeList.length === 1) {
    const camelKey = codeList[0].replace(/_([a-z])/g, (_, c) => c.toUpperCase())
    result.options = optionsMap[camelKey]
    result.labelOf = labelFnMap[camelKey]
    result.tagTypeOf = tagTypeFnMap[camelKey]
    result.code = codeList[0]
  } else {
    result.options = optionsMap
    result.labelOf = labelFnMap
    result.tagTypeOf = tagTypeFnMap
    result.codes = codeList
  }

  return result
}
