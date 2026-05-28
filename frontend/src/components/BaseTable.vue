<template>
  <div class="base-table" :class="{ 'is-mobile-card': isMobileCardMode }">
    <!-- 表格工具栏 -->
    <div v-if="showSearch || showColumnControl || showExport || $slots.toolbar" class="table-toolbar">
      <!-- 左侧内容 -->
      <div class="toolbar-left">
        <!-- 搜索框 -->
        <div v-if="showSearch" class="table-search">
          <el-input
            v-model="searchQuery"
            :placeholder="searchPlaceholder"
            prefix-icon="Search"
            clearable
            @input="(value) => handleSearch(value)"
            @clear="handleSearch('')"
            @keyup.enter="handleSearch(searchQuery)"
          >
          </el-input>
        </div>
        
        <!-- 自定义工具栏内容 -->
        <slot name="toolbar"></slot>
      </div>
      
      <!-- 右侧内容 -->
      <div class="toolbar-right">
        <!-- 列控制 -->
        <div v-if="showColumnControl" class="table-column-control">
          <el-dropdown trigger="click" @command="handleColumnCommand">
            <el-button type="primary" plain>
              <el-icon><Setting /></el-icon>
              列控制
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="column in columns" :key="column?.prop">
                  <el-dropdown-item
                    v-if="column && column.prop"
                    :command="column.prop"
                    divided
                  >
                    <el-checkbox :checked="visibleColumns[column.prop] !== false" @change="(val) => toggleColumnVisibility(column.prop)">
                      {{ column.label }}
                    </el-checkbox>
                  </el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <!-- 导出按钮 -->
        <div v-if="showExport" class="table-export">
          <el-dropdown trigger="click" @command="handleExport">
            <el-button type="success" plain>
              <el-icon><Download /></el-icon>
              {{ exportText }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="exportFormats.includes('excel')" command="excel">
                  <el-icon><Document /></el-icon>
                  导出Excel
                </el-dropdown-item>
                <el-dropdown-item v-if="exportFormats.includes('csv')" command="csv">
                  <el-icon><Files /></el-icon>
                  导出CSV
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
    
    <!-- 表格 -->
    <div v-if="loading && showSkeleton" class="table-skeleton">
      <div v-for="i in skeletonRowCount" :key="i" class="skeleton-row">
        <div v-if="showSelection" class="skeleton-cell selection-cell">
          <el-skeleton-item variant="circle" style="width: 20px; height: 20px;" />
        </div>
        <div v-for="column in columns" :key="column?.prop || column?.slotName" v-if="column && !column.type && (column.prop ? isColumnVisible(column.prop) : true)" class="skeleton-cell" :style="{ width: column?.width || '100px' }">
          <el-skeleton-item variant="p" />
        </div>
      </div>
    </div>
    <el-table
      v-else
      v-loading="loading"
      :data="processedData"
      :row-key="rowKey"
      style="width: 100%"
      :height="useVirtualScroll ? virtualScrollHeight : ''"
      :border="border"
      :stripe="stripe"
      :size="size"
      :action="props.action"
      :tree-props="treeProps"
      :indent="20"
      default-expand-all
      @sort-change="handleSortChange"
      @selection-change="handleSelectionChange"
      @select="handleSelect"
      @select-all="handleSelectAll"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        :width="60"
        :reserve-selection="true"
      />
      
      <!-- 自定义列 -->
      <template v-for="column in columns" :key="column?.prop || column?.slotName">
        <el-table-column
          v-if="column && !column.type && (column.prop ? isColumnVisible(column.prop) : true)"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :sortable="column.sortable"
          :fixed="column.fixed"
          :filter-multiple="column.filterable !== false"
          :filter-method="column.filterable ? (value, row) => handleColumnFilter(column.prop, value) : undefined"
          :filters="column.filterOptions"
          :show-overflow-tooltip="column.showOverflowTooltip !== false"
        >
          <template #default="scope">
            <slot :name="column.slot || column.slotName || column.prop" v-bind="scope">
              {{ column.formatter ? column.formatter(scope.row) : scope.row[column.prop] }}
            </slot>
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column
          v-else-if="column && column.type === 'operation'"
          :label="column.label || '操作'"
          :width="column.width || 200"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <slot name="operation" v-bind="scope"></slot>
          </template>
        </el-table-column>
      </template>
      
      <!-- 空状态插槽 -->
      <template #empty>
        <div class="empty-state">
          <el-icon class="empty-icon"><WarningFilled /></el-icon>
          <p class="empty-text">暂无数据</p>
          <el-button v-if="$slots.empty-action" type="primary" size="small">
            <slot name="empty-action" />
          </el-button>
        </div>
      </template>
    </el-table>

    <!-- 移动端卡片列表模式 -->
    <div v-if="isMobileCardMode" class="mobile-card-list">
      <div
        v-for="row in flatTreeData"
        :key="getRowKey(row, row._index)"
        class="mobile-card-item"
        :class="{ 'has-children': row[treeProps.hasChildren], 'is-expanded': isExpanded(row) }"
        :style="{ marginLeft: (row._depth || 0) * 16 + 'px' }"
        @click="handleCardClick(row)"
      >
        <!-- 展开/收起箭头 + 卡片标题行 -->
        <div class="card-item-title">
          <span class="card-title-content">
            <!-- 树形展开/收起箭头 -->
            <span
              v-if="row[treeProps.hasChildren] || (row[treeProps.children] && row[treeProps.children].length > 0)"
              class="tree-toggle"
              @click.stop="toggleExpand(row)"
            >
              <el-icon :class="{ 'is-expanded': isExpanded(row) }">
                <ArrowRight v-if="!isExpanded(row)" />
                <ArrowDown v-else />
              </el-icon>
            </span>
            <span v-else class="tree-toggle-placeholder" />
            <span>{{ getCardTitle(row) }}</span>
          </span>
          <slot name="card-title-right" :row="row" />
        </div>

        <!-- 卡片字段列表 -->
        <div
          v-for="field in mobileCardFields"
          :key="field.prop || field.label"
          class="card-item-row card-item-field"
          :data-prop="field.prop"
        >
          <span class="card-item-label">{{ field.label }}</span>
          <span class="card-item-value">
            <slot :name="field.slot || field.prop" :row="row">
              {{ field.formatter ? field.formatter(row) : row[field.prop] }}
            </slot>
          </span>
        </div>

        <!-- 卡片操作区 -->
        <div v-if="$slots.operation" class="card-item-actions">
          <slot name="operation" :row="row" />
        </div>
      </div>

      <!-- 卡面空状态 -->
      <div v-if="flatTreeData.length === 0" class="empty-state">
        <el-icon class="empty-icon"><WarningFilled /></el-icon>
        <p class="empty-text">暂无数据</p>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="showPagination && props.pagination">
      <el-pagination
        v-model:current-page="props.pagination.currentPage"
        v-model:page-size="props.pagination.pageSize"
        :page-sizes="props.pagination.pageSizes"
        layout="total, sizes, prev, pager, next, jumper"
        :total="props.pagination.total || 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch, onMounted, onUnmounted } from 'vue'
import { ArrowRight, ArrowDown, Search, Setting, Download, Document, Files, WarningFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 列配置
  columns: {
    type: Array,
    required: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 分页配置
  pagination: {
    type: Object,
    default: () => ({
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
      total: 0
    })
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 表格大小
  size: {
    type: String,
    default: 'default'
  },
  // 树形结构配置
  treeProps: {
    type: Object,
    default: () => ({
      children: 'children',
      hasChildren: 'hasChildren'
    })
  },
  // 是否显示全局搜索
  showSearch: {
    type: Boolean,
    default: false
  },
  // 搜索占位符
  searchPlaceholder: {
    type: String,
    default: '搜索'
  },
  // 是否显示列搜索
  showColumnSearch: {
    type: Boolean,
    default: false
  },
  // 是否显示选择列
  showSelection: {
    type: Boolean,
    default: false
  },
  // 选择类型：multiple/single
  selectionType: {
    type: String,
    default: 'multiple'
  },
  // 默认选中的行
  defaultSelectedRows: {
    type: Array,
    default: () => []
  },
  // 行key，用于树形表格和选择功能
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 是否显示列控制
  showColumnControl: {
    type: Boolean,
    default: false
  },
  // 默认显示的列
  defaultVisibleColumns: {
    type: Array,
    default: () => []
  },
  // 是否启用虚拟滚动
  useVirtualScroll: {
    type: Boolean,
    default: false
  },
  // 虚拟滚动的高度
  virtualScrollHeight: {
    type: String,
    default: '400px'
  },
  // 是否显示导出按钮
  showExport: {
    type: Boolean,
    default: false
  },
  // 是否是后端分页
  backendPagination: {
    type: Boolean,
    default: false
  },
  // 导出按钮文本
  exportText: {
    type: String,
    default: '导出'
  },
  // 导出格式选项
  exportFormats: {
    type: Array,
    default: () => ['excel', 'csv']
  },
  // 是否显示骨架屏
  showSkeleton: {
    type: Boolean,
    default: false
  },
  // 骨架屏行数
  skeletonRowCount: {
    type: Number,
    default: 5
  },
  // 操作属性，用于解决ElScrollbar警告
  action: {
    type: Object,
    default: () => ({})
  },
  // 移动端卡片模式：自动检测设备，用卡片替代表格
  mobileCardMode: {
    type: Boolean,
    default: true
  },
  // 卡片标题字段名
  cardTitleField: {
    type: String,
    default: ''
  },
  // 移动端卡片显示的字段列表 [{prop, label, slot, formatter}]
  cardFields: {
    type: Array,
    default: () => []
  }

})

// Emits
const emit = defineEmits([
  'size-change',
  'current-change',
  'sort-change',
  'search',
  'filter',
  'selection-change',
  'select',
  'select-all',
  'column-visibility-change',
  'export',
  'card-click'
])

// 展开/收起状态管理
const expandedRows = reactive(new Set())

// 搜索和筛选状态
const searchQuery = ref('')
const columnFilters = ref({})

// 选择状态管理
const selectedRows = ref([])

// 初始化默认展开的节点
const initDefaultExpandedNodes = () => {
  // 默认展开到第三级（level < 3）
  const expandToLevel = 3
  
  const traverseTree = (tree, currentLevel = 0) => {
    if (!Array.isArray(tree)) return
    
    tree.forEach(node => {
      if (node && currentLevel < expandToLevel && node.id) {
        expandedRows.add(node.id)
      }
      
      if (node && node[props.treeProps.children] && Array.isArray(node[props.treeProps.children])) {
        traverseTree(node[props.treeProps.children], currentLevel + 1)
      }
    })
  }
  
  traverseTree(props.data)
}

// 列显示/隐藏状态管理
const visibleColumns = ref({})

// 初始化可见列
const initVisibleColumns = () => {
  if (props.defaultVisibleColumns && props.defaultVisibleColumns.length > 0) {
    props.defaultVisibleColumns.forEach(prop => {
      visibleColumns.value[prop] = true
    })
  } else if (props.columns) {
    // 默认显示所有列
    props.columns.forEach(column => {
      if (column && column.prop) {
        visibleColumns.value[column.prop] = true
      }
    })
  }
}

// 切换列显示/隐藏
const toggleColumnVisibility = (columnProp) => {
  // 创建一个新的对象，而不是直接修改原对象
  const newVisibleColumns = { ...visibleColumns.value }
  newVisibleColumns[columnProp] = !newVisibleColumns[columnProp]
  // 重新赋值，触发响应式更新
  visibleColumns.value = newVisibleColumns
  emit('column-visibility-change', Object.keys(visibleColumns.value).filter(prop => visibleColumns.value[prop]))
}

// 检查列是否可见
const isColumnVisible = (columnProp) => {
  return visibleColumns.value[columnProp] !== false
}

// 安全获取列是否可见
const safeIsColumnVisible = (columnProp) => {
  return visibleColumns.value[columnProp] !== false
}

// 处理列控制命令
const handleColumnCommand = (command) => {
  // 这里可以添加额外的列控制逻辑
}

// 导出方法
const handleExport = (format) => {
  emit('export', {
    format,
    data: processedData.value,
    columns: props.columns.filter(column => column.prop && isColumnVisible(column.prop))
  })
}

// 监听props.data变化，重新初始化默认展开的节点
watch(
  () => props.data,
  (newData) => {
    if (newData && newData.length > 0) {
      // 清空之前的展开状态
      expandedRows.clear()
      // 重新初始化默认展开的节点
      initDefaultExpandedNodes()
    }
  },
  { immediate: true, deep: true }
)

// 初始化
initVisibleColumns()
initDefaultExpandedNodes()

// 搜索方法
const handleSearch = (value) => {
  searchQuery.value = value
  // 确保传递正确的搜索参数
  emit('search', { global: value, columnFilters: columnFilters.value })
}

// 列筛选方法
const handleColumnFilter = (columnProp, value) => {
  columnFilters.value[columnProp] = value
  emit('filter', { columnFilters: columnFilters.value, global: searchQuery.value })
}

// 选择方法
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  emit('selection-change', rows)
}

const handleSelect = (selection, row) => {
  emit('select', selection, row)
}

const handleSelectAll = (selection) => {
  emit('select-all', selection)
}

// 分页方法
const handleSizeChange = (size) => {
  if (props.pagination) {
    props.pagination.pageSize = size
    emit('size-change', size)
  }
}

const handleCurrentChange = (current) => {
  if (props.pagination) {
    props.pagination.currentPage = current
    emit('current-change', current)
  }
}

const handleSortChange = (sort) => {
  emit('sort-change', sort)
}

// 监听pagination属性变化
watch(
  () => props.pagination,
  (newPagination) => {
    if (newPagination) {
      // 当pagination变化时，更新内部状态
    }
  },
  { deep: true }
)

// 切换展开/收起状态
const toggleExpand = (row) => {
  if (row && row.id) {
    if (expandedRows.has(row.id)) {
      expandedRows.delete(row.id)
    } else {
      expandedRows.add(row.id)
    }
  }
}

// 检查是否展开
const isExpanded = (row) => {
  return row && row.id ? expandedRows.has(row.id) : false
}

// 处理后的数据
const processedData = computed(() => {
  // 确保数据是数组
  if (!Array.isArray(props.data)) {
    return []
  }
  
  // 对数据进行分页处理（仅非树形数据、非后端分页）
  if (props.showPagination && !props.backendPagination && props.pagination && !(props.data.length > 0 && props.data[0].children)) {
    const currentPage = props.pagination.currentPage || 1
    const pageSize = props.pagination.pageSize || 10
    const startIndex = (currentPage - 1) * pageSize
    const endIndex = startIndex + pageSize
    return props.data.slice(startIndex, endIndex)
  }
  
  return props.data
})

// 移动端卡片模式下，将树形数据扁平化（支持展开/收起）
const flatTreeData = computed(() => {
  const data = processedData.value
  if (!Array.isArray(data) || data.length === 0) return []
  if (!data[0] || (data[0][props.treeProps.children] === undefined && data[0][props.treeProps.hasChildren] === undefined)) {
    return data.map((item, i) => ({ ...item, _depth: 0, _index: i }))
  }
  // 递归扁平化：展开的节点将其 children 插入
  const result = []
  function flatten(nodes, depth = 0) {
    if (!Array.isArray(nodes)) return
    nodes.forEach((node, idx) => {
      if (!node) return
      result.push({ ...node, _depth: depth, _index: result.length })
      const childrenKey = props.treeProps.children
      const hasChildrenKey = props.treeProps.hasChildren
      const hasChildren = node[hasChildrenKey] === true || (Array.isArray(node[childrenKey]) && node[childrenKey].length > 0)
      if (hasChildren && expandedRows.has(node.id) && Array.isArray(node[childrenKey])) {
        flatten(node[childrenKey], depth + 1)
      }
    })
  }
  flatten(data)
  return result
})

// 移动端卡片模式相关
const windowWidth = ref(window.innerWidth)
onMounted(() => { window.addEventListener('resize', () => { windowWidth.value = window.innerWidth }) })
onUnmounted(() => {})

// 是否启用移动端卡片模式
const isMobileCardMode = computed(() => {
  if (!props.mobileCardMode) return false
  return windowWidth.value < 768
})

// 卡片展示的字段：优先使用 cardFields，否则取 columns 的非隐藏列
const mobileCardFields = computed(() => {
  if (props.cardFields && props.cardFields.length > 0) {
    return props.cardFields
  }
  // 自动从 columns 提取 max 5 个字段（排除 type='operation'、type='selection' 和 id/sort 类字段）
  const excludeProps = ['sort', 'id', 'jobId', 'employeeId', 'deptId', 'roleId', 'dictId', 'menuId', 'userId']
  return props.columns
    .filter(c => c && c.prop && !c.type && !excludeProps.includes(c.prop) && !c.prop.toLowerCase().endsWith('id'))
    .slice(0, 5)
    .map(c => ({ prop: c.prop, label: c.label }))
})

// 获取卡片标题
function getCardTitle(row) {
  if (props.cardTitleField && row) return row[props.cardTitleField]
  // 默认取第一个合适的字段值（跳过 id, sort, 等看起来像序号的字段）
  const titlePriorityList = [
    // 优先找名称类字段
    'name', 'title', 'jobName', 'employeeName', 'deptName', 
    'userName', 'roleName', 'dictName', 'menuName',
    // 然后找编码类
    'code', 'jobCode', 'employeeCode',
    // 最后找其他字段但排除序号
  ]
  // 先从优先级列表找
  for (const prop of titlePriorityList) {
    const field = mobileCardFields.value.find(f => f.prop === prop)
    if (field && row[field.prop]) {
      return row[field.prop]
    }
  }
  // 都没找到，就找第一个非 id/sort 的字段
  const firstValidField = mobileCardFields.value.find(f => 
    f.prop && f.prop !== 'sort' && f.prop !== 'id' && !f.prop.toLowerCase().endsWith('id')
  )
  if (firstValidField && row) return row[firstValidField.prop]
  // 实在找不到再用 id
  const idField = mobileCardFields.value.find(f => f.prop === 'id')
  if (idField && row) return row[idField.prop]
  return ''
}

// 获取行的唯一标识
function getRowKey(row, index) {
  if (typeof props.rowKey === 'function') return props.rowKey(row)
  if (typeof props.rowKey === 'string' && row) return row[props.rowKey]
  return index
}

// 卡片点击事件
function handleCardClick(row) {
  emit('card-click', row)
}
</script>

<style scoped>
.base-table {
  width: 100%;
}



.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 工具栏样式 */
.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* 搜索框样式 */
.table-search {
  display: flex;
  justify-content: flex-start;
}

.table-search .el-input {
  width: 300px;
}

/* 列控制样式 */
/* 列控制和导出按钮样式 */
.table-column-control,
.table-export {
  margin-left: 0;
}

/* 自定义工具栏内容样式 */
:deep(.dept-filter) {
  width: 200px;
  margin-left: 0;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin-bottom: 24px;
}

.empty-state .el-button {
  margin-top: 8px;
}

/* 骨架屏样式 */
.table-skeleton {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.skeleton-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding: 12px 0;
}

.skeleton-row:last-child {
  border-bottom: none;
}

.skeleton-cell {
  padding: 0 18px;
  flex: 1;
  min-width: 100px;
}

.selection-cell {
  width: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ======================================== */
/* 移动端卡片模式样式 */
/* ======================================== */
.mobile-card-list {
  padding: 8px 0;
}

.mobile-card-item {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: transform 0.12s ease, background-color 0.12s ease, box-shadow 0.12s ease;
}

.mobile-card-item:active {
  transform: scale(0.985);
  background-color: #f8f9fb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card-item-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title-content {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.card-title-content > span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  cursor: pointer;
  color: #909399;
  border-radius: 4px;
  transition: background 0.15s;
}

.tree-toggle:active {
  background: #f0f0f0;
}

.tree-toggle .el-icon {
  transition: transform 0.2s;
  font-size: 14px;
}

.tree-toggle .el-icon.is-expanded {
  transform: rotate(0deg);
}

.tree-toggle-placeholder {
  width: 24px;
  flex-shrink: 0;
}

/* 子任务卡片缩进外观 */
.mobile-card-item.has-children {
  border-left: 3px solid transparent;
}

.mobile-card-item[style*="margin-left"] {
  background: #fafbfc;
  border-radius: 6px;
  border-left: 3px solid #e0e4ea;
}

.card-item-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 13px;
  padding: 3px 0;
}

.card-item-label {
  color: #909399;
  min-width: 70px;
  flex-shrink: 0;
}

.card-item-value {
  color: #606266;
  text-align: right;
  word-break: break-all;
}

.card-item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;
}

.card-item-actions .el-button {
  min-height: 32px;
  font-size: 12px;
  padding: 5px 12px;
}

/* 移动端卡片模式下按钮更小 */
@media (max-width: 767px) {
  .card-item-actions .el-button {
    min-height: 22px;
    height: 22px;
    font-size: 10px;
    padding: 2px 6px;
    line-height: 18px;
  }
}

/* 卡片模式下隐藏表格 */
.is-mobile-card .el-table {
  display: none;
}

/* 卡片模式下分页简化 */
.is-mobile-card .pagination-container .el-pagination__sizes,
.is-mobile-card .pagination-container .el-pagination__jump {
  display: none;
}
</style>