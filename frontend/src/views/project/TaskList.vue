<template>
  <div class="task-list" style="width: 100%; height: 100%;">
    <el-card style="width: 100%; height: 100%;">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button v-if="canCreateTask" type="primary" @click="handleAdd">新建任务</el-button>
        </div>
      </template>

      <!-- 使用 TaskSearchForm 子组件 -->
      <TaskSearchForm
        v-model:search-form="searchForm"
        :project-list="projectList"
        @search="handleSearch"
        @reset="handleReset"
      />

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
        :indent="20"
        :backend-pagination="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="width: 100%;"
      >
        <template #taskName="{ row, level }">
          <div class="task-name-container" :class="{ 'pinned-task': row.isPinned === 1, 'parent-task': row.hasChildren }">
            <el-icon v-if="row.isPinned === 1" style="color: #f56c6c; margin-right: 4px;"><Star /></el-icon>
            <el-icon v-if="row.isFocus === 1" style="color: #e6a23c; margin-right: 4px;"><TrendCharts /></el-icon>
            <span class="task-name">{{ row.taskName }}</span>
            <el-tag v-if="row.isFocus === 1" size="small" type="warning" style="margin-left: 8px;">重点</el-tag>
            <el-tag v-if="row.hasChildren" size="small" type="info" style="margin-left: 8px;">
              {{ row.children ? row.children.length : 0 }}个子任务
            </el-tag>
          </div>
        </template>
        <template #progress="{ row }">
          <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
        </template>
        <template #status="{ row }">
          <el-tag :type="getTaskStatusType(row.status)">{{ getTaskStatusText(row.status) }}</el-tag>
        </template>
        <template #priority="{ row }">
          <el-tag :type="getTaskPriorityType(row.priority)">{{ getTaskPriorityText(row.priority) }}</el-tag>
        </template>
        <template #lastProgressUpdateTime="{ row }">
          {{ formatDateTime(row.lastProgressUpdateTime) }}
        </template>
        <template #operation="{ row }">
          <div style="display: flex; gap: 4px; white-space: nowrap;">
            <el-button 
              v-if="row.hasPermission" 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.hasPermission" 
              type="success" 
              size="small" 
              @click="handleUpdateProgress(row)"
            >
              更新进度
            </el-button>
            <el-button type="info" size="small" @click="handleViewTaskDetail(row)">详情</el-button>
            <el-button v-if="row.hasPermission" type="info" size="small" @click="handleViewBindings(row)">绑定</el-button>
            <el-button 
              v-if="row.hasPermission" 
              type="warning" 
              size="small" 
              @click="handlePin(row)"
              style="min-width: 70px;"
            >
              {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button 
              v-if="row.hasDeletePermission" 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </BaseTable>
    </el-card>

    <!-- 使用 TaskEditDialog 子组件 -->
    <TaskEditDialog
      v-model="editDialogVisible"
      :is-edit="isEditMode"
      :initial-data="currentEditTask"
      :project-list="projectList"
      :task-tree-data="taskTreeData"
      :employee-list="employeeList"
      @submit="handleTaskSubmit"
    />

    <!-- 使用 TaskProgressUpdateDialog 子组件 -->
    <TaskProgressUpdateDialog
      v-model="progressUpdateDialogVisible"
      :task="currentTaskForUpdate"
      :progress-updates="progressUpdates"
      @submit="handleProgressUpdateSubmit"
    />

    <!-- 绑定对话框 -->
    <el-dialog
      v-model="bindDialogVisible"
      :title="`绑定表格数据 - ${currentTask?.taskName || ''}`"
      width="1200px"
    >
      <div class="bind-dialog-content">
        <div class="bind-header">
          <span>已绑定的数据</span>
          <el-button type="primary" @click="showBindRowDialog = true">添加绑定</el-button>
        </div>
        
        <div v-if="taskBindings.length > 0" class="bindings-list">
          <el-card v-for="binding in taskBindings" :key="binding.id" class="binding-item">
            <div class="binding-info">
              <span class="binding-table-name">{{ binding.tableName }}</span>
              <span v-if="binding.orderNo" class="binding-order-no">单号: {{ binding.orderNo }}</span>
              <span v-if="binding.title" class="binding-title">标题: {{ binding.title }}</span>
              <span class="binding-time">{{ formatDateTime(binding.createTime) }}</span>
            </div>
            <div class="binding-data">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item 
                  v-for="(value, key) in binding.rowData" 
                  :key="key" 
                  :label="getColumnLabel(binding.tableId, key)"
                >
                  {{ value }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <div class="binding-actions">
              <el-button type="danger" size="small" @click="handleUnbind(binding.id)">解除绑定</el-button>
            </div>
          </el-card>
        </div>
        
        <div v-else class="no-bindings">
          暂无绑定数据
        </div>
      </div>
    </el-dialog>

    <!-- 绑定表格行对话框 -->
    <BindCustomRowDialog
      v-if="currentTask && currentTask.id"
      v-model="showBindRowDialog"
      :task-id="currentTask.id"
      @success="handleBindSuccess"
    />

    <!-- 任务详情对话框（只读） -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      :title="`任务详情 - ${currentTaskForDetail?.taskName || ''}`"
      width="1500px"
      max-width="1500px"
    >
      <TaskDetail :task="currentTaskForDetail" />
      <template #footer>
        <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, TrendCharts } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskDetail from '@/components/TaskDetail.vue'
import TaskSearchForm from '@/components/TaskSearchForm.vue'
import TaskEditDialog from '@/components/TaskEditDialog.vue'
import TaskProgressUpdateDialog from '@/components/TaskProgressUpdateDialog.vue'
import BindCustomRowDialog from '@/components/BindCustomRowDialog.vue'
import { getTaskPageV1, deleteTaskV1 } from '@/api/pm-v1'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { getTaskBindings, unbindCustomRow, getCustomTableColumns } from '@/api/customTable'
import { pinTask, getProjectTasks, createProgressUpdate, getTaskProgressUpdates } from '@/api/task'
import { useUserStore } from '@/stores/user'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus, formatDateTime, sortTasks } from '@/utils/taskUtils'

const route = useRoute()
const userStore = useUserStore()

// 基础状态
const loading = ref(false)
const tableData = ref([])
const projectList = ref([])
const employeeList = ref([])
const taskTreeData = ref([])
const bindDialogVisible = ref(false)
const showBindRowDialog = ref(false)
const taskDetailDialogVisible = ref(false)
const taskBindings = ref([])
const tableColumnMap = ref(new Map())
const currentTask = ref(null)
const currentTaskForDetail = ref(null)

// 编辑对话框相关
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const currentEditTask = ref(null)

// 进度更新对话框相关
const progressUpdateDialogVisible = ref(false)
const currentTaskForUpdate = ref(null)
const progressUpdates = ref([])

// 搜索表单
const searchForm = reactive({
  projectId: null,
  taskName: '',
  assigneeName: '',
  status: null,
  priority: null
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

// 表格列定义
const columns = [
  { prop: 'taskName', label: '任务名称', minWidth: 150, slot: 'taskName', indent: true },
  { prop: 'projectName', label: '所属项目', width: 180 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'progress', label: '进度', width: 120, slot: 'progress' },
  { prop: 'ownerName', label: '负责人', width: 80 },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'lastProgressUpdateTime', label: '更新时间', width: 160, slot: 'lastProgressUpdateTime' },
  { prop: 'operation', label: '操作', width: 480, fixed: 'right', slot: 'operation' }
]

// 计算属性 - 是否可以创建任务
const canCreateTask = computed(() => {
  return userStore.employeeId !== null
})

// 加载数据 - 使用 V1 API
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const data = await getTaskPageV1(params)
    const tasks = data.records || []
    
    // 处理附件和干系人数据，V1 API 已经带权限，无需单独查询
    for (const task of tasks) {
      if (task.attachments && typeof task.attachments === 'string') {
        task._attachments = task.attachments
        delete task.attachments
      }
      if (task.stakeholders && typeof task.stakeholders === 'string') {
        task._stakeholders = task.stakeholders
        delete task.stakeholders
      }
    }
    
    // 对任务进行排序
    const sortedTasks = sortTasks(tasks)
    
    // 构建树形结构
    tableData.value = buildTaskTree(sortedTasks)
    
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载项目列表
const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000, showArchived: true })
    projectList.value = data.records || []
    if (projectList.value.length > 0 && !searchForm.projectId) {
      searchForm.projectId = projectList.value[0].id
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

// 加载员工列表
const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000)
    employeeList.value = data?.records || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

// 加载任务树
const loadTaskTree = async (projectId, excludeTaskId = null) => {
  if (!projectId) {
    taskTreeData.value = []
    return
  }
  try {
    const data = await getProjectTasks(projectId)
    taskTreeData.value = buildTaskTree(data, 0, excludeTaskId)
  } catch (error) {
    console.error('加载任务树失败', error)
  }
}

// 构建任务树
const buildTaskTree = (tasks, parentId = 0, excludeTaskId = null) => {
  const filteredTasks = tasks.filter(task => task.parentId === parentId && task.id !== excludeTaskId)
  const sortedFilteredTasks = sortTasks(filteredTasks)
  return sortedFilteredTasks.map(task => {
    const children = buildTaskTree(tasks, task.id, excludeTaskId)
    return {
      ...task,
      children,
      hasChildren: children.length > 0
    }
  })
}

// 搜索和重置
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleReset = () => {
  searchForm.projectId = null
  searchForm.taskName = ''
  searchForm.assigneeName = ''
  searchForm.status = null
  searchForm.priority = null
  handleSearch()
}

// 分页事件
const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

// 添加任务
const handleAdd = () => {
  isEditMode.value = false
  currentEditTask.value = null
  if (searchForm.projectId) {
    loadTaskTree(searchForm.projectId)
  } else if (projectList.value.length > 0) {
    loadTaskTree(projectList.value[0].id)
  }
  editDialogVisible.value = true
}

// 编辑任务
const handleEdit = async (row) => {
  if (!row.hasPermission) {
    ElMessage.warning('您没有权限编辑此任务')
    return
  }
  isEditMode.value = true
  currentEditTask.value = { ...row }
  loadTaskTree(row.projectId, row.id)
  editDialogVisible.value = true
}

// 任务提交处理
const handleTaskSubmit = async (submitData) => {
  editDialogVisible.value = false
  ElMessage.success(isEditMode.value ? '任务更新成功' : '任务创建成功')
  loadData()
}

// 更新进度
const handleUpdateProgress = async (row) => {
  if (!row.hasPermission) {
    ElMessage.warning('您没有权限更新此任务进度')
    return
  }
  currentTaskForUpdate.value = { ...row }
  await loadProgressUpdates(row.id)
  progressUpdateDialogVisible.value = true
}

// 进度更新提交处理
const handleProgressUpdateSubmit = async (updateData) => {
  try {
    // 准备附件数据
    let attachmentsJson = null
    if (updateData.attachments && Array.isArray(updateData.attachments) && updateData.attachments.length > 0) {
      const attachments = updateData.attachments.map(file => ({
        name: file.name,
        url: file.url || '',
        previewUrl: file.previewUrl || '',
        type: file.type,
        size: file.size
      }))
      attachmentsJson = JSON.stringify(attachments)
    }
    
    const submitData = {
      taskId: updateData.taskId,
      employeeId: userStore.employeeId,
      progress: updateData.progress,
      description: updateData.description || ''
    }
    
    if (attachmentsJson !== null) {
      submitData.attachments = attachmentsJson
    }
    
    await createProgressUpdate(submitData)
    ElMessage.success('更新任务进展成功')
    progressUpdateDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新任务进展失败', error)
    ElMessage.error('更新任务进展失败')
  }
}

// 查看任务详情
const handleViewTaskDetail = async (row) => {
  currentTaskForDetail.value = { ...row }
  taskDetailDialogVisible.value = true
}

// 加载进度更新
const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载任务进展历史失败', error)
  }
}

// 查看绑定
const handleViewBindings = async (row) => {
  currentTask.value = row
  await loadBindings(row.id)
  bindDialogVisible.value = true
}

// 加载绑定数据
const loadBindings = async (taskId) => {
  try {
    const data = await getTaskBindings(taskId)
    taskBindings.value = (data || []).map(binding => ({
      ...binding,
      rowData: binding.rowData ? JSON.parse(binding.rowData) : {}
    }))
  } catch (error) {
    console.error('加载绑定数据失败', error)
  }
}

// 获取列标签
const getColumnLabel = (tableId, columnKey) => {
  return tableColumnMap.value.get(`${tableId}_${columnKey}`) || columnKey
}

// 解除绑定
const handleUnbind = async (bindingId) => {
  try {
    await ElMessageBox.confirm('确认解除此绑定？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unbindCustomRow(bindingId)
    ElMessage.success('解除绑定成功')
    await loadBindings(currentTask.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解除绑定失败', error)
      ElMessage.error('解除绑定失败')
    }
  }
}

// 绑定成功回调
const handleBindSuccess = () => {
  loadBindings(currentTask.value.id)
}

// 置顶/取消置顶
const handlePin = async (row) => {
  try {
    await pinTask(row.id, row.isPinned !== 1)
    ElMessage.success(row.isPinned === 1 ? '取消置顶成功' : '置顶成功')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 删除任务 - 使用 V1 API
const handleDelete = async (row) => {
  if (!row.hasDeletePermission) {
    ElMessage.warning('您没有权限删除此任务')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除任务"${row.taskName}"？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTaskV1(row.id)
    ElMessage.success('删除任务成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadProjectList(),
    loadEmployeeList()
  ])
  loadData()
})
</script>

<style scoped>
.task-list {
  width: 100%;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.task-name {
  font-weight: 500;
}

.pinned-task .task-name {
  color: #f56c6c;
  font-weight: 600;
}

.bind-dialog-content {
  width: 100%;
}

.bind-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.bindings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.binding-item {
  transition: all 0.3s;
}

.binding-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.binding-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.binding-table-name {
  font-weight: bold;
  color: #409eff;
  font-size: 14px;
}

.binding-order-no,
.binding-title {
  font-size: 13px;
  color: #606266;
}

.binding-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.binding-data {
  margin-bottom: 15px;
}

.binding-actions {
  text-align: right;
}

.no-bindings {
  padding: 20px;
  text-align: center;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
