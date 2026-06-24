<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title || '流程跟踪'"
    :width="width"
    :fullscreen="fullscreen"
    :close-on-click-modal="false"
    destroy-on-close
    class="process-trace-dialog"
    @open="onDialogOpen"
  >
    <div v-loading="loading" class="trace-wrapper">
      <!-- 顶部流程概览 -->
      <el-alert
        v-if="traceData"
        :closable="false"
        type="info"
        class="trace-header"
      >
        <template #title>
          <div class="header-info">
            <div class="info-item">
              <span class="info-label">流程名称：</span>
              <span class="info-value">{{ traceData.processDefinitionName || traceData.processDefinitionKey || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">实例ID：</span>
              <span class="info-value">{{ traceData.processInstanceId || processInstanceId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发起人：</span>
              <span class="info-value">{{ traceData.startUserName || traceData.startUserId || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发起时间：</span>
              <span class="info-value">{{ formatTime(traceData.startTime) }}</span>
            </div>
            <div class="info-item" v-if="traceData.endTime">
              <span class="info-label">结束时间：</span>
              <span class="info-value">{{ formatTime(traceData.endTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态：</span>
              <el-tag :type="statusTagType(traceData.status)" size="small">
                {{ statusText(traceData.status) }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-alert>

      <div class="trace-body">
        <!-- 左侧：流程图 -->
        <div class="trace-canvas">
          <BpmnViewer
            v-if="bpmnXml"
            :bpmn-xml="bpmnXml"
            :completed-activity-ids="completedIds"
            :current-activity-ids="currentIds"
            :terminated-activity-ids="terminatedIds"
            style="height: 100%"
          />
          <el-empty v-else description="未找到流程图" />
          <div class="canvas-legend">
            <div class="legend-item">
              <span class="legend-dot" style="background: #67c23a"></span>
              <span>已完成</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background: #f56c6c"></span>
              <span>当前节点</span>
            </div>
            <div class="legend-item" v-if="terminatedIds.length">
              <span class="legend-dot" style="background: #909399"></span>
              <span>已结束/跳过</span>
            </div>
          </div>
        </div>

        <!-- 右侧：流转历史 -->
        <div class="trace-history">
          <div class="history-title">
            <el-icon><clock /></el-icon>
            <span>流转历史</span>
          </div>
          <el-scrollbar height="100%">
            <el-timeline v-if="historyList.length">
              <el-timeline-item
                v-for="(item, idx) in historyList"
                :key="idx"
                :timestamp="formatTime(item.startTime)"
                :type="item.endTime ? 'success' : 'primary'"
                :hollow="!item.endTime"
                placement="top"
              >
                <div class="history-card">
                  <div class="history-name">
                    {{ item.activityName || item.activityId }}
                  </div>
                  <div class="history-meta">
                    <span v-if="item.assignee">处理人：{{ item.assignee }}</span>
                    <span v-if="item.durationInMs">耗时：{{ formatDuration(item.durationInMs) }}</span>
                  </div>
                  <div v-if="item.comment" class="history-comment">
                    {{ item.comment }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无流转历史" :image-size="60" />
          </el-scrollbar>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import BpmnViewer from '@/components/BpmnViewer.vue'
import { Clock } from '@element-plus/icons-vue'
import { getProcessTraceData } from '@/api/workflow/process'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  processInstanceId: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '90%'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const traceData = ref(null)
const bpmnXml = ref('')
const completedIds = ref([])
const currentIds = ref([])
const terminatedIds = ref([])
const historyList = ref([])

const statusText = (s) => {
  const map = {
    running: '进行中',
    completed: '已完成',
    withdrawn: '已撤回',
    cancelled: '已作废',
    suspended: '已挂起'
  }
  return map[s] || s || '进行中'
}

const statusTagType = (s) => {
  const map = {
    running: 'primary',
    completed: 'success',
    withdrawn: 'warning',
    cancelled: 'info',
    suspended: 'warning'
  }
  return map[s] || 'primary'
}

const formatTime = (t) => {
  if (!t) return '-'
  try {
    const d = new Date(t)
    if (isNaN(d.getTime())) return t
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch (e) {
    return t
  }
}

const formatDuration = (ms) => {
  if (!ms || ms < 0) return '-'
  const sec = Math.floor(ms / 1000)
  if (sec < 60) return `${sec}秒`
  const min = Math.floor(sec / 60)
  if (min < 60) return `${min}分${sec % 60}秒`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时${min % 60}分`
  const day = Math.floor(hour / 24)
  return `${day}天${hour % 24}小时`
}

const onDialogOpen = async () => {
  if (!props.processInstanceId) return
  loading.value = true
  try {
    const data = await getProcessTraceData(props.processInstanceId)
    traceData.value = data || {}
    bpmnXml.value = data?.bpmnXml || ''
    completedIds.value = data?.completedActivityIds || []
    currentIds.value = data?.currentActivityIds || []
    terminatedIds.value = data?.terminatedActivityIds || []
    historyList.value = data?.history || []
  } catch (e) {
    console.error('加载流程跟踪数据失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.trace-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 500px;
}

.trace-header :deep(.el-alert__title) {
  width: 100%;
}

.header-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  font-size: 13px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.trace-body {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 12px;
  height: 540px;
}

.trace-canvas {
  position: relative;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafbfc;
  overflow: hidden;
}

.canvas-legend {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  z-index: 5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.trace-history {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.history-card {
  background: #fff;
  padding: 4px 0;
}

.history-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.history-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.history-comment {
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 6px 10px;
  border-radius: 4px;
  border-left: 3px solid #c0c4cc;
  margin-top: 4px;
}

:deep(.process-trace-dialog .el-dialog__body) {
  padding: 16px 20px;
}

@media (max-width: 992px) {
  .trace-body {
    grid-template-columns: 1fr;
    height: auto;
  }
  .trace-canvas {
    height: 400px;
  }
}
</style>
