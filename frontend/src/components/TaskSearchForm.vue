<template>
  <el-form :inline="true" :model="searchForm" class="search-form">
    <el-form-item label="项目">
      <el-select
        v-model="searchForm.projectId"
        placeholder="请选择项目"
        clearable
        filterable
        @change="handleProjectChange"
      >
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
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="Number(opt.value)"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="优先级">
      <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable style="width: 100px;">
        <el-option
          v-for="opt in priorityOptions"
          :key="opt.value"
          :label="opt.label"
          :value="Number(opt.value)"
        />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDict } from '@/composables/useDict'

const props = defineProps({
  searchForm: {
    type: Object,
    required: true
  },
  projectList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['search', 'reset', 'projectChange'])

// 任务状态/优先级：业务枚举从字典 API 加载，禁止前端硬编码 label
const { options: statusOptions, loadDict: loadStatusDict } = useDict('task_status')
const { options: priorityOptions, loadDict: loadPriorityDict } = useDict('task_priority')

onMounted(async () => {
  await Promise.all([loadStatusDict(), loadPriorityDict()])
})

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}

const handleProjectChange = (value) => {
  emit('projectChange', value)
  // 项目选择变化时直接触发搜索
  emit('search')
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
