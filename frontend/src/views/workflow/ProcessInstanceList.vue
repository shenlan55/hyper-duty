<template>
  <div class="process-instance-list">
    <el-page-header title="我发起的流程">
      <template #extra>
        <el-button @click="loadData">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="运行中" name="running" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已撤回" name="withdrawn" />
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
          <el-button
            v-if="activeTab === 'running' && canWithdraw(row)"
            size="small"
            type="warning"
            @click="handleWithdrawProcess(row)"
          >
            撤回
          </el-button>
          <el-button
            v-if="activeTab === 'running' && canCancel(row)"
            size="small"
            type="danger"
            @click="handleCancelProcess(row)"
          >
            作废
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 流程跟踪对话框（带历史时间线） -->
    <ProcessTraceDialog
      v-model="trackerDialogVisible"
      :process-instance-id="trackerProcessInstanceId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import ProcessTraceDialog from '@/components/ProcessTraceDialog.vue'
import {
  pageMyStartedProcess,
  pageMyCompletedProcess,
  cancelProcess,
  withdrawProcess
} from '@/api/workflow/process'

const route = useRoute()

// 状态
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
    { prop: 'processDefinitionKey', label: '流程KEY', width: 160 },
    { prop: 'startTime', label: '开始时间', width: 180 }
  ]

  if (activeTab.value === 'completed' || activeTab.value === 'withdrawn') {
    baseColumns.push({ prop: 'endTime', label: '结束时间', width: 180 })
  }

  baseColumns.push({ prop: 'actions', label: '操作', width: 240, fixed: 'right', slot: 'actions' })
  return baseColumns
})

// 流程跟踪弹窗
const trackerDialogVisible = ref(false)
const trackerProcessInstanceId = ref('')

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
    } else if (activeTab.value === 'withdrawn') {
      // 已撤回走已完成接口 + 状态过滤（如果后端支持 status 参数）
      try {
        res = await pageMyCompletedProcess(pagination.currentPage, pagination.pageSize)
        if (res && res.records) {
          res.records = res.records.filter(r => r.status === 'withdrawn')
          res.total = res.records.length
        }
      } catch (e) {
        res = { records: [], total: 0 }
      }
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

const handleViewProcess = (row) => {
  if (!row?.id) return
  trackerProcessInstanceId.value = row.id
  trackerDialogVisible.value = true
}

// 判断当前流程是否可撤回：
// 条件：流程未结束 && 第一节点尚未审批（即只有一条 historic activity，且是 startEvent）
const canWithdraw = (row) => {
  if (!row) return false
  if (row.status && ['completed', 'withdrawn', 'cancelled'].includes(row.status)) return false
  // 后端有 firstTaskDone 字段则优先使用
  if (typeof row.firstTaskDone === 'boolean') {
    return !row.firstTaskDone
  }
  // 否则根据 historyActivities 数量判断（如果后端返回了）
  if (Array.isArray(row.historyActivities)) {
    const userTasks = row.historyActivities.filter(a => a.activityType === 'userTask')
    return userTasks.length === 0
  }
  // 默认展示按钮，由后端在撤回时做最终校验
  return true
}

const canCancel = (row) => {
  if (!row) return false
  if (row.status && ['completed', 'withdrawn', 'cancelled'].includes(row.status)) return false
  return true
}

const handleWithdrawProcess = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入撤回原因', '撤回流程', {
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
      inputPlaceholder: '撤回原因（可选）',
      inputValue: ''
    })

    await withdrawProcess(row.id, reason || '发起人撤回')
    ElMessage.success('撤回成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.message || '撤回失败')
    }
  }
}

const handleCancelProcess = async (row) => {
  try {
    await ElMessageBox.confirm('确定要作废该流程吗？此操作不可恢复', '作废确认', {
      type: 'warning',
      confirmButtonText: '确认作废',
      cancelButtonText: '取消'
    })
    await cancelProcess(row.id, '用户作废')
    ElMessage.success('作废成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
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

// 监听路由 query.highlight：发起成功后跳转过来，自动打开跟踪弹窗
watch(
  () => route.query.highlight,
  (id) => {
    if (id) {
      trackerProcessInstanceId.value = String(id)
      trackerDialogVisible.value = true
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.process-instance-list {
  padding: 12px;
}
</style>
