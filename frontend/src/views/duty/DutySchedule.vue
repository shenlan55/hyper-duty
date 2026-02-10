<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>值班表管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加值班表
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="请输入值班表名称"
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="pagedScheduleList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="scheduleName" label="值班表名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="success" size="small" @click="openEmployeeDialog(scope.row)">
              人员
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
          :total="filteredScheduleList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        label-position="top"
      >
        <el-form-item label="值班表名称" prop="scheduleName">
          <el-input
            v-model="scheduleForm.scheduleName"
            placeholder="请输入值班表名称"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="scheduleForm.description"
            placeholder="请输入值班表描述"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="scheduleForm.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="scheduleForm.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="scheduleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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

    <el-dialog
      v-model="employeeDialogVisible"
      title="值班人员管理"
      width="700px"
    >
      <div class="employee-manage">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="人员列表" name="list">
            <div class="selected-employees">
              <div class="section-title">已选人员（{{ selectedEmployeeList.length }}人）</div>
              <el-transfer
                v-model="selectedEmployeeList"
                :data="allEmployeeList"
                :titles="['可选人员', '已选人员']"
                filterable
                filter-placeholder="搜索人员"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="值班长设置" name="leader">
            <div class="leader-setting">
              <div class="section-title">设置值班长（{{ selectedLeaderList.length }}人）</div>
              <el-checkbox-group v-model="selectedLeaderList">
                <div class="leader-list">
                  <el-checkbox
                    v-for="employee in selectedEmployeeList"
                    :key="employee"
                    :value="employee"
                  >
                    {{ getEmployeeName(employee) }}
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
          </el-tab-pane>
          <el-tab-pane label="班次设置" name="shift">
            <div class="shift-setting">
              <div class="section-title">可选班次（{{ selectedShiftList.length }}个）</div>
              <el-transfer
                v-model="selectedShiftList"
                :data="allShiftList"
                :titles="['可选班次', '已选班次']"
                filterable
                filter-placeholder="搜索班次"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="employeeDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="employeeDialogLoading" @click="handleSaveEmployees">
            保存
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
  getScheduleList,
  getScheduleById,
  addSchedule,
  updateSchedule,
  deleteSchedule,
  getScheduleEmployees,
  getScheduleLeaders,
  getScheduleShifts,
  updateScheduleEmployees,
  updateScheduleLeaders,
  updateScheduleShifts,
  updateScheduleEmployeesAndLeaders
} from '../../api/duty/schedule'
import { getEmployeeList } from '../../api/employee'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'

const searchQuery = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加值班表')
const scheduleFormRef = ref()

const employeeDialogVisible = ref(false)
const employeeDialogLoading = ref(false)
const currentScheduleId = ref(null)
const activeTab = ref('list')
const selectedLeaderList = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const scheduleList = ref([])
const allEmployeeList = ref([])
const selectedEmployeeList = ref([])
const allShiftList = ref([])
const selectedShiftList = ref([])

const scheduleForm = reactive({
  id: null,
  scheduleName: '',
  description: '',
  startDate: null,
  endDate: null,
  status: 1
})

const scheduleRules = {
  scheduleName: [
    { required: true, message: '请输入值班表名称', trigger: 'blur' },
    { min: 2, max: 100, message: '值班表名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'blur' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'blur' }
  ]
}

const filteredScheduleList = computed(() => {
  let list = scheduleList.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(schedule => 
      schedule.scheduleName.toLowerCase().includes(query)
    )
  }
  
  return list
})

const pagedScheduleList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredScheduleList.value.slice(start, end)
})

const getEmployeeName = (employeeId) => {
  const employee = allEmployeeList.value.find(e => e.key === employeeId)
  return employee ? employee.label : '未知人员'
}

const fetchScheduleList = async () => {
  loading.value = true
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  } finally {
    loading.value = false
  }
}

const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      allEmployeeList.value = response.data.map(emp => ({
        key: emp.id,
        label: emp.employeeName,
        disabled: emp.status !== 1
      }))
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}

const shiftApi = shiftConfigApi()

const fetchShiftList = async () => {
  try {
    const response = await shiftApi.getShiftConfigList()
    if (response.code === 200) {
      allShiftList.value = response.data.map(shift => ({
        key: shift.id,
        label: shift.shiftName,
        disabled: shift.status !== 1
      }))
    }
  } catch (error) {
    console.error('获取班次列表失败:', error)
    ElMessage.error('获取班次列表失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '添加值班表'
  dialogVisible.value = true
}

const openEditDialog = (schedule) => {
  Object.assign(scheduleForm, schedule)
  dialogTitle.value = '编辑值班表'
  dialogVisible.value = true
}

const resetForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields()
  }
  Object.assign(scheduleForm, {
    id: null,
    scheduleName: '',
    description: '',
    startDate: null,
    endDate: null,
    status: 1
  })
}

const handleSave = async () => {
  try {
    await scheduleFormRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (scheduleForm.id) {
      response = await updateSchedule(scheduleForm)
    } else {
      response = await addSchedule(scheduleForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(scheduleForm.id ? '编辑值班表成功' : '添加值班表成功')
      dialogVisible.value = false
      fetchScheduleList()
    } else {
      ElMessage.error(response.message || (scheduleForm.id ? '编辑值班表失败' : '添加值班表失败'))
    }
  } catch (error) {
    console.error('保存值班表失败:', error)
    ElMessage.error('保存值班表失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班表吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteSchedule(id)
    if (response.code === 200) {
      ElMessage.success('删除值班表成功')
      fetchScheduleList()
    } else {
      ElMessage.error(response.message || '删除值班表失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除值班表失败:', error)
      ElMessage.error('删除值班表失败')
    }
  }
}

const openEmployeeDialog = async (schedule) => {
  currentScheduleId.value = schedule.id
  try {
    const [employeeRes, leaderRes, shiftRes] = await Promise.all([
      getScheduleEmployees(schedule.id),
      getScheduleLeaders(schedule.id),
      getScheduleShifts(schedule.id)
    ])
    if (employeeRes.code === 200) {
      selectedEmployeeList.value = employeeRes.data || []
    }
    if (leaderRes.code === 200) {
      selectedLeaderList.value = leaderRes.data || []
    }
    if (shiftRes.code === 200) {
      selectedShiftList.value = shiftRes.data || []
    }
  } catch (error) {
    console.error('获取值班信息失败:', error)
    ElMessage.error('获取值班信息失败')
  }
  employeeDialogVisible.value = true
}

const handleSaveEmployees = async () => {
  try {
    employeeDialogLoading.value = true
    // 先保存人员和值班长信息
    await updateScheduleEmployeesAndLeaders(currentScheduleId.value, selectedEmployeeList.value, selectedLeaderList.value)
    // 再保存班次信息
    await updateScheduleShifts(currentScheduleId.value, selectedShiftList.value)
    ElMessage.success('保存值班信息成功')
    employeeDialogVisible.value = false
  } catch (error) {
    console.error('保存值班信息失败:', error)
    ElMessage.error('保存值班信息失败')
  } finally {
    employeeDialogLoading.value = false
  }
}

onMounted(async () => {
  await fetchScheduleList()
  await fetchEmployeeList()
  await fetchShiftList()
})
</script>

<style scoped>
.schedule-container {
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

.employee-manage {
  padding: 10px 0;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.selected-employees {
  margin-bottom: 20px;
}

.leader-setting {
  padding: 10px 0;
}

.leader-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
