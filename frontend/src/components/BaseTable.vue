<template>
  <div class="base-table">
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
      style="width: 100%"
      :height="useVirtualScroll ? virtualScrollHeight : ''"
      :border="border"
      :stripe="stripe"
      :size="size"
      :action="props.action"
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
          :show-overflow-tooltip="column.showOverflowTooltip"
        >
          <template #default="scope">
            <div class="tree-node-content" :style="column.indent ? { marginLeft: `${scope.row.level * 20}px` } : {}">
              <!-- 展开/收起按钮 -->
              <span 
                v-if="column.indent && (scope.row.hasChildren || (scope.row.children && scope.row.children.length > 0))" 
                class="expand-icon"
                @click.stop="toggleExpand(scope.row)"
              >
                <el-icon v-if="isExpanded(scope.row)"><ArrowDown /></el-icon>
                <el-icon v-else><ArrowRight /></el-icon>
              </span>
              <span v-else-if="column.indent" class="expand-icon-placeholder"></span>
              
              <!-- 内容 -->
              <span class="node-content">
                <slot :name="column.slot || column.slotName || column.prop" v-bind="scope">
                  {{ column.formatter ? column.formatter(scope.row) : scope.row[column.prop] }}
                </slot>
              </span>
            </div>
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
import { ref, computed, reactive, watch } from 'vue'
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
  'export'
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
  
  // 将树形数据扁平化为数组，并保留层级信息
  const flattenTree = (tree, level = 0, parentExpanded = true) => {
    let result = []
    tree.forEach(node => {
      if (node) {
        // 为节点添加层级信息
        const nodeWithLevel = { ...node, level }
        result.push(nodeWithLevel)
        
        // 检查是否需要展开子节点
        const hasChildren = node[props.treeProps.hasChildren] || (node[props.treeProps.children] && node[props.treeProps.children].length > 0)
        const isRowExpanded = parentExpanded && (hasChildren ? isExpanded(node) : true)
        
        if (isRowExpanded && node[props.treeProps.children] && node[props.treeProps.children].length > 0) {
          result = result.concat(flattenTree(node[props.treeProps.children], level + 1, isRowExpanded))
        }
      }
    })
    return result
  }
  
  // 检查数据是否为树形结构
  if (props.data.length > 0 && props.data[0].children) {
    return flattenTree(props.data)
  }
  
  // 对数据进行分页处理
  if (props.showPagination && !props.backendPagination && props.pagination) {
    const currentPage = props.pagination.currentPage || 1
    const pageSize = props.pagination.pageSize || 10
    const startIndex = (currentPage - 1) * pageSize
    const endIndex = startIndex + pageSize
    return props.data.slice(startIndex, endIndex)
  }
  
  return props.data
})
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

/* 树形节点样式 */
.tree-node-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.expand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  cursor: pointer;
  color: #909399;
}

.expand-icon:hover {
  color: #409EFF;
}

.expand-icon-placeholder {
  width: 16px;
  margin-right: 8px;
  display: inline-block;
}

.node-content {
  flex: 1;
}
</style>