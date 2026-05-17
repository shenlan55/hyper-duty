<template>
  <el-dialog
    v-model="visible"
    title="创建影子任务"
    width="680px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="create-shadow-content">
      <div class="step-indicator">
        <div
          v-for="(step, index) in steps"
          :key="step.id"
          class="step-item"
          :class="{ active: currentStep === step.id, completed: currentStep > step.id }"
        >
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-label">{{ step.label }}</div>
          <div v-if="index < steps.length - 1" class="step-line"></div>
        </div>
      </div>

      <div class="step-content">
        <!-- 步骤1: 选择源任务 -->
        <div v-if="currentStep === 1" class="step-1">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="选择源任务" prop="sourceTaskId">
              <div class="source-task-selector">
                <el-select
                  v-model="form.sourceTaskId"
                  placeholder="请从所有项目中选择一个任务作为源任务"
                  filterable
                  style="width: 100%;"
                  @change="handleSourceTaskChange"
                >
                  <el-option-group
                    v-for="project in groupedProjects"
                    :key="project.id"
                    :label="project.projectName"
                  >
                    <el-option
                      v-for="task in project.tasks"
                      :key="task.id"
                      :label="task.taskName"
                      :value="task.id"
                    >
                      <div class="task-option">
                        <span class="task-name">{{ task.taskName }}</span>
                        <el-tag v-if="task.projectName" size="small" type="info" class="project-tag">
                          {{ task.projectName }}
                        </el-tag>
                      </div>
                    </el-option>
                  </el-option-group>
                </el-select>
              </div>
            </el-form-item>

            <el-form-item v-if="selectedTask" label="源任务预览">
              <div class="source-task-preview">
                <div class="preview-header">
                  <el-tag :type="getTaskPriorityType(selectedTask.priority)" size="small">
                    {{ getTaskPriorityText(selectedTask.priority) }}
                  </el-tag>
                  <el-tag :type="getTaskStatusType(selectedTask.status)" size="small">
                    {{ getTaskStatusText(selectedTask.status) }}
                  </el-tag>
                </div>
                <div class="preview-title">{{ selectedTask.taskName }}</div>
                <div class="preview-progress">
                  <span>当前进度</span>
                  <el-progress
                    :percentage="selectedTask.progress"
                    :status="getProgressStatus(selectedTask.progress)"
                    :stroke-width="8"
                    style="flex: 1; margin-left: 16px;"
                  />
                </div>
                <div v-if="selectedTask.description" class="preview-description">
                  {{ selectedTask.description }}
                </div>
                <div class="preview-meta">
                  <span v-if="selectedTask.ownerName">
                    <el-icon><User /></el-icon>
                    {{ selectedTask.ownerName }}
                  </span>
                  <span v-if="selectedTask.startDate">
                    <el-icon><Calendar /></el-icon>
                    {{ selectedTask.startDate }}
                  </span>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤2: 配置影子任务 -->
        <div v-if="currentStep === 2" class="step-2">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="目标项目" prop="targetProjectId">
              <el-select
                v-model="form.targetProjectId"
                placeholder="请选择要创建影子任务的目标项目"
                filterable
                style="width: 100%;"
              >
                <el-option
                  v-for="project in projectList"
                  :key="project.id"
                  :label="project.projectName"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="影子名称" prop="shadowName">
              <el-input
                v-model="form.shadowName"
                placeholder="不填则使用源任务名称"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="影子描述" prop="shadowDescription">
              <el-input
                v-model="form.shadowDescription"
                type="textarea"
                :rows="4"
                placeholder="可选，添加对影子任务的描述、注意事项等"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="currentStep > 1" @click="handlePrev">上一步</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="currentStep < steps.length"
          type="primary"
          @click="handleNext"
          :loading="nextLoading"
        >
          下一步
        </el-button>
        <el-button
          v-if="currentStep === steps.length"
          type="primary"
          @click="handleSubmit"
          :loading="submitLoading"
        >
          创建影子任务
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Calendar } from '@element-plus/icons-vue'
import { getTaskStatusType, getTaskStatusText, getTaskPriorityType, getTaskPriorityText, getProgressStatus } from '@/utils/taskUtils'
import { getProjectTasks } from '@/api/task'
import { getProjectPage } from '@/api/project'
import { createShadowTask } from '@/api/shadowTask'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  defaultProjectId: {
    type: Number,
    default: null
  },
  defaultSourceTaskId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const submitLoading = ref(false)
const nextLoading = ref(false)
const currentStep = ref(1)
const taskList = ref([])
const projectList = ref([])

const steps = [
  { id: 1, label: '选择源任务' },
  { id: 2, label: '配置影子' }
]

const form = ref({
  sourceTaskId: null,
  targetProjectId: null,
  shadowName: '',
  shadowDescription: ''
})

const rules = {
  sourceTaskId: [{ required: true, message: '请选择源任务', trigger: 'change' }],
  targetProjectId: [{ required: true, message: '请选择目标项目', trigger: 'change' }]
}

const selectedTask = computed(() => {
  return taskList.value.find(t => t.id === form.value.sourceTaskId)
})

const groupedProjects = computed(() => {
  const groups = {}
  projectList.value.forEach(project => {
    groups[project.id] = {
      ...project,
      tasks: taskList.value.filter(t => t.projectId === project.id)
    }
  })
  return Object.values(groups).filter(p => p.tasks && p.tasks.length > 0)
})

const initData = async () => {
  try {
    await Promise.all([
      loadTaskList(),
      loadProjectList()
    ])

    // 设置默认值
    if (props.defaultSourceTaskId) {
      form.value.sourceTaskId = props.defaultSourceTaskId
      await handleSourceTaskChange(props.defaultSourceTaskId)
    }
    if (props.defaultProjectId) {
      form.value.targetProjectId = props.defaultProjectId
    }
  } catch (error) {
    console.error('初始化数据失败', error)
  }
}

const loadTaskList = async () => {
  try {
    // 加载所有项目的任务用于选择
    const projectData = await getProjectPage({ pageNum: 1, pageSize: 1000, showArchived: true })
    const projects = projectData.records || []

    let allTasks = []
    for (const project of projects) {
      try {
        const tasks = await getProjectTasks(project.id)
        tasks.forEach(t => {
          t.projectId = project.id
          t.projectName = project.projectName
        })
        allTasks = allTasks.concat(tasks)
      } catch (e) {
        console.error(`加载项目${project.id}任务失败`, e)
      }
    }
    taskList.value = allTasks
  } catch (error) {
    console.error('加载任务列表失败', error)
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

const handleSourceTaskChange = (taskId) => {
  const task = taskList.value.find(t => t.id === taskId)
  if (task && !form.value.shadowName) {
    form.value.shadowName = task.taskName
  }
}

const handlePrev = () => {
  currentStep.value--
}

const handleNext = async () => {
  if (currentStep.value === 1) {
    try {
      await formRef.value.validateField('sourceTaskId')
      if (!selectedTask.value) {
        ElMessage.warning('请先选择源任务')
        return
      }
      currentStep.value++
    } catch (error) {
      // 验证失败，不继续
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    await createShadowTask(form.value)
    ElMessage.success('创建影子任务成功！')

    emit('success')
    handleClose()
  } catch (error) {
    console.error('创建失败', error)
    if (error !== false) {
      ElMessage.error(error.message || '创建失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    sourceTaskId: null,
    targetProjectId: null,
    shadowName: '',
    shadowDescription: ''
  }
  currentStep.value = 1
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    initData()
  }
})
</script>

<style scoped>
.create-shadow-content {
  padding: 8px 0;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.step-item {
  display: flex;
  align-items: center;
  position: relative;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e4e7ed;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s;
}

.step-item.active .step-number,
.step-item.completed .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.step-label {
  margin-left: 8px;
  font-size: 14px;
  color: #909399;
  transition: all 0.3s;
}

.step-item.active .step-label {
  color: #303133;
  font-weight: 500;
}

.step-item.completed .step-label {
  color: #67c23a;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e4e7ed;
  margin: 0 16px;
  transition: all 0.3s;
}

.step-item.completed + .step-item .step-line {
  background: linear-gradient(90deg, #67c23a 0%, #e4e7ed 100%);
}

.step-content {
  min-height: 200px;
}

.source-task-preview {
  background: linear-gradient(135deg, #f5f7fa 0%, #ebedee 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.preview-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.preview-progress {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.preview-description {
  padding: 12px;
  background: white;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.preview-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.preview-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.task-name {
  flex: 1;
}

.project-tag {
  flex-shrink: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
