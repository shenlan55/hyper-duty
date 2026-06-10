<template>
  <div class="shadow-task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>影子任务</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建影子
          </el-button>
        </div>
      </template>

      <el-table :data="shadowList" v-loading="loading" stripe>
        <el-table-column label="显示名称" min-width="180">
          <template #default="{ row }">
            <div>
              <div class="shadow-name">
                <el-tag size="small" type="info">影子</el-tag>
                {{ row.name }}
              </div>
              <div class="shadow-source" v-if="sourceProjectName">
                来自项目: {{ sourceProjectName }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.progress || 0" 
              :status="getProgressStatus(row.progress)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="批注数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.annotationCount || 0 }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button type="info" size="small" link @click="handleViewSource(row)">
              源任务
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && shadowList.length === 0" description="暂无影子任务" />
    </el-card>

    <!-- 创建影子任务对话框 -->
    <CreateShadowTaskDialog
      v-model="createDialogVisible"
      :default-project-id="projectId"
      @success="handleCreateSuccess"
    />

    <!-- 影子任务详情对话框 -->
    <ShadowTaskDetailDialog
      v-model="detailDialogVisible"
      :shadow-id="currentShadowId"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useDict } from '@/composables/useDict'
import { getProgressStatus, formatDateTime } from '@/utils/taskUtils'
import { getTaskListWithShadows, deleteShadowTask } from '@/api/shadowTask'
import CreateShadowTaskDialog from './CreateShadowTaskDialog.vue'
import ShadowTaskDetailDialog from './ShadowTaskDetailDialog.vue'

// 业务枚举：状态 走字典
const { labelOf: statusLabel, tagTypeOf: statusType } = useDict('task_status')

const props = defineProps({
  projectId: {
    type: Number,
    default: null
  }
})

const loading = ref(false)
const allTasks = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentShadowId = ref(null)

// 只筛选出影子任务
const shadowList = computed(() => {
  return allTasks.value.filter(task => task.taskType === 'shadow')
})

// 获取源任务项目名称（从第一个影子任务中获取）
const sourceProjectName = computed(() => {
  const firstShadow = shadowList.value[0]
  if (firstShadow?.sourceProjectId) {
    // 这里可以通过API获取项目名称，暂时简化
    return '项目' + firstShadow.sourceProjectId
  }
  return null
})

const loadTaskList = async () => {
  if (!props.projectId) return
  
  loading.value = true
  try {
    const data = await getTaskListWithShadows(props.projectId)
    allTasks.value = data || []
  } catch (error) {
    console.error('加载任务列表失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  createDialogVisible.value = true
}

const handleCreateSuccess = () => {
  loadTaskList()
}

const handleViewDetail = (row) => {
  currentShadowId.value = row.id
  detailDialogVisible.value = true
}

const handleViewSource = (row) => {
  // 这里可以打开源任务详情
  ElMessage.info('查看源任务功能待实现')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该影子任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteShadowTask(row.id)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

watch(() => props.projectId, () => {
  if (props.projectId) {
    loadTaskList()
  }
})

onMounted(() => {
  if (props.projectId) {
    loadTaskList()
  }
})
</script>

<style scoped>
.shadow-task-list {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shadow-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.shadow-source {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
