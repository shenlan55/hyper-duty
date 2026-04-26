<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`更新任务进展 - ${currentTask?.taskName || ''}`"
    width="1500px"
  >
    <div class="task-info-panel" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
      <h4 style="margin-bottom: 10px;">任务信息</h4>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="任务名称">{{ currentTask?.taskName }}</el-descriptions-item>
            <el-descriptions-item label="所属项目">{{ currentTask?.projectName }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentTask?.ownerName }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="12">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="优先级">{{ getTaskPriorityText(currentTask?.priority) }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ getTaskStatusText(currentTask?.status) }}</el-descriptions-item>
            <el-descriptions-item label="当前进度">{{ currentTask?.progress }}%</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </div>

    <div class="task-description" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">任务描述</h4>
      <div class="description-content" v-if="currentTask?.description" v-html="currentTask.description"></div>
      <div v-else class="no-data">暂无任务描述</div>
    </div>

    <div class="task-attachments" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">附件</h4>
      <AttachmentList :attachments="currentTask?.attachments || []" />
    </div>

    <div class="task-stakeholders" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">干系人</h4>
      <div v-if="currentTask?.stakeholders && currentTask.stakeholders.length > 0" class="stakeholders-container">
        <el-tag v-for="(stakeholder, index) in currentTask.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
          {{ stakeholder }}
        </el-tag>
      </div>
      <div v-else class="no-data">暂无干系人</div>
    </div>

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

    <div style="margin: 20px 0; display: flex; justify-content: flex-end; gap: 10px;">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </div>

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
                <AttachmentList :attachments="update.attachmentList" />
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskStatusText, getTaskPriorityText, formatDateTime } from '@/utils/taskUtils'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileUpload from '@/components/FileUpload.vue'
import AttachmentList from '@/components/AttachmentList.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: null
  },
  progressUpdates: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentTask = ref(null)

const progressUpdateForm = reactive({
  taskId: null,
  progress: 0,
  description: '',
  attachments: []
})

watch(() => props.task, (newVal) => {
  if (newVal) {
    currentTask.value = { ...newVal }
    progressUpdateForm.taskId = newVal.id
    progressUpdateForm.progress = newVal.progress || 0
    progressUpdateForm.description = ''
    progressUpdateForm.attachments = []
  }
}, { immediate: true })

const handleSubmit = () => {
  emit('submit', {
    taskId: progressUpdateForm.taskId,
    progress: progressUpdateForm.progress,
    description: progressUpdateForm.description,
    attachments: progressUpdateForm.attachments
  })
}

const handleUploadSuccess = () => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
}
</script>
