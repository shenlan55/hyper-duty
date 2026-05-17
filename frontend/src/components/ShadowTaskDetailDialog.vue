<template>
  <el-dialog
    v-model="visible"
    title="影子任务详情"
    width="900px"
    @close="handleClose"
  >
    <div v-loading="loading" class="shadow-detail">
      <div v-if="shadowData" class="detail-content">
        <!-- 影子任务基本信息 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>影子任务信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="影子名称">
              {{ shadowData.shadowName || shadowData.sourceTaskName }}
            </el-descriptions-item>
            <el-descriptions-item label="目标项目">
              {{ shadowData.targetProjectName }}
            </el-descriptions-item>
            <el-descriptions-item label="源项目" :span="2">
              {{ shadowData.sourceProjectName }}
            </el-descriptions-item>
            <el-descriptions-item label="影子描述" :span="2">
              {{ shadowData.shadowDescription || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(shadowData.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="批注数量">
              <el-tag type="success">{{ shadowData.annotationCount || 0 }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 源任务信息 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>源任务信息</span>
              <el-tag type="info">实时同步</el-tag>
            </div>
          </template>
          <div v-if="shadowData.sourceTask" class="source-task-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务名称">
                {{ shadowData.sourceTask.taskName }}
              </el-descriptions-item>
              <el-descriptions-item label="任务编码">
                {{ shadowData.sourceTask.taskCode || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="负责人">
                {{ shadowData.sourceTask.ownerName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getTaskPriorityType(shadowData.sourceTask.priority)">
                  {{ getTaskPriorityText(shadowData.sourceTask.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getTaskStatusType(shadowData.sourceTask.status)">
                  {{ getTaskStatusText(shadowData.sourceTask.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="进度">
                <el-progress
                  :percentage="shadowData.sourceTask.progress"
                  :status="getProgressStatus(shadowData.sourceTask.progress)"
                  :stroke-width="10"
                />
              </el-descriptions-item>
              <el-descriptions-item label="开始日期">
                {{ shadowData.sourceTask.startDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="结束日期">
                {{ shadowData.sourceTask.endDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="任务描述" :span="2">
                {{ shadowData.sourceTask.description || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-info">源任务已删除或无权限查看</div>
        </el-card>

        <!-- 批注区域 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>批注</span>
              <el-button type="primary" size="small" @click="showAddAnnotationDialog">
                添加批注
              </el-button>
            </div>
          </template>
          <div class="annotation-list">
            <div v-if="annotations.length === 0" class="empty-annotations">
              暂无批注
            </div>
            <div v-for="annotation in annotations" :key="annotation.id" class="annotation-item">
              <div class="annotation-header">
                <span class="annotation-author">{{ annotation.employeeName }}</span>
                <span class="annotation-time">{{ formatDateTime(annotation.createTime) }}</span>
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click="handleDeleteAnnotation(annotation)"
                >
                  删除
                </el-button>
              </div>
              <div class="annotation-content">{{ annotation.content }}</div>
              <div v-if="annotation.attachments" class="annotation-attachments">
                <div class="attachments-label">附件：</div>
                <div class="attachments-list">
                  {{ annotation.attachments }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 添加批注对话框 -->
    <el-dialog
      v-model="addAnnotationVisible"
      title="添加批注"
      width="500px"
    >
      <el-form :model="annotationForm" label-width="80px">
        <el-form-item label="批注内容">
          <el-input
            v-model="annotationForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入批注内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addAnnotationVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddAnnotation" :loading="addAnnotationLoading">
          提交
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTaskStatusType,
  getTaskStatusText,
  getTaskPriorityType,
  getTaskPriorityText,
  getProgressStatus,
  formatDateTime
} from '@/utils/taskUtils'
import {
  getShadowTaskDetail,
  getShadowAnnotations,
  addShadowAnnotation,
  deleteShadowAnnotation
} from '@/api/shadowTask'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  shadowId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const shadowData = ref(null)
const annotations = ref([])
const addAnnotationVisible = ref(false)
const addAnnotationLoading = ref(false)

const annotationForm = ref({
  content: ''
})

watch(() => props.shadowId, (newVal) => {
  if (newVal && visible.value) {
    loadShadowDetail()
    loadAnnotations()
  }
})

watch(visible, (val) => {
  if (val && props.shadowId) {
    loadShadowDetail()
    loadAnnotations()
  }
})

const loadShadowDetail = async () => {
  if (!props.shadowId) return
  
  loading.value = true
  try {
    const data = await getShadowTaskDetail(props.shadowId)
    shadowData.value = data
  } catch (error) {
    console.error('加载影子任务详情失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadAnnotations = async () => {
  if (!props.shadowId) return
  
  try {
    const data = await getShadowAnnotations(props.shadowId)
    annotations.value = data || []
  } catch (error) {
    console.error('加载批注失败', error)
  }
}

const showAddAnnotationDialog = () => {
  annotationForm.value = { content: '' }
  addAnnotationVisible.value = true
}

const handleAddAnnotation = async () => {
  if (!annotationForm.value.content.trim()) {
    ElMessage.warning('请输入批注内容')
    return
  }

  addAnnotationLoading.value = true
  try {
    await addShadowAnnotation({
      shadowId: props.shadowId,
      content: annotationForm.value.content
    })
    
    ElMessage.success('添加批注成功')
    addAnnotationVisible.value = false
    await loadAnnotations()
    await loadShadowDetail()
  } catch (error) {
    console.error('添加批注失败', error)
    ElMessage.error('添加失败')
  } finally {
    addAnnotationLoading.value = false
  }
}

const handleDeleteAnnotation = async (annotation) => {
  try {
    await ElMessageBox.confirm('确定删除该批注吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteShadowAnnotation(annotation.id)
    ElMessage.success('删除成功')
    await loadAnnotations()
    await loadShadowDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleClose = () => {
  shadowData.value = null
  annotations.value = []
  visible.value = false
}
</script>

<style scoped>
.shadow-detail {
  width: 100%;
}

.section-card {
  margin-bottom: 20px;
}

.section-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.source-task-content {
  width: 100%;
}

.empty-info {
  color: #909399;
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.annotation-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-annotations {
  color: #909399;
  text-align: center;
  padding: 30px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.annotation-item {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.annotation-item:last-child {
  margin-bottom: 0;
}

.annotation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.annotation-author {
  font-weight: 500;
  color: #303133;
}

.annotation-time {
  font-size: 12px;
  color: #909399;
}

.annotation-content {
  color: #606266;
  line-height: 1.6;
  word-break: break-word;
}

.annotation-attachments {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
  display: flex;
  gap: 8px;
}

.attachments-label {
  font-size: 12px;
  color: #909399;
}

.attachments-list {
  font-size: 12px;
  color: #409eff;
}
</style>
