<template>
  <div class="my-task">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-value">{{ stats.total }}</div>
            <div class="stats-label">总任务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-value pending">{{ stats.pending }}</div>
            <div class="stats-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-value in-progress">{{ stats.inProgress }}</div>
            <div class="stats-label">进行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-value completed">{{ stats.completed }}</div>
            <div class="stats-label">已完成</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的任务</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部任务" name="all" />
        <el-tab-pane label="待处理" name="pending" />
        <el-tab-pane label="进行中" name="inProgress" />
        <el-tab-pane label="即将到期" name="upcoming" />
      </el-tabs>

      <BaseTable
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="{
          currentPage: pagination.currentPage,
          pageSize: pagination.pageSize,
          pageSizes: pagination.pageSizes,
          total: pagination.total
        }"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #taskName="{ row }">
          <span :class="{ 'pinned-task': row.isPinned === 1 }">
            <el-icon v-if="row.isPinned === 1" style="color: #f56c6c; margin-right: 4px;"><Star /></el-icon>
            {{ row.taskName }}
          </span>
        </template>
        <template #progress="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
        </template>
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
        <template #priority="{ row }">
          <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
        </template>
        <template #endDate="{ row }">
          <span :class="{ 'overdue': isOverdue(row) }">{{ row.endDate }}</span>
        </template>
        <template #operation="{ row }">
          <el-button link type="primary" @click="handleUpdateProgress(row)">更新进度</el-button>
          <el-button link type="warning" @click="handlePin(row)">
            {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="progressDialogVisible"
      title="更新进度"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="任务名称">
          <el-input :value="progressForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="当前进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import { getMyTasks, updateProgress, pinTask, getUpcomingTasks } from '@/api/task'
import { getCurrentUserId } from '@/utils/jwt'

const loading = ref(false)
const tableData = ref([])
const progressDialogVisible = ref(false)
const activeTab = ref('all')

const stats = reactive({
  total: 0,
  pending: 0,
  inProgress: 0,
  completed: 0
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const progressForm = reactive({
  taskId: null,
  taskName: '',
  progress: 0
})

const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 200, slot: 'taskName' },
  { prop: 'projectName', label: '所属项目', width: 120 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110, slot: 'endDate' },
  { prop: 'operation', label: '操作', width: 160, fixed: 'right', slot: 'operation' }
]

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

const isOverdue = (row) => {
  if (!row.endDate || row.status === 3) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const endDate = new Date(row.endDate)
  return endDate < today
}

const getCurrentEmployeeId = () => {
  return getCurrentUserId()
}

const loadData = async () => {
  loading.value = true
  try {
    const employeeId = getCurrentEmployeeId()
    if (!employeeId) {
      ElMessage.error('无法获取用户信息')
      return
    }

    let data = []
    if (activeTab.value === 'upcoming') {
      data = await getUpcomingTasks()
    } else {
      data = await getMyTasks(employeeId)
    }

    if (activeTab.value === 'pending') {
      data = data.filter(task => task.status === 1)
    } else if (activeTab.value === 'inProgress') {
      data = data.filter(task => task.status === 2)
    }

    tableData.value = data
    pagination.total = data.length

    updateStats(data)
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateStats = (data) => {
  stats.total = data.length
  stats.pending = data.filter(task => task.status === 1).length
  stats.inProgress = data.filter(task => task.status === 2).length
  stats.completed = data.filter(task => task.status === 3).length
}

const handleTabChange = () => {
  pagination.currentPage = 1
  loadData()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
}

const handleUpdateProgress = (row) => {
  progressForm.taskId = row.id
  progressForm.taskName = row.taskName
  progressForm.progress = row.progress || 0
  progressDialogVisible.value = true
}

const handleSubmitProgress = async () => {
  try {
    await updateProgress(progressForm.taskId, progressForm.progress)
    ElMessage.success('更新进度成功')
    progressDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('更新进度失败')
  }
}

const handlePin = async (row) => {
  try {
    const pinned = row.isPinned === 1 ? false : true
    await pinTask(row.id, pinned)
    ElMessage.success(pinned ? '置顶成功' : '取消置顶成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-task {
  padding: 20px;
}

.stats-card {
  text-align: center;
}

.stats-item {
  padding: 10px 0;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stats-value.pending {
  color: #909399;
}

.stats-value.in-progress {
  color: #e6a23c;
}

.stats-value.completed {
  color: #67c23a;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pinned-task {
  color: #f56c6c;
  font-weight: bold;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}
</style>
