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
          v-for="(name, value) in taskStatusMap" 
          :key="value" 
          :label="name" 
          :value="Number(value)" 
        />
      </el-select>
    </el-form-item>
    <el-form-item label="优先级">
      <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable style="width: 100px;">
        <el-option 
          v-for="(name, value) in taskPriorityMap" 
          :key="value" 
          :label="name" 
          :value="Number(value)" 
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
import { TASK_STATUS_MAP, TASK_PRIORITY_MAP } from '@/constants/task'

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

const taskStatusMap = TASK_STATUS_MAP
const taskPriorityMap = TASK_PRIORITY_MAP

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}

const handleProjectChange = (value) => {
  emit('projectChange', value)
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
