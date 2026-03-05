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
          <el-select v-model="selectedProjectId" placeholder="选择项目" style="width: 200px; margin-left: 20px;" @change="handleProjectChange">
            <el-option label="所有项目" value="0" />
            <el-option v-for="project in projects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
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
          <el-tag :type="getTaskStatusType(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
        </template>
        <template #priority="{ row }">
          <el-tag :type="getTaskPriorityType(row.priority)">{{ getTaskPriorityText(row.priority) }}</el-tag>
        </template>
        <template #endDate="{ row }">
          <span :class="{ 'overdue': isOverdue(row) }">{{ row.endDate }}</span>
        </template>
        <template #operation="{ row }">
          <el-button type="success" size="small" @click="handleUpdateProgress(row)">更新进度</el-button>
          <el-button type="warning" size="small" @click="handlePin(row)" style="width: 70px;">
            {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="progressUpdateDialogVisible"
      :title="`更新任务进展 - ${currentTaskForUpdate?.taskName || ''}`"
      width="1500px"
    >
      <!-- 任务基本信息（只读） -->
      <div class="task-info-panel" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
        <h4 style="margin-bottom: 10px;">任务信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="任务名称">{{ currentTaskForUpdate?.taskName }}</el-descriptions-item>
              <el-descriptions-item label="所属项目">{{ currentTaskForUpdate?.projectName }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ currentTaskForUpdate?.ownerName }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="优先级">{{ getTaskPriorityText(currentTaskForUpdate?.priority) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ getTaskStatusText(currentTaskForUpdate?.status) }}</el-descriptions-item>
              <el-descriptions-item label="当前进度">{{ currentTaskForUpdate?.progress }}%</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>

      <!-- 任务描述 -->
      <div class="task-description" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">任务描述</h4>
        <div class="description-content" v-if="currentTaskForUpdate?.description" v-html="currentTaskForUpdate.description"></div>
        <div v-else class="no-data">暂无任务描述</div>
      </div>

      <!-- 附件列表 -->
      <div class="task-attachments" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">附件</h4>
        <div v-if="currentTaskForUpdate?.attachments && currentTaskForUpdate.attachments.length > 0" class="attachments-container">
          <div v-for="(attachment, index) in currentTaskForUpdate.attachments" :key="index" class="attachment-item">
            <div class="attachment-file">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name">{{ attachment.name || '未知文件' }}</span>
            </div>
            <div class="attachment-info">
              <span class="attachment-name">{{ attachment.name || '未知文件' }}</span>
              <div class="attachment-actions">
                <el-button size="small" type="primary" @click="handleAttachmentPreview(attachment)">
                  预览
                </el-button>
                <el-button size="small" @click="handleAttachmentDownload(attachment)">
                  下载
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无附件</div>
      </div>

      <!-- 参与人列表 -->
      <div class="project-stakeholders" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">参与人</h4>
        <div v-if="currentTaskForUpdate?.stakeholders && currentTaskForUpdate.stakeholders.length > 0" class="stakeholders-container">
          <el-tag v-for="(stakeholder, index) in currentTaskForUpdate.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
            {{ stakeholder }}
          </el-tag>
        </div>
        <div v-else class="no-data">暂无参与人</div>
      </div>

      <!-- 进度更新表单 -->
      <el-form label-width="100px">
        <el-form-item label="更新进度">
          <el-slider v-model="progressUpdateForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="进展描述">
          <RichTextEditor v-model="progressUpdateForm.description" placeholder="请输入进展描述" />
        </el-form-item>
        <el-form-item label="附件">
          <FileUpload
            v-model:fileList="progressUpdateForm.attachments"
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
          />
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <div style="margin: 20px 0; display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="progressUpdateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgressUpdate">确定</el-button>
      </div>

      <!-- 进展历史时间线 -->
      <div style="margin-top: 30px;">
        <ProgressHistory :progress-updates="progressUpdates" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Document } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import ProgressHistory from '@/components/ProgressHistory.vue'
import FileUpload from '@/components/FileUpload.vue'
import { getMyTasks, getMyTasksByProject, pinTask, getUpcomingTasks, createProgressUpdate, getTaskProgressUpdates, getTaskDetail } from '@/api/task'
import { getMyProjects } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { getCurrentUserId } from '@/utils/jwt'
import { useUserStore } from '@/stores/user'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, formatDateTime, getProgressStatus } from '@/utils/taskUtils'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const progressUpdateDialogVisible = ref(false)
const activeTab = ref('all')
const projects = ref([])
const selectedProjectId = ref('0')
const employeeList = ref([])

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

const currentTaskForUpdate = ref(null)
const progressUpdateForm = reactive({
  progress: 0,
  description: '',
  attachments: []
})
const progressUpdates = ref([])

const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 150, slot: 'taskName' },
  { prop: 'projectName', label: '所属项目', width: 180 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110, slot: 'endDate' },
  { prop: 'operation', label: '操作', width: 220, fixed: 'right', slot: 'operation' }
]



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

const loadProjects = async () => {
  try {
    const employeeId = getCurrentEmployeeId()
    if (!employeeId) {
      ElMessage.error('无法获取用户信息')
      return
    }
    const projectList = await getMyProjects(employeeId)
    projects.value = projectList
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000) // 获取足够多的员工数据
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
    employeeList.value = []
  }
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
      if (selectedProjectId.value === '0') {
        data = await getMyTasks(employeeId)
      } else {
        data = await getMyTasksByProject(employeeId, selectedProjectId.value)
      }
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

const handleProjectChange = () => {
  pagination.currentPage = 1
  loadData()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
}

const handleUpdateProgress = async (row) => {
  try {
    // 获取完整的任务详情数据
    const taskDetail = await getTaskDetail(row.id)
    
    // 处理附件数据
    if (taskDetail.attachments) {
      if (typeof taskDetail.attachments === 'string') {
        try {
          taskDetail.attachments = JSON.parse(taskDetail.attachments)
        } catch (error) {
          console.error('解析附件数据失败', error)
          taskDetail.attachments = []
        }
      } else if (!Array.isArray(taskDetail.attachments)) {
        taskDetail.attachments = []
      }
    } else {
      taskDetail.attachments = []
    }
    
    // 处理参与人数据
    if (taskDetail.stakeholders) {
      if (typeof taskDetail.stakeholders === 'string') {
        try {
          taskDetail.stakeholders = JSON.parse(taskDetail.stakeholders)
        } catch (error) {
          console.error('解析参与人数据失败', error)
          taskDetail.stakeholders = []
        }
      } else if (!Array.isArray(taskDetail.stakeholders)) {
        taskDetail.stakeholders = []
      }
    } else {
      taskDetail.stakeholders = []
    }
    
    // 将参与人ID转换为名称
    if (taskDetail.stakeholders && Array.isArray(taskDetail.stakeholders)) {
      taskDetail.stakeholders = taskDetail.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
    
    currentTaskForUpdate.value = taskDetail
    progressUpdateForm.progress = taskDetail.progress || 0
    progressUpdateForm.description = ''
    progressUpdateForm.attachments = []
    
    // 加载进度更新历史
    try {
      const updates = await getTaskProgressUpdates(row.id)
      progressUpdates.value = updates || []
    } catch (error) {
      console.error('加载进度更新历史失败', error)
    }
    
    progressUpdateDialogVisible.value = true
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败')
  }
}

const handleSubmitProgressUpdate = async () => {
  if (!currentTaskForUpdate.value) return
  
  try {
    // 准备附件数据
    let attachmentsJson = null
    if (progressUpdateForm.attachments && progressUpdateForm.attachments.length > 0) {
      const attachments = progressUpdateForm.attachments.map(file => ({
        name: file.name,
        url: file.url || '',
        previewUrl: file.previewUrl || '',
        type: file.type,
        size: file.size
      }))
      attachmentsJson = JSON.stringify(attachments)
    }
    
    // 提交进展更新
    const updateData = {
      taskId: currentTaskForUpdate.value.id,
      employeeId: userStore.employeeId,
      progress: progressUpdateForm.progress,
      description: progressUpdateForm.description || ''
    }
    
    // 只有当有附件时才添加 attachments 字段
    if (attachmentsJson !== null) {
      updateData.attachments = attachmentsJson
    }
    
    await createProgressUpdate(updateData)
    ElMessage.success('更新进度成功')
    progressUpdateDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新任务进展失败', error)
    ElMessage.error('更新进度失败')
  }
}

const handleUploadSuccess = (uploadedFile) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
}

const handleAttachmentPreview = (attachment) => {
  if (attachment.url) {
    window.open(attachment.url, '_blank')
  } else if (attachment.fileUrl) {
    window.open(attachment.fileUrl, '_blank')
  } else {
    ElMessage.warning('附件预览失败，文件路径不存在')
  }
}

const handleAttachmentDownload = (attachment) => {
  if (attachment.url) {
    window.open(attachment.url, '_blank')
  } else if (attachment.fileUrl) {
    window.open(attachment.fileUrl, '_blank')
  } else {
    ElMessage.warning('附件下载失败，文件路径不存在')
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
  loadProjects()
  loadEmployeeList()
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

.pinned-task.overdue {
  color: #f56c6c;
  font-weight: bold;
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

/* 附件样式 */
.attachments-container {
  display: block;
  width: 100%;
  box-sizing: border-box;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.attachment-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.attachment-file {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.file-icon {
  color: #409EFF;
  font-size: 20px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.attachment-name {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.attachment-actions {
  display: flex;
  gap: 5px;
}

/* 参与人样式 */
.stakeholders-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
