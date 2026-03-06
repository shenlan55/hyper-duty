<template>
  <div class="project-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="project-selector">
            <el-select v-model="selectedProjectId" placeholder="请选择项目" filterable @change="handleProjectChange">
              <el-option
                v-for="item in projectList"
                :key="item.id"
                :label="item.projectName"
                :value="item.id"
              />
            </el-select>
          </div>
          <div class="project-title" v-if="project.projectName">
            <h2>{{ project.projectName }}</h2>
            <el-tag :type="getStatusType(project.status)" size="small">
              {{ getStatusText(project.status) }}
            </el-tag>
          </div>
          <div class="project-actions" v-if="project.projectName">
            <el-button v-if="canAddTask" type="primary" @click="handleAddTask">
              <el-icon><Plus /></el-icon>
              添加任务
            </el-button>
            <el-button v-if="canEditProject" @click="handleEdit">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button v-if="canArchiveProject && project.status !== 5" @click="handleArchive">
              <el-icon><Document /></el-icon>
              归档
            </el-button>
          </div>
        </div>
      </template>

      <div class="project-info">
        <div class="info-row">
          <div class="info-item">
            <span class="label">项目编码：</span>
            <span class="value">{{ project.projectCode || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">负责人：</span>
            <span class="value">{{ project.ownerName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">优先级：</span>
            <el-tag :type="getPriorityType(project.priority)" size="small">
              {{ getPriorityText(project.priority) }}
            </el-tag>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="label">开始日期：</span>
            <span class="value">{{ project.startDate || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">结束日期：</span>
            <span class="value">{{ project.endDate || '-' }}</span>
          </div>
        </div>
        <div class="progress-section">
          <span class="label">项目进度：</span>
          <el-progress 
            :percentage="project.progress || 0" 
            :status="getProgressStatus(project.progress)"
            style="flex: 1; margin-left: 10px"
          />
        </div>
        <div class="description-section">
          <span class="label">项目描述：</span>
          <div class="description">
            {{ project.description || '暂无描述' }}
          </div>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>任务看板</span>
        </div>
      </template>

      <div class="task-board">
        <div v-for="status in statusList" :key="status.value" class="board-column">
          <div class="column-header">
            <span>{{ status.label }}</span>
            <el-tag size="small" type="info">
              {{ getTaskCountByStatus(status.value) }}
            </el-tag>
          </div>
          <div class="task-list">
            <div 
              v-for="task in getTasksByStatus(status.value)" 
              :key="task.id" 
              class="task-card"
              @click="handleViewTask(task)"
            >
              <div class="task-title">{{ task.taskName }}</div>
              <div class="task-meta">
                <span class="task-owner">
                  <el-icon><User /></el-icon>
                  {{ task.ownerName || '-' }}
                </span>
                <span class="task-deadline" v-if="task.endDate">
                  <el-icon><Clock /></el-icon>
                  {{ task.endDate }}
                </span>
              </div>
              <div class="task-progress">
                <el-progress 
                  :percentage="task.progress || 0" 
                  :stroke-width="6"
                  :show-text="false"
                />
              </div>
            </div>
            <div v-if="canAddTask" class="add-task-card" @click="handleAddTask(status.value)">
              <el-icon><Plus /></el-icon>
              <span>添加任务</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>
      <el-empty description="暂无操作日志" />
    </el-card>

    <el-dialog
      v-model="taskDialogVisible"
      :title="taskDialogTitle"
      width="1200px"
      @close="handleTaskDialogClose"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
      >
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="taskForm.projectId" placeholder="请选择项目" filterable disabled>
            <el-option
              v-for="item in projectList"
              :key="item.id"
              :label="item.projectName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父任务">
          <el-cascader
            v-model="taskForm.parentIdPath"
            :options="taskTreeData"
            :props="{ checkStrictly: true, value: 'id', label: 'taskName', emitPath: false }"
            placeholder="请选择父任务（可选）"
            clearable
            @change="handleParentChange"
          />
        </el-form-item>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级" style="width: 100px;">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-input
            v-model="assigneeName"
            placeholder="请选择负责人"
            readonly
            clearable
            @clear="handleAssigneeClear"
            @click="assigneeDialogVisible = true"
            style="cursor: pointer;"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="taskForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="taskForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <RichTextEditor v-model="taskForm.description" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="附件">
          <FileUpload
            v-model:fileList="taskForm.attachments"
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
          />
        </el-form-item>
        <el-form-item label="参与人">
          <el-input
            v-model="stakeholderNames"
            placeholder="请选择参与人"
            readonly
            clearable
            @clear="handleStakeholderClear"
            @click="stakeholderDialogVisible = true"
            style="cursor: pointer;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTaskSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="projectDialogVisible"
      :title="projectDialogTitle"
      width="600px"
      @close="handleProjectDialogClose"
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectRules"
        label-width="100px"
      >
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="projectForm.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码" prop="projectCode">
          <el-input v-model="projectForm.projectCode" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="projectForm.priority" placeholder="请选择优先级" style="width: 100px;">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="ownerId">
          <el-input
            v-model="projectForm.ownerName"
            placeholder="请选择负责人"
            readonly
            clearable
            @clear="handleOwnerClear"
            @click="ownerDialogVisible = true"
            style="cursor: pointer;"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="projectForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="projectForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="参与人">
          <el-input
            v-model="participantsNames"
            placeholder="请选择参与人"
            readonly
            clearable
            @clear="handleParticipantsClear"
            @click="participantsDialogVisible = true"
            style="cursor: pointer;"
          />
        </el-form-item>

        <!-- 参与人选择对话框 -->
        <el-dialog
          v-model="participantsDialogVisible"
          title="选择参与人"
          width="900px"
          max-width="90vw"
        >
          <div style="padding: 10px; height: 600px; overflow: auto;">
            <EmployeeSelector
              v-model="selectedParticipants"
              @change="handleParticipantsChange"
              style="height: 500px;"
            />
          </div>
          <template #footer>
            <el-button @click="participantsDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmParticipantsSelection">确认</el-button>
          </template>
        </el-dialog>
      </el-form>
      <template #footer>
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleProjectSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="taskDetailDialogVisible"
      :title="taskDetailDialogTitle"
      width="1500px"
      max-width="1500px"
    >
      <TaskDetail :task="currentTask" />
      <template #footer>
        <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 任务负责人选择对话框 -->
    <el-dialog
      v-model="assigneeDialogVisible"
      title="选择负责人"
      width="900px"
      max-width="90vw"
    >
      <div style="padding: 10px;">
        <EmployeeSelector
          v-model="selectedAssignees"
          style="height: 500px;"
        />
      </div>
      <template #footer>
        <el-button @click="assigneeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssigneeSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 项目负责人选择对话框 -->
    <el-dialog
      v-model="ownerDialogVisible"
      title="选择负责人"
      width="900px"
      max-width="90vw"
    >
      <div style="padding: 10px;">
        <EmployeeSelector
          v-model="selectedOwners"
          style="height: 500px;"
        />
      </div>
      <template #footer>
        <el-button @click="ownerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOwnerSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 任务参与人选择对话框 -->
    <el-dialog
      v-model="stakeholderDialogVisible"
      title="选择参与人"
      width="900px"
      max-width="90vw"
    >
      <div style="padding: 10px;">
        <EmployeeSelector
          v-model="selectedStakeholders"
          @change="handleStakeholderChange"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Document, User, Clock } from '@element-plus/icons-vue'
import TaskDetail from '@/components/TaskDetail.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileUpload from '@/components/FileUpload.vue'
import EmployeeSelector from '@/components/EmployeeSelector.vue'
import { getProjectDetail, updateProject, archiveProject, getProjectPage, createProject } from '@/api/project'
import { getProjectTasks, createTask, updateTask } from '@/api/task'
import { getEmployeeList } from '@/api/employee'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const project = ref({})
const tasks = ref([])
const employeeList = ref([])
const projectList = ref([])
const selectedProjectId = ref(null)

// 计算属性：是否是项目负责人
const isProjectOwner = computed(() => {
  return project.value.ownerId === userStore.employeeId
})

// 计算属性：是否是项目参与人员
const isProjectParticipant = computed(() => {
  if (!project.value.participants) return false
  const participants = Array.isArray(project.value.participants) ? project.value.participants : []
  return participants.includes(userStore.employeeId)
})

// 计算属性：是否可以添加任务（项目负责人或参与人员都可以）
const canAddTask = computed(() => {
  return isProjectOwner.value || isProjectParticipant.value
})

// 计算属性：是否可以编辑项目（只有项目负责人可以）
const canEditProject = computed(() => {
  return isProjectOwner.value
})

// 计算属性：是否可以归档项目（只有项目负责人可以）
const canArchiveProject = computed(() => {
  return isProjectOwner.value
})

const taskDialogVisible = ref(false)
const taskDialogTitle = ref('新建任务')
const taskFormRef = ref(null)
const defaultStatus = ref(1)
const taskTreeData = ref([])

const taskDetailDialogVisible = ref(false)
const taskDetailDialogTitle = ref('任务详情')
const currentTask = ref(null)

const projectDialogVisible = ref(false)
const projectDialogTitle = ref('编辑项目')
const projectFormRef = ref(null)

// 负责人选择相关变量
const assigneeDialogVisible = ref(false)
const assigneeName = ref('')
const selectedAssignees = ref([])

// 项目负责人选择相关变量
const ownerDialogVisible = ref(false)
const selectedOwners = ref([])

// 参与人员选择相关变量
const participantsDialogVisible = ref(false)
const participantsNames = ref('')
const selectedParticipants = ref([])

// 任务参与人选择相关变量
const stakeholderDialogVisible = ref(false)
const stakeholderNames = ref('')
const selectedStakeholders = ref([])

const statusList = [
  { label: '未开始', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已暂停', value: 4 }
]

const taskForm = reactive({
  id: null,
  projectId: null,
  parentId: null,
  parentIdPath: null,
  taskName: '',
  priority: 2,
  assigneeId: null,
  startDate: '',
  endDate: '',
  description: '',
  attachments: [],
  stakeholders: [],
  status: 1
})

const taskRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const projectForm = reactive({
  id: null,
  projectName: '',
  projectCode: '',
  priority: 2,
  ownerId: null,
  ownerName: '',
  startDate: '',
  endDate: '',
  description: '',
  participants: []
})

const projectRules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'primary', 3: 'success', 4: 'warning', 5: '' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '未开始', 2: '进行中', 3: '已完成', 4: '已暂停', 5: '已归档' }
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

const getTasksByStatus = (status) => {
  return tasks.value.filter(task => task.status === status)
}

const getTaskCountByStatus = (status) => {
  return tasks.value.filter(task => task.status === status).length
}

const loadProjectList = async () => {
  try {
    const data = await getProjectPage({ pageNum: 1, pageSize: 1000, showArchived: true })
    projectList.value = data.records || []
    // 如果没有指定项目ID，默认选择第一个项目
    if (projectList.value.length > 0 && !selectedProjectId.value) {
      selectedProjectId.value = projectList.value[0].id
      loadProject(selectedProjectId.value)
      loadTasks(selectedProjectId.value)
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadProject = async (id) => {
  loading.value = true
  try {
    if (id) {
      const data = await getProjectDetail(id)
      project.value = data || {}
    } else {
      project.value = {}
    }
  } catch (error) {
    ElMessage.error('加载项目详情失败')
  } finally {
    loading.value = false
  }
}

const loadTasks = async (id) => {
  try {
    if (id) {
      const data = await getProjectTasks(id)
      tasks.value = data || []
    } else {
      tasks.value = []
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
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

const handleProjectChange = (id) => {
  if (id) {
    loadProject(id)
    loadTasks(id)
  } else {
    project.value = {}
    tasks.value = []
  }
}

const handleEdit = async () => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }
  projectDialogTitle.value = '编辑项目'
  Object.assign(projectForm, project.value)
  
  // 同步参与人员信息
  if (projectForm.participants && Array.isArray(projectForm.participants)) {
    // 加载员工列表用于匹配姓名
    try {
      const data = await getEmployeeList(1, 1000)
      const employeeList = data?.records || []
      // 匹配参与人员姓名
      const participantNames = []
      const participantObjects = []
      projectForm.participants.forEach(participantId => {
        const employee = employeeList.find(emp => emp.id === participantId)
        if (employee) {
          participantNames.push(employee.employeeName)
          participantObjects.push(employee)
        }
      })
      participantsNames.value = participantNames.join(', ')
      selectedParticipants.value = participantObjects
    } catch (error) {
      console.error('加载员工列表失败', error)
    }
  } else {
    participantsNames.value = ''
    selectedParticipants.value = []
  }
  
  projectDialogVisible.value = true
}

const handleArchive = async () => {
  try {
    await ElMessageBox.confirm('确定要归档该项目吗？', '提示', {
      type: 'warning'
    })
    if (selectedProjectId.value) {
      await archiveProject(selectedProjectId.value)
      ElMessage.success('归档成功')
      loadProjectList()
      loadProject(selectedProjectId.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('归档失败')
    }
  }
}

const handleAddTask = (status = 1) => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择项目')
    return
  }
  taskDialogTitle.value = '新建任务'
  defaultStatus.value = status
  resetTaskForm()
  taskForm.status = status
  taskForm.projectId = selectedProjectId.value
  taskDialogVisible.value = true
}

const handleParentChange = (value) => {
  taskForm.parentId = value || null
}

// 负责人选择相关方法
const handleAssigneeChange = (persons) => {
  selectedAssignees.value = persons
}

const confirmAssigneeSelection = () => {
  if (selectedAssignees.value.length > 0) {
    const assignee = selectedAssignees.value[0]
    taskForm.assigneeId = assignee.id
    assigneeName.value = assignee.employeeName
  }
  assigneeDialogVisible.value = false
}

const handleAssigneeClear = () => {
  taskForm.assigneeId = null
  assigneeName.value = ''
  selectedAssignees.value = []
}

// 项目负责人选择相关方法
const handleOwnerChange = (persons) => {
  selectedOwners.value = persons
}

const confirmOwnerSelection = () => {
  if (selectedOwners.value.length > 0) {
    const owner = selectedOwners.value[0]
    projectForm.ownerId = owner.id
    projectForm.ownerName = owner.employeeName
  }
  ownerDialogVisible.value = false
}

const handleOwnerClear = () => {
  projectForm.ownerId = null
  projectForm.ownerName = ''
  selectedOwners.value = []
}

// 参与人员选择相关方法
const handleParticipantsChange = (persons) => {
  selectedParticipants.value = persons
}

const confirmParticipantsSelection = () => {
  if (selectedParticipants.value.length > 0) {
    // 提取参与人员ID数组
    projectForm.participants = selectedParticipants.value.map(person => person.id)
    // 提取参与人员姓名，用逗号分隔显示
    participantsNames.value = selectedParticipants.value.map(person => person.employeeName).join(', ')
  } else {
    projectForm.participants = []
    participantsNames.value = ''
  }
  participantsDialogVisible.value = false
}

const handleParticipantsClear = () => {
  projectForm.participants = []
  participantsNames.value = ''
  selectedParticipants.value = []
}

// 任务参与人选择相关方法
const handleStakeholderChange = (persons) => {
  selectedStakeholders.value = persons
}

const confirmStakeholderSelection = () => {
  if (selectedStakeholders.value.length > 0) {
    // 提取参与人员ID数组
    taskForm.stakeholders = selectedStakeholders.value.map(person => person.id)
    // 提取参与人员姓名，用逗号分隔显示
    stakeholderNames.value = selectedStakeholders.value.map(person => person.employeeName).join(', ')
  } else {
    taskForm.stakeholders = []
    stakeholderNames.value = ''
  }
  stakeholderDialogVisible.value = false
}

const handleStakeholderClear = () => {
  taskForm.stakeholders = []
  stakeholderNames.value = ''
  selectedStakeholders.value = []
}

const handleUploadSuccess = (file) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  ElMessage.error('文件上传失败')
}

const handleViewTask = (task) => {
  currentTask.value = { ...task }
  taskDetailDialogVisible.value = true
}

const handleTaskSubmit = async () => {
  try {
    await taskFormRef.value.validate()
    // 准备提交的数据
    const submitData = {
      ...taskForm,
      ownerId: taskForm.assigneeId, // 映射到后端需要的字段名
      // 移除stakeholders字段，因为后端实体类中没有这个字段
    }
    
    // 处理附件数据，转换为JSON字符串
    if (submitData.attachments && Array.isArray(submitData.attachments)) {
      submitData.attachments = JSON.stringify(submitData.attachments)
    } else {
      submitData.attachments = null
    }
    
    if (taskForm.id) {
      await updateTask(submitData)
      ElMessage.success('更新成功')
    } else {
      await createTask(submitData)
      ElMessage.success('创建成功')
    }
    taskDialogVisible.value = false
    if (selectedProjectId.value) {
      loadTasks(selectedProjectId.value)
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

const handleProjectSubmit = async () => {
  try {
    await projectFormRef.value.validate()
    if (projectForm.id) {
      await updateProject(projectForm)
      ElMessage.success('更新成功')
    } else {
      await createProject(projectForm)
      ElMessage.success('创建成功')
    }
    projectDialogVisible.value = false
    if (selectedProjectId.value) {
      loadProject(selectedProjectId.value)
      loadProjectList()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

const handleTaskDialogClose = () => {
  resetTaskForm()
}

const handleProjectDialogClose = () => {
  resetProjectForm()
}

const resetTaskForm = () => {
  taskForm.id = null
  taskForm.projectId = null
  taskForm.parentId = null
  taskForm.parentIdPath = null
  taskForm.taskName = ''
  taskForm.priority = 2
  taskForm.assigneeId = null
  taskForm.startDate = ''
  taskForm.endDate = ''
  taskForm.description = ''
  taskForm.attachments = []
  taskForm.stakeholders = []
  taskForm.status = defaultStatus.value || 1
  assigneeName.value = ''
  selectedAssignees.value = []
  assigneeDialogVisible.value = false
  stakeholderNames.value = ''
  selectedStakeholders.value = []
  stakeholderDialogVisible.value = false
  taskFormRef.value?.resetFields()
}

const resetProjectForm = () => {
  projectForm.id = null
  projectForm.projectName = ''
  projectForm.projectCode = ''
  projectForm.priority = 2
  projectForm.ownerId = null
  projectForm.ownerName = ''
  projectForm.startDate = ''
  projectForm.endDate = ''
  projectForm.description = ''
  selectedOwners.value = []
  ownerDialogVisible.value = false
  projectFormRef.value?.resetFields()
}

onMounted(async () => {
  await loadProjectList()
  const id = route.query.projectId || (route.params?.id ? Number(route.params.id) : null)
  if (id) {
    selectedProjectId.value = id
    loadProject(id)
    loadTasks(id)
  }
  loadEmployeeList()
})
</script>

<style scoped>
.project-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-selector {
  margin-right: 20px;
  min-width: 200px;
}

.project-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.project-title h2 {
  margin: 0;
  font-size: 20px;
}

.project-actions {
  display: flex;
  gap: 10px;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  gap: 40px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  color: #909399;
  font-weight: 500;
}

.value {
  color: #303133;
  margin-left: 8px;
}

.progress-section {
  display: flex;
  align-items: center;
}

.description-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.description {
  color: #606266;
  line-height: 1.6;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.task-board {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.board-column {
  min-width: 280px;
  flex-shrink: 0;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 500px;
  overflow-y: auto;
}

.task-card {
  background-color: #fff;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.task-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.task-owner,
.task-deadline {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-progress {
  margin-top: 4px;
}

.add-task-card {
  background-color: #fff;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  transition: all 0.3s;
}

.add-task-card:hover {
  border-color: #409eff;
  color: #409eff;
}

/* 确保富文本内容中的图片自适应容器宽度 */
:deep(.ql-editor img) {
  max-width: 100% !important;
  height: auto !important;
  display: block !important;
  margin: 0 auto !important;
  box-sizing: border-box !important;
}

/* 确保编辑器容器宽度自适应 */
:deep(.rich-text-editor) {
  width: 100%;
  box-sizing: border-box;
}

:deep(.editor-container) {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 任务详情弹框样式 */
:deep(.el-dialog) {
  max-width: 1500px !important;
}

:deep(.el-dialog__body) {
  overflow-x: hidden !important;
  padding: 20px;
}
</style>
