<template>
  <div class="operation-log-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable style="width: 150px;" popper-style="min-width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="查询" value="查询" />
            <el-option label="添加" value="添加" />
            <el-option label="修改" value="修改" />
            <el-option label="删除" value="删除" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.operationModule" placeholder="请选择操作模块" clearable style="width: 200px;" popper-style="min-width: 200px;">
            <el-option label="全部" value="" />
            <el-option label="部门管理" value="部门管理" />
            <el-option label="人员管理" value="人员管理" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色管理" value="角色管理" />
            <el-option label="菜单管理" value="菜单管理" />
            <el-option label="值班表管理" value="值班表管理" />
            <el-option label="值班安排" value="值班安排" />
            <el-option label="值班记录" value="值班记录" />
            <el-option label="班次配置" value="班次配置" />
            <el-option label="请假申请" value="请假申请" />
            <el-option label="调班管理" value="调班管理" />
            <el-option label="自动排班" value="自动排班" />
            <el-option label="排班统计" value="排班统计" />
            <el-option label="排班模式" value="排班模式" />
            <el-option label="操作日志" value="操作日志" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFormSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <BaseTable
        v-loading="loading"
        :data="logList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :backend-pagination="true"
        :show-search="true"
        :search-placeholder="'请输入操作人或操作描述'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleTableSearch"
        @export="handleExport"
      >
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #operation="{ row }">
          <el-space>
            <el-button type="primary" size="small" @click="openViewDialog(row)">
              查看
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </el-space>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="viewDialogVisible"
      title="日志详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ currentLog.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorMsg" label="错误信息" :span="2">
          <el-tag type="danger">{{ currentLog.errorMsg }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDateTime(currentLog.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOperationLogList,
  deleteOperationLog
} from '../../api/duty/operationLog'
import { safeInput } from '../../utils/xssUtil'
import { useSearchPagination } from '../../hooks/usePagination'
import BaseTable from '../../components/BaseTable.vue'
import { formatDateTime } from '../../utils/dateUtils'

const loading = ref(false)
const viewDialogVisible = ref(false)
const logList = ref([])
const currentLog = ref({})

// 分页配置
const {
  currentPage,
  pageSize,
  total,
  pagination,
  handleCurrentChange: originalHandleCurrentChange,
  handleSizeChange: originalHandleSizeChange,
  searchQuery,
  handleSearch
} = useSearchPagination()

const searchForm = reactive({
  operatorName: '',
  operationType: '',
  operationModule: '',
  dateRange: null
})

const fetchLogList = async () => {
  loading.value = true
  try {
    // 构建搜索参数
    const params = {
      operatorName: safeInput(searchForm.operatorName || searchQuery.value),
      operationType: safeInput(searchForm.operationType),
      operationModule: safeInput(searchForm.operationModule),
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加时间范围参数
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const data = await getOperationLogList(params)
    // 后端返回的是MyBatis Plus Page对象，包含records和total字段
    logList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0
  } catch (error) {
    console.error('获取操作日志列表失败:', error)
    ElMessage.error('获取操作日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleFormSearch = () => {
  currentPage.value = 1
  fetchLogList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    operatorName: '',
    operationType: '',
    operationModule: '',
    dateRange: null
  })
  currentPage.value = 1
  fetchLogList()
}

const handleSizeChange = (val) => {
  originalHandleSizeChange(val)
  fetchLogList()
}

const handleCurrentChange = (val) => {
  originalHandleCurrentChange(val)
  fetchLogList()
}

const openViewDialog = (row) => {
  currentLog.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该操作日志吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteOperationLog(id)
    ElMessage.success('删除操作日志成功')
    fetchLogList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除操作日志失败:', error)
      ElMessage.error('删除操作日志失败')
    }
  }
}

const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  handleSearch(keyword)
  fetchLogList()
}

const handleExport = () => {
  // 导出逻辑
  const exportData = logList.value
  const headers = ['操作人', '操作类型', '操作模块', '操作描述', '请求方法', 'IP地址', '执行时间(ms)', '状态', '操作时间']
  const rows = exportData.map(row => [
    row.operatorName,
    row.operationType,
    row.operationModule,
    row.operationDesc,
    row.requestMethod,
    row.ipAddress,
    row.executionTime,
    row.status === 1 ? '成功' : '失败',
    formatDateTime(row.createTime)
  ])
  
  // CSV导出实现
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `操作日志_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const exportLogs = () => {
  ElMessage.success('日志导出功能待实现')
}

// 表格列配置
const columns = [
  { prop: 'operatorName', label: '操作人', width: '120' },
  { prop: 'operationType', label: '操作类型', width: '100' },
  { prop: 'operationModule', label: '操作模块', width: '120' },
  { prop: 'operationDesc', label: '操作描述', minWidth: '200', showOverflowTooltip: true },
  { prop: 'requestMethod', label: '请求方法', width: '100' },
  { prop: 'ipAddress', label: 'IP地址', width: '130' },
  { prop: 'executionTime', label: '执行时间(ms)', width: '120' },
  {
    label: '状态',
    width: '100',
    slotName: 'status'
  },
  {
    label: '操作时间',
    width: '180',
    slotName: 'createTime'
  },
  {
    label: '操作',
    width: '180',
    fixed: 'right',
    slotName: 'operation'
  }
]

// 分页配置


onMounted(() => {
  fetchLogList()
})
</script>

<style scoped>
.operation-log-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
