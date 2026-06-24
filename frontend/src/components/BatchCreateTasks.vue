<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量新建任务"
    width="1800px"
    @close="handleClose"
  >
    <el-form label-width="100px" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="所属项目" prop="projectId">
            <el-select v-model="sharedForm.projectId" placeholder="请选择项目" filterable style="width: 100%;">
              <el-option
                v-for="project in projectList"
                :key="project.id"
                :label="project.projectName"
                :value="project.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
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
        </el-col>
        <el-col :span="8">
          <el-form-item label="默认优先级">
            <el-select v-model="sharedForm.priority" placeholder="默认优先级" style="width: 100%;">
              <el-option v-for="opt in priorityOptions" :key="opt.value" :label="opt.label" :value="Number(opt.value)" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-divider content-position="left">
      <span style="display: flex; align-items: center; gap: 10px;">
        任务列表
        <el-button type="primary" size="small" @click="addTaskRow">
          <el-icon><Plus /></el-icon>
          添加任务行
        </el-button>
      </span>
    </el-divider>

    <div style="overflow-x: auto;">
      <el-table 
        :data="taskList" 
        border 
        style="width: 100%;"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
      >
        <el-table-column type="index" label="#" width="50" align="center" />
        
        <el-table-column min-width="200">
          <template #header>
            <span style="color: #f56c6c;">*</span> 任务名称
          </template>
          <template #default="{ row }">
            <el-input 
              v-model="row.taskName" 
              placeholder="请输入任务名称"
              size="small"
              :class="{ 'is-error': !row.taskName.trim() }"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="优先级" width="130">
          <template #default="{ row }">
            <el-select v-model="row.priority" placeholder="优先级" size="small" style="width: 100%;">
              <el-option v-for="opt in priorityOptions" :key="opt.value" :label="opt.label" :value="Number(opt.value)" />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="是否重点" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isFocus"
              :active-value="1"
              :inactive-value="0"
              size="small"
            />
          </template>
        </el-table-column>
        
        <el-table-column width="150">
          <template #header>
            <span style="color: #f56c6c;">*</span> 开始日期
          </template>
          <template #default="{ row }">
            <el-date-picker
              v-model="row.startDate"
              type="date"
              placeholder="开始日期"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 100%;"
              :popper-class="!row.startDate ? 'date-picker-error' : ''"
              :class="{ 'is-error': !row.startDate }"
            />
          </template>
        </el-table-column>
        
        <el-table-column width="150">
          <template #header>
            <span style="color: #f56c6c;">*</span> 结束日期
          </template>
          <template #default="{ row }">
            <el-date-picker
              v-model="row.endDate"
              type="date"
              placeholder="结束日期"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 100%;"
              :class="{ 'is-error': !row.endDate }"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="任务描述" min-width="200">
          <template #default="{ row }">
            <el-input
              v-model="row.description"
              type="textarea"
              :rows="2"
              placeholder="任务描述"
              size="small"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="附件" width="180">
          <template #default="{ row, $index }">
            <div style="display: flex; flex-direction: column; gap: 5px;">
              <FileUpload
                v-model:fileList="row.attachments"
                :limit="5"
                @upload-success="handleUploadSuccess"
                @upload-error="handleUploadError"
              />
              <div v-if="row.attachments && row.attachments.length > 0" style="font-size: 12px; color: #909399;">
                已上传 {{ row.attachments.length }} 个文件
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="参与人" width="180">
          <template #default="{ row, $index }">
            <div style="display: flex; gap: 5px;">
              <el-input
                :model-value="getStakeholderNames(row.stakeholders)"
                placeholder="请选择"
                readonly
                size="small"
                style="cursor: pointer; flex: 1;"
                @click="openStakeholderDialog($index)"
              />
              <el-button type="primary" size="small" @click="openStakeholderDialog($index)">
                选择
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ $index }">
            <el-button
              type="danger"
              link
              size="small"
              @click="removeTaskRow($index)"
              :disabled="taskList.length <= 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="color: #909399; font-size: 14px;">共 {{ taskList.length }} 个任务</span>
        <div>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </div>
    </template>

    <!-- 负责人选择对话框 -->
    <el-dialog
      v-model="assigneeDialogVisible"
      title="选择负责人"
      width="1000px"
    >
      <div style="padding: 10px;">
        <PersonSelector
          v-model="selectedAssignees"
          style="height: 520px;"
        />
      </div>
      <template #footer>
        <el-button @click="assigneeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssigneeSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 参与人选择对话框 -->
    <el-dialog
      v-model="stakeholderDialogVisible"
      title="选择参与人"
      width="1000px"
    >
      <div style="padding: 10px;">
        <PersonSelector
          v-model="currentStakeholderSelection"
          style="height: 520px;"
        />
      </div>
      <template #footer>
        <el-button @click="stakeholderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStakeholderSelection">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useDict } from '@/composables/useDict'
import FileUpload from '@/components/FileUpload.vue'
import PersonSelector from '@/components/PersonSelector.vue'
import { batchCreateTasks } from '@/api/task'

// 业务枚举：优先级 走字典
const { options: priorityOptions, loadDict: loadPriorityDict } = useDict('task_priority')

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  projectList: {
    type: Array,
    default: () => []
  },
  employeeList: {
    type: Array,
    default: () => []
  },
  defaultProjectId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const assigneeDialogVisible = ref(false)
const stakeholderDialogVisible = ref(false)
const selectedAssignees = ref([])
const currentStakeholderSelection = ref([])
const currentStakeholderIndex = ref(-1)

const sharedForm = reactive({
  projectId: null,
  assigneeId: null,
  priority: 2
})

const taskList = ref([])

const assigneeName = computed(() => {
  if (!sharedForm.assigneeId) return ''
  const employee = props.employeeList.find(emp => emp.id === sharedForm.assigneeId)
  return employee ? employee.employeeName : ''
})

const getStakeholderNames = (stakeholders) => {
  if (!stakeholders || !Array.isArray(stakeholders)) return ''
  const names = stakeholders.map(id => {
    const employee = props.employeeList.find(emp => String(emp.id) === String(id))
    return employee ? employee.employeeName : id
  })
  return names.join(', ')
}

watch(() => dialogVisible.value, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

const resetForm = () => {
  sharedForm.projectId = props.defaultProjectId || null
  sharedForm.assigneeId = null
  sharedForm.priority = 2
  taskList.value = [createEmptyTask()]
  selectedAssignees.value = []
  currentStakeholderSelection.value = []
  currentStakeholderIndex.value = -1
}

const createEmptyTask = () => ({
  taskName: '',
  description: '',
  priority: sharedForm.priority,
  isFocus: 0,
  startDate: '',
  endDate: '',
  attachments: [],
  stakeholders: []
})

const addTaskRow = () => {
  taskList.value.push(createEmptyTask())
}

const removeTaskRow = (index) => {
  if (taskList.value.length > 1) {
    taskList.value.splice(index, 1)
  } else {
    ElMessage.warning('至少需要保留一个任务')
  }
}

const openStakeholderDialog = (index) => {
  currentStakeholderIndex.value = index
  const task = taskList.value[index]
  currentStakeholderSelection.value = props.employeeList.filter(emp => 
    task.stakeholders && task.stakeholders.includes(emp.id)
  )
  stakeholderDialogVisible.value = true
}

const confirmAssigneeSelection = () => {
  if (selectedAssignees.value.length > 0) {
    sharedForm.assigneeId = selectedAssignees.value[0].id
  }
  assigneeDialogVisible.value = false
}

const confirmStakeholderSelection = () => {
  if (currentStakeholderIndex.value >= 0 && currentStakeholderIndex.value < taskList.value.length) {
    taskList.value[currentStakeholderIndex.value].stakeholders = currentStakeholderSelection.value.map(item => item.id)
  }
  stakeholderDialogVisible.value = false
  currentStakeholderIndex.value = -1
}

onMounted(() => {
  // 加载优先级字典
  loadPriorityDict()
})

const handleSubmit = async () => {
  if (!sharedForm.projectId) {
    ElMessage.error('请选择所属项目')
    return
  }
  if (!sharedForm.assigneeId) {
    ElMessage.error('请选择负责人')
    return
  }

  const validTasks = taskList.value.filter(task => task.taskName.trim())
  if (validTasks.length === 0) {
    ElMessage.error('请至少填写一个有效的任务名称')
    return
  }

  // 验证必填项
  const errors = []
  validTasks.forEach((task, index) => {
    const taskNum = index + 1
    if (!task.taskName.trim()) {
      errors.push(`第 ${taskNum} 行：任务名称不能为空`)
    }
    if (!task.startDate) {
      errors.push(`第 ${taskNum} 行：开始日期不能为空`)
    }
    if (!task.endDate) {
      errors.push(`第 ${taskNum} 行：结束日期不能为空`)
    }
    if (task.startDate && task.endDate && task.startDate > task.endDate) {
      errors.push(`第 ${taskNum} 行：开始日期不能晚于结束日期`)
    }
  })

  if (errors.length > 0) {
    ElMessage.error({
      message: errors.join('\n'),
      duration: 5000,
      showClose: true
    })
    return
  }

  submitting.value = true
  try {
    const tasks = validTasks.map(task => {
      let attachmentsJson = ''
      if (task.attachments && Array.isArray(task.attachments) && task.attachments.length > 0) {
        const attachments = task.attachments.map(file => ({
          name: file.name,
          url: file.url || '',
          previewUrl: file.previewUrl || '',
          filePath: file.filePath || '',
          type: file.type,
          size: file.size
        }))
        attachmentsJson = JSON.stringify(attachments)
      }
      
      let stakeholdersJson = ''
      if (task.stakeholders && Array.isArray(task.stakeholders) && task.stakeholders.length > 0) {
        stakeholdersJson = JSON.stringify(task.stakeholders)
      }
      
      return {
        projectId: sharedForm.projectId,
        parentId: 0,
        taskName: task.taskName.trim(),
        description: task.description || '',
        priority: task.priority || sharedForm.priority,
        status: 1,
        progress: 0,
        assigneeId: sharedForm.assigneeId,
        startDate: task.startDate || '',
        endDate: task.endDate || '',
        isFocus: task.isFocus !== undefined ? task.isFocus : 0,
        attachments: attachmentsJson,
        stakeholders: stakeholdersJson
      }
    })
    
    await batchCreateTasks({ tasks })
    ElMessage.success(`成功批量创建 ${tasks.length} 个任务`)
    emit('submit')
    dialogVisible.value = false
  } catch (error) {
    console.error('批量创建任务失败', error)
    ElMessage.error('批量创建任务失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  resetForm()
}

const handleUploadSuccess = () => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
}
</script>

<style scoped>
:deep(.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}
:deep(.is-error .el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}
:deep(.el-table__row:hover .is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}
</style>
