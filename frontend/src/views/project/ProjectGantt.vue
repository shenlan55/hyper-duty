<template>
  <MobileGuard>
    <div class="project-gantt">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>甘特图</span>
            <div class="header-actions">
              <el-button type="primary" @click="handleExport" :disabled="!selectedProjectId">
                <el-icon><Download /></el-icon>
                导出Excel
              </el-button>
              <el-select v-model="selectedProjectId" placeholder="请选择项目" clearable filterable @change="handleProjectChange" style="width: 200px">
                <el-option
                  v-for="project in projectList"
                  :key="project.id"
                  :label="project.projectName"
                  :value="project.id"
                />
              </el-select>
            </div>
          </div>
          
          <!-- 工具栏 -->
          <div class="toolbar" v-if="selectedProjectId">
            <div class="toolbar-left">
              <el-select v-model="timeRange" placeholder="选择时间范围" @change="handleTimeRangeChange" style="width: 110px">
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="本季度" value="quarter" />
                <el-option label="本年" value="year" />
                <el-option label="自定义" value="custom" />
              </el-select>
              
              <el-date-picker
                v-if="timeRange === 'custom'"
                v-model="customDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                @change="handleCustomDateChange"
                style="width: 260px"
              />
              
              <el-radio-group v-model="viewMode" size="small" class="view-mode-group">
                <el-radio-button value="day">日</el-radio-button>
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
            
            <div class="toolbar-right">
              <el-button size="small" @click="expandAll">
                <el-icon><Expand /></el-icon>
                展开全部
              </el-button>
              <el-button size="small" @click="collapseAll">
                <el-icon><Fold /></el-icon>
                折叠全部
              </el-button>
            </div>
          </div>
        </template>

        <div v-if="!selectedProjectId" class="empty-tip">
          <el-empty description="请选择项目查看甘特图" />
        </div>

        <div v-else class="gantt-container">
          <div class="gantt-wrapper">
            <!-- 左侧任务列表 -->
            <div class="gantt-fixed-col">
              <div class="gantt-header">
                <div class="gantt-task-header">任务名称</div>
              </div>
              <div class="gantt-body" ref="fixedBodyRef">
                <template v-for="(task, index) in visibleFlatList" :key="task.id">
                  <div class="gantt-row">
                    <div 
                      class="gantt-task-cell"
                      :class="[`level-${task.taskLevel}`, { 'last-child': isLastChild(task, index) }]"
                    >
                      <el-icon 
                        v-if="hasChildren(task.id)" 
                        class="collapse-icon"
                        :class="{ expanded: !isCollapsed(task.id) }"
                        @click="toggleCollapse(task.id)"
                      >
                        <ArrowRight />
                      </el-icon>
                      <span v-else class="placeholder-icon"></span>
                      
                      <span v-if="task.isPinned === 1" class="pinned-icon">📌</span>
                      
                      <span :class="{ 'pinned-task': task.isPinned === 1 }">
                        {{ task.taskName }}
                      </span>
                    </div>
                  </div>
                </template>
              </div>
            </div>
            
            <!-- 右侧时间轴 -->
            <div class="gantt-scroll-col">
              <div class="gantt-header" ref="timelineHeaderRef">
                <div class="gantt-timeline-header">
                  <div
                    v-for="date in dateList"
                    :key="date"
                    class="timeline-cell"
                    :class="{ 'is-today': isToday(date), 'is-weekend': isWeekend(date) }"
                    :style="{ width: cellWidth + 'px', minWidth: cellWidth + 'px' }"
                  >
                    {{ formatDateLabel(date) }}
                  </div>
                </div>
              </div>
              <div class="gantt-body" @scroll="handleScroll">
                <template v-for="(task, index) in visibleFlatList" :key="task.id">
                  <div class="gantt-row">
                    <div class="gantt-timeline-cell">
                      <div
                        v-for="date in dateList"
                        :key="date"
                        class="timeline-cell"
                        :class="{ 'is-today': isToday(date), 'is-weekend': isWeekend(date) }"
                        :style="{ width: cellWidth + 'px', minWidth: cellWidth + 'px' }"
                      ></div>
                      <div
                        v-if="task.startDate && task.endDate"
                        class="gantt-bar"
                        :class="getBarClass(task)"
                        :style="getBarStyle(task)"
                      >
                        <span class="bar-text">{{ task.progress }}%</span>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <div v-if="taskList.length === 0" class="no-data">
            <el-empty description="暂无任务数据" />
          </div>
        </div>
      </el-card>
    </div>
  </MobileGuard>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, ArrowRight, Expand, Fold } from '@element-plus/icons-vue'
import MobileGuard from '@/components/MobileGuard.vue'
import { getProjectPage } from '@/api/project'
import { getProjectTasks, exportGantt } from '@/api/task'

// ==================== 基础状态 ====================
const selectedProjectId = ref(null)
const projectList = ref([])
const taskList = ref([])
const fixedBodyRef = ref(null)
const timelineHeaderRef = ref(null)

// 折叠状态独立管理（用 Set 存储已折叠的任务ID）
const collapsedTaskIds = ref(new Set())

const state = reactive({
  timeRange: 'week',
  viewMode: 'day',
  customDateRange: null,
  customStartDate: null,
  customEndDate: null
})

// ==================== 日期工具 ====================
const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取周数
const getWeekNumber = (date) => {
  const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
  const dayNum = d.getUTCDay() || 7
  d.setUTCDate(d.getUTCDate() + 4 - dayNum)
  const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1))
  return Math.ceil((((d - yearStart) / 86400000) + 1) / 7)
}

// 获取某个日期所在周的周一日期
const getWeekStart = (dateStr) => {
  const d = new Date(dateStr)
  const day = d.getDay() || 7
  d.setDate(d.getDate() - day + 1)
  return formatDate(d)
}

// 获取某个日期所在月的第一天日期
const getMonthStart = (dateStr) => {
  const d = new Date(dateStr)
  return formatDate(new Date(d.getFullYear(), d.getMonth(), 1))
}

// ==================== 时间范围计算 ====================
const calculateTimeRange = () => {
  const now = new Date()
  let start, end
  
  switch (state.timeRange) {
    case 'week': {
      const day = now.getDay() || 7
      start = new Date(now)
      start.setDate(now.getDate() - day + 1)
      end = new Date(start)
      end.setDate(start.getDate() + 6)
      break
    }
    case 'month':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      break
    case 'quarter': {
      const quarter = Math.floor(now.getMonth() / 3)
      start = new Date(now.getFullYear(), quarter * 3, 1)
      end = new Date(now.getFullYear(), quarter * 3 + 3, 0)
      break
    }
    case 'year':
      start = new Date(now.getFullYear(), 0, 1)
      end = new Date(now.getFullYear(), 11, 31)
      break
    case 'custom':
      if (state.customStartDate && state.customEndDate) {
        return { startDate: state.customStartDate, endDate: state.customEndDate }
      }
      return calculateDefaultRange()
    default:
      return calculateDefaultRange()
  }
  
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

const calculateDefaultRange = () => {
  if (taskList.value.length === 0) {
    const now = new Date()
    const start = new Date(now); start.setDate(now.getDate() - 7)
    const end = new Date(now); end.setDate(now.getDate() + 7)
    return { startDate: formatDate(start), endDate: formatDate(end) }
  }
  
  let minDate = null, maxDate = null
  taskList.value.forEach(task => {
    if (task.startDate) {
      const s = new Date(task.startDate)
      if (!minDate || s < minDate) minDate = s
    }
    if (task.endDate) {
      const e = new Date(task.endDate)
      if (!maxDate || e > maxDate) maxDate = e
    }
  })
  
  if (!minDate || !maxDate) {
    const now = new Date()
    const start = new Date(now); start.setDate(now.getDate() - 7)
    const end = new Date(now); end.setDate(now.getDate() + 7)
    return { startDate: formatDate(start), endDate: formatDate(end) }
  }
  
  minDate = new Date(minDate); minDate.setDate(minDate.getDate() - 7)
  maxDate = new Date(maxDate); maxDate.setDate(maxDate.getDate() + 7)
  return { startDate: formatDate(minDate), endDate: formatDate(maxDate) }
}

// ==================== 计算属性 ====================
const timeRange = computed({
  get: () => state.timeRange,
  set: (val) => { state.timeRange = val }
})

const viewMode = computed({
  get: () => state.viewMode,
  set: (val) => { state.viewMode = val }
})

const customDateRange = computed({
  get: () => state.customDateRange,
  set: (val) => { state.customDateRange = val }
})

// 日期列表
const dateList = computed(() => {
  const { startDate, endDate } = calculateTimeRange()
  const dates = []
  const current = new Date(startDate)
  const end = new Date(endDate)
  
  while (current <= end) {
    dates.push(formatDate(current))
    switch (state.viewMode) {
      case 'day': current.setDate(current.getDate() + 1); break
      case 'week': current.setDate(current.getDate() + 7); break
      case 'month': current.setMonth(current.getMonth() + 1); break
    }
  }
  return dates
})

// 单元格宽度
const cellWidth = computed(() => {
  switch (state.viewMode) {
    case 'day': return 40
    case 'week': return 80
    case 'month': return 120
    default: return 40
  }
})

// 格式化日期标签
const formatDateLabel = (dateStr) => {
  const date = new Date(dateStr)
  switch (state.viewMode) {
    case 'day': return `${date.getMonth() + 1}/${date.getDate()}`
    case 'week': return `第${getWeekNumber(date)}周`
    case 'month': return `${date.getFullYear()}年${date.getMonth() + 1}月`
    default: return `${date.getMonth() + 1}/${date.getDate()}`
  }
}

const isToday = (dateStr) => dateStr === formatDate(new Date())
const isWeekend = (dateStr) => {
  const day = new Date(dateStr).getDay()
  return day === 0 || day === 6
}

// ==================== 任务树结构 ====================
// 构建任务树（纯函数，不修改 collapsed 状态）
const buildTaskTree = (tasks) => {
  const taskMap = new Map()
  const roots = []
  
  tasks.forEach(task => {
    taskMap.set(task.id, { ...task, children: [] })
  })
  
  tasks.forEach(task => {
    const node = taskMap.get(task.id)
    if (task.parentId && taskMap.has(task.parentId)) {
      taskMap.get(task.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })
  
  return roots
}

// 判断任务日期是否与当前可视时间范围有交集
const isTaskInRange = (task) => {
  if (!task.startDate || !task.endDate) return false
  const { startDate, endDate } = calculateTimeRange()
  return task.endDate >= startDate && task.startDate <= endDate
}

// 递归裁剪树：保留范围内任务 + 有子任务在范围内的父任务
const pruneTree = (nodes) => {
  return nodes.filter(node => {
    if (node.children && node.children.length > 0) {
      node.children = pruneTree(node.children)
    }
    // 保留条件：自身在范围内，或保留了至少一个子任务（作为层级父节点）
    return isTaskInRange(node) || (node.children && node.children.length > 0)
  })
}

// 扁平化任务列表（裁剪后展开所有层级，用于 collapseAll 等）
const flatTaskList = computed(() => {
  const tree = buildTaskTree(taskList.value)
  const pruned = pruneTree(tree)
  const result = []
  const flatten = (tasks) => {
    tasks.forEach(task => {
      result.push(task)
      if (task.children && task.children.length > 0) {
        flatten(task.children)
      }
    })
  }
  flatten(pruned)
  return result
})

// 可见的扁平列表（裁剪 + 过滤折叠的）
const visibleFlatList = computed(() => {
  const tree = buildTaskTree(taskList.value)
  const pruned = pruneTree(tree)
  const result = []
  const collapsedSet = collapsedTaskIds.value
  
  const flatten = (tasks, parentCollapsed) => {
    tasks.forEach(task => {
      const isHidden = parentCollapsed
      if (!isHidden) {
        result.push(task)
      }
      const taskCollapsed = collapsedSet.has(task.id)
      if (task.children && task.children.length > 0) {
        flatten(task.children, isHidden || taskCollapsed)
      }
    })
  }
  
  flatten(pruned, false)
  return result
})

// 父子关系映射
const parentMap = computed(() => {
  const map = new Map()
  flatTaskList.value.forEach(task => {
    if (task.parentId) {
      if (!map.has(task.parentId)) map.set(task.parentId, [])
      map.get(task.parentId).push(task.id)
    }
  })
  return map
})

const hasChildren = (taskId) => {
  return parentMap.value.has(taskId) && parentMap.value.get(taskId).length > 0
}

// 判定是否是同级最后一个可见子任务
const isLastChild = (task, currentIndex) => {
  if (!task.parentId) return false
  const siblings = visibleFlatList.value.filter(t => t.parentId === task.parentId)
  const idx = siblings.findIndex(t => t.id === task.id)
  return idx === siblings.length - 1
}

// ==================== 折叠/展开 ====================
const isCollapsed = (taskId) => collapsedTaskIds.value.has(taskId)

const toggleCollapse = (taskId) => {
  const newSet = new Set(collapsedTaskIds.value)
  if (newSet.has(taskId)) {
    newSet.delete(taskId)
  } else {
    newSet.add(taskId)
  }
  collapsedTaskIds.value = newSet
}

const expandAll = () => {
  collapsedTaskIds.value = new Set()
}

const collapseAll = () => {
  const ids = new Set()
  flatTaskList.value.forEach(task => {
    if (hasChildren(task.id)) {
      ids.add(task.id)
    }
  })
  collapsedTaskIds.value = ids
}

// ==================== 任务条位置计算 ====================
// 将任意日期格式归一化为 YYYY-MM-DD 字符串
const normalizeDateStr = (d) => {
  if (!d) return ''
  if (typeof d === 'string') return d.substring(0, 10)
  return formatDate(d)
}

const getBarStyle = (task) => {
  const taskStart = normalizeDateStr(task.startDate)
  const taskEnd = normalizeDateStr(task.endDate)
  
  if (!taskStart || !taskEnd || dateList.value.length === 0) return {}
  
  const firstVisible = dateList.value[0]
  const lastVisible = dateList.value[dateList.value.length - 1]
  
  // 任务完全在可视范围之外，不显示
  if (taskEnd < firstVisible || taskStart > lastVisible) return {}
  
  let startIndex = -1
  let endIndex = -1
  
  if (state.viewMode === 'day') {
    // 日视图：精确匹配日期
    startIndex = dateList.value.indexOf(taskStart)
    endIndex = dateList.value.indexOf(taskEnd)
  } else if (state.viewMode === 'week') {
    // 周视图：找到 startDate / endDate 所在的周一起始日期
    startIndex = dateList.value.indexOf(getWeekStart(taskStart))
    endIndex = dateList.value.indexOf(getWeekStart(taskEnd))
  } else {
    // 月视图：找到 startDate / endDate 所在的月第一天
    startIndex = dateList.value.indexOf(getMonthStart(taskStart))
    endIndex = dateList.value.indexOf(getMonthStart(taskEnd))
  }
  
  // 超出可视范围的边界则夹紧到首/尾
  if (startIndex < 0) startIndex = 0
  if (endIndex < 0) endIndex = dateList.value.length - 1
  
  const left = startIndex * cellWidth.value
  const width = (endIndex - startIndex + 1) * cellWidth.value
  
  return {
    left: left + 'px',
    width: width + 'px',
    '--progress': (task.progress || 0) + '%'
  }
}

const getBarClass = (task) => {
  if (task.status === 3) return 'completed'
  if (task.status === 4) return 'paused'
  if (task.progress >= 60) return 'high-progress'
  if (task.progress >= 30) return 'medium-progress'
  return 'low-progress'
}

// ==================== 数据加载 ====================
const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000 })
    projectList.value = data.records || []
    if (projectList.value.length > 0 && !selectedProjectId.value) {
      selectedProjectId.value = projectList.value[0].id
      await handleProjectChange()
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

const handleProjectChange = async () => {
  if (!selectedProjectId.value) {
    taskList.value = []
    collapsedTaskIds.value = new Set()
    return
  }
  try {
    const data = await getProjectTasks(selectedProjectId.value)
    taskList.value = data || []
    collapsedTaskIds.value = new Set()
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  }
}

// ==================== 事件处理 ====================
const handleTimeRangeChange = () => {
  if (state.timeRange !== 'custom') {
    state.customDateRange = null
    state.customStartDate = null
    state.customEndDate = null
  }
}

const handleCustomDateChange = (val) => {
  if (val && val.length === 2) {
    state.customStartDate = val[0]
    state.customEndDate = val[1]
  } else {
    state.customStartDate = null
    state.customEndDate = null
  }
}

const handleScroll = (e) => {
  // 同步左侧任务列表竖向滚动
  if (fixedBodyRef.value) {
    fixedBodyRef.value.scrollTop = e.target.scrollTop
  }
  // 同步右侧表头横向滚动
  if (timelineHeaderRef.value) {
    timelineHeaderRef.value.scrollLeft = e.target.scrollLeft
  }
}

const handleExport = async () => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }
  try {
    const blob = await exportGantt(selectedProjectId.value)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const contentDisposition = blob.headers?.['content-disposition']
    let fileName = '项目_甘特图.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*?=['"]?(?:UTF-8'')?([^;'\s]+)/i)
      if (fileNameMatch) fileName = decodeURIComponent(fileNameMatch[1])
    }
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadProjectList()
})
</script>

<style scoped>
.project-gantt {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  flex-wrap: nowrap;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: nowrap;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-shrink: 0;
}

.view-mode-group {
  flex-shrink: 0;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.empty-tip {
  padding: 100px 0;
}

.gantt-container {
  height: calc(100vh - 260px);
  overflow: hidden;
}

.gantt-wrapper {
  display: flex;
  height: 100%;
  border: 1px solid #ebeef5;
}

.gantt-fixed-col {
  width: 240px;
  min-width: 240px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.gantt-fixed-col .gantt-header {
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
  flex-shrink: 0;
}

.gantt-fixed-col .gantt-body {
  overflow: hidden;
  flex: 1;
}

.gantt-scroll-col {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.gantt-scroll-col .gantt-header {
  flex-shrink: 0;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
  overflow: hidden;
}

.gantt-scroll-col .gantt-body {
  overflow: auto;
  flex: 1;
}

.gantt-header {
  display: flex;
}

.gantt-task-header {
  width: 240px;
  min-width: 240px;
  padding: 0 10px;
  font-weight: bold;
  flex-shrink: 0;
  height: 41px;
  line-height: 41px;
  box-sizing: border-box;
}

.gantt-timeline-header {
  display: flex;
}

.timeline-cell {
  padding: 0 2px;
  text-align: center;
  font-size: 12px;
  border-right: 1px solid #ebeef5;
  flex-shrink: 0;
  box-sizing: border-box;
  height: 41px;
  line-height: 41px;
}

.timeline-cell.is-today {
  background: #ecf5ff;
  color: #409eff;
}

.timeline-cell.is-weekend {
  background: #fafafa;
  color: #909399;
}

.gantt-body {
  overflow: visible;
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  height: 41px;
  box-sizing: border-box;
}

.gantt-row:hover {
  background: #f5f7fa;
}

.gantt-task-cell {
  width: 240px;
  min-width: 240px;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  height: 41px;
  box-sizing: border-box;
}

/* 树形竖线样式 */
.gantt-task-cell::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 0;
  bottom: 0;
  width: 1px;
  background: #dcdfe6;
}

.gantt-task-cell::after {
  content: '';
  position: absolute;
  left: 10px;
  top: 50%;
  width: 10px;
  height: 1px;
  background: #dcdfe6;
}

/* 最后一个子任务，只显示上半部分竖线 */
.gantt-task-cell.last-child::before {
  height: 50%;
}

/* 根任务不显示竖线 */
.gantt-task-cell.level-1::before,
.gantt-task-cell.level-1::after {
  display: none;
}

/* 不同层级的缩进调整 */
.gantt-task-cell.level-1 { padding-left: 10px; }
.gantt-task-cell.level-2 { padding-left: 30px; }
.gantt-task-cell.level-3 { padding-left: 50px; }
.gantt-task-cell.level-4 { padding-left: 70px; }
.gantt-task-cell.level-5 { padding-left: 90px; }

/* 调整竖线位置以匹配缩进 */
.gantt-task-cell.level-2::before,
.gantt-task-cell.level-2::after { left: 20px; }
.gantt-task-cell.level-3::before,
.gantt-task-cell.level-3::after { left: 40px; }
.gantt-task-cell.level-4::before,
.gantt-task-cell.level-4::after { left: 60px; }
.gantt-task-cell.level-5::before,
.gantt-task-cell.level-5::after { left: 80px; }

.collapse-icon {
  cursor: pointer;
  transition: transform 0.2s;
  flex-shrink: 0;
  color: #909399;
  font-size: 14px;
}

.collapse-icon:hover {
  color: #409eff;
}

.collapse-icon.expanded {
  transform: rotate(90deg);
}

.placeholder-icon {
  width: 14px;
  flex-shrink: 0;
}

.pinned-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.gantt-timeline-cell {
  position: relative;
  display: flex;
  height: 41px;
  box-sizing: border-box;
}

.gantt-bar {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 1;
}

.gantt-bar:hover {
  opacity: 0.8;
  transform: translateY(-50%) scale(1.02);
}

.bar-text {
  font-size: 11px;
  color: #fff;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.gantt-bar.low-progress {
  background: linear-gradient(90deg, #f56c6c 0%, #f56c6c var(--progress), #e4e7ed var(--progress));
}

.gantt-bar.medium-progress {
  background: linear-gradient(90deg, #e6a23c 0%, #e6a23c var(--progress), #e4e7ed var(--progress));
}

.gantt-bar.high-progress {
  background: linear-gradient(90deg, #67c23a 0%, #67c23a var(--progress), #e4e7ed var(--progress));
}

.gantt-bar.completed {
  background: #67c23a;
}

.gantt-bar.paused {
  background: #909399;
}

.pinned-task {
  color: #f56c6c;
  font-weight: bold;
}

.no-data {
  padding: 50px 0;
}
</style>