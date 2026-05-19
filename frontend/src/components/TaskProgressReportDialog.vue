<template>
  <el-dialog
    v-model="dialogVisible"
    title="任务进展报告"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="queryForm" label-width="140px">
      <el-form-item label="项目">
        <el-select
          v-model="queryForm.projectIds"
          multiple
          placeholder="请选择项目（可选，不选则导出全部）"
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

      <el-divider content-position="left">任务时间范围</el-divider>
      
      <el-form-item label="开始日期">
        <el-date-picker
          v-model="startDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="结束日期">
        <el-date-picker
          v-model="endDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
        />
      </el-form-item>

      <el-divider content-position="left">进展更新时间范围</el-divider>

      <el-form-item label="更新时间">
        <el-date-picker
          v-model="updateTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%;"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleExport" :loading="exporting">
        导出报告
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProjectPage } from '@/api/project'
import { exportTaskProgressReport } from '@/api/task'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const dialogVisible = ref(props.modelValue)
const exporting = ref(false)
const projectList = ref([])
const startDateRange = ref(null)
const endDateRange = ref(null)
const updateTimeRange = ref(null)

const queryForm = reactive({
  projectIds: []
})

const loadProjectList = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000,
      showArchived: false
    }
    const data = await getProjectPage(params)
    projectList.value = data.records || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    const params = {}
    
    if (queryForm.projectIds && queryForm.projectIds.length > 0) {
      params.projectIds = queryForm.projectIds
    }
    
    if (startDateRange.value && startDateRange.value.length === 2) {
      params.taskStartDateFrom = startDateRange.value[0]
      params.taskStartDateTo = startDateRange.value[1]
    }
    
    if (endDateRange.value && endDateRange.value.length === 2) {
      params.taskEndDateFrom = endDateRange.value[0]
      params.taskEndDateTo = endDateRange.value[1]
    }
    
    if (updateTimeRange.value && updateTimeRange.value.length === 2) {
      params.progressUpdateTimeFrom = updateTimeRange.value[0]
      params.progressUpdateTimeTo = updateTimeRange.value[1]
    }
    
    const blob = await exportTaskProgressReport(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    const fileName = `任务进展报告_${new Date().toISOString().replace(/T/, '_').replace(/\..+/, '')}.xlsx`
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
    handleClose()
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleClose = () => {
  queryForm.projectIds = []
  startDateRange.value = null
  endDateRange.value = null
  updateTimeRange.value = null
  emit('update:modelValue', false)
}

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    // 对话框打开时清空表单，确保每次都是干净状态
    queryForm.projectIds = []
    startDateRange.value = null
    endDateRange.value = null
    updateTimeRange.value = null
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

onMounted(() => {
  loadProjectList()
})
</script>
