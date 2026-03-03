<template>
  <div class="task-detail">
    <!-- 任务基本信息（只读） -->
    <div class="task-info-panel" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
      <h4 style="margin-bottom: 10px;">任务信息</h4>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="任务名称">{{ task?.taskName }}</el-descriptions-item>
            <el-descriptions-item label="所属项目">{{ task?.projectName }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ task?.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ task?.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ task?.endDate }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="12">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="优先级">{{ getTaskPriorityText(task?.priority) }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ getTaskStatusText(task?.status) }}</el-descriptions-item>
            <el-descriptions-item label="当前进度">{{ task?.progress }}%</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </div>

    <!-- 任务描述 -->
    <div class="task-description" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">任务描述</h4>
      <div class="description-content" v-if="task?.description" v-html="task.description"></div>
      <el-empty v-else description="暂无任务描述" />
    </div>

    <!-- 附件列表 -->
    <div class="task-attachments" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">附件</h4>
      <div v-if="task?.attachments && task.attachments.length > 0" class="attachments-container">
        <div v-for="(attachment, index) in task.attachments" :key="index" class="attachment-item">
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
      <el-empty v-else description="暂无附件" />
    </div>

    <!-- 干系人列表 -->
    <div class="task-stakeholders" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">干系人</h4>
      <div v-if="task?.stakeholders && task.stakeholders.length > 0" class="stakeholders-container">
        <el-tag v-for="(stakeholder, index) in task.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
          {{ stakeholder }}
        </el-tag>
      </div>
      <el-empty v-else description="暂无干系人" />
    </div>

    <!-- 进展历史时间线 -->
    <div style="margin-top: 30px;">
      <ProgressHistory :progress-updates="progressUpdates" />
      <el-empty v-if="progressUpdates.length === 0" description="暂无进展历史" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getTaskProgressUpdates } from '@/api/task'
import { getEmployeeList } from '@/api/employee'
import { getTaskStatusText, getTaskPriorityText } from '@/utils/taskUtils'
import ProgressHistory from '@/components/ProgressHistory.vue'

const employeeList = ref([])

// 加载员工列表
const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList()
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

// 组件挂载时加载员工列表
onMounted(() => {
  loadEmployeeList()
})

const props = defineProps({
  task: {
    type: Object,
    default: null
  },
  taskId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['close'])

const progressUpdates = ref([])

// 加载进度更新
const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载进度更新失败', error)
  }
}

// 监听任务变化，加载进度更新
watch(
  () => props.task,
  (newTask) => {
    if (newTask?.id) {
      loadProgressUpdates(newTask.id)
      // 转换干系人ID为名称
      if (newTask.stakeholders && Array.isArray(newTask.stakeholders)) {
        newTask.stakeholders = newTask.stakeholders.map(stakeholderId => {
          const employee = employeeList.value.find(emp => emp.id === stakeholderId)
          return employee ? employee.employeeName : stakeholderId
        })
      }
    }
  },
  { immediate: true }
)

// 监听任务ID变化，加载进度更新
watch(
  () => props.taskId,
  (newTaskId) => {
    if (newTaskId) {
      loadProgressUpdates(newTaskId)
    }
  },
  { immediate: true }
)

// 监听员工列表变化，重新转换干系人名称
watch(
  () => employeeList.value,
  () => {
    if (props.task && props.task.stakeholders && Array.isArray(props.task.stakeholders)) {
      props.task.stakeholders = props.task.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
  },
  { deep: true }
)

// 处理附件预览
const handleAttachmentPreview = (attachment) => {
  if (attachment.previewUrl) {
    window.open(attachment.previewUrl, '_blank')
  } else {
    window.open(attachment.url, '_blank')
  }
}

// 处理附件下载
const handleAttachmentDownload = (attachment) => {
  let downloadUrl = attachment.url
  if (downloadUrl.includes('/file/preview')) {
    downloadUrl = downloadUrl.replace('/file/preview', '/file/download')
  }
  
  if (downloadUrl.includes('fileName=')) {
    const urlObj = new URL(downloadUrl, window.location.origin)
    urlObj.searchParams.delete('fileName')
    downloadUrl = urlObj.toString()
  }
  
  if (attachment.name) {
    const separator = downloadUrl.includes('?') ? '&' : '?'
    downloadUrl += `${separator}fileName=${encodeURIComponent(attachment.name)}`
  }
  
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

</script>

<style scoped>
.task-detail {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  max-width: 1460px;
  margin: 0 auto;
}

/* 任务描述样式 */
.description-content {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  width: 100%;
  box-sizing: border-box;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.description-content img {
  max-width: 100% !important;
  height: auto !important;
  display: block !important;
  margin: 0 auto !important;
  box-sizing: border-box !important;
}

.description-content table {
  max-width: 100% !important;
  overflow-x: auto !important;
  display: block !important;
  box-sizing: border-box !important;
}

/* 附件容器样式 */
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

.attachment-actions .el-button {
  padding: 0 12px;
}

/* 干系人容器样式 */
.stakeholders-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

/* 确保响应式布局 */
@media screen and (max-width: 768px) {
  .task-info-panel .el-row {
    flex-direction: column;
  }
  
  .task-info-panel .el-col {
    width: 100% !important;
    margin-bottom: 10px;
  }
  
  .attachment-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .attachment-info {
    width: 100%;
    justify-content: space-between;
  }
}
</style>