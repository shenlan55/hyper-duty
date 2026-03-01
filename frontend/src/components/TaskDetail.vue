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
            <el-descriptions-item label="任务描述">{{ task?.description }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
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
import { getTaskProgressUpdates } from '@/api/task'
import { getTaskStatusText, getTaskPriorityText } from '@/utils/taskUtils'
import ProgressHistory from '@/components/ProgressHistory.vue'

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
</style>