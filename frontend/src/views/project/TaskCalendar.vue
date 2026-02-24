<template>
  <div class="task-calendar">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务日历</span>
          <el-select v-model="selectedProject" placeholder="选择项目" clearable @change="handleProjectChange">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
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
      width="600px"
    >
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务名称">{{ selectedTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ selectedTask.projectName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getTaskType(selectedTask.priority)">{{ getPriorityText(selectedTask.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedTask.status)">{{ getStatusText(selectedTask.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="进度">
            <el-progress :percentage="selectedTask.progress" :status="getProgressStatus(selectedTask.progress)" />
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ selectedTask.ownerName }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ selectedTask.startDate }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ selectedTask.endDate }}</el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ selectedTask.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskPage } from '@/api/task'
import { getProjectPage } from '@/api/project'

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
}
</style>