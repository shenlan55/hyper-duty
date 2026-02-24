<template>
  <el-popover
    placement="bottom"
    :width="700"
    trigger="click"
    v-model:visible="popoverVisible"
  >
    <template #reference>
      <el-input
        v-model="displayValue"
        :placeholder="placeholder"
        readonly
        clearable
        @clear="handleClear"
      />
    </template>
    <div class="employee-selector">
      <div class="employee-selector-header">
        <el-form :inline="true" :model="searchForm" class="employee-search-form">
          <el-form-item label="部门">
            <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable @change="handleDeptChange" style="width: 150px;">
              <el-option
                v-for="dept in deptList"
                :key="dept.id"
                :label="dept.deptName"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.keyword" placeholder="请输入姓名" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="employee-selector-body">
        <el-table
          :data="employeeList"
          style="width: 100%"
          @row-click="handleSelect"
          :row-class-name="row => value === row.id ? 'selected-employee' : ''"
          size="small"
        >
          <el-table-column prop="employeeName" label="姓名" width="100" />
          <el-table-column prop="employeeCode" label="工号" width="100" />
          <el-table-column prop="deptName" label="部门" width="150" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="200" />
        </el-table>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { getEmployeeList } from '@/api/employee'
import { getDeptList } from '@/api/dept'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null
  },
  placeholder: {
    type: String,
    default: '请选择人员'
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const popoverVisible = ref(false)
const displayValue = ref('')
const value = ref(props.modelValue)
const deptList = ref([])
const employeeList = ref([])

const searchForm = reactive({
  deptId: null,
  keyword: ''
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  value.value = newValue
  if (newValue) {
    // 查找对应的员工姓名
    const employee = employeeList.value.find(emp => emp.id === newValue)
    if (employee) {
      displayValue.value = employee.employeeName
    }
  } else {
    displayValue.value = ''
  }
}, { immediate: true })

// 加载部门列表
const loadDeptList = async () => {
  try {
    const data = await getDeptList()
    deptList.value = data || []
  } catch (error) {
    console.error('加载部门列表失败', error)
    deptList.value = []
  }
}

// 加载员工列表
const loadEmployeeList = async () => {
  try {
    const data = await getEmployeeList()
    employeeList.value = Array.isArray(data) ? data : (data?.records || [])
    
    // 如果已经有选中值，更新显示
    if (value.value) {
      const employee = employeeList.value.find(emp => emp.id === value.value)
      if (employee) {
        displayValue.value = employee.employeeName
      }
    }
  } catch (error) {
    console.error('加载员工列表失败', error)
    employeeList.value = []
  }
}

// 处理部门变更
const handleDeptChange = () => {
  // 根据选择的部门筛选员工
  if (searchForm.deptId) {
    loadEmployeeListByDept(searchForm.deptId)
  } else {
    loadEmployeeList()
  }
}

// 根据部门ID加载员工列表
const loadEmployeeListByDept = async (deptId) => {
  try {
    // 这里可以调用专门的API根据部门ID获取员工列表
    // 暂时使用前端筛选
    const allEmployees = await getEmployeeList()
    const employees = Array.isArray(allEmployees) ? allEmployees : (allEmployees?.records || [])
    employeeList.value = employees.filter(emp => emp.deptId === deptId)
  } catch (error) {
    console.error('加载部门员工列表失败', error)
    employeeList.value = []
  }
}

// 处理搜索
const handleSearch = () => {
  // 根据部门和关键词筛选员工
  let filteredEmployees = employeeList.value
  
  if (searchForm.deptId) {
    filteredEmployees = filteredEmployees.filter(emp => emp.deptId === searchForm.deptId)
  }
  
  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    filteredEmployees = filteredEmployees.filter(emp => 
      emp.employeeName.toLowerCase().includes(keyword)
    )
  }
  
  employeeList.value = filteredEmployees
}

// 处理选择
const handleSelect = (row) => {
  value.value = row.id
  displayValue.value = row.employeeName
  emit('update:modelValue', row.id)
  emit('select', row)
  popoverVisible.value = false
}

// 处理清空
const handleClear = () => {
  value.value = null
  displayValue.value = ''
  emit('update:modelValue', null)
  emit('select', null)
}

// 初始化
onMounted(() => {
  loadDeptList()
  loadEmployeeList()
})
</script>

<style scoped>
.employee-selector {
  padding: 15px;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.employee-selector-header {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.employee-search-form {
  margin-bottom: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.employee-selector-body {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

:deep(.selected-employee) {
  background-color: #ecf5ff !important;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

:deep(.el-table th) {
  background-color: #f9fafc;
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: 4px;
  overflow: hidden;
}
</style>