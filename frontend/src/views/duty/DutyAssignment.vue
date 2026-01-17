<template>
  <div class="assignment-container">
    <div class="page-header">
      <h2>值班安排</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加值班安排
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
        <el-select
          v-model="selectedScheduleId"
          placeholder="选择值班表"
          clearable
          class="schedule-select"
          @change="handleScheduleChange"
          style="width: 250px; margin-right: 10px"
        >
          <el-option
            v-for="schedule in scheduleList"
            :key="schedule.id"
            :label="schedule.scheduleName"
            :value="schedule.id"
          />
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="请输入人员姓名"
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="pagedAssignmentList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dutyDate" label="值班日期" width="150" />
        <el-table-column prop="dutyShift" label="班次" width="100">
          <template #default="scope">
            <el-tag :type="'info'">
              {{ getShiftName(scope.row.dutyShift) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="employeeId" label="值班人员" min-width="150">
          <template #default="scope">
            {{ getEmployeeName(scope.row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredAssignmentList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑值班安排对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="assignmentFormRef"
        :model="assignmentForm"
        :rules="assignmentRules"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="值班表" prop="scheduleId">
              <el-select
                v-model="assignmentForm.scheduleId"
                placeholder="请选择值班表"
                style="width: 100%"
              >
                <el-option
                  v-for="schedule in scheduleList"
                  :key="schedule.id"
                  :label="schedule.scheduleName"
                  :value="schedule.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班日期" prop="dutyDate">
              <el-date-picker
                v-model="assignmentForm.dutyDate"
                type="date"
                placeholder="选择值班日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班次" prop="dutyShift">
              <el-select
                v-model="assignmentForm.dutyShift"
                placeholder="请选择班次"
                style="width: 100%"
              >
                <el-option label="早班" :value="1" />
                <el-option label="中班" :value="2" />
                <el-option label="晚班" :value="3" />
                <el-option label="全天" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班人员" prop="employeeId">
              <el-select
                v-model="assignmentForm.employeeId"
                placeholder="请选择值班人员"
                style="width: 100%"
                filterable
                remote
                :remote-method="remoteSearchEmployee"
                :loading="employeeLoading"
              >
                <el-option
                  v-for="employee in employeeList"
                  :key="employee.id"
                  :label="employee.employeeName"
                  :value="employee.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="assignmentForm.status">
            <el-radio :label="1">有效</el-radio>
            <el-radio :label="0">无效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="assignmentForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="handleSave">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAssignmentList,
  getAssignmentsByScheduleId,
  addAssignment,
  updateAssignment,
  deleteAssignment
} from '../../api/duty/assignment'
import { getScheduleList } from '../../api/duty/schedule'
import { getEmployeeList } from '../../api/employee'

// 响应式数据
const searchQuery = ref('')
const selectedScheduleId = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加值班安排')
const assignmentFormRef = ref()
const employeeLoading = ref(false)

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 数据列表
const scheduleList = ref([])
const employeeList = ref([])
const assignmentList = ref([])

// 表单数据
const assignmentForm = reactive({
  id: null,
  scheduleId: null,
  dutyDate: null,
  dutyShift: 1,
  employeeId: null,
  status: 1,
  remark: ''
})

// 表单验证规则
const assignmentRules = {
  scheduleId: [
    { required: true, message: '请选择值班表', trigger: 'blur' }
  ],
  dutyDate: [
    { required: true, message: '请选择值班日期', trigger: 'blur' }
  ],
  dutyShift: [
    { required: true, message: '请选择班次', trigger: 'blur' }
  ],
  employeeId: [
    { required: true, message: '请选择值班人员', trigger: 'blur' }
  ]
}

// 班次名称映射
const shiftNames = {
  1: '早班',
  2: '中班',
  3: '晚班',
  4: '全天'
}

// 获取班次名称
const getShiftName = (shift) => {
  return shiftNames[shift] || '未知班次'
}

// 获取员工姓名
const getEmployeeName = (employeeId) => {
  const employee = employeeList.value.find(e => e.id === employeeId)
  return employee ? employee.employeeName : '未知人员'
}

// 过滤后的值班安排列表
const filteredAssignmentList = computed(() => {
  let list = assignmentList.value
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(assignment => {
      const employeeName = getEmployeeName(assignment.employeeId).toLowerCase()
      return employeeName.includes(query)
    })
  }
  
  return list
})

// 分页后的值班安排列表
const pagedAssignmentList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAssignmentList.value.slice(start, end)
})

// 远程搜索员工
const remoteSearchEmployee = async (query) => {
  if (query) {
    employeeLoading.value = true
    try {
      const response = await getEmployeeList()
      if (response.code === 200) {
        employeeList.value = response.data.filter(employee => 
          employee.employeeName.toLowerCase().includes(query.toLowerCase())
        )
      }
    } catch (error) {
      console.error('搜索员工失败:', error)
    } finally {
      employeeLoading.value = false
    }
  } else {
    // 如果搜索词为空，加载所有员工
    await fetchEmployeeList()
  }
}

// 获取值班表列表
const fetchScheduleList = async () => {
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  }
}

// 获取员工列表
const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      employeeList.value = response.data
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}

// 获取值班安排列表
const fetchAssignmentList = async (scheduleId = null) => {
  loading.value = true
  try {
    let response
    if (scheduleId) {
      response = await getAssignmentsByScheduleId(scheduleId)
    } else {
      response = await getAssignmentList()
    }
    
    if (response.code === 200) {
      assignmentList.value = response.data
    }
  } catch (error) {
    console.error('获取值班安排列表失败:', error)
    ElMessage.error('获取值班安排列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 值班表选择变化
const handleScheduleChange = (scheduleId) => {
  fetchAssignmentList(scheduleId)
  currentPage.value = 1
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 打开添加对话框
const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '添加值班安排'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (assignment) => {
  Object.assign(assignmentForm, assignment)
  dialogTitle.value = '编辑值班安排'
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (assignmentFormRef.value) {
    assignmentFormRef.value.resetFields()
  }
  Object.assign(assignmentForm, {
    id: null,
    scheduleId: selectedScheduleId.value || null,
    dutyDate: null,
    dutyShift: 1,
    employeeId: null,
    status: 1,
    remark: ''
  })
}

// 保存值班安排
const handleSave = async () => {
  try {
    await assignmentFormRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (assignmentForm.id) {
      // 编辑值班安排
      response = await updateAssignment(assignmentForm)
    } else {
      // 添加值班安排
      response = await addAssignment(assignmentForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(assignmentForm.id ? '编辑值班安排成功' : '添加值班安排成功')
      dialogVisible.value = false
      fetchAssignmentList(selectedScheduleId.value)
    } else {
      ElMessage.error(response.message || (assignmentForm.id ? '编辑值班安排失败' : '添加值班安排失败'))
    }
  } catch (error) {
    console.error('保存值班安排失败:', error)
    ElMessage.error('保存值班安排失败')
  } finally {
    dialogLoading.value = false
  }
}

// 删除值班安排
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班安排吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteAssignment(id)
    if (response.code === 200) {
      ElMessage.success('删除值班安排成功')
      fetchAssignmentList(selectedScheduleId.value)
    } else {
      ElMessage.error(response.message || '删除值班安排失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除值班安排失败:', error)
      ElMessage.error('删除值班安排失败')
    }
  }
}

// 生命周期钩子
onMounted(async () => {
  await fetchScheduleList()
  await fetchEmployeeList()
  await fetchAssignmentList()
})
</script>

<style scoped>
.assignment-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.content-card {
  margin-bottom: 10px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>