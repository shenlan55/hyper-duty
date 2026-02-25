<template>
  <div class="task-list" style="width: 100%; height: 100%;">
    <el-card style="width: 100%; height: 100%;">
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
        style="width: 100%;"
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
          <el-button type="info" size="small" @click="handleViewComments(row)">批注</el-button>
          <el-button 
            v-if="row.hasPermission " 
            type="warning" 
            size="small" 
            @click="handlePin(row)"
            style="width: 70px;"
          >
            {{ row.isPinned === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button 
            v-if="row.hasPermission " 
            type="danger" 
            size="small" 
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

    <el-dialog
      v-model="progressUpdateDialogVisible"
      :title="`更新任务进展 - ${currentTaskForUpdate?.taskName || ''}`"
      width="1000px"
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
              <el-descriptions-item label="优先级">{{ getPriorityText(currentTaskForUpdate?.priority) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ getStatusText(currentTaskForUpdate?.status) }}</el-descriptions-item>
              <el-descriptions-item label="当前进度">{{ currentTaskForUpdate?.progress }}%</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
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
          <el-upload
            class="upload-demo"
            :http-request="handleCustomUpload"
            :file-list="progressUpdateForm.attachments"
            :auto-upload="true"
            :limit="5"
            :before-upload="beforeUpload"
            list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传JPG/PNG图片、Word、Excel、PDF、PPT和ZIP文件，且不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

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
                  <div class="attachments-container">
                    <div v-for="(attachment, idx) in update.attachmentList" :key="idx" class="attachment-item">
                      <div class="attachment-file">
                        <el-icon class="file-icon"><Document /></el-icon>
                        <span class="file-name">{{ attachment.name || '未知文件' }}</span>
                      </div>
                      <div class="attachment-info">
                        <span class="attachment-name">{{ attachment.name || '未知文件' }}</span>
                        <div class="attachment-actions">
                          <el-button size="small" type="primary" @click="handleAttachmentPreview(attachment)">
                            预览
                          </el-button>
                          <el-button size="small" @click="handleAttachmentDownload(attachment)">
                            下载
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="progressUpdateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProgressUpdate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框（只读） -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      :title="`任务详情 - ${currentTaskForDetail?.taskName || ''}`"
      width="1000px"
    >
      <!-- 任务基本信息（只读） -->
      <div class="task-info-panel" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
        <h4 style="margin-bottom: 10px;">任务信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="任务名称">{{ currentTaskForDetail?.taskName }}</el-descriptions-item>
              <el-descriptions-item label="所属项目">{{ currentTaskForDetail?.projectName }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ currentTaskForDetail?.ownerName }}</el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ currentTaskForDetail?.startDate }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ currentTaskForDetail?.endDate }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="优先级">{{ getPriorityText(currentTaskForDetail?.priority) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ getStatusText(currentTaskForDetail?.status) }}</el-descriptions-item>
              <el-descriptions-item label="当前进度">{{ currentTaskForDetail?.progress }}%</el-descriptions-item>
              <el-descriptions-item label="任务描述">{{ currentTaskForDetail?.description }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </div>

      <!-- 进展历史时间线 -->
      <div style="margin-top: 30px;">
        <h4 style="margin-bottom: 15px;">进展历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(update, index) in taskDetailProgressUpdates"
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
                  <div class="attachments-container">
                    <div v-for="(attachment, idx) in update.attachmentList" :key="idx" class="attachment-item">
                      <div class="attachment-file">
                        <el-icon class="file-icon"><Document /></el-icon>
                        <span class="file-name">{{ attachment.name || '未知文件' }}</span>
                      </div>
                      <div class="attachment-info">
                        <span class="attachment-name">{{ attachment.name || '未知文件' }}</span>
                        <div class="attachment-actions">
                          <el-button size="small" type="primary" @click="handleAttachmentPreview(attachment)">
                            预览
                          </el-button>
                          <el-button size="small" @click="handleAttachmentDownload(attachment)">
                            下载
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

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
import { Star, Document } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import TaskComment from '@/components/TaskComment.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { getTaskPage, createTask, updateTask, deleteTask, updateProgress, pinTask, getProjectTasks, createProgressUpdate, getTaskProgressUpdates, hasTaskPermission } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

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
const taskDetailDialogVisible = ref(false)
const currentTask = ref(null)
const currentTaskForDetail = ref(null)
const dialogTitle = ref('新建任务')
const formRef = ref(null)
const taskDetailProgressUpdates = ref([])
const progressUpdateDialogVisible = ref(false)
const currentTaskForUpdate = ref(null)
const progressUpdateForm = reactive({
  taskId: null,
  progress: 0,
  description: '',
  attachments: []
})
const progressUpdates = ref([])

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
  { prop: 'taskName', label: '任务名称', minWidth: 150, slot: 'taskName' },
  { prop: 'projectName', label: '所属项目', width: 180 },
  { prop: 'priority', label: '优先级', width: 80, slot: 'priority' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'progress', label: '进度', width: 120, slot: 'progress' },
  { prop: 'ownerName', label: '负责人', width: 100 },
  { prop: 'startDate', label: '开始日期', width: 110 },
  { prop: 'endDate', label: '结束日期', width: 110 },
  { prop: 'operation', label: '操作', width: 410, fixed: 'right', slot: 'operation' }
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
    
    // 为每个任务计算权限状态
    for (const task of tableData.value) {
      try {
        task.hasPermission = await hasTaskPermission(task.id, userStore.employeeId)
      } catch (error) {
        console.error('检查任务权限失败', error)
        task.hasPermission = false
      }
    }
    
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
  loadTaskTree(row.projectId)
  dialogVisible.value = true
}

const handleUpdateProgress = async (row) => {
  if (!(await hasPermission(row))) {
    ElMessage.warning('您没有权限更新此任务进度')
    return
  }
  currentTaskForUpdate.value = row
  progressUpdateForm.taskId = row.id
  progressUpdateForm.progress = row.progress || 0
  progressUpdateForm.description = ''
  progressUpdateForm.attachments = []
  
  // 加载任务的进展历史
  await loadProgressUpdates(row.id)
  
  progressUpdateDialogVisible.value = true
}

const handleViewTaskDetail = async (row) => {
  currentTaskForDetail.value = row
  
  // 加载任务的进展历史
  await loadTaskDetailProgressUpdates(row.id)
  
  taskDetailDialogVisible.value = true
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

const loadTaskDetailProgressUpdates = async (taskId) => {
  try {
    const data = await getTaskProgressUpdates(taskId)
    taskDetailProgressUpdates.value = data || []
  } catch (error) {
    console.error('加载任务进展历史失败', error)
    ElMessage.error('加载任务进展历史失败')
  }
}

const handleSubmitProgressUpdate = async () => {
  try {
    // 准备附件数据
    let attachmentsJson = null
    if (progressUpdateForm.attachments && progressUpdateForm.attachments.length > 0) {
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

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handlePreview = (file) => {
  console.log('预览文件', file)
  // 实际项目中应该打开文件预览
}

const handleRemove = (file, fileList) => {
  console.log('删除文件', file, fileList)
  // 实际项目中应该处理文件删除逻辑
}

const handleCustomUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 更新文件列表
    if (response) {
      const uploadedFile = {
        uid: file.uid,
        name: file.name,
        url: response.fileUrl,
        previewUrl: response.previewUrl,
        filePath: response.filePath,
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
      data: response 
    })
  } catch (error) {
    console.error('文件上传失败', error)
    ElMessage.error('文件上传失败')
    onError(error)
  }
}

const handleUploadSuccess = (response, file, fileList) => {
  // 上传成功后，更新文件列表
  if (response && response.data) {
    const fileData = response.data
    const uploadedFile = {
      uid: file.uid,
      name: file.name,
      url: fileData.fileUrl,
      previewUrl: fileData.previewUrl,
      filePath: fileData.filePath,
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
  } else {
    ElMessage.error('文件上传失败')
  }
}

const handleUploadError = (error, file, fileList) => {
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
  if (!(await hasPermission(row))) {
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

const hasPermission = async (task) => {
  try {
    const result = await hasTaskPermission(task.id, userStore.employeeId)
    return result
  } catch (error) {
    console.error('检查任务权限失败', error)
    return false
  }
}

const isImageType = (fileName) => {
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  const ext = fileName.toLowerCase().substring(fileName.lastIndexOf('.'))
  return imageExtensions.includes(ext)
}

const handleAttachmentPreview = (attachment) => {
  console.log('预览附件', attachment)
  // 强制使用KKFileView预览URL
  if (attachment.previewUrl) {
    window.open(attachment.previewUrl, '_blank')
  } else {
    window.open(attachment.url, '_blank')
  }
}

const handleAttachmentDownload = (attachment) => {
  console.log('下载附件', attachment)
  const link = document.createElement('a')
  link.href = attachment.url
  link.download = attachment.name || '未知文件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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

/* 操作列按钮强制在一行显示 */
:deep(.el-table__cell:last-child) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作按钮样式调整 */
:deep(.el-table__cell:last-child .el-button) {
  margin-right: 5px;
  padding: 0 8px;
  font-size: 12px;
}

/* 确保操作列内容容器不换行 */
:deep(.el-table__cell:last-child) > div {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 对话框内容样式，防止横向滚动 */
:deep(.el-dialog__body) {
  overflow-x: hidden !important;
  padding: 20px;
}

/* 更新内容区域样式 */
.update-content {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 描述内容自适应 */
.update-description {
  width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  box-sizing: border-box;
}

/* 富文本编辑器内容自适应 */
.update-description :deep(img) {
  max-width: 100%;
  height: auto;
}

.update-description :deep(table) {
  max-width: 100%;
  overflow-x: auto;
  display: block;
}

/* 附件区域自适应 */
.update-attachments {
  width: 100%;
  box-sizing: border-box;
}

/* 附件图片容器，使用flex布局支持自适应换行 */
.attachments-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
  box-sizing: border-box;
}

/* 附件图片样式 */
.attachment-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.attachment-image:hover {
  transform: scale(1.05);
}

.attachment-item {
  position: relative;
  margin-bottom: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background-color: #f9f9f9;
  transition: all 0.3s;
}

.attachment-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.attachment-file {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.attachment-file:hover {
  transform: scale(1.05);
}

.file-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 8px;
}

.file-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  word-break: break-all;
  padding: 0 8px;
}

.attachment-info {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-name {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  word-break: break-all;
}

.attachment-actions {
  display: flex;
  gap: 8px;
  margin-top: 5px;
}

.attachment-actions .el-button {
  padding: 0 12px;
}

/* 确保附件容器的布局正确 */
.attachments-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  width: 100%;
  box-sizing: border-box;
}

/* 时间线内容自适应 */
:deep(.el-timeline-item__content) {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 卡片内容自适应 */
:deep(.el-card__body) {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  padding: 15px;
}

/* 任务信息面板自适应 */
.task-info-panel {
  width: 100%;
  box-sizing: border-box;
}

/* 确保行和列布局自适应 */
:deep(.el-row) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.el-col) {
  box-sizing: border-box;
}

/* 描述列表自适应 */
:deep(.el-descriptions) {
  width: 100%;
}

:deep(.el-descriptions__body) {
  width: 100%;
}

/* 分割线自适应 */
:deep(.el-divider) {
  width: 100%;
  box-sizing: border-box;
}
</style>
