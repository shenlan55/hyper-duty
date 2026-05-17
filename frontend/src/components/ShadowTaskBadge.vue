<template>
  <div class="shadow-task-badge">
    <!-- 源任务：显示有多少个影子 -->
    <el-tooltip
      v-if="isSourceTask && shadowCount > 0"
      :content="`有 ${shadowCount} 个影子任务`"
      placement="top"
    >
      <div
        class="badge source-badge"
        @click="handleViewShadows"
      >
        <el-icon><Connection /></el-icon>
        <span class="count">{{ shadowCount }}</span>
      </div>
    </el-tooltip>

    <!-- 影子任务：显示来源标识 -->
    <el-tooltip
      v-else-if="isShadowTask"
      :content="`来自：${sourceProjectName || '未知项目'}`"
      placement="top"
    >
      <div
        class="badge shadow-badge"
        @click="handleViewSource"
      >
        <el-icon><Link /></el-icon>
        <span class="label">影子</span>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import { Connection, Link } from '@element-plus/icons-vue'

const props = defineProps({
  isSourceTask: {
    type: Boolean,
    default: false
  },
  isShadowTask: {
    type: Boolean,
    default: false
  },
  shadowCount: {
    type: Number,
    default: 0
  },
  sourceTaskId: {
    type: Number,
    default: null
  },
  sourceProjectName: {
    type: String,
    default: ''
  },
  taskId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['viewShadows', 'viewSource', 'createShadow'])

const handleViewShadows = () => {
  emit('viewShadows', props.taskId)
}

const handleViewSource = () => {
  emit('viewSource', props.sourceTaskId)
}
</script>

<style scoped>
.shadow-task-badge {
  display: inline-block;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.badge:hover {
  transform: scale(1.05);
}

.source-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.shadow-badge {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.badge .count,
.badge .label {
  font-weight: 500;
}

.badge .el-icon {
  font-size: 14px;
}
</style>
