<template>
  <div class="project-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <el-button type="primary" @click="handleAdd">新建项目</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.projectName" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="未开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已暂停" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="searchForm.showArchived">显示归档项目</el-checkbox>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #progress="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
        </template>
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
        <template #priority="{ row }">
          <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
        </template>
        <template #operation="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="handleArchive(row)">归档</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="未开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已暂停" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="ownerId">
          <EmployeeSelector
            v-model="form.ownerId"
            placeholder="请选择负责人"
            @select="handleEmployeeSelect"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="参与人员">
          <el-select
            v-model="form.participants"
            multiple
            placeholder="请选择参与人员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import EmployeeSelector from '@/components/EmployeeSelector.vue'
import { getProjectPage, getProjectDetail, createProject, updateProject, archiveProject, deleteProject } from '@/api/project'
import { getEmployeeList } from '@/api/employee'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const formRef = ref(null)
const employeeList = ref([])

const searchForm = reactive({
  projectName: '',
  status: null,
  showArchived: true
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const form = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  priority: 2,
  status: 1,
  ownerId: null,
  ownerName: '',
  startDate: '',
  endDate: '',
  description: '',
  participants: []
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const columns = [
  { prop: 'projectName', label: '项目名称', minWidth: 150 },
  { prop: 'projectCode', label: '项目编码', width: 120 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'ownerName', label: '负责人', width: 100 },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'operation', label: '操作', width: 220, fixed: 'right', slot: 'operation' }
]

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'primary', 3: 'success', 4: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '未开始', 2: '进行中', 3: '已完成', 4: '已暂停' }
  return texts[status] || '未知'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { 1: '高', 2: '中', 3: '低' }
  return texts[priority] || '未知'
}

const getProgressStatus = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 60) return ''
  if (progress >= 30) return 'warning'
  return 'exception'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      projectName: searchForm.projectName,
      status: searchForm.status,
      showArchived: searchForm.showArchived
    }
    const data = await getProjectPage(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleReset = () => {
  searchForm.projectName = ''
  searchForm.status = null
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

const handleAdd = () => {
  dialogTitle.value = '新建项目'
  resetForm()
  dialogVisible.value = true
}

const handleView = (row) => {
  router.push(`/project/task?projectId=${row.id}`)
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑项目'
  try {
    // 调用getProjectDetail获取完整的项目信息，包括参与人员列表
    const projectDetail = await getProjectDetail(row.id)
    Object.assign(form, projectDetail)
  } catch (error) {
    console.error('获取项目详情失败', error)
    ElMessage.error('获取项目详情失败')
  }
  dialogVisible.value = true
}

const handleArchive = async (row) => {
  try {
    await ElMessageBox.confirm('确定要归档该项目吗？', '提示', {
      type: 'warning'
    })
    await archiveProject(row.id)
    ElMessage.success('归档成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('归档失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？', '提示', {
      type: 'warning'
    })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateProject(form)
      ElMessage.success('更新成功')
    } else {
      await createProject(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.projectName = ''
  form.projectCode = ''
  form.priority = 2
  form.ownerId = null
  form.ownerName = ''
  form.startDate = ''
  form.endDate = ''
  form.description = ''
  form.participants = []
  formRef.value?.resetFields()
}

const handleEmployeeSelect = (row) => {
  // 选择员工
  if (row) {
    form.ownerId = row.id
    form.ownerName = row.employeeName
  } else {
    form.ownerId = null
    form.ownerName = ''
  }
}

const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList()
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

onMounted(() => {
  loadData()
  loadEmployeeList()
})
</script>

<style scoped>
.project-list {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 10px;
}


</style>
