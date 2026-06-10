<template>
  <el-dialog
    v-model="visible"
    title="影子任务详情"
    width="1200px"
    @close="handleClose"
  >
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="warning" @click="handleEdit">编辑影子信息</el-button>
      <el-button type="danger" @click="handleDelete">删除影子</el-button>
    </template>
    
    <div v-loading="loading" class="shadow-detail">
      <div v-if="shadowData" class="detail-content">
        <!-- 头部标识 -->
        <el-alert title="影子任务信息" type="info" :closable="false" show-icon style="margin-bottom: 20px;">
          <template #default>
            这是一个影子任务，源任务来自「{{ shadowData.sourceProjectName || '-' }}」项目，数据实时同步源任务。
          </template>
        </el-alert>

        <!-- 影子任务基本信息 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>影子任务信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="影子名称">
              <span v-if="shadowData.shadowAlias" style="color: #409eff; font-weight: bold;">{{ shadowData.shadowAlias }}</span>
              <span v-else>{{ shadowData.sourceTaskName || shadowData.taskName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="所属项目">
              {{ shadowData.projectName }}
            </el-descriptions-item>
            <el-descriptions-item label="源任务" :span="2">
              <el-tag type="info">{{ shadowData.sourceTaskName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="源项目" :span="2">
              <el-tag type="success">{{ shadowData.sourceProjectName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="影子描述" :span="2">
              {{ shadowData.shadowDescription || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ shadowData.createdByName || shadowData.createdBy || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(shadowData.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="批注数量">
              <el-tag type="success">{{ shadowData.annotationCount || 0 }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 源任务基本信息 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>源任务信息（实时同步）</span>
              <el-tag type="info">实时同步</el-tag>
            </div>
          </template>
          <div class="task-info-panel">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-descriptions :column="1" size="small">
                  <el-descriptions-item label="任务名称">
                    {{ shadowData.sourceTaskName || shadowData.taskName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="负责人">
                    {{ shadowData.ownerName || shadowData.assigneeName || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="开始日期">
                    {{ shadowData.startDate || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="结束日期">
                    {{ shadowData.endDate || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
              <el-col :span="12">
                <el-descriptions :column="1" size="small">
                  <el-descriptions-item label="优先级">
                    {{ priorityLabel(shadowData.priority) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    {{ statusLabel(shadowData.status) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="当前进度">
                    {{ shadowData.progress }}%
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 任务描述 -->
        <div class="task-description-section" style="margin-bottom: 20px;">
      <h4 style="margin-bottom: 10px;">任务描述</h4>
      <div class="description-content richtext-content" v-if="shadowData.description" v-html="shadowData.description"></div>
      <div v-else class="no-data">暂无任务描述</div>
    </div>

        <!-- 附件列表 -->
        <div class="task-attachments" style="margin-bottom: 20px;">
          <h4 style="margin-bottom: 10px;">附件</h4>
          <AttachmentList :attachments="processedShadowData.attachments || []" />
        </div>

        <!-- 参与人列表 -->
        <div class="task-stakeholders" style="margin-bottom: 20px;">
          <h4 style="margin-bottom: 10px;">参与人</h4>
          <div v-if="processedShadowData.stakeholders && processedShadowData.stakeholders.length > 0" class="stakeholders-container">
            <el-tag v-for="(stakeholder, index) in processedShadowData.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
              {{ stakeholder }}
            </el-tag>
          </div>
          <div v-else class="no-data">暂无参与人</div>
        </div>

        <!-- 进展历史时间线 -->
        <div style="margin-top: 20px;">
          <h4 style="margin-bottom: 10px;">进展历史</h4>
          <ProgressHistory :progress-updates="progressUpdates" />
          <el-empty v-if="progressUpdates.length === 0" description="暂无进展历史" />
        </div>

        <!-- 批注区域 -->
        <el-card class="section-card" style="margin-top: 20px;">
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
                <span class="annotation-author">{{ annotation.createdByName || annotation.createdBy || '未知用户' }}</span>
                <span class="annotation-time">{{ formatDateTime(annotation.createdAt || annotation.createTime) }}</span>
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
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/composables/useDict'
import { getProgressStatus, formatDateTime } from '@/utils/taskUtils'
import {
  getShadowTaskDetail,
  getShadowAnnotations,
  addShadowAnnotation,
  deleteShadowAnnotation,
  getTaskProgressUpdates
} from '@/api/task'
import { getEmployeeList } from '@/api/employee'
import ProgressHistory from '@/components/ProgressHistory.vue'
import AttachmentList from '@/components/AttachmentList.vue'

// 业务枚举：状态/优先级 走字典
const { labelOf: statusLabel } = useDict('task_status')
const { labelOf: priorityLabel } = useDict('task_priority')

const employeeList = ref([])

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

const emit = defineEmits(['update:modelValue', 'edit', 'delete'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const shadowData = ref(null)
const annotations = ref([])
const progressUpdates = ref([])
const addAnnotationVisible = ref(false)
const addAnnotationLoading = ref(false)

const annotationForm = ref({
  content: ''
})

// 处理后的影子数据
const processedShadowData = computed(() => {
  if (!shadowData.value) {
    return null
  }
  
  const data = { ...shadowData.value }
  
  // 处理附件数据
  if (data.attachments) {
    if (typeof data.attachments === 'string') {
      try {
        data.attachments = JSON.parse(data.attachments)
      } catch (error) {
        console.error('解析附件数据失败', error)
        data.attachments = []
      }
    } else if (!Array.isArray(data.attachments)) {
      data.attachments = []
    }
  } else {
    data.attachments = []
  }
  
  // 处理参与人数据
  if (data.stakeholders) {
    if (typeof data.stakeholders === 'string') {
      try {
        data.stakeholders = JSON.parse(data.stakeholders)
      } catch (error) {
        console.error('解析参与人数据失败', error)
        data.stakeholders = []
      }
    } else if (!Array.isArray(data.stakeholders)) {
      data.stakeholders = []
    }
  } else {
    data.stakeholders = []
  }
  
  // 转换参与人ID为名称
  if (data.stakeholders && Array.isArray(data.stakeholders)) {
    data.stakeholders = data.stakeholders.map(stakeholderId => {
      const employee = employeeList.value.find(emp => {
        return String(emp.id) === String(stakeholderId)
      })
      return employee ? employee.employeeName : stakeholderId
    })
  }
  
  return data
})

const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000)
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

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
    // 加载影子任务详情后，加载源任务的进展更新
    if (data?.sourceTaskId) {
      await loadProgressUpdates()
    }
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

const loadProgressUpdates = async () => {
  if (!shadowData.value?.sourceTaskId) return
  
  try {
    const data = await getTaskProgressUpdates(shadowData.value.sourceTaskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载进度更新失败', error)
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

const handleEdit = () => {
  emit('edit')
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确认删除影子任务？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除影子任务失败', error)
      ElMessage.error('删除影子任务失败')
    }
  }
}

const handleClose = () => {
  shadowData.value = null
  annotations.value = []
  progressUpdates.value = []
  visible.value = false
}

onMounted(() => {
  loadEmployeeList()
})
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

.task-info-panel {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

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

.description-content :deep(p) {
  margin: 0 0 8px 0;
  line-height: 1.6;
}

.description-content :deep(p:last-child) {
  margin-bottom: 0;
}

.description-content :deep(img) {
  max-width: 100% !important;
  height: auto !important;
  display: block !important;
  margin: 0 auto !important;
  box-sizing: border-box !important;
}

.description-content :deep(table) {
  max-width: 100% !important;
  overflow-x: auto !important;
  display: block !important;
  box-sizing: border-box !important;
}

.stakeholders-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.no-data {
  padding: 10px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
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
</style>
