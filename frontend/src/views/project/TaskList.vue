<template>
  <div class="task-list" style="width: 100%; height: 100%;">
    <el-card style="width: 100%; height: 100%;">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-button type="info" @click="handleOpenExportDialog">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
            <el-button v-if="canCreateTask" type="success" @click="handleBatchCreate">
              <el-icon><DocumentAdd /></el-icon>
              批量新建
            </el-button>
            <el-button v-if="canCreateTask" type="warning" @click="handleCreateShadowTask">
              <el-icon><DocumentCopy /></el-icon>
              创建影子任务
            </el-button>
            <el-button v-if="canCreateTask" type="primary" @click="handleAdd">新建任务</el-button>
          </div>
        </div>
      </template>

      <!-- 使用 TaskSearchForm 子组件 -->
      <TaskSearchForm
        v-model:search-form="searchForm"
        :project-list="projectList"
        @search="handleSearch"
        @reset="handleReset"
        @project-change="handleProjectChange"
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
        :row-key="'id'"
        :backend-pagination="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="width: 100%;"
      >
        <template #taskName="{ row, level }">
          <span>
            <el-icon v-if="row.isPinned === 1" style="color: #f56c6c; margin-right: 4px;"><Star /></el-icon>
            <el-icon v-if="row.isFocus === 1" style="color: #e6a23c; margin-right: 4px;"><TrendCharts /></el-icon>
            <el-tag v-if="row.isShadow === 1" size="small" type="warning" style="margin-right: 8px;">影子</el-tag>
            <span :style="row.isPinned === 1 ? 'color: #f56c6c; font-weight: bold;' : ''">{{ row.taskName }}</span>
            <el-tag v-if="row.isFocus === 1" size="small" type="warning" style="margin-left: 8px;">重点</el-tag>
            <el-tag v-if="row.hasChildren" size="small" type="info" style="margin-left: 8px;">
              {{ row.children ? row.children.length : 0 }}个子任务
            </el-tag>
          </span>
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
        <template #updateTime="{ row }">
            {{ formatDateTime(row.updateTime) }}
        </template>
        <template #lastProgressUpdateTime="{ row }">
            {{ formatDateTime(row.lastProgressUpdateTime) }}
        </template>
        <template #operation="{ row }">
          <div style="display: flex; gap: 4px; white-space: nowrap;">
            <!-- 影子任务的操作 -->
            <template v-if="row.isShadow === 1">
              <el-button type="info" size="small" @click="handleViewShadowDetail(row)">详情</el-button>
              <el-button 
                v-if="row.hasPermission" 
                type="warning" 
                size="small" 
                @click="handleEditShadow(row)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="row.hasDeletePermission" 
                type="danger" 
                size="small" 
                @click="handleDeleteShadow(row)"
              >
                删除
              </el-button>
            </template>
            <!-- 真实任务的操作 -->
            <template v-else>
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
            </template>
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
      :default-project-id="searchForm.projectId"
      @submit="handleTaskSubmit"
    />

    <!-- 使用 TaskProgressUpdateDialog 子组件 -->
    <TaskProgressUpdateDialog
      v-model="progressUpdateDialogVisible"
      :task="currentTaskForUpdate"
      :progress-updates="progressUpdates"
      @submit="handleProgressUpdateSubmit"
    />

    <!-- 批量新建任务对话框 -->
    <BatchCreateTasks
      v-model="batchCreateDialogVisible"
      :project-list="projectList"
      :employee-list="employeeList"
      :default-project-id="searchForm.projectId"
      @submit="handleBatchCreateSubmit"
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

    <!-- 创建影子任务对话框 -->
    <el-dialog
      v-model="createShadowDialogVisible"
      title="创建影子任务"
      width="800px"
    >
      <el-alert title="影子任务说明" type="info" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          影子任务是源任务在当前项目的视图，源任务的进度、状态等信息会实时同步。影子任务可以有自己的别名和描述。
        </template>
      </el-alert>
      <el-form :model="shadowForm" label-width="120px">
        <el-form-item label="源任务" required>
          <el-select v-model="shadowForm.sourceTaskId" placeholder="请选择源任务" style="width: 100%;" filterable>
            <el-option
              v-for="task in allProjectTasks"
              :key="task.id"
              :label="`${task.projectName} - ${task.taskName}`"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标项目" required>
          <el-select v-model="shadowForm.projectId" placeholder="请选择目标项目" style="width: 100%;" filterable @change="loadShadowProjectTaskTree">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父任务">
          <el-tree-select
            v-model="shadowForm.parentId"
            :data="shadowProjectTaskTree"
            :props="{ label: 'taskName', children: 'children', value: 'id' }"
            placeholder="请选择父任务（可选）"
            style="width: 100%;"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="影子别名">
          <el-input v-model="shadowForm.shadowAlias" placeholder="请输入影子别名（可选，留空则显示源任务名称）" />
        </el-form-item>
        <el-form-item label="影子描述">
          <el-input v-model="shadowForm.shadowDescription" type="textarea" :rows="4" placeholder="请输入影子描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createShadowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateShadowSubmit">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑影子任务对话框 -->
    <el-dialog
      v-model="editShadowDialogVisible"
      title="编辑影子任务"
      width="800px"
    >
      <el-alert title="编辑说明" type="info" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #default>
          影子任务可以编辑「父任务」、「影子别名」和「影子描述」，源任务的其他信息（进度、状态等）会自动同步源任务。
        </template>
      </el-alert>
      <el-form :model="shadowForm" label-width="120px">
        <el-form-item label="父任务">
          <el-tree-select
            v-model="shadowForm.parentId"
            :data="shadowProjectTaskTree"
            :props="{ label: 'taskName', children: 'children', value: 'id' }"
            placeholder="请选择父任务（可选）"
            style="width: 100%;"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="影子别名">
          <el-input v-model="shadowForm.shadowAlias" placeholder="请输入影子别名（留空则显示源任务名称）" />
        </el-form-item>
        <el-form-item label="影子描述">
          <el-input v-model="shadowForm.shadowDescription" type="textarea" :rows="4" placeholder="请输入影子描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editShadowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditShadowSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 影子任务详情对话框 -->
    <ShadowTaskDetailDialog
      v-model="shadowDetailDialogVisible"
      :shadow-id="currentShadowId"
      @edit="handleEditShadowFromDetail"
      @delete="handleDeleteShadowFromDetail"
    />

    <!-- 任务进展报告导出对话框 -->
    <TaskProgressReportDialog
      v-model="exportDialogVisible"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, TrendCharts, DocumentAdd, DocumentCopy, Download } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskDetail from '@/components/TaskDetail.vue'
import TaskSearchForm from '@/components/TaskSearchForm.vue'
import TaskEditDialog from '@/components/TaskEditDialog.vue'
import TaskProgressUpdateDialog from '@/components/TaskProgressUpdateDialog.vue'
import BatchCreateTasks from '@/components/BatchCreateTasks.vue'
import BindCustomRowDialog from '@/components/BindCustomRowDialog.vue'
import ShadowTaskDetailDialog from '@/components/ShadowTaskDetailDialog.vue'
import TaskProgressReportDialog from '@/components/TaskProgressReportDialog.vue'
import { getTaskPage, getTaskDetail, deleteTask, pinTask, getProjectTasks, createProgressUpdate, getTaskProgressUpdates, createShadowTask, updateShadowTask, deleteShadowTask, getShadowTaskDetail, getShadowTaskBySource } from '@/api/task'
import { pageTaskListWithShadows } from '@/api/shadowTask'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { getTaskBindings, unbindCustomRow, getCustomTableColumns } from '@/api/customTable'
import { useUserStore } from '@/stores/user'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus, formatDateTime, sortTasks } from '@/utils/taskUtils'

const route = useRoute()
const userStore = useUserStore()

// 基础状态
const loading = ref(false)
const tableData = ref([])
const allTasks = ref([]) // 所有任务（真实+影子）
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

// 批量操作相关
const batchCreateDialogVisible = ref(false)
const selectedTasks = ref([])

// 影子任务相关
const createShadowDialogVisible = ref(false)
const editShadowDialogVisible = ref(false)
const shadowDetailDialogVisible = ref(false)

// 导出对话框相关
const exportDialogVisible = ref(false)
const shadowForm = reactive({
  id: null,
  sourceTaskId: null,
  projectId: null,
  parentId: null,
  shadowAlias: '',
  shadowDescription: ''
})
const currentShadowTask = ref(null)
const currentShadowId = ref(null)
const allProjectTasks = ref([])
const shadowProjectTaskTree = ref([])

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
  { prop: 'taskName', label: '任务名称', minWidth: 280, slot: 'taskName' },
  { prop: 'projectName', label: '所属项目', width: 180 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 90, slot: 'status' },
  { prop: 'progress', label: '进度', width: 120, slot: 'progress' },
  { prop: 'ownerName', label: '负责人', width: 80 },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'updateTime', label: '任务更新时间', width: 160, slot: 'updateTime' },
  { prop: 'lastProgressUpdateTime', label: '进展更新时间', width: 160, slot: 'lastProgressUpdateTime' },
  { prop: 'operation', label: '操作', width: 480, fixed: 'right', slot: 'operation' }
]

// 计算属性 - 是否可以创建任务
const canCreateTask = computed(() => {
  return userStore.employeeId !== null
})

// 获取任务完整名称用于 tooltip
const getTaskFullName = (row) => {
  let fullName = row.taskName || ''
  if (row.isFocus === 1) {
    fullName += ' [重点]'
  }
  if (row.hasChildren) {
    fullName += ` [${row.children ? row.children.length : 0}个子任务]`
  }
  return fullName
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    if (!searchForm.projectId) {
      tableData.value = []
      pagination.total = 0
      return
    }
    
    // 使用新的后端分页 API 加载真实任务 + 影子任务
    const page = await pageTaskListWithShadows({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      projectId: searchForm.projectId,
      taskName: searchForm.taskName || null,
      assigneeName: searchForm.assigneeName || null,
      status: searchForm.status,
      priority: searchForm.priority
    })
    
    const tasks = page.records || []
    pagination.total = page.total || 0
    
    // 处理附件和干系人数据（不删除原始字段）
    for (const task of tasks) {
      if (task.attachments && typeof task.attachments === 'string') {
        task._attachments = task.attachments
      }
      if (task.stakeholders && typeof task.stakeholders === 'string') {
        task._stakeholders = task.stakeholders
      }
      // 添加影子任务标记
      task.isShadow = task.isShadow || 0
      
      // 设置权限 - 优先使用后端返回的权限，否则设为false
      task.hasPermission = task.hasPermission !== null && task.hasPermission !== undefined 
        ? task.hasPermission 
        : false
      task.hasDeletePermission = task.hasDeletePermission !== null && task.hasDeletePermission !== undefined 
        ? task.hasDeletePermission 
        : false
    }
    
    // 构建树形结构用于显示
    tableData.value = buildTaskTree(tasks)
    
  } catch (error) {
    console.error('加载数据失败', error)
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
    // 只有在没有通过 URL 参数设置 projectId 时，才默认选择第一个项目
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

// 加载影子任务的项目任务树
const loadShadowProjectTaskTree = async (projectId) => {
  if (!projectId) {
    shadowProjectTaskTree.value = []
    return
  }
  try {
    const data = await getProjectTasks(projectId)
    shadowProjectTaskTree.value = buildTaskTree(data, 0, null)
  } catch (error) {
    console.error('加载影子项目任务树失败', error)
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

// 处理项目选择变化
const handleProjectChange = (projectId) => {
  // 当项目变化时，更新任务树数据
  if (projectId) {
    loadTaskTree(projectId)
  }
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
const handleOpenExportDialog = () => {
  exportDialogVisible.value = true
}

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

// 批量新建任务
const handleBatchCreate = () => {
  batchCreateDialogVisible.value = true
}

// 批量新建任务提交处理
const handleBatchCreateSubmit = () => {
  loadData()
}

// 加载所有项目的所有任务
const loadAllProjectTasks = async () => {
  try {
    const tasks = []
    for (const project of projectList.value) {
      try {
        const projectTasks = await getProjectTasks(project.id)
        tasks.push(...(projectTasks || []).map(task => ({
          ...task,
          projectName: project.projectName
        })))
      } catch (error) {
        console.error(`加载项目 ${project.projectName} 任务失败`, error)
      }
    }
    allProjectTasks.value = tasks
  } catch (error) {
    console.error('加载所有项目任务失败', error)
  }
}

// 创建影子任务
const handleCreateShadowTask = async () => {
  if (allProjectTasks.value.length === 0) {
    await loadAllProjectTasks()
  }
  // 重置表单
  Object.assign(shadowForm, {
    id: null,
    sourceTaskId: null,
    projectId: searchForm.projectId, // 后端字段是 projectId，不是 targetProjectId
    parentId: null,
    shadowAlias: '', // 后端是 shadowAlias，不是 taskName
    shadowDescription: '' // 后端是 shadowDescription，不是 description
  })
  // 加载任务树
  if (searchForm.projectId) {
    await loadShadowProjectTaskTree(searchForm.projectId)
  } else {
    shadowProjectTaskTree.value = []
  }
  createShadowDialogVisible.value = true
}

// 提交创建影子任务
const handleCreateShadowSubmit = async () => {
  try {
    if (!shadowForm.sourceTaskId) {
      ElMessage.warning('请选择源任务')
      return
    }
    if (!shadowForm.projectId) {
      ElMessage.warning('请选择目标项目')
      return
    }

    // 检查是否已存在相同的影子任务
    try {
      const existing = await getShadowTaskBySource(shadowForm.sourceTaskId)
      if (existing && existing.some(shadow => shadow.projectId === shadowForm.projectId)) {
        ElMessage.warning('该源任务在目标项目中已存在影子任务')
        return
      }
    } catch (error) {
      // 不存在，继续创建
    }

    await createShadowTask({
      sourceTaskId: shadowForm.sourceTaskId,
      projectId: shadowForm.projectId,
      parentId: shadowForm.parentId,
      shadowAlias: shadowForm.shadowAlias,
      shadowDescription: shadowForm.shadowDescription
    })
    ElMessage.success('创建影子任务成功')
    createShadowDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('创建影子任务失败', error)
    ElMessage.error('创建影子任务失败')
  }
}

// 编辑影子任务
const handleEditShadow = async (row) => {
  try {
    const data = await getShadowTaskDetail(row.id)
    currentShadowTask.value = data
    Object.assign(shadowForm, {
      id: data.id,
      parentId: data.parentId,
      shadowAlias: data.shadowAlias,
      shadowDescription: data.shadowDescription
    })
    // 加载任务树
    await loadShadowProjectTaskTree(row.projectId)
    editShadowDialogVisible.value = true
  } catch (error) {
    console.error('获取影子任务详情失败', error)
    ElMessage.error('获取影子任务详情失败')
  }
}

// 提交更新影子任务
const handleEditShadowSubmit = async () => {
  try {
    await updateShadowTask({
      id: shadowForm.id,
      parentId: shadowForm.parentId,
      shadowAlias: shadowForm.shadowAlias,
      shadowDescription: shadowForm.shadowDescription
    })
    ElMessage.success('更新影子任务成功')
    editShadowDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新影子任务失败', error)
    ElMessage.error('更新影子任务失败')
  }
}

// 查看影子任务详情
const handleViewShadowDetail = async (row) => {
  try {
    currentShadowId.value = row.id
    const data = await getShadowTaskDetail(row.id)
    currentShadowTask.value = data
    shadowDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取影子任务详情失败', error)
    ElMessage.error('获取影子任务详情失败')
  }
}

// 删除影子任务
const handleDeleteShadow = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除影子任务"${row.taskName}"？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteShadowTask(row.id)
    ElMessage.success('删除影子任务成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除影子任务失败', error)
      ElMessage.error('删除影子任务失败')
    }
  }
}

// 从详情页编辑影子任务
const handleEditShadowFromDetail = async () => {
  try {
    if (!currentShadowTask.value) {
      const data = await getShadowTaskDetail(currentShadowId.value)
      currentShadowTask.value = data
    }
    shadowDetailDialogVisible.value = false
    Object.assign(shadowForm, {
      id: currentShadowTask.value.id,
      parentId: currentShadowTask.value.parentId,
      shadowAlias: currentShadowTask.value.shadowAlias,
      shadowDescription: currentShadowTask.value.shadowDescription
    })
    // 加载任务树
    await loadShadowProjectTaskTree(currentShadowTask.value.projectId)
    editShadowDialogVisible.value = true
  } catch (error) {
    console.error('打开编辑对话框失败', error)
  }
}

// 从详情页删除影子任务
const handleDeleteShadowFromDetail = async () => {
  try {
    if (!currentShadowTask.value) {
      const data = await getShadowTaskDetail(currentShadowId.value)
      currentShadowTask.value = data
    }
    await deleteShadowTask(currentShadowTask.value.id)
    ElMessage.success('删除影子任务成功')
    shadowDetailDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除影子任务失败', error)
      ElMessage.error('删除影子任务失败')
    }
  }
}

// 表格选择变化处理（保留用于可能的未来扩展）
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 编辑任务
const handleEdit = async (row) => {
  if (!row.hasPermission) {
    ElMessage.warning('您没有权限编辑此任务')
    return
  }
  isEditMode.value = true
  try {
    const data = await getTaskDetail(row.id)
    currentEditTask.value = data || { ...row }
  } catch (error) {
    console.error('获取任务详情失败', error)
    currentEditTask.value = { ...row }
  }
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
        filePath: file.filePath || '',
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
  try {
    const data = await getTaskDetail(row.id)
    currentTaskForDetail.value = data || { ...row }
    taskDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取任务详情失败', error)
    currentTaskForDetail.value = { ...row }
    taskDetailDialogVisible.value = true
  }
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
    
    // 加载所有相关表格的列配置
    const tableIds = [...new Set(taskBindings.value.map(b => b.tableId))]
    for (const tableId of tableIds) {
      const columns = await getCustomTableColumns(tableId)
      for (const column of columns) {
        tableColumnMap.value.set(`${tableId}_${column.columnCode}`, column.columnName)
      }
    }
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
    // 添加参数验证
    if (!bindingId || bindingId === undefined) {
      ElMessage.error('绑定ID无效')
      return
    }
    if (!currentTask.value || !currentTask.value.id) {
      ElMessage.error('任务信息无效')
      return
    }
    
    await ElMessageBox.confirm('确认解除此绑定？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unbindCustomRow(currentTask.value.id, bindingId)
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

// 删除任务
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
    await deleteTask(row.id)
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
    loadEmployeeList()
  ])
  
  // 先加载项目列表
  await loadProjectList()
  
  // 检查 URL 参数中的 projectId，这会覆盖 loadProjectList 中可能设置的默认值
  const projectIdFromUrl = route.query.projectId
  if (projectIdFromUrl) {
    const parsedProjectId = parseInt(projectIdFromUrl, 10)
    if (!isNaN(parsedProjectId)) {
      // 查找是否存在该项目
      const projectExists = projectList.value.some(p => p.id === parsedProjectId)
      if (projectExists) {
        searchForm.projectId = parsedProjectId
      }
    }
  }
  
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

/* 移动端适配 */
@media (max-width: 767px) {
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .card-header > div {
    flex-direction: column;
    width: 100%;
  }

  .card-header .el-input,
  .card-header .el-select,
  .card-header .el-button {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 4px;
  }

  .pagination-container {
    justify-content: center;
  }
}
</style>
