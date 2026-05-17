<template>
  <div class="task-shadow-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>影子任务列表</span>
          <el-tag type="info">{{ shadowList.length }} 个影子</el-tag>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="shadowList.length === 0" class="empty-container">
        <el-empty description="暂无影子任务" />
      </div>

      <div v-else class="shadow-list">
        <div v-for="shadow in shadowList" :key="shadow.id" class="shadow-item">
          <div class="shadow-header">
            <div class="shadow-title">
              <el-tag size="small" type="info">影子</el-tag>
              <span class="shadow-name">{{ shadow.shadowName || shadow.sourceTaskName }}</span>
            </div>
            <div class="shadow-meta">
              <span class="target-project">
                <el-icon><Folder /></el-icon>
                {{ shadow.targetProjectName }}
              </span>
              <span class="create-time">{{ formatDateTime(shadow.createTime) }}</span>
            </div>
          </div>

          <div v-if="shadow.shadowDescription" class="shadow-desc">
            {{ shadow.shadowDescription }}
          </div>

          <div class="shadow-footer">
            <span class="annotation-count">
              <el-icon><ChatDotRound /></el-icon>
              {{ shadow.annotationCount || 0 }} 条批注
            </span>
            <el-button type="primary" size="small" link @click="handleViewShadow(shadow)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 影子任务详情对话框 -->
    <ShadowTaskDetailDialog
      v-model="detailDialogVisible"
      :shadowId="currentShadowId"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, ChatDotRound } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/taskUtils'
import { getShadowTasksBySourceTask } from '@/api/shadowTask'
import ShadowTaskDetailDialog from './ShadowTaskDetailDialog.vue'

const props = defineProps({
  taskId: {
    type: Number,
    default: null
  }
})

const loading = ref(false)
const shadowList = ref([])
const detailDialogVisible = ref(false)
const currentShadowId = ref(null)

watch(() => props.taskId, (newVal) => {
  if (newVal) {
    loadShadowList()
  }
})

onMounted(() => {
  if (props.taskId) {
    loadShadowList()
  }
})

const loadShadowList = async () => {
  if (!props.taskId) return

  loading.value = true
  try {
    const data = await getShadowTasksBySourceTask(props.taskId)
    shadowList.value = data || []
  } catch (error) {
    console.error('加载影子任务列表失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleViewShadow = (shadow) => {
  currentShadowId.value = shadow.id
  detailDialogVisible.value = true
}
</script>

<style scoped>
.task-shadow-list {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container,
.empty-container {
  padding: 20px 0;
}

.shadow-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.shadow-item {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.shadow-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.shadow-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shadow-name {
  font-weight: 500;
  font-size: 15px;
  color: #303133;
}

.shadow-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.target-project {
  display: flex;
  align-items: center;
  gap: 4px;
}

.shadow-desc {
  margin-bottom: 12px;
  padding: 8px 12px;
  background-color: #fff;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.shadow-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.annotation-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}
</style>
