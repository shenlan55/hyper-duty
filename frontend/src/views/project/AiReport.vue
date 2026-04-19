<template>
  <div class="ai-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 报告生成</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 日报生成 -->
        <el-tab-pane label="日报生成" name="daily">
          <el-form :inline="true" :model="dailyForm" class="search-form">
            <el-form-item label="日期">
              <el-date-picker
                v-model="dailyForm.reportDate"
                type="date"
                placeholder="请选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="项目">
              <el-select v-model="dailyForm.projectIds" placeholder="全部项目" clearable multiple style="width: 350px;">
                <el-option v-for="project in projectList" :key="project.id" :label="project.projectName" :value="project.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleGenerateDaily" :loading="generating">生成日报</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 周报生成 -->
        <el-tab-pane label="周报生成" name="weekly">
          <el-form :inline="true" :model="weeklyForm" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="weeklyForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="项目">
              <el-select v-model="weeklyForm.projectIds" placeholder="全部项目" clearable multiple style="width: 350px;">
                <el-option v-for="project in projectList" :key="project.id" :label="project.projectName" :value="project.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleGenerateWeekly" :loading="generating">生成周报</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 生成的报告预览 -->
      <el-card v-if="currentReport" class="report-preview" shadow="never">
        <template #header>
          <div class="card-header">
            <span>{{ currentReport.reportTitle }}</span>
            <el-button type="success" size="small" @click="handleSaveReport">保存报告</el-button>
          </div>
        </template>
        <div class="report-content">
          <pre>{{ currentReport.reportContent }}</pre>
        </div>
      </el-card>
    </el-card>

    <!-- 历史报告列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>历史报告</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="报告类型">
          <el-select v-model="searchForm.reportType" placeholder="全部类型" clearable style="width: 150px;">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="searchForm.projectId" placeholder="全部项目" clearable style="width: 250px;">
            <el-option v-for="project in projectList" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
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
        :backend-pagination="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #reportType="{ row }">
          <el-tag :type="row.reportType === 'daily' ? 'primary' : 'success'">
            {{ row.reportType === 'daily' ? '日报' : '周报' }}
          </el-tag>
        </template>
        <template #operation="{ row }">
          <el-button type="info" size="small" @click="handleView(row)">查看</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 查看报告对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="viewReport?.reportTitle"
      width="800px"
    >
      <div class="report-content">
        <pre>{{ viewReport?.reportContent }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import {
  generateDailyReport,
  generateWeeklyReport,
  getReportPage,
  getReportById,
  deleteReport
} from '@/api/ai-report'
import { getProjectPage } from '@/api/project'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref('daily')
const generating = ref(false)
const loading = ref(false)
const currentReport = ref(null)
const viewDialogVisible = ref(false)
const viewReport = ref(null)
const projectList = ref([])

const dailyForm = reactive({
  reportDate: new Date().toISOString().split('T')[0],
  projectIds: []
})

const weeklyForm = reactive({
  dateRange: [],
  projectIds: []
})

const searchForm = reactive({
  reportType: null,
  projectId: null
})

const tableData = ref([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'reportTitle', label: '报告标题', width: 250 },
  { prop: 'reportType', label: '报告类型', width: 100, slot: 'reportType' },
  { prop: 'projectName', label: '项目名称', width: 200 },
  { prop: 'reportDate', label: '报告日期', width: 120 },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'operation', label: '操作', width: 150, slot: 'operation', fixed: 'right' }
]

const loadProjects = async () => {
  try {
    const res = await getProjectPage({ pageNum: 1, pageSize: 100, showArchived: false })
    projectList.value = res.records || []
  } catch (error) {
    console.error('加载项目失败', error)
  }
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await getReportPage({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      reportType: searchForm.reportType,
      projectId: searchForm.projectId
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载报告列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleGenerateDaily = async () => {
  if (!dailyForm.reportDate) {
    ElMessage.warning('请选择日期')
    return
  }
  
  generating.value = true
  try {
    const employeeId = userStore.userInfo?.id || 1
    const res = await generateDailyReport({
      reportDate: dailyForm.reportDate,
      projectIds: dailyForm.projectIds?.length > 0 ? dailyForm.projectIds : null,
      employeeId
    })
    ElMessage.success(res?.message || '日报生成中，请稍后在历史报告中查看')
    // 立即刷新历史报告列表
    loadReports()
  } catch (error) {
    // 超时情况下，前端超时了但后台还在跑，给用户一个友好的提示
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ElMessage.warning('请求已超时，但AI仍在后台生成中，请稍后在历史报告中查看')
    } else {
      ElMessage.error('日报生成失败')
      console.error(error)
    }
  } finally {
    generating.value = false
  }
}

const handleGenerateWeekly = async () => {
  if (!weeklyForm.dateRange || weeklyForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  generating.value = true
  try {
    const employeeId = userStore.userInfo?.id || 1
    const res = await generateWeeklyReport({
      startDate: weeklyForm.dateRange[0],
      endDate: weeklyForm.dateRange[1],
      projectIds: weeklyForm.projectIds?.length > 0 ? weeklyForm.projectIds : null,
      employeeId
    })
    ElMessage.success(res?.message || '周报生成中，请稍后在历史报告中查看')
    // 立即刷新历史报告列表
    loadReports()
  } catch (error) {
    // 超时情况下，前端超时了但后台还在跑，给用户一个友好的提示
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ElMessage.warning('请求已超时，但AI仍在后台生成中，请稍后在历史报告中查看')
    } else {
      ElMessage.error('周报生成失败')
      console.error(error)
    }
  } finally {
    generating.value = false
  }
}

const handleSaveReport = () => {
  currentReport.value = null
  loadReports()
  ElMessage.success('报告已保存')
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadReports()
}

const handleReset = () => {
  searchForm.reportType = null
  searchForm.projectId = null
  pagination.currentPage = 1
  loadReports()
}

const handleView = async (row) => {
  try {
    const res = await getReportById(row.id)
    viewReport.value = res
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取报告详情失败')
    console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这份报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteReport(row.id)
    ElMessage.success('删除成功')
    loadReports()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const handleSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  loadReports()
}

const handleCurrentChange = (currentPage) => {
  pagination.currentPage = currentPage
  loadReports()
}

onMounted(() => {
  loadProjects()
  loadReports()
})
</script>

<style scoped>
.ai-report {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.report-preview {
  margin-top: 20px;
}

.report-content {
  max-height: 500px;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.report-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}
</style>
