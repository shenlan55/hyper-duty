<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="700px"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v) => $emit('update:modelValue', v)"
    @open="handleOpen"
  >
    <!-- 搜索栏 -->
    <el-input
      v-model="keyword"
      placeholder="输入姓名/编码/工号进行搜索"
      clearable
      style="margin-bottom: 12px;"
      @input="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <!-- 列表 -->
    <el-table
      ref="tableRef"
      :data="filteredOptions"
      :border="true"
      :height="380"
      :row-key="(row) => row[valueKey]"
      @selection-change="handleSelectionChange"
      v-loading="loading"
      empty-text="暂无可选项"
    >
      <el-table-column
        v-if="multiple"
        type="selection"
        width="55"
        :reserve-selection="true"
      />
      <el-table-column
        v-else
        width="55"
      >
        <template #default="{ row }">
          <el-radio
            :model-value="singleValue"
            :label="row[valueKey]"
            @change="(v) => onRadioChange(v, row)"
          ><span></span></el-radio>
        </template>
      </el-table-column>
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :min-width="col.width || 120"
        :show-overflow-tooltip="true"
      />
    </el-table>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { ref, computed, watch, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'

/**
 * 通用对象选择弹窗
 * 用于 BPMN 设计器选人 / 选组场景，支持单选与多选，外部传入 options 与 columns
 */
export default {
  name: 'SelectorDialog',
  components: { Search },
  props: {
    // v-model: 显示状态
    modelValue: { type: Boolean, default: false },
    // 弹窗标题
    title: { type: String, default: '请选择' },
    // 选项全集（数组，由父组件传入）
    options: { type: Array, default: () => [] },
    // 表格列配置 [{ prop, label, width }]
    columns: { type: Array, default: () => [] },
    // 行的唯一键字段
    valueKey: { type: String, default: 'id' },
    // 多选模式
    multiple: { type: Boolean, default: false },
    // 已选值（单选：id；多选：id 数组）
    selected: { default: null }
  },
  emits: ['update:modelValue', 'confirm'],
  setup(props, { emit }) {
    const keyword = ref('')
    const tableRef = ref(null)
    const loading = ref(false)
    // 多选模式下表格内部维护的当前选择
    const multipleSelection = ref([])
    // 单选模式下当前选中的 id
    const singleValue = ref(null)

    /** 过滤后的选项（前端关键词过滤） */
    const filteredOptions = computed(() => {
      if (!keyword.value) return props.options
      const kw = keyword.value.toLowerCase()
      return props.options.filter((item) => {
        return props.columns.some((col) => {
          const v = item[col.prop]
          return v != null && String(v).toLowerCase().includes(kw)
        })
      })
    })

    /** 打开弹窗时还原已选状态 */
    const handleOpen = () => {
      keyword.value = ''
      nextTick(() => {
        if (props.multiple) {
          // 多选：还原勾选
          multipleSelection.value = []
          if (tableRef.value && Array.isArray(props.selected)) {
            props.selected.forEach((val) => {
              const row = props.options.find((o) => o[props.valueKey] === val)
              if (row) {
                multipleSelection.value.push(row)
                // 触发表格勾选
                tableRef.value.toggleRowSelection(row, true)
              }
            })
          }
        } else {
          // 单选：还原 radio
          singleValue.value = props.selected != null ? props.selected : null
        }
      })
    }

    /** 多选模式：表格选择变化 */
    const handleSelectionChange = (rows) => {
      multipleSelection.value = rows
    }

    /** 单选模式：radio 变化 */
    const onRadioChange = (val) => {
      singleValue.value = val
    }

    /** 输入搜索 */
    const handleSearch = () => {
      // 关键词变化后，若已选行被过滤掉，需要重置勾选状态
      nextTick(() => {
        if (props.multiple) {
          // 清理已不存在于 filteredOptions 中的勾选
          const validIds = new Set(filteredOptions.value.map((o) => o[props.valueKey]))
          multipleSelection.value = multipleSelection.value.filter((r) => validIds.has(r[props.valueKey]))
        }
      })
    }

    /** 确认 */
    const handleConfirm = () => {
      if (props.multiple) {
        const ids = multipleSelection.value.map((r) => r[props.valueKey])
        emit('confirm', ids)
      } else {
        emit('confirm', singleValue.value)
      }
      emit('update:modelValue', false)
    }

    return {
      keyword,
      tableRef,
      loading,
      filteredOptions,
      multipleSelection,
      singleValue,
      handleOpen,
      handleSelectionChange,
      onRadioChange,
      handleSearch,
      handleConfirm
    }
  }
}
</script>
