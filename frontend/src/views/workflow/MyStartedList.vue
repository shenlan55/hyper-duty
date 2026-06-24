<template>
  <div class="my-started-list">
    <el-page-header title="我发起的流程">
      <template #extra>
        <el-button @click="loadData">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4" shadow="never">
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
            v-if="canWithdraw(row)"
            size="small"
            type="warning"
            @click="handleWithdrawProcess(row)"
          >
            撤回
          </el-button>
          <el-button
            v-if="canCancel(row)"
            size="small"
            type="danger"
            @click="handleCancelProcess(row)"
          >
            作废
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 流程跟踪弹窗（与 ProcessInstanceList 共用同一组件） -->
    <ProcessTraceDialog
      v-model="trackerDialogVisible"
      :process-instance-id="trackerProcessInstanceId"
    />
  </div>
</template>

<script setup>
/**
 * 我发起的流程列表（独立页面）
 * ----------------------------------------------------------------------------
 * 设计目的：
 *   - 数据库 sys_menu 表 sort=10 的菜单"我发起的"对应的独立入口（/workflow/my-started）
 *   - 与"流程实例"（/workflow/instance-list，含已完成/已撤回 Tab 的综合页）形成分工
 *
 * 与 ProcessInstanceList.vue 的关系：
 *   - 同样使用 pageMyStartedProcess API + ProcessTraceDialog 弹窗 + 撤回/作废操作
 *   - 本页专注"运行中"的我发起的流程，不展示 Tab 切换
 *   - 代码存在局部重复（撤回/作废/分页逻辑），后续若再造第三个相似页面，
 *     建议抽 useProcessInstance composable 统一抽象（本次改动不抽，避免改动范围扩大）
 * ----------------------------------------------------------------------------
 */
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import ProcessTraceDialog from '@/components/ProcessTraceDialog.vue'
import {
  pageMyStartedProcess,
  cancelProcess,
  withdrawProcess
} from '@/api/workflow/process'

const route = useRoute()

// 表格状态
const loading = ref(false)
const tableData = ref([])

// 分页（与 BaseTable 约定：分页对象 reactive）
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

// 列定义：专注"我发起的运行中流程"，无"结束时间"列
// 操作列固定右侧、宽 240px 以容纳 3 个操作按钮
const columns = [
  { prop: 'id', label: '实例ID', width: 200 },
  { prop: 'processDefinitionName', label: '流程名称', minWidth: 150 },
  { prop: 'processDefinitionKey', label: '流程KEY', width: 160 },
  { prop: 'startTime', label: '开始时间', width: 180 },
  { prop: 'actions', label: '操作', width: 240, fixed: 'right', slot: 'actions' }
]

// 流程跟踪弹窗
const trackerDialogVisible = ref(false)
const trackerProcessInstanceId = ref('')

/**
 * 加载我发起的流程列表
 * 后端接口：/workflow/process/instance/my/page（已存在于 process.js）
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await pageMyStartedProcess(pagination.currentPage, pagination.pageSize)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 打开流程跟踪弹窗（带历史时间线）
 */
const handleViewProcess = (row) => {
  if (!row?.id) return
  trackerProcessInstanceId.value = row.id
  trackerDialogVisible.value = true
}

/**
 * 判断当前流程是否可撤回
 * 条件：流程未结束 && 第一节点尚未审批（即只有一条 historic activity，且是 startEvent）
 */
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

/**
 * 判断当前流程是否可作废
 * 条件：流程未结束
 */
const canCancel = (row) => {
  if (!row) return false
  if (row.status && ['completed', 'withdrawn', 'cancelled'].includes(row.status)) return false
  return true
}

/**
 * 撤回流程（弹窗输入原因 → 调后端 → 刷新列表）
 */
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
    // 用户点取消时 ElMessageBox 抛 'cancel' / 'close'，不视作错误
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.message || '撤回失败')
    }
  }
}

/**
 * 作废流程（确认弹窗 → 调后端 → 刷新列表）
 */
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

// 监听路由 query.highlight：发起流程成功后跳转过来，自动打开跟踪弹窗
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
.my-started-list {
  padding: 12px;
}
</style>
