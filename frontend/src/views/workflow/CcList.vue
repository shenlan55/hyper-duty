<template>
  <MobileGuard>
    <div class="cc-list">
      <el-page-header title="抄送我的">
        <template #extra>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="warning" @click="handleMarkAllRead">
            全部已读
          </el-button>
        </template>
      </el-page-header>

      <!-- 顶部 Tab 切换 -->
      <el-card class="mt-4" shadow="never">
        <el-tabs v-model="readStatus" @tab-change="handleTabChange">
          <el-tab-pane :label="`未读 (${unreadCount})`" name="0" />
          <el-tab-pane label="已读" name="1" />
          <el-tab-pane label="全部" name="" />
        </el-tabs>

        <BaseTable
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <template #title="{ row }">
            <span :class="{ 'unread-title': !row.readFlag }">
              {{ row.title || row.processName || '抄送通知' }}
            </span>
          </template>
          <template #fromUserName="{ row }">
            <el-tag size="small" effect="plain" type="info">
              <el-icon><user /></el-icon>
              <span style="margin-left: 4px">{{ row.fromUserName || row.fromUserId || '-' }}</span>
            </el-tag>
          </template>
          <template #readStatus="{ row }">
            <el-tag v-if="row.readFlag" size="small" type="success" effect="plain">已读</el-tag>
            <el-tag v-else size="small" type="danger" effect="dark">
              <el-icon><bell-filled /></el-icon>
              <span style="margin-left: 2px">未读</span>
            </el-tag>
          </template>
          <template #actions="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleViewTrace(row)">流程跟踪</el-button>
            <el-button v-if="!row.readFlag" size="small" type="success" @click="handleMarkRead(row)">
              标记已读
            </el-button>
          </template>
        </BaseTable>
      </el-card>

      <!-- 抄送详情对话框 -->
      <el-dialog
        v-model="detailVisible"
        :title="detailRow?.title || '抄送详情'"
        width="640px"
        :close-on-click-modal="false"
      >
        <div v-if="detailRow" class="cc-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="标题" :span="2">
              {{ detailRow.title || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="流程名称">
              {{ detailRow.processName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="流程实例ID">
              <el-link type="primary" @click="jumpToTrace(detailRow)">
                {{ detailRow.processInstanceId || '-' }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="抄送节点">
              {{ detailRow.nodeName || detailRow.nodeId || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="发起人">
              {{ detailRow.fromUserName || detailRow.fromUserId || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="抄送时间" :span="2">
              {{ formatTime(detailRow.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="抄送内容" :span="2">
              <div class="content-box">{{ detailRow.content || '（无）' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="detailRow && !detailRow.readFlag"
            type="primary"
            @click="handleMarkRead(detailRow); detailVisible = false;"
          >
            标记已读
          </el-button>
          <el-button type="primary" @click="jumpToTrace(detailRow); detailVisible = false;">
            查看流程跟踪
          </el-button>
        </template>
      </el-dialog>

      <!-- 流程跟踪 -->
      <ProcessTraceDialog
        v-model="traceVisible"
        :process-instance-id="traceInstanceId"
      />
    </div>
  </MobileGuard>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, User, BellFilled } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import MobileGuard from '@/components/MobileGuard.vue'
import ProcessTraceDialog from '@/components/ProcessTraceDialog.vue'
import { pageMineCc, markRead, markAllRead } from '@/api/workflow/cc'

const router = useRouter()

// 状态
const loading = ref(false)
const tableData = ref([])
const readStatus = ref('0')
const unreadCount = ref(0)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'title', label: '标题', minWidth: 220, slot: 'title' },
  { prop: 'processName', label: '流程名称', minWidth: 160 },
  { prop: 'nodeName', label: '节点', minWidth: 120 },
  { prop: 'fromUserName', label: '发起人', minWidth: 100, slot: 'fromUserName' },
  { prop: 'readFlag', label: '状态', width: 90, slot: 'readStatus' },
  { prop: 'createTime', label: '抄送时间', minWidth: 170 },
  { prop: 'actions', label: '操作', width: 260, fixed: 'right', slot: 'actions' }
]

// 详情
const detailVisible = ref(false)
const detailRow = ref(null)

// 跟踪
const traceVisible = ref(false)
const traceInstanceId = ref('')

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await pageMineCc(pagination.currentPage, pagination.pageSize, readStatus.value)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const res = await pageMineCc(1, 1, 0)
    unreadCount.value = res.total || 0
  } catch (e) {
    unreadCount.value = 0
  }
}

const handleTabChange = () => {
  pagination.currentPage = 1
  loadData()
}

const handleView = (row) => {
  detailRow.value = row
  detailVisible.value = true
  // 查看即视为已读
  if (!row.readFlag) {
    handleMarkRead(row, true)
  }
}

const handleViewTrace = (row) => {
  if (!row.processInstanceId) {
    ElMessage.warning('缺少流程实例ID')
    return
  }
  traceInstanceId.value = row.processInstanceId
  traceVisible.value = true
  if (!row.readFlag) {
    handleMarkRead(row, true)
  }
}

const jumpToTrace = (row) => {
  if (!row?.processInstanceId) return
  traceInstanceId.value = row.processInstanceId
  traceVisible.value = true
}

const handleMarkRead = async (row, silent = false) => {
  try {
    await markRead(row.id)
    row.readFlag = true
    if (!silent) ElMessage.success('已标记为已读')
    loadUnreadCount()
  } catch (e) {
    if (!silent) ElMessage.error(e.message || '标记失败')
  }
}

const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定将所有未读抄送标记为已读？', '提示', {
      type: 'info',
      confirmButtonText: '全部已读',
      cancelButtonText: '取消'
    })
    const count = await markAllRead()
    ElMessage.success(`已标记 ${count || 0} 条`)
    loadData()
    loadUnreadCount()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const formatTime = (t) => {
  if (!t) return '-'
  try {
    const d = new Date(t)
    if (isNaN(d.getTime())) return t
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch (e) {
    return t
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
  loadUnreadCount()
})
</script>

<style scoped>
.cc-list {
  padding: 12px;
}

.unread-title {
  font-weight: 600;
  color: #f56c6c;
}

.content-box {
  background: #fafbfc;
  padding: 10px 12px;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}
</style>
