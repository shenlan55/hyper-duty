<template>
  <div class="team-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>团队视图</span>
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

      <div class="team-container">
        <el-tabs type="border-card">
          <el-tab-pane 
            v-for="member in teamMembers" 
            :key="member.id"
            :label="`${member.employeeName} (${getMemberTaskCount(member.id)})`"
          >
            <BaseTable
              :data="getMemberTasks(member.id)"
              :columns="columns"
              :loading="loading"
              :pagination="false"
            >
              <template #progress="{ row }">
                <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
              </template>
              <template #status="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
              <template #priority="{ row }">
                <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
              </template>
              <template #operation="{ row }">
                <el-button link type="primary" @click="handleViewTask(row)">查看</el-button>
                <el-button link type="primary" @click="handleEditTask(row)">编辑</el-button>
              </template>
            </BaseTable>
          </el-tab-pane>
        </el-tabs>
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
            <el-tag :type="getPriorityType(selectedTask.priority)">{{ getPriorityText(selectedTask.priority) }}</el-tag>
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
import BaseTable from '@/components/BaseTable.vue'
import { getTaskPage } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'

const loading = ref(false)
const selectedProject = ref(null)
const projectList = ref([])
const teamMembers = ref([])
const tasks = ref([])
const taskDialogVisible = ref(false)
const selectedTask = ref(null)

const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 200 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'operation', label: '操作', width: 150, fixed: 'right', slot: 'operation' }
]

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000 })
    projectList.value = data.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadTeamMembers = async () => {
  try {
    const data = await getEmployeeList()
    teamMembers.value = data?.records || []
  } catch (error) {
    console.error('加载团队成员失败', error)
  }
}

const loadTasks = async () => {
  loading.value = true
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
    ElMessage.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

const handleProjectChange = () => {
  loadTasks()
}

const getMemberTasks = (memberId) => {
  return tasks.value.filter(task => task.assigneeId === memberId || task.ownerId === memberId)
}

const getMemberTaskCount = (memberId) => {
  return getMemberTasks(memberId).length
}

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'primary', 3: 'success', 4: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '未开始', 2: '进行中', 3: '已完成', 4: '已暂停' }
  return texts[status] || '未知'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { 1: '高', 2: '中', 3: '低' }
  return texts[priority] || '未知'
}

const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 60) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

const handleViewTask = (row) => {
  selectedTask.value = row
  taskDialogVisible.value = true
}

const handleEditTask = (row) => {
  // 跳转到任务编辑页面
  // 实际项目中应该使用路由跳转
  ElMessage.info('编辑功能待实现')
}

onMounted(() => {
  loadProjectList()
  loadTeamMembers()
  loadTasks()
})
</script>

<style scoped>
.team-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-container {
  margin-top: 20px;
}

.task-detail {
  padding: 10px;
}
</style>