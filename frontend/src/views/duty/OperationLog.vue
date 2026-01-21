<template>
  <div class="operation-log-container">
    <div class="page-header">
      <h2>操作日志</h2>
      <el-button type="primary" @click="exportLogs">
        <el-icon><Download /></el-icon>
        导出日志
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="logList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100" />
        <el-table-column prop="operationModule" label="操作模块" width="120" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="100" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="executionTime" label="执行时间(ms)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button type="primary" size="small" @click="openViewDialog(scope.row)">
                查看
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
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

const loading = ref(false)
const viewDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logList = ref([])
const currentLog = ref({})

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
      operatorName: searchForm.operatorName,
      operationType: searchForm.operationType,
      operationModule: searchForm.operationModule,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加时间范围参数
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await getOperationLogList(params)
    if (response.code === 200) {
      // 后端返回的是PageResult对象，包含data和total字段
      logList.value = response.data.data
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取操作日志列表失败:', error)
    ElMessage.error('获取操作日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
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

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchLogList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
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
    
    const response = await deleteOperationLog(id)
    if (response.code === 200) {
      ElMessage.success('删除操作日志成功')
      fetchLogList()
    } else {
      ElMessage.error(response.message || '删除操作日志失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除操作日志失败:', error)
      ElMessage.error('删除操作日志失败')
    }
  }
}

const exportLogs = () => {
  ElMessage.success('日志导出功能待实现')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  fetchLogList()
})
</script>

<style scoped>
.operation-log-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.content-card {
  margin-bottom: 10px;
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
