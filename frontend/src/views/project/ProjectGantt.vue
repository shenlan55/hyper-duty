<template>
  <div class="project-gantt">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甘特图</span>
          <div class="header-actions">
            <el-select v-model="selectedProjectId" placeholder="请选择项目" filterable @change="handleProjectChange">
              <el-option
                v-for="project in projectList"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div v-if="!selectedProjectId" class="empty-tip">
        <el-empty description="请选择项目查看甘特图" />
      </div>

      <div v-else class="gantt-container">
        <div class="gantt-header">
          <div class="gantt-task-header">任务名称</div>
          <div class="gantt-timeline-header">
            <div
              v-for="date in dateList"
              :key="date"
              class="timeline-cell"
              :class="{ 'is-today': isToday(date), 'is-weekend': isWeekend(date) }"
            >
              {{ formatDate(date) }}
            </div>
          </div>
        </div>

        <div class="gantt-body">
          <div
            v-for="task in taskList"
            :key="task.id"
            class="gantt-row"
          >
            <div class="gantt-task-cell" :style="{ paddingLeft: (task.taskLevel - 1) * 20 + 'px' }">
              <span :class="{ 'pinned-task': task.isPinned === 1 }">
                {{ task.taskName }}
              </span>
            </div>
            <div class="gantt-timeline-cell">
              <div
                v-for="date in dateList"
                :key="date"
                class="timeline-cell"
                :class="{ 'is-today': isToday(date), 'is-weekend': isWeekend(date) }"
              />
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
        </div>

        <div v-if="taskList.length === 0" class="no-data">
          <el-empty description="暂无任务数据" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProjectPage } from '@/api/project'
import { getProjectTasks } from '@/api/task'

const selectedProjectId = ref(null)
const projectList = ref([])
const taskList = ref([])

const dateList = computed(() => {
  if (taskList.value.length === 0) return []
  
  let minDate = null
  let maxDate = null
  
  taskList.value.forEach(task => {
    if (task.startDate) {
      const start = new Date(task.startDate)
      if (!minDate || start < minDate) minDate = start
    }
    if (task.endDate) {
      const end = new Date(task.endDate)
      if (!maxDate || end > maxDate) maxDate = end
    }
  })
  
  if (!minDate || !maxDate) return []
  
  minDate = new Date(minDate)
  minDate.setDate(minDate.getDate() - 7)
  
  maxDate = new Date(maxDate)
  maxDate.setDate(maxDate.getDate() + 7)
  
  const dates = []
  const current = new Date(minDate)
  while (current <= maxDate) {
    dates.push(current.toISOString().split('T')[0])
    current.setDate(current.getDate() + 1)
  }
  
  return dates
})

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

const isToday = (dateStr) => {
  return dateStr === new Date().toISOString().split('T')[0]
}

const isWeekend = (dateStr) => {
  const date = new Date(dateStr)
  const day = date.getDay()
  return day === 0 || day === 6
}

const getBarStyle = (task) => {
  if (!task.startDate || !task.endDate || dateList.value.length === 0) return {}
  
  const startIndex = dateList.value.indexOf(task.startDate)
  const endIndex = dateList.value.indexOf(task.endDate)
  
  if (startIndex === -1 || endIndex === -1) return {}
  
  const left = startIndex * 40
  const width = (endIndex - startIndex + 1) * 40
  const progress = task.progress || 0
  
  return {
    left: left + 'px',
    width: width + 'px',
    '--progress': progress + '%'
  }
}

const getBarClass = (task) => {
  if (task.status === 3) return 'completed'
  if (task.status === 4) return 'paused'
  if (task.progress >= 60) return 'high-progress'
  if (task.progress >= 30) return 'medium-progress'
  return 'low-progress'
}

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000 })
    projectList.value = data.records || []
    // 自动选择第一个项目
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
    return
  }
  
  try {
    const data = await getProjectTasks(selectedProjectId.value)
    taskList.value = data || []
  } catch (error) {
    ElMessage.error('加载任务列表失败')
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
}

.header-actions {
  display: flex;
  gap: 10px;
}

.empty-tip {
  padding: 100px 0;
}

.gantt-container {
  overflow-x: auto;
}

.gantt-header {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
  position: sticky;
  top: 0;
  z-index: 10;
}

.gantt-task-header {
  width: 200px;
  min-width: 200px;
  padding: 10px;
  font-weight: bold;
  border-right: 1px solid #ebeef5;
}

.gantt-timeline-header {
  display: flex;
}

.timeline-cell {
  width: 40px;
  min-width: 40px;
  padding: 10px 2px;
  text-align: center;
  font-size: 12px;
  border-right: 1px solid #ebeef5;
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
  max-height: 500px;
  overflow-y: auto;
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.gantt-row:hover {
  background: #f5f7fa;
}

.gantt-task-cell {
  width: 200px;
  min-width: 200px;
  padding: 10px;
  border-right: 1px solid #ebeef5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-timeline-cell {
  position: relative;
  display: flex;
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
  transition: all 0.3s;
}

.gantt-bar:hover {
  opacity: 0.8;
}

.bar-text {
  font-size: 12px;
  color: #fff;
  font-weight: bold;
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
