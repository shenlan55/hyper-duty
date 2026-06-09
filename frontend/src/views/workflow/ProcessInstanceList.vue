<template>
  <div class="process-instance-list">
    <el-page-header title="我发起的流程" />

    <el-card class="mt-4">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="运行中" name="running" />
        <el-tab-pane label="已完成" name="completed" />
      </el-tabs>

      <BaseTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #actions="{ row }">
          <el-button size="small" type="primary" @click="handleViewProcess(row)">流程跟踪</el-button>
          <el-button v-if="activeTab === 'running'" size="small" type="danger" @click="handleCancelProcess(row)">作废</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 流程跟踪对话框 -->
    <el-dialog
      v-model="trackerDialogVisible"
      title="流程跟踪"
      width="80%"
      fullscreen
      :close-on-click-modal="false"
    >
      <el-alert
        v-if="trackerProcessInstance"
        style="margin-bottom: 20px"
        type="info"
        :closable="false"
      >
        <template #title>
          流程实例ID: {{ trackerProcessInstance.id }} | 
          流程定义ID: {{ trackerProcessInstance.processDefinitionId }}
        </template>
      </el-alert>

      <div class="tracker-content">
        <BpmnViewer
          :bpmn-xml="trackerBpmnXml"
          :completed-activity-ids="trackerCompletedActivityIds"
          :current-activity-ids="trackerCurrentActivityIds"
        />
        
        <div class="tracker-legend">
          <div class="legend-item">
            <span class="legend-color" style="background: #67c23a; border-color: #67c23a"></span>
            <span class="legend-text">已完成</span>
          </div>
          <div class="legend-item">
            <span class="legend-color" style="background: #f56c6c; border-color: #f56c6c"></span>
            <span class="legend-text">当前任务</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="trackerDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import BpmnViewer from '@/components/BpmnViewer.vue'
import { 
  pageMyStartedProcess, 
  pageMyCompletedProcess,
  cancelProcess, 
  getProcessBpmnXml,
  getHistoricActivityInstances 
} from '@/api/workflow/process'

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('running')

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = computed(() => {
  const baseColumns = [
    { prop: 'id', label: '实例ID', width: 200 },
    { prop: 'processDefinitionName', label: '流程名称', minWidth: 150 },
    { prop: 'processDefinitionKey', label: '流程KEY', width: 180 },
    { prop: 'startTime', label: '开始时间', width: 180 }
  ]
  
  if (activeTab.value === 'completed') {
    baseColumns.push({ prop: 'endTime', label: '结束时间', width: 180 })
  }
  
  baseColumns.push({ prop: 'actions', label: '操作', width: 180, fixed: 'right', slot: 'actions' })
  return baseColumns
})

// 流程跟踪相关
const trackerDialogVisible = ref(false)
const trackerProcessInstance = ref(null)
const trackerBpmnXml = ref('')
const trackerCompletedActivityIds = ref([])
const trackerCurrentActivityIds = ref([])

const handleTabChange = () => {
  pagination.currentPage = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'completed') {
      res = await pageMyCompletedProcess(pagination.currentPage, pagination.pageSize)
    } else {
      res = await pageMyStartedProcess(pagination.currentPage, pagination.pageSize)
    }
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleViewProcess = async (row) => {
  trackerProcessInstance.value = row
  
  try {
    // 1. 获取 BPMN XML
    const bpmnRes = await getProcessBpmnXml(row.processDefinitionId)
    let bpmnXml = bpmnRes
    // 兼容不同返回格式
    if (bpmnRes && bpmnRes.data) {
      bpmnXml = bpmnRes.data
    }
    trackerBpmnXml.value = bpmnXml || ''
    
    // 2. 获取历史活动
    const activitiesRes = await getHistoricActivityInstances(row.id)
    let activities = activitiesRes
    if (activitiesRes && activitiesRes.records) {
      activities = activitiesRes.records
    }
    
    const completedIds = []
    const currentIds = []
    
    if (Array.isArray(activities)) {
      activities.forEach(activity => {
        if (activity.endTime) {
          completedIds.push(activity.activityId)
        } else {
          currentIds.push(activity.activityId)
        }
      })
    }
    
    trackerCompletedActivityIds.value = completedIds
    trackerCurrentActivityIds.value = currentIds
    
    trackerDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载流程跟踪失败')
  }
}

const handleCancelProcess = async (row) => {
  try {
    await ElMessageBox.confirm('确定要作废该流程吗？', '提示', {
      type: 'warning'
    })
    
    await cancelProcess(row.id, '用户作废')
    ElMessage.success('作废成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '作废失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.process-instance-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.tracker-content {
  min-height: 500px;
}

.tracker-legend {
  display: flex;
  gap: 30px;
  padding: 15px 0;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 2px solid;
}

.legend-text {
  font-size: 14px;
  color: #606266;
}
</style>
