<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" @click="handleAdd">新建任务</el-button>
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
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="未开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已暂停" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable style="width: 100px;">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
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
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #taskName="{ row }">
          <span :class="{ 'pinned-task': row.isPinned === 1 }">
            <el-icon v-if="row.isPinned === 1" style="color: #f56c6c; margin-right: 4px;"><Star /></el-icon>
            {{ row.taskName }}
          </span>
        </template>
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
          <el-button 
            v-if="hasPermission(row)" 
            link type="primary" 
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasPermission(row)" 
            link type="primary" 
            @click="handleUpdateProgress(row)"
          >
            更新进度
          </el-button>
          <el-button link type="primary" @click="handleViewComments(row)">批注</el-button>
          <el-button 
            v-if="hasPermission(row)" 
            link type="warning" 
            @click="handlePin(row)"
          >
            {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button 
            v-if="hasPermission(row)" 
            link type="danger" 
            @click="handleDelete(row)"
          >
            删除
          </el-button>
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
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" filterable>
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父任务">
          <el-cascader
            v-model="form.parentIdPath"
            :options="taskTreeData"
            :props="{ checkStrictly: true, value: 'id', label: 'taskName', emitPath: false }"
            placeholder="请选择父任务（可选）"
            clearable
            @change="handleParentChange"
          />
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100px;">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-select v-model="form.assigneeId" placeholder="请选择负责人" filterable>
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
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
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="干系人">
          <el-select
            v-model="form.stakeholders"
            multiple
            placeholder="请选择干系人"
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

    <el-dialog
      v-model="progressDialogVisible"
      title="更新进度"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="当前进度">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgress">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="commentDialogVisible"
      :title="`任务批注 - ${currentTask?.taskName || ''}`"
      width="600px"
    >
      <TaskComment v-if="currentTask" :task-id="currentTask.id" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskComment from '@/components/TaskComment.vue'
import { getTaskPage, createTask, updateTask, deleteTask, updateProgress, pinTask, getProjectTasks } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const projectList = ref([])
const employeeList = ref([])
const taskTreeData = ref([])
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const currentTask = ref(null)
const dialogTitle = ref('新建任务')
const formRef = ref(null)

const searchForm = reactive({
  projectId: null,
  status: null,
  priority: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const form = reactive({
  id: null,
  projectId: null,
  parentId: 0,
  parentIdPath: null,
  taskName: '',
  priority: 2,
  assigneeId: null,
  startDate: '',
  endDate: '',
  description: '',
  stakeholders: []
})

const progressForm = reactive({
  taskId: null,
  progress: 0
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 200, slot: 'taskName' },
  { prop: 'projectName', label: '所属项目', width: 120 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 150, slot: 'progress' },
  { prop: 'ownerName', label: '负责人', width: 100 },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'operation', label: '操作', width: 260, fixed: 'right', slot: 'operation' }
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
      ...searchForm
    }
    const data = await getTaskPage(params)
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
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000 })
    projectList.value = data.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
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

const loadTaskTree = async (projectId) => {
  if (!projectId) {
    taskTreeData.value = []
    return
  }
  try {
    const data = await getProjectTasks(projectId)
    taskTreeData.value = buildTaskTree(data)
  } catch (error) {
    console.error('加载任务树失败', error)
  }
}

const buildTaskTree = (tasks, parentId = 0) => {
  return tasks
    .filter(task => task.parentId === parentId)
    .map(task => ({
      ...task,
      children: buildTaskTree(tasks, task.id)
    }))
}

const handleParentChange = (val) => {
  form.parentId = val || 0
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleReset = () => {
  searchForm.projectId = null
  searchForm.status = null
  searchForm.priority = null
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
  dialogTitle.value = '新建任务'
  resetForm()
  if (searchForm.projectId) {
    form.projectId = searchForm.projectId
    loadTaskTree(searchForm.projectId)
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!hasPermission(row)) {
    ElMessage.warning('您没有权限编辑此任务')
    return
  }
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  form.parentId = row.parentId || 0
  form.parentIdPath = row.parentId && row.parentId > 0 ? row.parentId : null
  form.assigneeId = row.assigneeId || row.ownerId || null
  loadTaskTree(row.projectId)
  dialogVisible.value = true
}

const handleUpdateProgress = (row) => {
  if (!hasPermission(row)) {
    ElMessage.warning('您没有权限更新此任务进度')
    return
  }
  progressForm.taskId = row.id
  progressForm.progress = row.progress || 0
  progressDialogVisible.value = true
}

const handlePin = async (row) => {
  if (!hasPermission(row)) {
    ElMessage.warning('您没有权限置顶此任务')
    return
  }
  try {
    const pinned = row.isPinned === 1 ? false : true
    await pinTask(row.id, pinned)
    ElMessage.success(pinned ? '置顶成功' : '取消置顶成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  if (!hasPermission(row)) {
    ElMessage.warning('您没有权限删除此任务')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？删除后将同时删除所有子任务。', '提示', {
      type: 'warning'
    })
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmitProgress = async () => {
  try {
    await updateProgress(progressForm.taskId, progressForm.progress)
    ElMessage.success('更新进度成功')
    progressDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('更新进度失败')
  }
}

const handleViewComments = (row) => {
  currentTask.value = row
  commentDialogVisible.value = true
}

const hasPermission = (task) => {
  // 检查是否是任务负责人
  if (task.assigneeId === userStore.employeeId || task.ownerId === userStore.employeeId) {
    return true
  }
  // 检查是否是授权干系人（这里简化处理，实际应该从后端获取权限信息）
  if (task.stakeholders && task.stakeholders.includes(userStore.employeeId)) {
    return true
  }
  return false
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await updateTask(form)
      ElMessage.success('更新成功')
    } else {
      await createTask(form)
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
  form.projectId = null
  form.parentId = 0
  form.parentIdPath = null
  form.taskName = ''
  form.priority = 2
  form.assigneeId = null
  form.startDate = ''
  form.endDate = ''
  form.description = ''
  form.stakeholders = []
  formRef.value?.resetFields()
}

onMounted(() => {
  if (route.query.projectId) {
    searchForm.projectId = parseInt(route.query.projectId)
  }
  loadData()
  loadProjectList()
  loadEmployeeList()
})
</script>

<style scoped>
.task-list {
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

.pinned-task {
  color: #f56c6c;
  font-weight: bold;
}
</style>
