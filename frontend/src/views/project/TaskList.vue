<template>
  <div class="task-list" style="width: 100%; height: 100%;">
    <el-card style="width: 100%; height: 100%;">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button v-if="canCreateTask" type="primary" @click="handleAdd">新建任务</el-button>
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
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.taskName" placeholder="请输入任务名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="searchForm.assigneeName" placeholder="请输入负责人" clearable style="width: 150px;" />
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
              v-if="row.hasPermission " 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.hasPermission " 
              type="success" 
              size="small" 
              @click="handleUpdateProgress(row)"
            >
              更新进度
            </el-button>
            <el-button type="info" size="small" @click="handleViewTaskDetail(row)">详情</el-button>
            <el-button v-if="row.hasPermission" type="info" size="small" @click="handleViewBindings(row)">绑定</el-button>
            <el-button 
              v-if="row.hasPermission " 
              type="warning" 
              size="small" 
              @click="handlePin(row)"
              style="min-width: 70px;"
            >
              {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button 
              v-if="row.hasDeletePermission " 
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1200px"
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
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 120px;">
            <el-option label="未开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已暂停" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度">
          <el-slider v-model="form.progress" :min="0" :max="100" show-input style="width: 300px;" />
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-input
            v-model="assigneeName"
            placeholder="请选择负责人"
            readonly
            prefix-icon="UserFilled"
            @click="assigneeDialogVisible = true"
            style="cursor: pointer;"
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
        <el-form-item label="是否重点" prop="isFocus">
          <el-switch
            v-model="form.isFocus"
            :active-value="1"
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <RichTextEditor v-model="form.description" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="附件">
          <FileUpload
            v-model:fileList="form.attachments"
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
          />
        </el-form-item>
        <el-form-item label="参与人">
          <el-input
            v-model="stakeholderNames"
            placeholder="请选择参与人"
            readonly
            prefix-icon="UserFilled"
            @click="stakeholderDialogVisible = true"
            style="cursor: pointer;"
          />
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

    <el-dialog
      v-model="progressUpdateDialogVisible"
      :title="`更新任务进展 - ${currentTaskForUpdate?.taskName || ''}`"
      width="1500px"
    >
      <!-- 任务基本信息（只读） -->
      <div class="task-info-panel" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
        <h4 style="margin-bottom: 10px;">任务信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="任务名称">{{ currentTaskForUpdate?.taskName }}</el-descriptions-item>
              <el-descriptions-item label="所属项目">{{ currentTaskForUpdate?.projectName }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ currentTaskForUpdate?.ownerName }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="优先级">{{ getTaskPriorityText(currentTaskForUpdate?.priority) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ getTaskStatusText(currentTaskForUpdate?.status) }}</el-descriptions-item>
              <el-descriptions-item label="当前进度">{{ currentTaskForUpdate?.progress }}%</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>

      <!-- 任务描述 -->
      <div class="task-description" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">任务描述</h4>
        <div class="description-content" v-if="currentTaskForUpdate?.description" v-html="currentTaskForUpdate.description"></div>
        <div v-else class="no-data">暂无任务描述</div>
      </div>

      <!-- 附件列表 -->
      <div class="task-attachments" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">附件</h4>
        <AttachmentList :attachments="currentTaskForUpdate?.attachments || []" />
      </div>

      <!-- 干系人列表 -->
      <div class="task-stakeholders" style="margin-bottom: 20px;">
        <h4 style="margin-bottom: 10px;">干系人</h4>
        <div v-if="currentTaskForUpdate?.stakeholders && currentTaskForUpdate.stakeholders.length > 0" class="stakeholders-container">
          <el-tag v-for="(stakeholder, index) in currentTaskForUpdate.stakeholders" :key="index" style="margin-right: 8px; margin-bottom: 8px;">
            {{ stakeholder }}
          </el-tag>
        </div>
        <div v-else class="no-data">暂无干系人</div>
      </div>

      <!-- 进度更新表单 -->
      <el-form label-width="100px">
        <el-form-item label="更新进度">
          <el-slider v-model="progressUpdateForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="进展描述">
          <RichTextEditor v-model="progressUpdateForm.description" placeholder="请输入进展描述" />
        </el-form-item>
        <el-form-item label="附件">
          <FileUpload
            v-model:fileList="progressUpdateForm.attachments"
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
          />
        </el-form-item>
      </el-form>

      <!-- 操作按钮区域 -->
      <div style="margin: 20px 0; display: flex; justify-content: flex-end; gap: 10px;">
        <el-button @click="progressUpdateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgressUpdate">确定</el-button>
      </div>

      <!-- 进展历史时间线 -->
      <div style="margin-top: 30px;">
        <h4 style="margin-bottom: 15px;">进展历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(update, index) in progressUpdates"
            :key="index"
            :timestamp="formatDateTime(update.createTime)"
            type="primary"
            placement="top"
          >
            <el-card>
              <div class="update-content">
                <div class="update-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                  <span style="font-weight: bold;">{{ update.employeeName }}</span>
                  <span style="color: #606266;">进度更新至 {{ update.progress }}%</span>
                </div>
                <div class="update-description" v-if="update.description" style="margin-bottom: 10px;" v-html="update.description"></div>
                <div class="update-attachments" v-if="update.attachmentList && update.attachmentList.length > 0">
                  <el-divider content-position="left">附件</el-divider>
                  <AttachmentList :attachments="update.attachmentList" />
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

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

    <!-- 负责人选择对话框 -->
    <el-dialog
      v-model="assigneeDialogVisible"
      title="选择负责人"
      width="900px"
      max-width="90vw"
    >
      <div style="padding: 10px;">
        <PersonSelector
          v-model="selectedAssignees"
          style="height: 500px;"
        />
      </div>
      <template #footer>
        <el-button @click="assigneeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssigneeSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 干系人选择对话框 -->
    <el-dialog
      v-model="stakeholderDialogVisible"
      title="选择参与人"
      width="900px"
      max-width="90vw"
    >
      <div style="padding: 10px;">
        <PersonSelector
          v-model="selectedStakeholders"
          style="height: 500px;"
        />
      </div>
      <template #footer>
        <el-button @click="stakeholderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStakeholderSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElPopover } from 'element-plus'
import { Star, Document, UserFilled, TrendCharts } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskDetail from '@/components/TaskDetail.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileUpload from '@/components/FileUpload.vue'
import PersonSelector from '@/components/PersonSelector.vue'
import BindCustomRowDialog from '@/components/BindCustomRowDialog.vue'
import AttachmentList from '@/components/AttachmentList.vue'
import { getTaskPage, createTask, updateTask, deleteTask, updateProgress, pinTask, getProjectTasks, createProgressUpdate, getTaskProgressUpdates, hasTaskPermission, hasTaskDeletePermission, getTaskDetail } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { getTaskBindings, unbindCustomRow, getCustomTableColumns } from '@/api/customTable'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus, formatDateTime, sortTasks, getStatusByProgress, getProgressByStatus } from '@/utils/taskUtils'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const projectList = ref([])
const employeeList = ref([])
const taskTreeData = ref([])
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const bindDialogVisible = ref(false)
const showBindRowDialog = ref(false)
const taskDetailDialogVisible = ref(false)
const taskBindings = ref([])
const tableColumnMap = ref(new Map())
const currentTask = ref(null)
const currentTaskForDetail = ref(null)
const dialogTitle = ref('新建任务')
const formRef = ref(null)
const progressUpdateDialogVisible = ref(false)
const currentTaskForUpdate = ref(null)
const progressUpdateForm = reactive({
  taskId: null,
  progress: 0,
  description: '',
  attachments: []
})
const progressUpdates = ref([])

// 负责人选择
const assigneeDialogVisible = ref(false)
const selectedAssignees = ref([])

// 干系人选择
const stakeholderDialogVisible = ref(false)
const selectedStakeholders = ref([])

const searchForm = reactive({
  projectId: null,
  taskName: '',
  assigneeName: '',
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
  taskCode: '',
  priority: 2,
  status: 1,
  progress: 0,
  assigneeId: null,
  startDate: '',
  endDate: '',
  description: '',
  stakeholders: [],
  attachments: [],
  isFocus: 0
})

// 保存编辑任务时的原始状态
let originalTaskStatus = null

// 保存编辑任务时的原始进度
let originalTaskProgress = null

// 监听状态变化，自动调整进度（新建和编辑时都生效）
watch(() => form.status, (newStatus) => {
  const newProgress = getProgressByStatus(newStatus, form.progress)
  if (newProgress !== null) {
    form.progress = newProgress
  }
})

// 监听进度变化，自动调整状态（新建和编辑时都生效）
watch(() => form.progress, (newProgress) => {
  const newStatus = getStatusByProgress(newProgress)
  form.status = newStatus
})

const progressForm = reactive({
  taskId: null,
  progress: 0
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

// 计算属性：负责人名称
const assigneeName = computed(() => {
  if (!form.assigneeId) return ''
  const employee = employeeList.value.find(emp => emp.id === form.assigneeId)
  return employee ? employee.employeeName : ''
})

// 计算属性：干系人名称
const stakeholderNames = computed(() => {
  if (!form.stakeholders || !Array.isArray(form.stakeholders)) return ''
  const names = form.stakeholders.map(id => {
    const employee = employeeList.value.find(emp => {
      // 考虑ID类型不匹配的情况，进行类型转换后比较
      return String(emp.id) === String(id)
    })
    return employee ? employee.employeeName : id
  })
  return names.join(', ')
})

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



const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const data = await getTaskPage(params)
    const tasks = data.records || []
    
    // 为每个任务计算权限状态
    if (userStore.employeeId) {
      for (const task of tasks) {
        try {
          task.hasPermission = await hasTaskPermission(task.id, userStore.employeeId)
          task.hasDeletePermission = await hasTaskDeletePermission(task.id, userStore.employeeId)
        } catch (error) {
          console.error('检查任务权限失败', error)
          task.hasPermission = false
          task.hasDeletePermission = false
        }
      }
    } else {
      // 如果没有employeeId，设置所有任务都没有权限
      for (const task of tasks) {
        task.hasPermission = false
        task.hasDeletePermission = false
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

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000, showArchived: true })
    projectList.value = data.records || []
    // 如果没有指定项目ID，默认选择第一个项目
    if (projectList.value.length > 0 && !searchForm.projectId) {
      searchForm.projectId = projectList.value[0].id
    }
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

const handleParentChange = (val) => {
  form.parentId = val || 0
}

// 确认负责人选择
const confirmAssigneeSelection = () => {
  if (selectedAssignees.value.length > 0) {
    form.assigneeId = selectedAssignees.value[0].id
  }
  assigneeDialogVisible.value = false
}

// 确认干系人选择
const confirmStakeholderSelection = () => {
  form.stakeholders = selectedStakeholders.value.map(item => item.id)
  stakeholderDialogVisible.value = false
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleProjectChange = () => {
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
  } else if (projectList.value.length > 0) {
    // 如果searchForm.projectId为null，直接使用项目列表中的第一个项目
    form.projectId = projectList.value[0].id
    loadTaskTree(projectList.value[0].id)
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  if (!(await hasPermission(row))) {
    ElMessage.warning('您没有权限编辑此任务')
    return
  }
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  form.parentId = row.parentId || 0
  form.parentIdPath = row.parentId && row.parentId > 0 ? row.parentId : null
  form.assigneeId = row.assigneeId || row.ownerId || null
  form.progress = row.progress || 0
  
  // 保存原始状态和进度，用于判断是否需要自动调整
  originalTaskStatus = row.status
  originalTaskProgress = row.progress || 0
  
  // 确保attachments是数组类型
  if (row.attachments) {
    if (typeof row.attachments === 'string') {
      try {
        form.attachments = JSON.parse(row.attachments)
      } catch (error) {
        console.error('解析附件数据失败', error)
        form.attachments = []
      }
    } else if (!Array.isArray(row.attachments)) {
      form.attachments = []
    }
  } else {
    form.attachments = []
  }
  
  // 确保stakeholders是数组类型
  if (row.stakeholders) {
    if (typeof row.stakeholders === 'string') {
      try {
        form.stakeholders = JSON.parse(row.stakeholders)
      } catch (error) {
        console.error('解析干系人数据失败', error)
        form.stakeholders = []
      }
    } else if (!Array.isArray(row.stakeholders)) {
      form.stakeholders = []
    }
  } else {
    form.stakeholders = []
  }
  
  // 初始化选中的负责人和干系人
  selectedAssignees.value = []
  if (form.assigneeId) {
    const assignee = employeeList.value.find(emp => emp.id === form.assigneeId)
    if (assignee) {
      selectedAssignees.value = [assignee]
    }
  }
  
  selectedStakeholders.value = []
  if (form.stakeholders && Array.isArray(form.stakeholders)) {
    selectedStakeholders.value = employeeList.value.filter(emp => 
      form.stakeholders.includes(emp.id)
    )
  }
  
  // 加载任务树时排除当前任务
  loadTaskTree(row.projectId, row.id)
  dialogVisible.value = true
}

const handleUpdateProgress = async (row) => {
  if (!(await hasPermission(row))) {
    ElMessage.warning('您没有权限更新此任务进度')
    return
  }
  
  try {
    // 获取完整的任务详情数据
    const taskDetail = await getTaskDetail(row.id)
    // 处理附件数据
    if (taskDetail.attachments) {
      if (typeof taskDetail.attachments === 'string') {
        try {
          taskDetail.attachments = JSON.parse(taskDetail.attachments)
        } catch (error) {
          console.error('解析附件数据失败', error)
          taskDetail.attachments = []
        }
      } else if (!Array.isArray(taskDetail.attachments)) {
        taskDetail.attachments = []
      }
    } else {
      taskDetail.attachments = []
    }
    
    // 处理干系人数据
    if (taskDetail.stakeholders) {
      if (typeof taskDetail.stakeholders === 'string') {
        try {
          taskDetail.stakeholders = JSON.parse(taskDetail.stakeholders)
        } catch (error) {
          console.error('解析干系人数据失败', error)
          taskDetail.stakeholders = []
        }
      } else if (!Array.isArray(taskDetail.stakeholders)) {
        taskDetail.stakeholders = []
      }
    } else {
      taskDetail.stakeholders = []
    }
    
    // 将干系人ID转换为名称
    if (taskDetail.stakeholders && Array.isArray(taskDetail.stakeholders)) {
      taskDetail.stakeholders = taskDetail.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
    
    currentTaskForUpdate.value = taskDetail
  } catch (error) {
    console.error('加载任务详情失败', error)
    // 如果API调用失败，使用表格中的行数据作为备用
    currentTaskForUpdate.value = row
    // 处理备用数据中的附件和干系人
    if (row.attachments) {
      if (typeof row.attachments === 'string') {
        try {
          row.attachments = JSON.parse(row.attachments)
        } catch (error) {
          console.error('解析附件数据失败', error)
          row.attachments = []
        }
      } else if (!Array.isArray(row.attachments)) {
        row.attachments = []
      }
    } else {
      row.attachments = []
    }
    
    if (row.stakeholders) {
      if (typeof row.stakeholders === 'string') {
        try {
          row.stakeholders = JSON.parse(row.stakeholders)
        } catch (error) {
          console.error('解析干系人数据失败', error)
          row.stakeholders = []
        }
      } else if (!Array.isArray(row.stakeholders)) {
        row.stakeholders = []
      }
    } else {
      row.stakeholders = []
    }
    
    // 将干系人ID转换为名称
    if (row.stakeholders && Array.isArray(row.stakeholders)) {
      row.stakeholders = row.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
  }
  
  progressUpdateForm.taskId = currentTaskForUpdate.value.id
  progressUpdateForm.progress = currentTaskForUpdate.value.progress || 0
  progressUpdateForm.description = ''
  progressUpdateForm.attachments = []
  
  // 加载任务的进展历史
  await loadProgressUpdates(currentTaskForUpdate.value.id)
  
  progressUpdateDialogVisible.value = true
}

const handleViewTaskDetail = async (row) => {
  try {
    const taskDetail = await getTaskDetail(row.id)
    // 处理附件数据
    if (taskDetail.attachments) {
      if (typeof taskDetail.attachments === 'string') {
        try {
          taskDetail.attachments = JSON.parse(taskDetail.attachments)
        } catch (error) {
          console.error('解析附件数据失败', error)
          taskDetail.attachments = []
        }
      } else if (!Array.isArray(taskDetail.attachments)) {
        taskDetail.attachments = []
      }
    } else {
      taskDetail.attachments = []
    }
    
    // 处理干系人数据
    if (taskDetail.stakeholders) {
      if (typeof taskDetail.stakeholders === 'string') {
        try {
          taskDetail.stakeholders = JSON.parse(taskDetail.stakeholders)
        } catch (error) {
          console.error('解析干系人数据失败', error)
          taskDetail.stakeholders = []
        }
      } else if (!Array.isArray(taskDetail.stakeholders)) {
        taskDetail.stakeholders = []
      }
    } else {
      taskDetail.stakeholders = []
    }
    
    // 将干系人ID转换为名称
    if (taskDetail.stakeholders && Array.isArray(taskDetail.stakeholders)) {
      taskDetail.stakeholders = taskDetail.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
    
    currentTaskForDetail.value = taskDetail
    taskDetailDialogVisible.value = true
  } catch (error) {
    console.error('加载任务详情失败', error)
    ElMessage.error('加载任务详情失败')
    // 如果API调用失败，使用表格中的行数据作为备用
    currentTaskForDetail.value = row
    // 处理备用数据中的附件和干系人
    if (row.attachments) {
      if (typeof row.attachments === 'string') {
        try {
          row.attachments = JSON.parse(row.attachments)
        } catch (error) {
          console.error('解析附件数据失败', error)
          row.attachments = []
        }
      } else if (!Array.isArray(row.attachments)) {
        row.attachments = []
      }
    } else {
      row.attachments = []
    }
    
    if (row.stakeholders) {
      if (typeof row.stakeholders === 'string') {
        try {
          row.stakeholders = JSON.parse(row.stakeholders)
        } catch (error) {
          console.error('解析干系人数据失败', error)
          row.stakeholders = []
        }
      } else if (!Array.isArray(row.stakeholders)) {
        row.stakeholders = []
      }
    } else {
      row.stakeholders = []
    }
    
    // 将干系人ID转换为名称
    if (row.stakeholders && Array.isArray(row.stakeholders)) {
      row.stakeholders = row.stakeholders.map(stakeholderId => {
        const employee = employeeList.value.find(emp => emp.id === stakeholderId)
        return employee ? employee.employeeName : stakeholderId
      })
    }
    taskDetailDialogVisible.value = true
  }
}

const loadProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    progressUpdates.value = data || []
  } catch (error) {
    console.error('加载任务进展历史失败', error)
    ElMessage.error('加载任务进展历史失败')
  }
}



const handleSubmitProgressUpdate = async () => {
  try {
    // 准备附件数据
    let attachmentsJson = null
    if (progressUpdateForm.attachments && Array.isArray(progressUpdateForm.attachments) && progressUpdateForm.attachments.length > 0) {
      const attachments = progressUpdateForm.attachments.map(file => ({
        name: file.name,
        url: file.url || '',
        previewUrl: file.previewUrl || '',
        type: file.type,
        size: file.size
      }))
      attachmentsJson = JSON.stringify(attachments)
    }
    
    // 提交进展更新
    const updateData = {
      taskId: progressUpdateForm.taskId,
      employeeId: userStore.employeeId,
      progress: progressUpdateForm.progress,
      description: progressUpdateForm.description || ''
    }
    
    // 只有当有附件时才添加 attachments 字段
    if (attachmentsJson !== null) {
      updateData.attachments = attachmentsJson
    }
    
    await createProgressUpdate(updateData)
    ElMessage.success('更新任务进展成功')
    
    // 重新加载数据
    progressUpdateDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新任务进展失败', error)
    ElMessage.error('更新任务进展失败')
  }
}



const handlePreview = (file) => {
  // 实际项目中应该打开文件预览
}

const handleRemove = (file, fileList) => {
  // 实际项目中应该处理文件删除逻辑
}

// 分片上传配置
const CHUNK_SIZE = 1024 * 1024 * 5 // 5MB 分片大小

const handleCustomUpload = async (options) => {
  const { file, onSuccess, onError, onProgress } = options
  try {
    // 生成文件唯一标识
    const fileHash = await generateFileHash(file)
    const fileName = file.name
    const fileSize = file.size
    const chunkCount = Math.ceil(fileSize / CHUNK_SIZE)
    
    // 检查文件是否已存在
    const checkResponse = await request.post('/file/check', {
      fileHash,
      fileName,
      fileSize
    })
    
    if (checkResponse && checkResponse.exists) {
      // 文件已存在，直接返回结果
      const uploadedFile = {
        uid: file.uid,
        name: file.name,
        url: checkResponse.fileUrl,
        previewUrl: checkResponse.previewUrl,
        filePath: checkResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 查找并替换文件列表中的文件
      const index = progressUpdateForm.attachments.findIndex(item => item.uid === file.uid)
      if (index !== -1) {
        progressUpdateForm.attachments[index] = uploadedFile
      } else {
        progressUpdateForm.attachments.push(uploadedFile)
      }
      
      ElMessage.success('文件上传成功')
      onSuccess({ 
        status: 'success', 
        data: checkResponse 
      })
      return
    }
    
    // 分片上传
    const uploadedChunks = []
    for (let i = 0; i < chunkCount; i++) {
      const start = i * CHUNK_SIZE
      const end = Math.min(start + CHUNK_SIZE, fileSize)
      const chunk = file.slice(start, end)
      
      const formData = new FormData()
      formData.append('file', chunk)
      formData.append('fileHash', fileHash)
      formData.append('fileName', fileName)
      formData.append('chunkIndex', i)
      formData.append('chunkCount', chunkCount)
      formData.append('chunkSize', CHUNK_SIZE)
      
      // 上传分片
      await request.post('/file/upload-chunk', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          const percent = Math.round((i * CHUNK_SIZE + progressEvent.loaded) / fileSize * 100)
          if (onProgress) {
            onProgress({ percent })
          }
        }
      })
      
      uploadedChunks.push(i)
    }
    
    // 合并分片
    const mergeResponse = await request.post('/file/merge', {
      fileHash,
      fileName,
      chunkCount
    })
    
    // 更新文件列表
    if (mergeResponse) {
      const uploadedFile = {
        uid: file.uid,
        name: file.name,
        url: mergeResponse.fileUrl,
        previewUrl: mergeResponse.previewUrl,
        filePath: mergeResponse.filePath,
        type: file.type,
        size: file.size
      }
      
      // 查找并替换文件列表中的文件
      const index = progressUpdateForm.attachments.findIndex(item => item.uid === file.uid)
      if (index !== -1) {
        progressUpdateForm.attachments[index] = uploadedFile
      } else {
        progressUpdateForm.attachments.push(uploadedFile)
      }
      
      ElMessage.success('文件上传成功')
    }
    
    // 构造el-upload组件期望的响应格式
    onSuccess({ 
      status: 'success', 
      data: mergeResponse 
    })
  } catch (error) {
    console.error('文件上传失败', error)
    ElMessage.error('文件上传失败')
    onError(error)
  }
}

// 生成文件哈希
const generateFileHash = (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const arrayBuffer = e.target.result
      const uint8Array = new Uint8Array(arrayBuffer)
      let hash = 0
      for (let i = 0; i < uint8Array.length; i++) {
        hash = ((hash << 5) - hash) + uint8Array[i]
        hash = hash & hash
      }
      resolve(Math.abs(hash).toString(16) + '_' + Date.now())
    }
    reader.readAsArrayBuffer(file)
  })
}

const handleUploadSuccess = (uploadedFile) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
}

const beforeUpload = (file) => {
  // 检查文件类型
  const allowedTypes = [
    'image/jpeg', 'image/png', // 图片
    'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // Word
    'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // Excel
    'application/pdf', // PDF
    'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', // PPT
    'application/zip', 'application/x-zip-compressed' // ZIP
  ]
  
  const isAllowedType = allowedTypes.includes(file.type)
  if (!isAllowedType) {
    ElMessage.error('只能上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件')
    return false
  }
  
  // 检查文件大小（增加到10MB）
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

const handlePin = async (row) => {
  if (!(await hasPermission(row))) {
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
  if (!(await hasDeletePermission(row))) {
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

const handleViewBindings = async (row) => {
  currentTask.value = row
  await loadTaskBindings(row.id)
  bindDialogVisible.value = true
}

const getColumnLabel = (tableId, columnCode) => {
  const columnMap = tableColumnMap.value.get(tableId)
  if (columnMap && columnMap.has(columnCode)) {
    return columnMap.get(columnCode)
  }
  return columnCode
}

const loadTaskBindings = async (taskId) => {
  try {
    const data = await getTaskBindings(taskId)
    const bindings = data || []
    
    for (const binding of bindings) {
      const tableId = binding.tableId
      if (tableId && !tableColumnMap.value.has(tableId)) {
        try {
          const columns = await getCustomTableColumns(tableId)
          const columnMap = new Map()
          if (columns && Array.isArray(columns)) {
            columns.forEach(col => {
              columnMap.set(col.columnCode, col.columnName)
            })
          }
          tableColumnMap.value.set(tableId, columnMap)
        } catch (err) {
          console.error('获取表格列配置失败', err)
        }
      }
    }
    
    taskBindings.value = bindings.map(binding => ({
      ...binding,
      rowData: binding.rowData ? JSON.parse(binding.rowData) : {}
    }))
  } catch (error) {
    ElMessage.error('加载绑定数据失败')
    taskBindings.value = []
  }
}

const handleBindSuccess = () => {
  showBindRowDialog.value = false
  if (currentTask.value) {
    loadTaskBindings(currentTask.value.id)
  }
}

const handleUnbind = async (bindingId) => {
  if (!(await hasPermission(currentTask.value))) {
    ElMessage.warning('您没有权限解除此任务的绑定')
    return
  }
  try {
    await ElMessageBox.confirm('确定要解除绑定吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unbindCustomRow(currentTask.value.id, bindingId)
    ElMessage.success('解除绑定成功')
    if (currentTask.value) {
      loadTaskBindings(currentTask.value.id)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解除绑定失败')
    }
  }
}

const hasPermission = async (task) => {
  if (!userStore.employeeId || !task || !task.id) {
    return false
  }
  try {
    const result = await hasTaskPermission(task.id, userStore.employeeId)
    return result
  } catch (error) {
    console.error('检查任务权限失败', error)
    return false
  }
}

const hasDeletePermission = async (task) => {
  if (!userStore.employeeId || !task || !task.id) {
    return false
  }
  try {
    const result = await hasTaskDeletePermission(task.id, userStore.employeeId)
    return result
  } catch (error) {
    console.error('检查任务删除权限失败', error)
    return false
  }
}

const canCreateTask = computed(() => {
  if (!searchForm.projectId) return false
  const currentProject = projectList.value.find(p => String(p.id) === String(searchForm.projectId))
  if (!currentProject) return false
  
  // 项目负责人可以创建任务
  if (String(currentProject.ownerId) === String(userStore.employeeId)) return true
  
  // 项目代理负责人可以创建任务
  if (currentProject.deputyOwnerIds && Array.isArray(currentProject.deputyOwnerIds)) {
    if (currentProject.deputyOwnerIds.some(id => String(id) === String(userStore.employeeId))) return true
  }
  
  // 项目参与人员可以创建任务
  if (currentProject.participants) {
    let participants
    if (typeof currentProject.participants === 'string') {
      try {
        participants = JSON.parse(currentProject.participants)
      } catch (e) {
        participants = []
      }
    } else {
      participants = currentProject.participants
    }
    return Array.isArray(participants) && participants.some(id => String(id) === String(userStore.employeeId))
  }
  
  return false
})

const isImageType = (fileName) => {
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  const ext = fileName.toLowerCase().substring(fileName.lastIndexOf('.'))
  return imageExtensions.includes(ext)
}

const handleAttachmentPreview = (attachment) => {
  // 强制使用KKFileView预览URL
  if (attachment.previewUrl) {
    window.open(attachment.previewUrl, '_blank')
  } else {
    window.open(attachment.url, '_blank')
  }
}

const handleAttachmentDownload = (attachment) => {
  // 将 preview 接口改为 download 接口
  let downloadUrl = attachment.url
  if (downloadUrl.includes('/file/preview')) {
    downloadUrl = downloadUrl.replace('/file/preview', '/file/download')
  }
  
  // 如果 URL 中已经有 fileName 参数，先移除它
  if (downloadUrl.includes('fileName=')) {
    const urlObj = new URL(downloadUrl, window.location.origin)
    urlObj.searchParams.delete('fileName')
    downloadUrl = urlObj.toString()
  }
  
  // 添加 fileName 参数
  if (attachment.name) {
    const separator = downloadUrl.includes('?') ? '&' : '?'
    downloadUrl += `${separator}fileName=${encodeURIComponent(attachment.name)}`
  }
  
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 准备附件数据
    let attachmentsJson = null
    if (form.attachments && Array.isArray(form.attachments) && form.attachments.length > 0) {
      const attachments = form.attachments.map(file => ({
        name: file.name,
        url: file.url || '',
        previewUrl: file.previewUrl || '',
        type: file.type,
        size: file.size
      }))
      attachmentsJson = JSON.stringify(attachments)
    }
    
    // 准备干系人数据
    let stakeholdersJson = null
    if (form.stakeholders && Array.isArray(form.stakeholders) && form.stakeholders.length > 0) {
      stakeholdersJson = JSON.stringify(form.stakeholders)
    }
    
    // 准备提交数据
    const submitData = {
      ...form,
      // 确保attachments是字符串类型
      attachments: attachmentsJson || '',
      // 确保stakeholders是字符串类型
      stakeholders: stakeholdersJson || ''
    }
    
    if (form.id) {
      await updateTask(submitData)
      ElMessage.success('更新成功')
    } else {
      await createTask(submitData)
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
  formRef.value?.resetFields()
  form.id = null
  form.projectId = null
  form.parentId = 0
  form.parentIdPath = null
  form.taskName = ''
  form.taskCode = ''
  form.priority = 2
  form.status = 1
  form.progress = 0
  form.assigneeId = null
  form.startDate = ''
  form.endDate = ''
  form.description = ''
  form.stakeholders = []
  form.attachments = []
  
  // 重置原始状态和进度
  originalTaskStatus = null
  originalTaskProgress = null
  
  // 重置选择的人员
  selectedAssignees.value = []
  selectedStakeholders.value = []
}

onMounted(async () => {
  if (route.query.projectId) {
    searchForm.projectId = parseInt(route.query.projectId)
  }
  await loadProjectList()
  await loadEmployeeList()
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

.search-form {
  margin-bottom: 20px;
}

.task-name-container {
  display: flex;
  align-items: center;
  padding: 4px 0;
  transition: all 0.3s ease;
}

.task-name-container:hover {
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
  padding-left: 4px;
}

.task-name {
  flex: 1;
  font-size: 14px;
  line-height: 20px;
}

.pinned-task {
  font-weight: 600;
}

.parent-task .task-name {
  font-weight: 500;
  color: #409EFF;
}

/* 自定义树形表格的样式 */
:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: rgba(64, 158, 255, 0.05) !important;
}

:deep(.el-table__expand-icon) {
  font-size: 16px;
  transition: transform 0.3s ease;
}

:deep(.el-table__expand-icon--expanded) {
  transform: rotate(90deg);
}

/* 优化任务优先级和状态的显示 */
:deep(.el-tag) {
  margin-right: 0;
}

/* 优化操作按钮的显示 */
:deep(.el-button--small) {
  margin-right: 8px;
}

/* 任务详情面板样式 */
.task-info-panel {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.task-description {
  margin-bottom: 20px;
}

.task-attachments {
  margin-bottom: 20px;
}

.task-stakeholders {
  margin-bottom: 20px;
}

.attachments-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attachment-item {
  flex: 0 0 calc(33.333% - 10px);
  min-width: 200px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.attachment-file {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.file-icon {
  margin-right: 8px;
  color: #409EFF;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
}

.attachment-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attachment-name {
  font-size: 12px;
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-actions {
  display: flex;
  gap: 4px;
}

.no-data {
  color: #909399;
  font-size: 14px;
  padding: 10px 0;
}

/* 进度更新历史时间线样式 */
:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #909399;
}

.update-content {
  width: 100%;
}

.update-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.update-description {
  margin-bottom: 10px;
  line-height: 1.5;
}

.update-attachments {
  margin-top: 10px;
}

/* 绑定对话框样式 */
.bind-dialog-content {
  padding: 10px 0;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.bind-dialog-content::-webkit-scrollbar {
  width: 8px;
}

.bind-dialog-content::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

.bind-dialog-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

.bind-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 16px;
  position: sticky;
  top: 0;
  background-color: white;
  z-index: 10;
  padding-top: 5px;
}

.bindings-list-scroll {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 450px;
  overflow-y: auto;
  padding-right: 8px;
}

.bindings-list-scroll::-webkit-scrollbar {
  width: 6px;
}

.bindings-list-scroll::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.bindings-list-scroll::-webkit-scrollbar-track {
  background-color: #f5f7fa;
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.binding-table-name {
  font-weight: bold;
  color: #409EFF;
  font-size: 14px;
}

.binding-order-no {
  font-size: 14px;
  color: #67C23A;
  font-weight: 500;
}

.binding-time {
  font-size: 12px;
  color: #909399;
}

.binding-data {
  margin-bottom: 15px;
}

.binding-actions {
  display: flex;
  justify-content: flex-end;
}

.no-bindings {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
