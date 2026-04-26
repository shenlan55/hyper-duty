<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="1200px"
    @close="handleClose"
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
          <el-option v-for="(name, value) in taskPriorityMap" :key="value" :label="name" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态" style="width: 120px;">
          <el-option v-for="(name, value) in taskStatusMap" :key="value" :label="name" :value="Number(value)" />
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
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { TASK_STATUS_MAP, TASK_PRIORITY_MAP } from '@/constants/task'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus, formatDateTime, getStatusByProgress, getProgressByStatus } from '@/utils/taskUtils'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileUpload from '@/components/FileUpload.vue'
import PersonSelector from '@/components/PersonSelector.vue'
import { createTask, updateTask } from '@/api/task'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: null
  },
  projectList: {
    type: Array,
    default: () => []
  },
  taskTreeData: {
    type: Array,
    default: () => []
  },
  employeeList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => props.isEdit ? '编辑任务' : '新建任务')

const formRef = ref(null)
const assigneeDialogVisible = ref(false)
const stakeholderDialogVisible = ref(false)
const selectedAssignees = ref([])
const selectedStakeholders = ref([])

let originalTaskStatus = null
let originalTaskProgress = null

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

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const taskStatusMap = TASK_STATUS_MAP
const taskPriorityMap = TASK_PRIORITY_MAP

const assigneeName = computed(() => {
  if (!form.assigneeId) return ''
  const employee = props.employeeList.find(emp => emp.id === form.assigneeId)
  return employee ? employee.employeeName : ''
})

const stakeholderNames = computed(() => {
  if (!form.stakeholders || !Array.isArray(form.stakeholders)) return ''
  const names = form.stakeholders.map(id => {
    const employee = props.employeeList.find(emp => String(emp.id) === String(id))
    return employee ? employee.employeeName : id
  })
  return names.join(', ')
})

watch(() => props.initialData, (newVal) => {
  if (newVal) {
    initForm(newVal)
  }
}, { immediate: true })

watch(() => form.status, (newStatus) => {
  const newProgress = getProgressByStatus(newStatus, form.progress)
  if (newProgress !== null) {
    form.progress = newProgress
  }
})

watch(() => form.progress, (newProgress) => {
  const newStatus = getStatusByProgress(newProgress)
  form.status = newStatus
})

const initForm = (data) => {
  if (!data) return
  
  Object.assign(form, data)
  form.parentId = data.parentId || 0
  form.parentIdPath = data.parentId && data.parentId > 0 ? data.parentId : null
  form.assigneeId = data.assigneeId || data.ownerId || null
  form.progress = data.progress || 0
  
  originalTaskStatus = data.status
  originalTaskProgress = data.progress || 0
  
  const attachmentsSource = data._attachments || data.attachments
  if (attachmentsSource) {
    if (typeof attachmentsSource === 'string') {
      try {
        form.attachments = JSON.parse(attachmentsSource)
      } catch (error) {
        console.error('解析附件数据失败', error)
        form.attachments = []
      }
    } else if (!Array.isArray(attachmentsSource)) {
      form.attachments = []
    } else {
      form.attachments = attachmentsSource
    }
  } else {
    form.attachments = []
  }
  
  const stakeholdersSource = data._stakeholders || data.stakeholders
  if (stakeholdersSource) {
    if (typeof stakeholdersSource === 'string') {
      try {
        form.stakeholders = JSON.parse(stakeholdersSource)
      } catch (error) {
        console.error('解析干系人数据失败', error)
        form.stakeholders = []
      }
    } else if (!Array.isArray(stakeholdersSource)) {
      form.stakeholders = []
    } else {
      form.stakeholders = stakeholdersSource
    }
  } else {
    form.stakeholders = []
  }
  
  selectedAssignees.value = []
  if (form.assigneeId) {
    const assignee = props.employeeList.find(emp => emp.id === form.assigneeId)
    if (assignee) {
      selectedAssignees.value = [assignee]
    }
  }
  
  selectedStakeholders.value = []
  if (form.stakeholders && Array.isArray(form.stakeholders)) {
    selectedStakeholders.value = props.employeeList.filter(emp => 
      form.stakeholders.includes(emp.id)
    )
  }
}

const handleParentChange = (val) => {
  form.parentId = val || 0
}

const confirmAssigneeSelection = () => {
  if (selectedAssignees.value.length > 0) {
    form.assigneeId = selectedAssignees.value[0].id
  }
  assigneeDialogVisible.value = false
}

const confirmStakeholderSelection = () => {
  form.stakeholders = selectedStakeholders.value.map(item => item.id)
  stakeholderDialogVisible.value = false
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
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
    
    let stakeholdersJson = null
    if (form.stakeholders && Array.isArray(form.stakeholders) && form.stakeholders.length > 0) {
      stakeholdersJson = JSON.stringify(form.stakeholders)
    }
    
    const submitData = {
      ...form,
      attachments: attachmentsJson || '',
      stakeholders: stakeholdersJson || ''
    }
    
    if (props.isEdit) {
      await updateTask(submitData)
    } else {
      await createTask(submitData)
    }
    
    emit('submit', submitData)
    resetForm()
    dialogVisible.value = false
  } catch (error) {
    console.error('操作失败', error)
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

const handleClose = () => {
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
  form.isFocus = 0
  
  originalTaskStatus = null
  originalTaskProgress = null
  selectedAssignees.value = []
  selectedStakeholders.value = []
}

const handleUploadSuccess = () => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败', error)
  ElMessage.error('文件上传失败')
}
</script>
