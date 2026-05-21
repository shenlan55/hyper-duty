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
            <el-descriptions-item label="是否重点">
              <el-tag v-if="task?.isFocus === 1" type="warning">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </div>

    <!-- 任务描述 -->
    <div class="task-description" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">任务描述</h4>
      <div class="description-content richtext-content" v-if="task?.description" v-html="task.description"></div>
      <div v-else class="no-data">暂无任务描述</div>
    </div>

    <!-- 附件列表 -->
    <div class="task-attachments" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">附件</h4>
      <AttachmentList :attachments="processedTask?.attachments || []" />
    </div>

    <!-- 参与人列表 -->
    <div class="task-stakeholders" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">参与人</h4>
      <div v-if="processedTask?.stakeholders && processedTask.stakeholders.length > 0" class="stakeholders-container">
        <el-tag v-for="(stakeholder, index) in processedTask.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
          {{ stakeholder }}
        </el-tag>
      </div>
      <div v-else class="no-data">暂无参与人</div>
    </div>

    <!-- 影子任务列表 -->
    <div class="task-shadows" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">
        影子任务
        <el-tag type="info" size="small" style="margin-left: 8px;">
          {{ shadowTasks.length }} 个项目
        </el-tag>
      </h4>
      <div v-if="shadowTasks.length > 0" class="shadows-container">
        <el-card v-for="shadow in shadowTasks" :key="shadow.id" class="shadow-card" shadow="hover">
          <div class="shadow-header">
            <el-tag type="warning" size="small">影子</el-tag>
            <span class="shadow-name">{{ shadow.shadowAlias || shadow.sourceTaskName || '影子任务' }}</span>
          </div>
          <div class="shadow-info">
            <div class="info-item">
              <span class="info-label">所属项目：</span>
              <el-tag type="success" size="small">{{ shadow.targetProjectName || shadow.projectName }}</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">创建人：</span>
              <span>{{ shadow.createdByName || shadow.createdBy || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间：</span>
              <span>{{ formatDateTime(shadow.createdAt) }}</span>
            </div>
            <div class="info-item" v-if="shadow.shadowDescription">
              <span class="info-label">影子描述：</span>
              <span>{{ shadow.shadowDescription }}</span>
            </div>
          </div>
        </el-card>
      </div>
      <div v-else class="no-data">暂无影子任务</div>
    </div>

    <!-- 进展历史时间线 -->
    <div style="margin-top: 30px;">
      <ProgressHistory :progress-updates="progressUpdates" />
      <el-empty v-if="progressUpdates.length === 0" description="暂无进展历史" />
    </div>

    <!-- 影子任务批注 -->
    <el-card class="section-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>影子批注</span>
          <el-tag type="info" size="small" style="margin-left: 8px;">
            {{ shadowAnnotations.length }} 条
          </el-tag>
        </div>
      </template>
      <div class="annotation-list">
        <div v-if="shadowAnnotations.length === 0" class="empty-annotations">
          暂无影子批注
        </div>
        <div v-for="annotation in shadowAnnotations" :key="annotation.id" class="annotation-item">
          <div class="annotation-header">
            <div class="annotation-source">
              <el-tag type="warning" size="small">{{ annotation.targetProjectName }}</el-tag>
              <span class="shadow-alias">{{ annotation.shadowAlias || '影子任务' }}</span>
            </div>
            <span class="annotation-author">{{ annotation.createdByName || annotation.createdBy || '未知用户' }}</span>
            <span class="annotation-time">{{ formatDateTime(annotation.createdAt || annotation.createTime) }}</span>
          </div>
          <div class="annotation-content">{{ annotation.content }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getTaskProgressUpdates, getShadowTaskBySource, getShadowAnnotationsBySource } from '@/api/task'
import { getEmployeeList } from '@/api/employee'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, formatDateTime } from '@/utils/taskUtils'
import ProgressHistory from '@/components/ProgressHistory.vue'
import AttachmentList from '@/components/AttachmentList.vue'

const employeeList = ref([])
const shadowTasks = ref([])
const shadowLoading = ref(false)
const shadowAnnotations = ref([])

// 加载影子任务
const loadShadowTasks = async (taskId) => {
  if (!taskId) return
  
  shadowLoading.value = true
  try {
    const data = await getShadowTaskBySource(taskId)
    shadowTasks.value = data || []
  } catch (error) {
    console.error('加载影子任务失败', error)
    shadowTasks.value = []
  } finally {
    shadowLoading.value = false
  }
}

// 加载影子批注
const loadShadowAnnotations = async (taskId) => {
  if (!taskId) return
  
  try {
    const data = await getShadowAnnotationsBySource(taskId)
    shadowAnnotations.value = data || []
  } catch (error) {
    console.error('加载影子批注失败', error)
    shadowAnnotations.value = []
  }
}

// 加载员工列表
const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000)
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

// 处理后的任务数据
const processedTask = computed(() => {
  if (!props.task) {
    return null
  }
  
  const task = { ...props.task }
  
  // 处理附件数据
  if (task.attachments) {
    if (typeof task.attachments === 'string') {
      try {
        task.attachments = JSON.parse(task.attachments)
      } catch (error) {
        console.error('解析附件数据失败', error)
        task.attachments = []
      }
    } else if (!Array.isArray(task.attachments)) {
      task.attachments = []
    }
  } else {
    task.attachments = []
  }
  
  // 处理参与人数据
  if (task.stakeholders) {
    if (typeof task.stakeholders === 'string') {
      try {
        task.stakeholders = JSON.parse(task.stakeholders)
      } catch (error) {
        console.error('解析参与人数据失败', error)
        task.stakeholders = []
      }
    } else if (!Array.isArray(task.stakeholders)) {
      task.stakeholders = []
    }
  } else {
    task.stakeholders = []
  }
  
  // 转换参与人ID为名称
  if (task.stakeholders && Array.isArray(task.stakeholders)) {
    task.stakeholders = task.stakeholders.map(stakeholderId => {
      const employee = employeeList.value.find(emp => {
        // 考虑ID类型不匹配的情况，进行类型转换后比较
        return String(emp.id) === String(stakeholderId)
      })
      return employee ? employee.employeeName : stakeholderId
    })
  }
  
  return task
})

// 加载进度更新
const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载进度更新失败', error)
  }
}

// 监听任务变化，加载进度更新、影子任务和影子批注
watch(
  () => props.task,
  (newTask) => {
    if (newTask?.id) {
      loadProgressUpdates(newTask.id)
      loadShadowTasks(newTask.id)
      loadShadowAnnotations(newTask.id)
    }
  },
  { immediate: true }
)

// 监听任务ID变化，加载进度更新、影子任务和影子批注
watch(
  () => props.taskId,
  (newTaskId) => {
    if (newTaskId) {
      loadProgressUpdates(newTaskId)
      loadShadowTasks(newTaskId)
      loadShadowAnnotations(newTaskId)
    }
  },
  { immediate: true }
)

// 监听员工列表变化，重新转换参与人名称
watch(
  () => employeeList.value,
  () => {
    if (props.task && props.task.stakeholders && Array.isArray(props.task.stakeholders)) {
      // 无论参与人是什么格式，都尝试转换为名称
      // 检查是否需要转换：如果有参与人是数字ID（字符串或数字形式），则进行转换
      const needConversion = props.task.stakeholders.some(stakeholder => {
        // 判断是否为数字ID（可以是数字或数字字符串）
        return !isNaN(Number(stakeholder))
      })
      
      if (needConversion) {
        props.task.stakeholders = props.task.stakeholders.map(stakeholderId => {
          const employee = employeeList.value.find(emp => {
            // 考虑ID类型不匹配的情况，进行类型转换后比较
            return String(emp.id) === String(stakeholderId)
          })
          return employee ? employee.employeeName : stakeholderId
        })
      }
    }
  },
  { deep: true, immediate: true }
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

/* 暂无数据样式 */
.no-data {
  padding: 10px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

/* 绑定数据样式 */
.bindings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.binding-item {
  transition: all 0.3s;
}

.binding-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.binding-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.binding-table-name {
  font-weight: bold;
  color: #409EFF;
  font-size: 14px;
}

.binding-time {
  font-size: 12px;
  color: #909399;
}

.binding-data {
  margin-bottom: 15px;
}

/* 影子任务样式 */
.shadows-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shadow-card {
  padding: 16px;
  background-color: #fffbeb;
  border: 1px solid #f59e0b;
}

.shadow-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.shadow-name {
  font-size: 16px;
  font-weight: 600;
  color: #92400e;
}

.shadow-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #78350f;
}

.info-label {
  font-weight: 500;
  color: #451a03;
}

.source-task-info {
  padding-top: 8px;
}

.description-text {
  display: block;
  margin-left: 8px;
  color: #606266;
  line-height: 1.6;
}

/* 影子批注样式 */
.card-header {
  display: flex;
  align-items: center;
}

.annotation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-annotations {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.annotation-item {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #f59e0b;
}

.annotation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.annotation-source {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shadow-alias {
  font-weight: 500;
  color: #92400e;
}

.annotation-author {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.annotation-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.annotation-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
</style>