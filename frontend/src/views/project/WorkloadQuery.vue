<template>
  <div class="workload-query" style="width: 100%; height: 100%;">
    <el-card style="width: 100%; height: 100%;">
      <template #header>
        <div class="card-header">
          <span>工作量查询</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目">
          <el-select v-model="searchForm.projectId" placeholder="请选择项目" clearable filterable>
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务名">
          <el-input v-model="searchForm.taskName" placeholder="请输入任务名" clearable />
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="searchForm.assigneeId" placeholder="请选择责任人" clearable filterable>
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务时间">
          <el-date-picker
            v-model="taskDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleTaskDateChange"
          />
        </el-form-item>
        <el-form-item label="绑定时间">
          <el-date-picker
            v-model="bindDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleBindDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <BaseTable
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="{
          currentPage: pagination.currentPage,
          pageSize: pagination.pageSize,
          pageSizes: pagination.pageSizes,
          total: pagination.total
        }"
        row-key="id"
        :backend-pagination="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="width: 100%;"
      >
        <template #taskStatus="{ row }">
          <el-tag :type="getTaskStatusType(row.taskStatus)">{{ row.taskStatusText }}</el-tag>
        </template>
        <template #progress="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
        </template>
        <template #bindings="{ row }">
          <div class="bindings-cell">
            <div v-if="row.bindings && row.bindings.length > 0" class="bindings-list">
              <el-tag v-for="binding in row.bindings" :key="binding.id" size="small" style="margin: 2px;">
                {{ binding.tableName || '表格' }}
                <span v-if="binding.orderNo" style="margin-left: 4px;">({{ binding.orderNo }})</span>
              </el-tag>
              <el-button type="primary" link size="small" @click="handleViewBindings(row)">查看详情</el-button>
            </div>
            <span v-else style="color: #909399;">暂无绑定</span>
          </div>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="bindingDialogVisible"
      title="绑定详情"
      width="1200px"
    >
      <div v-if="currentWorkload" class="binding-detail">
        <div class="task-info">
          <h4>任务信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="项目">{{ currentWorkload.projectName }}</el-descriptions-item>
            <el-descriptions-item label="任务名称">{{ currentWorkload.taskName }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ currentWorkload.taskStatusText }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentWorkload.assigneeName }}</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ currentWorkload.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ currentWorkload.endDate }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <el-divider />
        <div class="bindings-info">
          <h4>绑定数据</h4>
          <div v-if="currentWorkload.bindings && currentWorkload.bindings.length > 0">
            <el-card v-for="binding in currentWorkload.bindings" :key="binding.id" class="binding-card" style="margin-bottom: 16px;">
              <div class="binding-header">
                <span class="binding-table-name">{{ binding.tableName || '自定义表格' }}</span>
                <span v-if="binding.orderNo" class="binding-order-no">单号: {{ binding.orderNo }}</span>
                <span class="binding-time">{{ formatDateTime(binding.createTime) }}</span>
              </div>
              <div v-if="binding.rowData" class="binding-data">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item 
                    v-for="(value, key) in binding.rowData" 
                    :key="key" 
                    :label="key"
                  >
                    {{ value }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
          </div>
          <div v-else class="no-bindings">
            暂无绑定数据
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="bindingDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { getWorkloadPage } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const tableData = ref([])
const projectList = ref([])
const employeeList = ref([])
const bindingDialogVisible = ref(false)
const currentWorkload = ref(null)
const taskDateRange = ref([])
const bindDateRange = ref([])

const searchForm = reactive({
  projectId: null,
  taskName: '',
  assigneeId: null,
  taskStartDate: null,
  taskEndDate: null,
  bindStartTime: null,
  bindEndTime: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'projectName', label: '项目', width: 180 },
  { prop: 'taskName', label: '任务名', minWidth: 200 },
  { prop: 'taskStatus', label: '状态', width: 100, slot: 'taskStatus' },
  { prop: 'progress', label: '进度', width: 120, slot: 'progress' },
  { prop: 'assigneeName', label: '责任人', width: 120 },
  { prop: 'startDate', label: '开始日期', width: 120 },
  { prop: 'endDate', label: '结束日期', width: 120 },
  { prop: 'bindings', label: '绑定信息', minWidth: 250, slot: 'bindings' }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const data = await getWorkloadPage(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000, showArchived: true })
    projectList.value = data.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000)
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

const handleTaskDateChange = (val) => {
  if (val && val.length === 2) {
    searchForm.taskStartDate = val[0]
    searchForm.taskEndDate = val[1]
  } else {
    searchForm.taskStartDate = null
    searchForm.taskEndDate = null
  }
}

const handleBindDateChange = (val) => {
  if (val && val.length === 2) {
    searchForm.bindStartTime = val[0]
    searchForm.bindEndTime = val[1]
  } else {
    searchForm.bindStartTime = null
    searchForm.bindEndTime = null
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleReset = () => {
  searchForm.projectId = null
  searchForm.taskName = ''
  searchForm.assigneeId = null
  searchForm.taskStartDate = null
  searchForm.taskEndDate = null
  searchForm.bindStartTime = null
  searchForm.bindEndTime = null
  taskDateRange.value = []
  bindDateRange.value = []
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

const handleViewBindings = (row) => {
  currentWorkload.value = row
  bindingDialogVisible.value = true
}

const getTaskStatusType = (status) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'warning'
    default: return 'info'
  }
}

const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 60) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

onMounted(() => {
  loadProjectList()
  loadEmployeeList()
  loadData()
})
</script>

<style scoped>
.workload-query {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.bindings-cell {
  min-height: 40px;
}

.bindings-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.binding-detail {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.binding-detail::-webkit-scrollbar {
  width: 8px;
}

.binding-detail::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

.binding-detail::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

.binding-detail .task-info,
.binding-detail .bindings-info {
  margin-bottom: 16px;
}

.binding-detail h4 {
  margin-bottom: 12px;
}

.bindings-scroll {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.bindings-scroll::-webkit-scrollbar {
  width: 6px;
}

.bindings-scroll::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.bindings-scroll::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

.binding-card .binding-header {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.binding-table-name {
  font-weight: bold;
  color: #303133;
}

.binding-order-no {
  color: #409eff;
}

.binding-time {
  color: #909399;
  font-size: 12px;
}

.no-bindings {
  color: #909399;
  text-align: center;
  padding: 20px;
}
</style>
