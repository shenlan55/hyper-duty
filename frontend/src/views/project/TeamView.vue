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
          <el-upload
            class="upload-demo"
            :http-request="handleCustomUpload"
            :file-list="progressUpdateForm.attachments"
            :auto-upload="true"
            :limit="5"
            :before-upload="beforeUpload"
            list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件，且不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <div style="margin: 20px 0; display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="progressUpdateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgressUpdate">确定</el-button>
      </div>

      <!-- 进展历史时间线 -->
      <div style="margin-top: 30px;">
        <h4 style="margin-bottom: 15px;">进展历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(update, index) in progressUpdates"
            :key="index"
            :timestamp="formatDateTime(update.createTime)"
            type="primary"
            placement="top"
          >
            <el-card>
              <div class="update-content">
                <div class="update-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                  <span style="font-weight: bold;">{{ update.employeeName }}</span>
                  <span style="color: #606266;">进度更新至 {{ update.progress }}%</span>
                </div>
                <div class="update-description" v-if="update.description" style="margin-bottom: 10px;" v-html="update.description"></div>
                <div class="update-attachments" v-if="update.attachmentList && update.attachmentList.length > 0">
                  <el-divider content-position="left">附件</el-divider>
                  <div class="attachments-container">
                    <div v-for="(attachment, idx) in update.attachmentList" :key="idx" class="attachment-item">
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
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskDetail from '@/components/TaskDetail.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { getTaskPage, createProgressUpdate, getTaskProgressUpdates } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
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



// 分片上传配置
const CHUNK_SIZE = 1024 * 1024 * 5 // 5MB 分片大小

const handleCustomUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  try {
    // 生成文件唯一标识
    const fileHash = await generateFileHash(file)
    const fileName = file.name
    const fileSize = file.size
    const chunkCount = Math.ceil(fileSize / CHUNK_SIZE)
    
    // 检查文件是否已存在
    const checkResponse = await request.post('/file/check', {
      fileHash,
      fileName,
      fileSize
    })
    
    if (checkResponse && checkResponse.exists) {
      // 文件已存在，直接返回结果
      const uploadedFile = {
        uid: file.uid,
        name: file.name,
        url: checkResponse.fileUrl,
        previewUrl: checkResponse.previewUrl,
        filePath: checkResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 查找并替换文件列表中的文件
      const index = progressUpdateForm.attachments.findIndex(item => item.uid === file.uid)
      if (index !== -1) {
        progressUpdateForm.attachments[index] = uploadedFile
      } else {
        progressUpdateForm.attachments.push(uploadedFile)
      }
      
      ElMessage.success('文件上传成功')
      onSuccess({ 
        status: 'success', 
        data: checkResponse 
      })
      return
    }
    
    // 分片上传
    const uploadedChunks = []
    for (let i = 0; i < chunkCount; i++) {
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, fileSize)
      const chunk = file.slice(start, end)
      
      const formData = new FormData()
      formData.append('file', chunk)
      formData.append('fileHash', fileHash)
      formData.append('fileName', fileName)
      formData.append('chunkIndex', i)
      formData.append('chunkCount', chunkCount)
      formData.append('chunkSize', CHUNK_SIZE)
      
      // 上传分片
      await request.post('/file/upload-chunk', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          const percent = Math.round((i * CHUNK_SIZE + progressEvent.loaded) / fileSize * 100)
          if (onProgress) {
            onProgress({ percent })
          }
        }
      })
      
      uploadedChunks.push(i)
    }
    
    // 合并分片
    const mergeResponse = await request.post('/file/merge', {
      fileHash,
      fileName,
      chunkCount
    })
    
    // 更新文件列表
    if (mergeResponse) {
      const uploadedFile = {
        uid: file.uid,
        name: file.name,
        url: mergeResponse.fileUrl,
        previewUrl: mergeResponse.previewUrl,
        filePath: mergeResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 查找并替换文件列表中的文件
      const index = progressUpdateForm.attachments.findIndex(item => item.uid === file.uid)
      if (index !== -1) {
        progressUpdateForm.attachments[index] = uploadedFile
      } else {
        progressUpdateForm.attachments.push(uploadedFile)
      }
      
      ElMessage.success('文件上传成功')
    }
    
    // 构造el-upload组件期望的响应格式
    onSuccess({ 
      status: 'success', 
      data: mergeResponse 
    })
  } catch (error) {
    console.error('文件上传失败', error)
    ElMessage.error('文件上传失败')
    onError(error)
  }
}

// 生成文件哈希
const generateFileHash = (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const arrayBuffer = e.target.result
      const uint8Array = new Uint8Array(arrayBuffer)
      let hash = 0
      for (let i = 0; i < uint8Array.length; i++) {
        hash = ((hash << 5) - hash) + uint8Array[i]
        hash = hash & hash
      }
      resolve(Math.abs(hash).toString(16) + '_' + Date.now())
    }
    reader.readAsArrayBuffer(file)
  })
}

const beforeUpload = (file) => {
  // 检查文件类型
  const allowedTypes = [
    'image/jpeg', 'image/png', // 图片
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // Word
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // Excel
    'application/pdf', // PDF
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', // PPT
    'application/zip', 'application/x-zip-compressed' // ZIP
  ]
  
  const isAllowedType = allowedTypes.includes(file.type)
  if (!isAllowedType) {
    ElMessage.error('只能上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件')
    return false
  }
  
  // 检查文件大小（增加到10MB）
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

const handleAttachmentPreview = (attachment) => {
  // 强制使用KKFileView预览URL
  if (attachment.previewUrl) {
    window.open(attachment.previewUrl, '_blank')
  } else {
    window.open(attachment.url, '_blank')
  }
}

const handleAttachmentDownload = (attachment) => {
  console.log('下载附件', attachment)
  console.log('原始 URL:', attachment.url)
  console.log('文件名:', attachment.name)
  
  // 将 preview 接口改为 download 接口
  let downloadUrl = attachment.url
  if (downloadUrl.includes('/file/preview')) {
    downloadUrl = downloadUrl.replace('/file/preview', '/file/download')
  }
  
  console.log('替换后 URL:', downloadUrl)
  
  // 如果 URL 中已经有 fileName 参数，先移除它
  if (downloadUrl.includes('fileName=')) {
    const urlObj = new URL(downloadUrl, window.location.origin)
    urlObj.searchParams.delete('fileName')
    downloadUrl = urlObj.toString()
  }
  
  // 添加 fileName 参数
  if (attachment.name) {
    const separator = downloadUrl.includes('?') ? '&' : '?'
    downloadUrl += `${separator}fileName=${encodeURIComponent(attachment.name)}`
  }
  
  console.log('最终下载 URL:', downloadUrl)
  
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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

/* 附件容器，使用block布局支持垂直排列 */
.attachments-container {
  display: block;
  width: 100%;
  box-sizing: border-box;
}

/* 附件项目样式 */
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
}

.file-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.attachment-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}
</style>