<template>
  <div class="person-selector">
    <!-- 左侧部门选择 -->
    <div class="dept-section">
      <div class="section-title">部门选择</div>
      <el-tree
        :data="deptTree"
        node-key="id"
        :props="deptTreeProps"
        :default-expand-all="true"
        @node-click="handleDeptClick"
        :current-node-key="selectedDeptId"
        highlight-current
        style="height: 100%"
        :indent="20"
      />
    </div>

    <!-- 中间人员列表 -->
    <div class="employee-section">
      <div class="section-title">
        人员列表
        <el-input
          v-model="searchKeyword"
          placeholder="搜索人员"
          clearable
          size="small"
          @input="handleSearch"
          @keyup.enter="handleSearchEnter"
          style="width: 180px; margin-left: 10px"
        />
      </div>
      <el-table
        ref="employeeTable"
        :data="filteredEmployeeList"
        style="width: 100%"
        height="calc(100% - 40px)"
        size="small"
        @selection-change="handleEmployeeSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="employeeName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
      </el-table>
    </div>

    <!-- 中间转移按钮 -->
    <div class="transfer-buttons">
      <el-button
        type="primary"
        @click="transferToRight"
        :disabled="selectedEmployeeRows.length === 0"
        size="small"
        icon="el-icon-right"
      >
        选择 &gt;&gt;
      </el-button>
      <el-button
        type="primary"
        @click="transferAllToRight"
        :disabled="employeeList.length === 0"
        size="small"
        icon="el-icon-right"
      >
        全部 &gt;&gt;
      </el-button>
      <el-button
        type="info"
        @click="transferToLeft"
        :disabled="selectedSelectedRows.length === 0"
        size="small"
        icon="el-icon-left"
      >
        &lt;&lt; 移除
      </el-button>
      <el-button
        type="info"
        @click="transferAllToLeft"
        :disabled="selectedEmployees.length === 0"
        size="small"
        icon="el-icon-left"
      >
        &lt;&lt; 全部
      </el-button>
    </div>

    <!-- 右侧已选人员 -->
    <div class="selected-section">
      <div class="section-title">
        已选人员 ({{ selectedEmployees.length }})
      </div>
      <el-table
        ref="selectedTable"
        :data="selectedEmployees"
        style="width: 100%"
        height="calc(100% - 40px)"
        size="small"
        @selection-change="handleSelectedSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="employeeName" label="姓名" min-width="150" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { getDeptTree } from '@/api/dept'
import { getEmployeesByDeptId, getEmployeeList } from '@/api/employee'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择人员'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 部门相关
const deptTree = ref([])
const selectedDeptId = ref(null)
const deptTreeProps = {
  label: 'deptName',
  children: 'children'
}

// 人员相关
const employeeList = ref([])
const selectedEmployees = ref([])
const searchKeyword = ref('')

// 选择状态
const selectedEmployeeRows = ref([])
const selectedSelectedRows = ref([])

// 计算属性：过滤后的人员列表
const filteredEmployeeList = computed(() => {
  if (!searchKeyword.value) return employeeList.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return employeeList.value.filter(emp => 
    emp.employeeName.toLowerCase().includes(keyword) ||
    emp.employeeCode.toLowerCase().includes(keyword) ||
    (emp.phone && emp.phone.includes(keyword))
  )
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  selectedEmployees.value = Array.isArray(newValue) ? newValue : []
}, { deep: true, immediate: true })

// 加载部门树
const loadDeptTree = async () => {
  try {
    const data = await getDeptTree()
    console.log('部门树数据:', data)
    // 确保数据结构正确，转换为树结构所需的格式
    if (data && Array.isArray(data)) {
      deptTree.value = data.map(dept => ({
        ...dept,
        children: dept.children || []
      }))
      console.log('处理后的部门树数据:', deptTree.value)
    } else {
      deptTree.value = []
    }
  } catch (error) {
    console.error('加载部门树失败', error)
    deptTree.value = []
  }
}

// 加载部门人员
const loadEmployeesByDept = async (deptId) => {
  try {
    const data = await getEmployeesByDeptId(deptId)
    // 过滤掉已选中的人员
    const filteredData = (data || []).filter(emp => 
      !selectedEmployees.value.some(selected => selected.id === emp.id)
    )
    employeeList.value = filteredData
  } catch (error) {
    console.error('加载部门人员失败', error)
    employeeList.value = []
  }
}

// 处理部门点击
const handleDeptClick = (node) => {
  selectedDeptId.value = node.id
  loadEmployeesByDept(node.id)
  // 重置全量搜索状态
  isFullSearch.value = false
}

// 处理人员选择变化
const handleEmployeeSelectionChange = (rows) => {
  selectedEmployeeRows.value = rows
}

// 处理已选人员选择变化
const handleSelectedSelectionChange = (rows) => {
  selectedSelectedRows.value = rows
}

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑已在computed中处理
  // 重置全量搜索状态
  isFullSearch.value = false
}

// 处理搜索框回车事件
const isFullSearch = ref(false)

const handleSearchEnter = async () => {
  console.log('handleSearchEnter called')
  if (!searchKeyword.value.trim()) {
    console.log('Search keyword is empty')
    return
  }
  
  console.log('Search keyword:', searchKeyword.value)
  console.log('isFullSearch:', isFullSearch.value)
  
  if (!isFullSearch.value) {
    // 第一次回车：跳到对应部门，展示部门下所有人
    try {
      console.log('Searching for employee and navigating to department...')
      // 调用全量搜索API
      const response = await getEmployeeList(1, 1000, searchKeyword.value)
      console.log('API response:', response)
      const employees = response?.records || []
      console.log('Employees:', employees)
      
      if (employees.length > 0) {
        // 找到第一个匹配人员所在的部门
        const firstEmployee = employees[0]
        const deptId = firstEmployee.deptId
        console.log('Found employee:', firstEmployee)
        console.log('Employee deptId:', deptId)
        
        // 找到对应的部门节点并选中
        const selectDeptNode = (nodes, targetDeptId) => {
          for (const node of nodes) {
            if (node.id === targetDeptId) {
              return node
            }
            if (node.children && node.children.length > 0) {
              const found = selectDeptNode(node.children, targetDeptId)
              if (found) {
                return found
              }
            }
          }
          return null
        }
        
        const deptNode = selectDeptNode(deptTree.value, deptId)
        console.log('Found dept node:', deptNode)
        
        if (deptNode) {
          // 选中该部门
          selectedDeptId.value = deptId
          console.log('Selected dept ID:', deptId)
          // 加载该部门下的所有人员
          await loadEmployeesByDept(deptId)
          console.log('Loaded employees for dept:', deptId)
        } else {
          // 如果没找到对应部门，显示全量搜索结果
          const filteredData = employees.filter(emp => 
            !selectedEmployees.value.some(selected => selected.id === emp.id)
          )
          employeeList.value = filteredData
        }
      } else {
        // 没有找到匹配的人员
        employeeList.value = []
      }
      
      isFullSearch.value = true
      console.log('isFullSearch set to:', isFullSearch.value)
    } catch (error) {
      console.error('搜索人员失败', error)
    }
  } else {
    // 第二次回车：在当前部门下过滤人员
    console.log('Performing local filter in current department...')
    // 搜索逻辑已在computed中处理，只需触发重新计算
    employeeList.value = [...employeeList.value]
    console.log('Local filter applied')
  }
}

// 转移到右侧
const transferToRight = () => {
  if (selectedEmployeeRows.value.length === 0) return
  
  // 添加选中的人员到已选列表
  selectedEmployeeRows.value.forEach(emp => {
    if (!selectedEmployees.value.some(selected => selected.id === emp.id)) {
      selectedEmployees.value.push(emp)
    }
  })
  
  // 从左侧列表移除已选中的人员
  employeeList.value = employeeList.value.filter(emp => 
    !selectedEmployeeRows.value.some(selected => selected.id === emp.id)
  )
  
  // 清空选择
  selectedEmployeeRows.value = []
  if (employeeList.value.length > 0) {
    // 重新加载表格以清除选择状态
    employeeList.value = [...employeeList.value]
  }
  
  // 触发事件
  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

// 全部转移到右侧
const transferAllToRight = () => {
  if (employeeList.value.length === 0) return
  
  // 添加所有人员到已选列表
  employeeList.value.forEach(emp => {
    if (!selectedEmployees.value.some(selected => selected.id === emp.id)) {
      selectedEmployees.value.push(emp)
    }
  })
  
  // 清空左侧列表
  employeeList.value = []
  
  // 触发事件
  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

// 转移到左侧
const transferToLeft = () => {
  if (selectedSelectedRows.value.length === 0) return
  
  // 添加选中的人员到左侧列表
  selectedSelectedRows.value.forEach(emp => {
    if (!employeeList.value.some(selected => selected.id === emp.id)) {
      employeeList.value.push(emp)
    }
  })
  
  // 从右侧列表移除已选中的人员
  selectedEmployees.value = selectedEmployees.value.filter(emp => 
    !selectedSelectedRows.value.some(selected => selected.id === emp.id)
  )
  
  // 清空选择
  selectedSelectedRows.value = []
  if (selectedEmployees.value.length > 0) {
    // 重新加载表格以清除选择状态
    selectedEmployees.value = [...selectedEmployees.value]
  }
  
  // 触发事件
  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

// 全部转移到左侧
const transferAllToLeft = () => {
  if (selectedEmployees.value.length === 0) return
  
  // 添加所有人员到左侧列表
  selectedEmployees.value.forEach(emp => {
    if (!employeeList.value.some(selected => selected.id === emp.id)) {
      employeeList.value.push(emp)
    }
  })
  
  // 清空右侧列表
  selectedEmployees.value = []
  
  // 触发事件
  emit('update:modelValue', selectedEmployees.value)
  emit('change', selectedEmployees.value)
}

// 初始化
onMounted(() => {
  loadDeptTree()
})
</script>

<style scoped>
.person-selector {
  display: flex;
  height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  overflow: hidden;
}

.dept-section {
  width: 200px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.employee-section {
  flex: 2;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  min-width: 400px;
}

.selected-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 200px;
}

.transfer-buttons {
  width: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  background-color: #f9fafc;
  border-right: 1px solid #e4e7ed;
}

.transfer-buttons :deep(.el-button) {
  width: 100%;
  text-align: left !important;
  justify-content: flex-start !important;
  padding-left: 10px !important;
  margin: 0 !important;
}

.transfer-buttons :deep(.el-button .el-button__content) {
  text-align: left !important;
  justify-content: flex-start !important;
}

.section-title {
  padding: 10px 15px;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
  background-color: #f9fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

:deep(.el-tree) {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

:deep(.el-table) {
  flex: 1;
  border-top: none;
  border-bottom: none;
}

:deep(.el-table__header-wrapper) {
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff !important;
  color: #409eff;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa !important;
}

:deep(.el-button) {
  width: 100%;
  justify-content: center;
}
</style>