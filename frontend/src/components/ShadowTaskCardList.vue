<template>
  <div class="shadow-task-card-list">
    <div class="header-section">
      <div class="title-wrapper">
        <el-icon class="title-icon"><DocumentCopy /></el-icon>
        <h3 class="title">影子任务</h3>
        <el-tag type="info" class="count-tag">{{ shadowList.length }} 个</el-tag>
      </div>
      <el-button type="primary" @click="handleCreate" class="create-btn">
        <el-icon><Plus /></el-icon>
        创建影子
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="shadowList.length === 0" class="empty-container">
      <el-empty description="暂无影子任务">
        <el-button type="primary" @click="handleCreate">创建第一个影子</el-button>
      </el-empty>
    </div>

    <div v-else class="cards-container">
      <div
        v-for="shadow in shadowList"
        :key="shadow.id"
        class="shadow-card"
        @click="handleViewDetail(shadow)"
      >
        <div class="card-header">
          <div class="shadow-title">
            <div class="name-wrapper">
              <el-icon class="shadow-icon"><Link /></el-icon>
              <span class="shadow-name">{{ shadow.shadowName || shadow.sourceTaskName }}</span>
            </div>
            <div class="source-project">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ shadow.sourceProjectName }}</span>
            </div>
          </div>
          <div class="status-badge">
            <el-tag v-if="shadow.sourceTask" :type="getTaskStatusType(shadow.sourceTask.status)" size="small">
              {{ getTaskStatusText(shadow.sourceTask.status) }}
            </el-tag>
            <el-tag v-else type="info" size="small">源任务已删除</el-tag>
          </div>
        </div>

        <div v-if="shadow.sourceTask" class="progress-section">
          <div class="progress-label">
            <span>源任务进度</span>
            <span class="progress-value">{{ shadow.sourceTask.progress }}%</span>
          </div>
          <el-progress
            :percentage="shadow.sourceTask.progress"
            :status="getProgressStatus(shadow.sourceTask.progress)"
            :stroke-width="8"
          />
        </div>

        <div v-if="shadow.shadowDescription" class="description-section">
          <div class="desc-label">影子描述</div>
          <div class="desc-content">{{ shadow.shadowDescription }}</div>
        </div>

        <div class="card-footer">
          <div class="meta-info">
            <span class="annotation-count">
              <el-icon><ChatDotRound /></el-icon>
              {{ shadow.annotationCount || 0 }} 条批注
            </span>
            <span class="create-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(shadow.createTime) }}
            </span>
          </div>
          <div class="actions">
            <el-button type="primary" size="small" link @click.stop="handleViewDetail(shadow)">
              查看详情
            </el-button>
            <el-button type="danger" size="small" link @click.stop="handleDelete(shadow)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建影子任务对话框 -->
    <CreateShadowTaskDialog
      v-model="createDialogVisible"
      :defaultProjectId="projectId"
      @success="handleCreateSuccess"
    />

    <!-- 影子任务详情对话框 -->
    <ShadowTaskDetailDialog
      v-model="detailDialogVisible"
      :shadowId="currentShadowId"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentCopy, Plus, Link, FolderOpened, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { getTaskStatusType, getTaskStatusText, getProgressStatus, formatDateTime } from '@/utils/taskUtils'
import { getShadowTasksByTargetProject, deleteShadowTask } from '@/api/shadowTask'
import CreateShadowTaskDialog from './CreateShadowTaskDialog.vue'
import ShadowTaskDetailDialog from './ShadowTaskDetailDialog.vue'

const props = defineProps({
  projectId: {
    type: Number,
    default: null
  }
})

const loading = ref(false)
const shadowList = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentShadowId = ref(null)

watch(() => props.projectId, () => {
  if (props.projectId) {
    loadShadowList()
  }
})

onMounted(() => {
  if (props.projectId) {
    loadShadowList()
  }
})

const loadShadowList = async () => {
  if (!props.projectId) return
  
  loading.value = true
  try {
    const data = await getShadowTasksByTargetProject(props.projectId)
    shadowList.value = data || []
  } catch (error) {
    console.error('加载影子任务列表失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  createDialogVisible.value = true
}

const handleCreateSuccess = () => {
  loadShadowList()
}

const handleViewDetail = (shadow) => {
  currentShadowId.value = shadow.id
  detailDialogVisible.value = true
}

const handleDelete = async (shadow) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除影子任务"${shadow.shadowName || shadow.sourceTaskName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteShadowTask(shadow.id)
    ElMessage.success('删除成功')
    loadShadowList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.shadow-task-card-list {
  width: 100%;
  padding: 0;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 24px;
  color: #409eff;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.count-tag {
  font-size: 12px;
}

.create-btn {
  border-radius: 8px;
}

.loading-container,
.empty-container {
  padding: 40px 0;
}

.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.shadow-card {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shadow-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.shadow-title {
  flex: 1;
  min-width: 0;
}

.name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.shadow-icon {
  color: #f5576c;
  font-size: 18px;
}

.shadow-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-project {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.source-project .el-icon {
  font-size: 14px;
}

.status-badge {
  flex-shrink: 0;
  margin-left: 12px;
}

.progress-section {
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.progress-value {
  font-weight: 500;
  color: #409eff;
}

.description-section {
  margin-bottom: 16px;
}

.desc-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.desc-content {
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.meta-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-info .el-icon {
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 4px;
}
</style>
