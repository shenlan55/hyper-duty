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
          <el-select v-model="searchForm.projectId" placeholder="请选择项目" clearable filterable @change="handleProjectChange">
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
        :columns="dynamicColumns"
        :loading="loading"
        :pagination="{
          currentPage: pagination.currentPage,
          pageSize: pagination.pageSize,
          pageSizes: pagination.pageSizes,
          total: pagination.total
        }"
        row-key="id"
        :backend-pagination="true"
        :show-export="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @export="handleExport"
        style="width: 100%;"
      >
        <template #taskStatus="{ row }">
          <el-tag :type="getTaskStatusType(row.taskStatus)">{{ row.taskStatusText }}</el-tag>
        </template>
        <template #progress="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
        </template>
      </BaseTable>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { getWorkloadPage } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { getCustomTableColumns } from '@/api/customTable'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const tableData = ref([])
const projectList = ref([])
const employeeList = ref([])
const taskDateRange = ref([])
const bindDateRange = ref([])
const bindColumns = ref([])
const currentTableId = ref(null)

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

const baseColumns = [
  { prop: 'projectName', label: '项目', width: 180, fixed: 'left', showOverflowTooltip: true },
  { prop: 'taskName', label: '任务名', minWidth: 200, fixed: 'left', showOverflowTooltip: true },
  { prop: 'taskStatus', label: '状态', width: 100, slot: 'taskStatus', fixed: 'left' },
  { prop: 'progress', label: '进度', width: 120, slot: 'progress', fixed: 'left' },
  { prop: 'assigneeName', label: '责任人', width: 120, fixed: 'left', showOverflowTooltip: true },
  { prop: 'startDate', label: '开始日期', width: 120, fixed: 'left', showOverflowTooltip: true },
  { prop: 'endDate', label: '结束日期', width: 120, fixed: 'left', showOverflowTooltip: true }
]

const bindingInfoColumns = [
  { prop: 'orderNo', label: '单号', width: 150, showOverflowTooltip: true },
  { 
    prop: 'bindTime', 
    label: '绑定时间', 
    width: 180,
    showOverflowTooltip: true,
    formatter: (row) => formatDateTime(row.bindTime)
  }
]

const dynamicColumns = computed(() => {
  const result = [...baseColumns]
  
  if (bindColumns.value && bindColumns.value.length > 0) {
    result.push(...bindingInfoColumns)
    
    bindColumns.value.forEach(col => {
      result.push({
        prop: col.columnCode,
        label: col.columnName,
        width: col.columnWidth || 150,
        showOverflowTooltip: true
      })
    })
  }
  
  return result
})

const exportAllData = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: 1,
      pageSize: 10000,
      ...searchForm
    }
    const data = await getWorkloadPage(params)
    return (data.records || []).map(row => {
      const flattened = { ...row }
      if (row.bindData) {
        Object.keys(row.bindData).forEach(key => {
          flattened[key] = row.bindData[key]
        })
      }
      return flattened
    })
  } catch (error) {
    ElMessage.error('导出数据失败')
    return []
  } finally {
    loading.value = false
  }
}

const handleExport = async ({ format, data, columns }) => {
  try {
    loading.value = true
    
    let exportData = data
    if (data.length < pagination.total) {
      exportData = await exportAllData()
    }
    
    const exportColumns = dynamicColumns.value.filter(col => col.prop)
    const exportDataList = exportData.map(row => {
      const result = {}
      exportColumns.forEach(col => {
        let value
        if (col.prop === 'bindTime') {
          value = row.bindTime ? formatDateTime(row.bindTime) : ''
        } else {
          value = row[col.prop] !== undefined && row[col.prop] !== null ? row[col.prop] : ''
        }
        result[col.label] = value
      })
      return result
    })
    
    const XLSX = await import('xlsx')
    const worksheet = XLSX.utils.json_to_sheet(exportDataList)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '工作量查询')
    XLSX.writeFile(workbook, `工作量查询_${formatDateTime(new Date(), 'YYYYMMDDHHmmss')}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const data = await getWorkloadPage(params)
    tableData.value = (data.records || []).map(row => {
      const flattened = { ...row }
      if (row.bindData) {
        Object.keys(row.bindData).forEach(key => {
          flattened[key] = row.bindData[key]
        })
      }
      return flattened
    })
    pagination.total = data.total || 0
    
    if (tableData.value.length > 0 && !currentTableId.value) {
      const firstRow = tableData.value.find(row => row.tableId)
      if (firstRow) {
        currentTableId.value = firstRow.tableId
        await loadBindColumns(currentTableId.value)
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadBindColumns = async (tableId) => {
  try {
    const columns = await getCustomTableColumns(tableId)
    bindColumns.value = columns || []
  } catch (error) {
    console.error('加载绑定表格列失败', error)
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
  bindColumns.value = []
  currentTableId.value = null
  loadData()
}

const handleProjectChange = () => {
  pagination.currentPage = 1
  bindColumns.value = []
  currentTableId.value = null
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

:deep(.el-table .el-table__row) {
  height: 40px !important;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0 !important;
}

:deep(.el-table .cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: keep-all;
}
</style>
