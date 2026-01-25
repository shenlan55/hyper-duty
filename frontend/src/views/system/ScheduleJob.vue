<template>
  <div class="schedule-job-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>定时任务管理</span>
          <el-button type="primary" plain @click="openAddDialog">
            <el-icon><Plus /></el-icon> 新增任务
          </el-button>
        </div>
      </template>

      <!-- 任务列表 -->
      <el-table v-loading="loading" :data="jobList" style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="jobName" label="任务名称" />
        <el-table-column prop="jobGroup" label="任务分组" width="120" />
        <el-table-column prop="jobCode" label="任务编码" width="150" />
        <el-table-column prop="cronExpression" label="Cron表达式" width="180" />
        <el-table-column prop="beanName" label="Bean名称" />
        <el-table-column prop="methodName" label="方法名称" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '启用' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="concurrent" label="并发" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.concurrent === 1 ? 'info' : 'warning'">
              {{ scope.row.concurrent === 1 ? '允许' : '禁止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="任务描述" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '暂停' : '恢复' }}
            </el-button>
            <el-button size="small" type="primary" @click="handleRunJob(scope.row.id)">
              立即执行
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteJob(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 任务日志 -->
    <el-card class="box-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>任务执行日志</span>
          <el-button type="danger" plain @click="openCleanLogDialog">
            <el-icon><Delete /></el-icon> 清理日志
          </el-button>
        </div>
      </template>

      <!-- 日志列表 -->
      <el-table v-loading="logLoading" :data="logList" style="width: 100%">
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="jobId" label="任务ID" width="80" />
        <el-table-column prop="jobName" label="任务名称" />
        <el-table-column prop="jobGroup" label="任务分组" width="120" />
        <el-table-column prop="jobCode" label="任务编码" width="150" />
        <el-table-column prop="status" label="执行状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间(ms)" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 新增/编辑任务对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form
        ref="jobFormRef"
        :model="jobForm"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="jobName">
              <el-input v-model="jobForm.jobName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务分组" prop="jobGroup">
              <el-input v-model="jobForm.jobGroup" placeholder="请输入任务分组" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务编码" prop="jobCode">
              <el-input v-model="jobForm.jobCode" placeholder="请输入任务编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Cron表达式" prop="cronExpression">
              <el-input v-model="jobForm.cronExpression" placeholder="请输入Cron表达式" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Bean名称" prop="beanName">
              <el-input v-model="jobForm.beanName" placeholder="请输入Bean名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="方法名称" prop="methodName">
              <el-input v-model="jobForm.methodName" placeholder="请输入方法名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参数" prop="params">
          <el-input
            v-model="jobForm.params"
            type="textarea"
            placeholder="请输入参数"
            :rows="3"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="jobForm.status">
                <el-radio :value="'1'">启用</el-radio>
                <el-radio :value="'0'">暂停</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许并发" prop="concurrent">
              <el-radio-group v-model="jobForm.concurrent">
                <el-radio :value="'1'">允许</el-radio>
                <el-radio :value="'0'">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="jobForm.description"
            type="textarea"
            placeholder="请输入任务描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveJob">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog
      v-model="cleanLogDialogVisible"
      title="清理任务日志"
      width="400px"
    >
      <el-form :model="cleanLogForm" label-width="120px">
        <el-form-item label="清理方式">
          <el-radio-group v-model="cleanLogForm.cleanType" @change="handleCleanTypeChange">
            <el-radio :value="1">清理指定天数之前的日志</el-radio>
            <el-radio :value="2">清理所有日志</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="清理天数" v-if="cleanLogForm.cleanType === 1">
          <el-input-number
            v-model="cleanLogForm.days"
            :min="1"
            :max="365"
            :step="1"
            placeholder="请输入清理天数"
          />
        </el-form-item>
        <el-form-item>
          <span class="text-warning" v-if="cleanLogForm.cleanType === 1">
            提示：将清理{{ cleanLogForm.days }}天之前的任务执行日志
          </span>
          <span class="text-warning" v-else>
            提示：将清理所有任务执行日志，此操作不可恢复！
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cleanLogDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleCleanLogs">确认清理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scheduleJobApi } from '../../api/system/scheduleJob'
import { formatDateTime } from '../../utils/dateUtils'

const scheduleApi = scheduleJobApi()

// 任务列表
const jobList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 日志列表
const logList = ref([])
const logLoading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增任务')
const jobFormRef = ref()

// 表单数据
const jobForm = reactive({
  id: null,
  jobName: '',
  jobGroup: '',
  jobCode: '',
  cronExpression: '',
  beanName: '',
  methodName: '',
  params: '',
  status: 1,
  concurrent: 0,
  description: ''
})

// 清理日志表单
const cleanLogDialogVisible = ref(false)
const cleanLogForm = reactive({
  cleanType: 1, // 1: 清理指定天数之前的日志, 2: 清理所有日志
  days: 30
})

// 处理清理方式变化
const handleCleanTypeChange = () => {
  if (cleanLogForm.cleanType === 2) {
    // 清理所有日志时，将 days 设为 0
    cleanLogForm.days = 0
  }
}

// 表单验证规则
const rules = {
  jobName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  jobGroup: [
    { required: true, message: '请输入任务分组', trigger: 'blur' }
  ],
  jobCode: [
    { required: true, message: '请输入任务编码', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ],
  beanName: [
    { required: true, message: '请输入Bean名称', trigger: 'blur' }
  ],
  methodName: [
    { required: true, message: '请输入方法名称', trigger: 'blur' }
  ]
}

// 获取任务列表
const fetchJobList = async () => {
  loading.value = true
  try {
    const response = await scheduleApi.getJobList()
    if (response.code === 200) {
      jobList.value = response.data
      total.value = response.data.length
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 获取日志列表
const fetchLogList = async () => {
  logLoading.value = true
  try {
    const response = await scheduleApi.getLogList()
    if (response.code === 200) {
      logList.value = response.data
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    logLoading.value = false
  }
}

// 打开新增对话框
const openAddDialog = () => {
  dialogTitle.value = '新增任务'
  Object.assign(jobForm, {
    id: null,
    jobName: '',
    jobGroup: '',
    jobCode: '',
    cronExpression: '',
    beanName: '',
    methodName: '',
    params: '',
    status: 1,
    concurrent: 0,
    description: ''
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (job) => {
  dialogTitle.value = '编辑任务'
  // 先清空表单
  Object.assign(jobForm, {
    id: null,
    jobName: '',
    jobGroup: '',
    jobCode: '',
    cronExpression: '',
    beanName: '',
    methodName: '',
    params: '',
    status: '1',
    concurrent: '0',
    description: ''
  })
  // 再赋值并转换类型
  jobForm.id = job.id
  jobForm.jobName = job.jobName
  jobForm.jobGroup = job.jobGroup
  jobForm.jobCode = job.jobCode
  jobForm.cronExpression = job.cronExpression
  jobForm.beanName = job.beanName
  jobForm.methodName = job.methodName
  jobForm.params = job.params
  jobForm.status = job.status.toString()
  jobForm.concurrent = job.concurrent.toString()
  jobForm.description = job.description
  dialogVisible.value = true
}

// 保存任务
const handleSaveJob = async () => {
  try {
    await jobFormRef.value.validate()
    // 创建提交数据的副本并转换类型
    const submitData = {
      ...jobForm,
      status: parseInt(jobForm.status),
      concurrent: parseInt(jobForm.concurrent)
    }
    let response
    if (jobForm.id) {
      response = await scheduleApi.updateJob(submitData)
    } else {
      response = await scheduleApi.addJob(submitData)
    }
    if (response.code === 200) {
      ElMessage.success(jobForm.id ? '更新任务成功' : '新增任务成功')
      dialogVisible.value = false
      fetchJobList()
    } else {
      ElMessage.error(jobForm.id ? '更新任务失败' : '新增任务失败')
    }
  } catch (error) {
    console.error('保存任务失败:', error)
    ElMessage.error('保存任务失败')
  }
}

// 处理状态变更
const handleStatusChange = async (job) => {
  try {
    const response = job.status === 1
      ? await scheduleApi.pauseJob(job.id)
      : await scheduleApi.resumeJob(job.id)
    if (response.code === 200) {
      ElMessage.success(job.status === 1 ? '暂停任务成功' : '恢复任务成功')
      fetchJobList()
    } else {
      ElMessage.error(job.status === 1 ? '暂停任务失败' : '恢复任务失败')
    }
  } catch (error) {
    console.error('变更任务状态失败:', error)
    ElMessage.error('变更任务状态失败')
  }
}

// 立即执行任务
const handleRunJob = async (jobId) => {
  try {
    const response = await scheduleApi.runJob(jobId)
    if (response.code === 200) {
      ElMessage.success('任务执行成功')
      // 刷新日志列表
      setTimeout(() => {
        fetchLogList()
      }, 1000)
    } else {
      ElMessage.error('任务执行失败')
    }
  } catch (error) {
    console.error('执行任务失败:', error)
    ElMessage.error('执行任务失败')
  }
}

// 删除任务
const handleDeleteJob = async (jobId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该任务吗？此操作不可恢复！',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    const response = await scheduleApi.deleteJob(jobId)
    if (response.code === 200) {
      ElMessage.success('删除任务成功')
      fetchJobList()
    } else {
      ElMessage.error('删除任务失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 打开清理日志对话框
const openCleanLogDialog = () => {
  cleanLogDialogVisible.value = true
}

// 清理日志
const handleCleanLogs = async () => {
  try {
    let days = cleanLogForm.days
    if (cleanLogForm.cleanType === 2) {
      // 清理所有日志时，传递 0 给后端
      days = 0
    }
    
    const response = await scheduleApi.cleanLogs(days)
    if (response.code === 200) {
      ElMessage.success(`清理日志成功，共清理 ${response.data} 条记录`)
      cleanLogDialogVisible.value = false
      fetchLogList()
    } else {
      ElMessage.error('清理日志失败')
    }
  } catch (error) {
    console.error('清理日志失败:', error)
    ElMessage.error('清理日志失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchJobList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchJobList()
}

// 初始化
onMounted(() => {
  fetchJobList()
  fetchLogList()
})
</script>

<style scoped>
.schedule-job-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-warning {
  color: #f56c6c;
}
</style>
