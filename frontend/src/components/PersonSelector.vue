<template>
  <div class="person-selector">
    <!-- 左侧部门选择 -->
    <div class="dept-section">
      <div class="section-header">
        <span class="section-title">部门选择</span>
        <el-button link type="primary" size="small" @click="handleRefreshDeptTree">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>

      <div class="dept-search-wrapper">
        <el-input
          v-model="deptSearchText"
          placeholder="搜索部门"
          clearable
          size="small"
          :prefix-icon="Search"
        />
      </div>

      <el-tree
        ref="deptTreeRef"
        :data="deptTree"
        node-key="id"
        :props="deptTreeProps"
        :default-expand-all="true"
        :filter-node-method="filterDeptNode"
        @node-click="handleDeptClick"
        :current-node-key="selectedDeptId"
        highlight-current
        class="dept-tree"
      >
        <template #default="{ node, data }">
          <div class="custom-tree-node">
            <span class="node-label">{{ node.label }}</span>
            <el-badge
              v-if="getDeptEmployeeCount(data.id) > 0"
              :value="getDeptEmployeeCount(data.id)"
              class="employee-count-badge"
              type="info"
              :max="99"
            />
          </div>
        </template>
      </el-tree>

      <!-- 当前选中提示 -->
      <div class="current-dept-info">
        <span class="dept-name">{{ currentDeptName || '全部' }}</span>
      </div>
    </div>

    <!-- 中间人员列表 -->
    <div class="employee-section">
      <div class="section-header">
        <span class="section-title">人员列表</span>
        <div class="header-actions">
          <!-- 当前/全部 切换开关 -->
          <el-radio-group v-model="scopeMode" size="small" @change="handleScopeChange" class="scope-switch">
            <el-radio-button value="current">当前</el-radio-button>
            <el-radio-button value="all">全部</el-radio-button>
          </el-radio-group>

          <el-input
            v-model="searchKeyword"
            placeholder="搜索人员"
            clearable
            size="small"
            class="search-input"
            :prefix-icon="Search"
          />
        </div>
      </div>

      <el-table
        ref="employeeTable"
        :data="filteredEmployeeList"
        style="width: 100%"
        height="calc(100% - 52px)"
        size="small"
        @selection-change="handleEmployeeSelectionChange"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column prop="employeeName" label="姓名" width="90" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="140" show-overflow-tooltip />

        <!-- 状态列 -->
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status !== 1" size="small" type="danger">禁用</el-tag>
            <span v-else style="color: #67c23a; font-size: 12px;">正常</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 中间转移按钮 -->
    <div class="transfer-buttons">
      <el-button
        type="primary"
        @click="transferToRight"
        :disabled="selectedEmployeeRows.length === 0"
        size="small"
      >
        选择 &gt;&gt;
      </el-button>
      <el-button
        type="primary"
        @click="transferAllToRight"
        :disabled="availableEmployeeCount === 0"
        size="small"
      >
        全部 &gt;&gt;
      </el-button>
      <el-button
        type="info"
        @click="transferToLeft"
        :disabled="selectedSelectedRows.length === 0"
        size="small"
      >
        &lt;&lt; 移除
      </el-button>
      <el-button
        type="info"
        @click="transferAllToLeft"
        :disabled="selectedEmployees.length === 0"
        size="small"
      >
        &lt;&lt; 全部
      </el-button>
    </div>

    <!-- 右侧已选人员 -->
    <div class="selected-section">
      <div class="section-header">
        <span class="section-title">已选人员 ({{ selectedEmployees.length }})</span>
      </div>

      <el-table
        ref="selectedTable"
        :data="selectedEmployees"
        style="width: 100%"
        height="calc(100% - 44px)"
        size="small"
        @selection-change="handleSelectedSelectionChange"
        :row-key="row => row.id || row.employeeId"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="姓名" show-overflow-tooltip min-width="60">
          <template #default="{ row }">
            <!-- 兼容多种字段名格式：驼峰、小写、Pascal等 -->
            <span v-if="row.employeeName">{{ row.employeeName }}</span>
            <span v-else-if="row.employeename">{{ row.employeename }}</span>
            <span v-else-if="row.name">{{ row.name }}</span>
            <span v-else-if="row.realName">{{ row.realName }}</span>
            <span v-else-if="row.realname">{{ row.realname }}</span>
            <span v-else-if="row.userName">{{ row.userName }}</span>
            <span v-else-if="row.username">{{ row.username }}</span>
            <span v-else-if="row.nickname">{{ row.nickname }}</span>
            <span v-else style="color: #909399; font-size: 11px;" :title="JSON.stringify(row)">
              {{ Object.keys(row).length > 0 ? 'ID:' + (row.id || row.employeeId || '-') : '空对象' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getDeptTree, getActiveDeptTree } from '@/api/dept'
import { getEmployeesByDeptId, getEmployeeList } from '@/api/employee'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择人员'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// ==================== 部门相关 ====================
const deptTreeRef = ref()
const deptTree = ref([])
const selectedDeptId = ref(null)
const currentDeptName = ref('')
const deptSearchText = ref('')
const deptTreeProps = {
  label: 'deptName',
  children: 'children'
}
const deptEmployeeCountMap = ref({}) // 部门-人数映射

// 范围模式：current=当前部门，all=全部子部门
const scopeMode = ref('current')

// ==================== 人员相关 ====================
const employeeList = ref([])
const selectedEmployees = ref([])
const searchKeyword = ref('')

// 选择状态
const selectedEmployeeRows = ref([])
const selectedSelectedRows = ref([])

// ==================== 计算属性 ====================

/**
 * 过滤后的人员列表（支持关键词搜索）
 */
const filteredEmployeeList = computed(() => {
  if (!searchKeyword.value) return employeeList.value

  const keyword = searchKeyword.value.toLowerCase().trim()
  if (!keyword) return employeeList.value

  return employeeList.value.filter(emp =>
    emp.employeeName?.toLowerCase().includes(keyword) ||
    emp.employeeCode?.toLowerCase().includes(keyword) ||
    emp.phone?.includes(keyword)
  )
})

/**
 * 可用人员数量（排除已禁用的）
 */
const availableEmployeeCount = computed(() => {
  return filteredEmployeeList.value.filter(emp => emp.status === 1).length
})

/**
 * 获取指定部门的人数
 */
const getDeptEmployeeCount = (deptId) => {
  return deptEmployeeCountMap.value[deptId] || 0
}

// ==================== 监听器 ====================

watch(() => props.modelValue, (newValue) => {
  if (Array.isArray(newValue)) {
    // 过滤空对象，保留有效数据
    selectedEmployees.value = newValue.filter(item => item != null && Object.keys(item).length > 0)
  } else {
    selectedEmployees.value = []
  }
}, { deep: true, immediate: true })

// 监听部门搜索文本变化，实时过滤
watch(deptSearchText, (val) => {
  deptTreeRef.value?.filter(val)
})

// ==================== 部门树方法 ====================

/**
 * 加载部门树数据
 */
const loadDeptTree = async () => {
  try {
    const data = await getActiveDeptTree()
    if (data && Array.isArray(data)) {
      deptTree.value = data.map(dept => ({
        ...dept,
        children: dept.children || []
      }))
    } else {
      deptTree.value = []
    }
  } catch (error) {
    console.error('加载部门树失败', error)
    deptTree.value = []
  }
}

/**
 * 处理部门节点点击
 */
const handleDeptClick = async (node) => {
  selectedDeptId.value = node.id
  currentDeptName.value = node.deptName

  // 根据范围模式加载人员
  await loadEmployeesByScope(node.id)
}

/**
 * 范围模式切换
 */
const handleScopeChange = () => {
  if (selectedDeptId.value) {
    loadEmployeesByScope(selectedDeptId.value)
  }
}

/**
 * 根据范围模式加载人员
 * - current: 只加载当前部门的人员
 * - all: 加载当前部门及其所有子部门的人员
 */
const loadEmployeesByScope = async (deptId) => {
  try {
    let employees = []

    if (scopeMode.value === 'all') {
      // 全部模式：获取该部门及所有子部门的所有人员
      const response = await getEmployeeList(1, 1000, '')
      const allEmployees = response?.records || []

      // 获取该部门及其所有子部门的ID列表
      const deptIds = getAllChildDeptIds(deptId, deptTree.value)

      // 筛选出这些部门下的人员
      employees = allEmployees.filter(emp =>
        emp.deptId && deptIds.includes(emp.deptId)
      )
    } else {
      // 当前模式：只获取当前部门的人员
      const data = await getEmployeesByDeptId(deptId)
      employees = data || []
    }

    // 过滤掉已选中的人员和禁用的人员
    const filteredData = employees.filter(emp =>
      emp.status === 1 &&
      !selectedEmployees.value.some(selected => selected.id === emp.id)
    )

    employeeList.value = filteredData
  } catch (error) {
    console.error('加载人员失败', error)
    employeeList.value = []
  }
}

/**
 * 递归获取部门ID及其所有子部门ID
 */
const getAllChildDeptIds = (deptId, depts) => {
  const ids = [deptId]

  const findChildren = (nodes) => {
    for (const node of nodes) {
      if (node.id === deptId && node.children) {
        for (const child of node.children) {
          ids.push(child.id)
          findChildren([child]) // 递归处理子节点的子节点
        }
        break
      }
      if (node.children && node.children.length > 0) {
        findChildren(node.children)
      }
    }
  }

  findChildren(depts)
  return ids
}

/**
 * 统计各部门人数
 */
const updateDeptEmployeeCountMap = (employees) => {
  const countMap = {}
  for (const emp of employees) {
    if (emp.deptId) {
      countMap[emp.deptId] = (countMap[emp.deptId] || 0) + 1
    }
  }
  deptEmployeeCountMap.value = countMap
}

/**
 * 部门树过滤方法
 */
const filterDeptNode = (value, data) => {
  if (!value) return true
  return data.deptName?.toLowerCase().includes(value.toLowerCase())
}

/**
 * 刷新部门树
 */
const handleRefreshDeptTree = async () => {
  await loadDeptTree()

  // 重新统计人数
  try {
    const response = await getEmployeeList(1, 10000, '')
    updateDeptEmployeeCountMap(response?.records || [])
  } catch (error) {
    console.error('统计部门人数失败', error)
  }

  console.log('部门树已刷新')
}

// ==================== 人员操作方法 ====================

/**
 * 表格行样式（禁用员工灰色背景）
 */
const tableRowClassName = ({ row }) => {
  return row.status !== 1 ? 'disabled-row' : ''
}

/**
 * 处理人员选择变化
 */
const handleEmployeeSelectionChange = (rows) => {
  // 只有单个选择时才自动移动到右侧
  if (rows.length === 1 && selectedEmployeeRows.value.length === 0) {
    const emp = rows[0]
    if (!selectedEmployees.value.some(selected => selected.id === emp.id)) {
      selectedEmployees.value.push(emp)
    }

    // 从左侧列表移除
    employeeList.value = employeeList.value.filter(e => e.id !== emp.id)

    // 触发事件
    emit('update:modelValue', selectedEmployees.value)
    emit('change', selectedEmployees.value)
  } else {
    // 多选或全选时，只记录选择状态，配合按钮使用
    selectedEmployeeRows.value = rows
  }
}

/**
 * 处理已选人员选择变化
 */
const handleSelectedSelectionChange = (rows) => {
  selectedSelectedRows.value = rows
}

/**
 * 转移到右侧（批量选择）
 */
const transferToRight = () => {
  if (selectedEmployeeRows.value.length === 0) return

  // 添加选中的人员到已选列表
  selectedEmployeeRows.value.forEach(emp => {
    if (!selectedEmployees.value.some(selected => selected.id === emp.id)) {
      selectedEmployees.value.push(emp)
    }
  })

  // 从左侧列表移除
  employeeList.value = employeeList.value.filter(emp =>
    !selectedEmployeeRows.value.some(selected => selected.id === emp.id)
  )

  // 清空选择
  selectedEmployeeRows.value = []
  if (employeeList.value.length > 0) {
    employeeList.value = [...employeeList.value]
  }

  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

/**
 * 全部转移到右侧
 */
const transferAllToRight = () => {
  if (employeeList.value.length === 0) return

  // 添加所有可用人员到已选列表
  employeeList.value.forEach(emp => {
    if (!selectedEmployees.value.some(selected => selected.id === emp.id)) {
      selectedEmployees.value.push(emp)
    }
  })

  // 清空左侧列表
  employeeList.value = []

  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

/**
 * 转移到左侧
 */
const transferToLeft = () => {
  if (selectedSelectedRows.value.length === 0) return

  // 添加选中的人员到左侧列表
  selectedSelectedRows.value.forEach(emp => {
    if (!employeeList.value.some(selected => selected.id === emp.id)) {
      employeeList.value.push(emp)
    }
  })

  // 从右侧列表移除
  selectedEmployees.value = selectedEmployees.value.filter(emp =>
    !selectedSelectedRows.value.some(selected => selected.id === emp.id)
  )

  // 清空选择
  selectedSelectedRows.value = []
  if (selectedEmployees.value.length > 0) {
    selectedEmployees.value = [...selectedEmployees.value]
  }

  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

/**
 * 全部转移到左侧
 */
const transferAllToLeft = () => {
  if (selectedEmployees.value.length === 0) return

  // 添加所有人员到左侧列表
  selectedEmployees.value.forEach(emp => {
    if (!employeeList.value.some(selected => selected.id === emp.id)) {
      employeeList.value.push(emp)
    }
  })

  // 清空右侧列表
  selectedEmployees.value = []

  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

// ==================== 初始化 ====================

onMounted(async () => {
  await loadDeptTree()

  // 加载全量人员统计各部门人数
  try {
    const response = await getEmployeeList(1, 10000, '')
    updateDeptEmployeeCountMap(response?.records || [])
  } catch (error) {
    console.error('统计部门人数失败', error)
  }
})
</script>

<style scoped>
/* ==================== 整体布局 ==================== */
.person-selector {
  display: flex;
  height: 520px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #ffffff;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* ==================== 左侧部门树 ==================== */
.dept-section {
  width: 220px;
  min-width: 220px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  background-color: #fafbfc;
  position: relative; /* 为搜索框提供定位上下文 */
}

.section-header {
  padding: 12px 14px;
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.section-title {
  color: #303133;
  letter-spacing: 0.5px;
}

.dept-search-wrapper {
  margin: 10px 12px;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.dept-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

/* 自定义树节点 */
.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  line-height: 24px;
  color: #606266;
}

.employee-count-badge {
  margin-left: 8px;
  flex-shrink: 0;
}

.current-dept-info {
  margin-top: auto;
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
  background-color: #fff;
  flex-shrink: 0;
}

.dept-name {
  color: #409eff;
  font-weight: 500;
  font-size: 13px;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

/* ==================== 中间人员列表 ==================== */
.employee-section {
  flex: 2.5;
  min-width: 420px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scope-switch {
  flex-shrink: 0;
}

.search-input {
  width: 160px;
  flex-shrink: 0;
}

/* ==================== 转移按钮 ==================== */
.transfer-buttons {
  width: 85px;
  min-width: 85px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center; /* 改回center确保所有按钮居中 */
  gap: 10px;
  padding: 16px 8px;
  background-color: #fafbfc;
  border-right: 1px solid #ebeef5;
}

.transfer-buttons :deep(.el-button) {
  width: 100%;
  justify-content: center !important;
  text-align: center !important; /* 确保文字居中 */
  padding: 8px 4px !important;
  font-size: 12px !important;
  border-radius: 4px !important;
  box-sizing: border-box !important; /* 确保padding不影响宽度 */
  margin-left: 0 !important; /* 清除可能的左边距 */
  margin-right: 0 !important; /* 清除可能的右边距 */
}

/* ==================== 右侧已选人员 ==================== */
.selected-section {
  flex: 0.7;
  min-width: 130px;
  max-width: 160px;
  display: flex;
  flex-direction: column;
  background-color: #fafbfc;
}

/* 右侧表格特殊优化 */
.selected-section :deep(.el-table) {
  --el-table-header-bg-color: #f0f2f5; /* 稍深的背景色区分 */
}

/* 确保右侧表格内容可见 */
.selected-section :deep(.el-table__body-wrapper) {
  overflow-y: auto !important; /* 确保可滚动 */
}

.selected-section :deep(.el-table__empty-block) {
  min-height: 60px !important;
}

/* ==================== 通用样式 ==================== */

/* 树形组件样式 */
:deep(.el-tree) {
  background-color: transparent;
}

:deep(.el-tree-node__content) {
  height: 28px;
  border-radius: 4px;
  margin: 2px 0;
  /* 不强制设置padding-left，保留默认的层级缩进 */
}

:deep(.el-tree-node__content:hover) {
  background-color: #ecf5ff !important;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #d9ecff !important;
  color: #409eff;
  font-weight: 500;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 13px;
  margin-right: 4px; /* 调整展开图标间距 */
}

/* 表格样式 */
:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f5f7fa;
  --el-table-row-hover-bg-color: #f5f7fa;
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
  font-size: 12px;
  padding: 8px 0; /* 表头内边距 */
}

:deep(.el-table td.el-table__cell) {
  padding: 4px 0; /* 减少单元格内边距，避免空行 */
}

:deep(.el-table .cell) {
  padding: 0 10px; /* 单元格左右内边距 */
  line-height: 20px; /* 减少行高 */
}

/* 禁用行样式 */
:deep(.disabled-row) {
  background-color: #fdfdfd !important;
  color: #c0c4cc !important;
}

:deep(.disabled-row td) {
  color: #c0c4cc !important;
}

/* 输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-input--small .el-input__inner) {
  font-size: 12px;
}

/* 单选按钮组样式 */
:deep(.el-radio-group) {
  --el-fill-color-blank: #fff;
}

:deep(.el-radio-button__inner) {
  padding: 6px 14px; /* 增加按钮内边距 */
  font-size: 12px;
}

/* Badge 样式 */
:deep(.el-badge__content) {
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

/* 滚动条美化（整体） */
:deep(::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(::-webkit-scrollbar-track) {
  background-color: #f1f1f1;
  border-radius: 3px;
}

:deep(::-webkit-scrollbar-thumb) {
  background-color: #c1c1c1;
  border-radius: 3px;
}

:deep(::-webkit-scrollbar-thumb:hover) {
  background-color: #a8a8a8;
}
</style>
