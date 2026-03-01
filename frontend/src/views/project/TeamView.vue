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
              :show-pagination="false"
            >
              <template #progress="{ row }">
                <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
              </template>
              <template #status="{ row }">
                <el-tag :type="getTaskStatusType(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
              </template>
              <template #priority="{ row }">
                <el-tag :type="getTaskPriorityType(row.priority)">{{ getTaskPriorityText(row.priority) }}</el-tag>
              </template>
              <template #operation="{ row }">
                <el-button type="primary" size="small" @click="handleEditTask(row)">编辑</el-button>
                <el-button type="success" size="small" @click="handleUpdateProgress(row)">更新进度</el-button>
                <el-button type="info" size="small" @click="handleViewTask(row)">查看</el-button>
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
      width="1500px"
      max-width="1500px"
    >
      <TaskDetail v-if="selectedTask" :task="selectedTask" />
    </el-dialog>

    <!-- 进度更新对话框 -->
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

      <!-- 进展历史 -->
      <div style="margin-top: 30px;">
        <ProgressHistory :progress-updates="progressUpdates" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import TaskDetail from '@/components/TaskDetail.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileUpload from '@/components/FileUpload.vue'
import ProgressHistory from '@/components/ProgressHistory.vue'
import { getTaskPage, createProgressUpdate, getTaskProgressUpdates } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus, formatDateTime } from '@/utils/taskUtils'

const loading = ref(false)
const selectedProject = ref(null)
const projectList = ref([])
const teamMembers = ref([])
const tasks = ref([])
const taskDialogVisible = ref(false)
const selectedTask = ref(null)
const userStore = useUserStore()
const progressUpdateDialogVisible = ref(false)
const currentTaskForUpdate = ref(null)
const progressUpdateForm = reactive({
  taskId: null,
  progress: 0,
  description: '',
  attachments: []
})
const progressUpdates = ref([])

const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 200 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'operation', label: '操作', width: 410, fixed: 'right', slot: 'operation' }
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



const handleViewTask = (row) => {
  selectedTask.value = row
  taskDialogVisible.value = true
}

const handleEditTask = (row) => {
  // 跳转到任务编辑页面
  // 实际项目中应该使用路由跳转
  ElMessage.info('编辑功能待实现')
}

const handleUpdateProgress = async (row) => {
  currentTaskForUpdate.value = row
  progressUpdateForm.taskId = row.id
  progressUpdateForm.progress = row.progress || 0
  progressUpdateForm.description = ''
  progressUpdateForm.attachments = []
  
  // 加载任务的进展历史
  await loadProgressUpdates(row.id)
  
  progressUpdateDialogVisible.value = true
}

const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载任务进展历史失败', error)
    ElMessage.error('加载任务进展历史失败')
  }
}

const handleSubmitProgressUpdate = async () => {
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
      taskId: progressUpdateForm.taskId,
      employeeId: userStore.employeeId,
      progress: progressUpdateForm.progress,
      description: progressUpdateForm.description || ''
    }
    
    // 只有当有附件时才添加 attachments 字段
    if (attachmentsJson !== null) {
      updateData.attachments = attachmentsJson
    }
    
    await createProgressUpdate(updateData)
    ElMessage.success('更新任务进展成功')
    
    // 重新加载数据
    progressUpdateDialogVisible.value = false
    loadTasks()
  } catch (error) {
    console.error('更新任务进展失败', error)
    ElMessage.error('更新任务进展失败')
  }
}



const handleUploadSuccess = (uploadedFile) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
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
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 确保富文本内容中的图片自适应容器宽度 */
.task-detail img {
  max-width: 100% !important;
  height: auto !important;
  display: block !important;
  margin: 0 auto !important;
  box-sizing: border-box !important;
}

/* 确保容器宽度自适应 */
:deep(.el-descriptions) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.el-descriptions__body) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.el-descriptions__cell) {
  box-sizing: border-box;
  overflow-x: hidden;
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