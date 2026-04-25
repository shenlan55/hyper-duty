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
      <div class="description-content" v-if="task?.description" v-html="task.description"></div>
      <div v-else class="no-data">暂无任务描述</div>
    </div>

    <!-- 附件列表 -->
    <div class="task-attachments" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">附件</h4>
      <AttachmentList :attachments="task?.attachments || []" />
    </div>

    <!-- 参与人列表 -->
    <div class="task-stakeholders" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">参与人</h4>
      <div v-if="task?.stakeholders && task.stakeholders.length > 0" class="stakeholders-container">
        <el-tag v-for="(stakeholder, index) in task.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
          {{ stakeholder }}
        </el-tag>
      </div>
      <div v-else class="no-data">暂无参与人</div>
    </div>

    <!-- 绑定数据 -->
    <div class="task-bindings" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">绑定数据</h4>
      <div v-if="taskBindings.length > 0" class="bindings-list">
        <el-card v-for="binding in taskBindings" :key="binding.id" class="binding-item">
          <div class="binding-info">
            <span class="binding-table-name">{{ binding.tableName }}</span>
            <span class="binding-time">{{ formatDateTime(binding.createTime) }}</span>
          </div>
          <div class="binding-data">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item 
                v-for="(value, key) in binding.rowData" 
                :key="key" 
                :label="key"
              >
                {{ value }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
      <div v-else class="no-data">暂无绑定数据</div>
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
import { getTaskBindings } from '@/api/customTable'
import { getTaskStatusText, getTaskPriorityText, formatDateTime } from '@/utils/taskUtils'
import ProgressHistory from '@/components/ProgressHistory.vue'
import AttachmentList from '@/components/AttachmentList.vue'

const employeeList = ref([])

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
const taskBindings = ref([])

// 加载进度更新
const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载进度更新失败', error)
  }
}

// 加载绑定数据
const loadTaskBindings = async (taskId) => {
  try {
    const data = await getTaskBindings(taskId)
    taskBindings.value = (data || []).map(binding => ({
      ...binding,
      rowData: binding.rowData ? JSON.parse(binding.rowData) : {}
    }))
  } catch (error) {
    console.error('加载绑定数据失败', error)
    taskBindings.value = []
  }
}

// 监听任务变化，加载进度更新和绑定数据
watch(
  () => props.task,
  (newTask) => {
    if (newTask?.id) {
      loadProgressUpdates(newTask.id)
      loadTaskBindings(newTask.id)
      // 处理附件数据
      if (newTask.attachments) {
        if (typeof newTask.attachments === 'string') {
          try {
            newTask.attachments = JSON.parse(newTask.attachments)
          } catch (error) {
            console.error('解析附件数据失败', error)
            newTask.attachments = []
          }
        } else if (!Array.isArray(newTask.attachments)) {
          newTask.attachments = []
        }
      } else {
        newTask.attachments = []
      }
      // 处理参与人数据
      if (newTask.stakeholders) {
        if (typeof newTask.stakeholders === 'string') {
          try {
            newTask.stakeholders = JSON.parse(newTask.stakeholders)
          } catch (error) {
            console.error('解析参与人数据失败', error)
            newTask.stakeholders = []
          }
        } else if (!Array.isArray(newTask.stakeholders)) {
          newTask.stakeholders = []
        }
      } else {
        newTask.stakeholders = []
      }
      // 转换参与人ID为名称
      if (newTask.stakeholders && Array.isArray(newTask.stakeholders)) {
        newTask.stakeholders = newTask.stakeholders.map(stakeholderId => {
          const employee = employeeList.value.find(emp => {
            // 考虑ID类型不匹配的情况，进行类型转换后比较
            return String(emp.id) === String(stakeholderId)
          })
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
</style>