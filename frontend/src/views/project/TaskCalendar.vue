<template>
  <div class="task-calendar">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务日历</span>
          <div class="header-actions">
            <el-select v-model="selectedProject" placeholder="选择项目" clearable @change="handleProjectChange">
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

      <div class="calendar-container">
        <el-calendar v-model="currentDate">
          <template #dateCell="{ date, data }">
            <div class="calendar-cell">
              <span class="date">{{ data.day.split('-').slice(1).join('-') }}</span>
              <div class="tasks">
                <el-tag 
                  v-for="task in getTasksByDate(date)" 
                  :key="task.id" 
                  :type="getTaskType(task.priority)"
                  size="small"
                  class="task-tag"
                  @click="handleViewTask(task)"
                >
                  {{ task.taskName }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="`任务详情 - ${selectedTask?.taskName || ''}`"
      width="1500px"
      max-width="1500px"
    >
      <TaskDetail v-if="selectedTask" :task="selectedTask" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskPage } from '@/api/task'
import { getProjectPage } from '@/api/project'
import TaskDetail from '@/components/TaskDetail.vue'

const currentDate = ref(new Date())
const selectedProject = ref(null)
const projectList = ref([])
const tasks = ref([])
const taskDialogVisible = ref(false)
const selectedTask = ref(null)

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000 })
    projectList.value = data.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadTasks = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000,
      projectId: selectedProject.value
    }
    const data = await getTaskPage(params)
    tasks.value = data.records || []
  } catch (error) {
    console.error('加载任务列表失败', error)
  }
}

const handleProjectChange = () => {
  loadTasks()
}

const getTasksByDate = (date) => {
  const dateStr = date.format('YYYY-MM-DD')
  return tasks.value.filter(task => {
    return task.startDate === dateStr || task.endDate === dateStr
  })
}

const getTaskType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { 1: '高', 2: '中', 3: '低' }
  return texts[priority] || '未知'
}

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'primary', 3: 'success', 4: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '未开始', 2: '进行中', 3: '已完成', 4: '已暂停' }
  return texts[status] || '未知'
}

const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 60) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

const handleViewTask = (task) => {
  selectedTask.value = task
  taskDialogVisible.value = true
}

onMounted(() => {
  loadProjectList()
  loadTasks()
})
</script>

<style scoped>
.task-calendar {
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

.calendar-container {
  margin-top: 20px;
}

.calendar-cell {
  position: relative;
  height: 120px;
  padding: 8px;
}

.date {
  font-size: 14px;
  font-weight: bold;
}

.tasks {
  margin-top: 8px;
  max-height: 90px;
  overflow-y: auto;
}

.task-tag {
  display: block;
  margin: 4px 0;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-detail {
  padding: 10px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  max-width: 1460px;
  margin: 0 auto;
}

/* 任务信息面板样式 */
.task-info-panel {
  width: 100%;
  box-sizing: border-box;
}

/* 表单样式 */
:deep(el-form) {
  width: 100%;
  box-sizing: border-box;
}

:deep(el-form-item) {
  width: 100%;
  box-sizing: border-box;
}

/* 富文本编辑器样式 */
:deep(.rich-text-editor) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.editor-container) {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 附件上传样式 */
:deep(.el-upload) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.el-upload-list) {
  width: 100%;
  box-sizing: border-box;
}

/* 对话框内容样式，防止横向滚动 */
:deep(.el-dialog__body) {
  overflow-x: hidden !important;
  padding: 20px;
}
</style>