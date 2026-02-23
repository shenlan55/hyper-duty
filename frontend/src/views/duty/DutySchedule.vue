<template>
  <div class="schedule-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>值班表管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加值班表
          </el-button>
        </div>
      </template>
      
      <BaseTable
        v-loading="loading"
        :data="scheduleList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :backend-pagination="true"
        :show-search="true"
        :search-placeholder="'请输入值班表名称'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleTableSearch"
        @export="handleExport"
        @sort-change="handleSortChange"
      >
        <template #startDate="{ row }">
          {{ formatDate(row.startDate) }}
        </template>
        <template #endDate="{ row }">
          {{ formatDate(row.endDate) }}
        </template>
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #operation="{ row }">
          <el-button type="primary" size="small" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button type="success" size="small" @click="openEmployeeDialog(row)">
            人员
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </BaseTable>
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
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="scheduleForm.sortOrder"
            :min="0"
            :step="1"
            placeholder="请输入排序数字"
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
import BaseTable from '../../components/BaseTable.vue'
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
import { safeInput } from '../../utils/xssUtil'
import { useSearchPagination } from '../../hooks/usePagination'

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

// 分页配置
const {
  currentPage,
  pageSize,
  total,
  pagination,
  handleCurrentChange: originalHandleCurrentChange,
  handleSizeChange: originalHandleSizeChange,
  searchQuery,
  handleSearch
} = useSearchPagination()

const scheduleList = ref([])
const allEmployeeList = ref([])
const selectedEmployeeList = ref([])
const allShiftList = ref([])
const selectedShiftList = ref([])

const sortOrder = ref(null)
const sortProp = ref(null)

const scheduleForm = reactive({
  id: null,
  scheduleName: '',
  description: '',
  startDate: null,
  endDate: null,
  status: 1,
  sortOrder: 0
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



const columns = [
  { prop: 'sortOrder', label: '排序', width: '100', sortable: true },
  { prop: 'scheduleName', label: '值班表名称', minWidth: '150', sortable: true },
  { prop: 'description', label: '描述', minWidth: '200' },
  { prop: 'startDate', label: '开始日期', width: '150', sortable: true },
  { prop: 'endDate', label: '结束日期', width: '150', sortable: true },
  { prop: 'status', label: '状态', width: '100', sortable: true },
  { type: 'operation', label: '操作', width: '240', fixed: 'right' }
]

const getEmployeeName = (employeeId) => {
  const employee = allEmployeeList.value.find(e => e.key === employeeId)
  return employee ? employee.label : '未知人员'
}

const handleSortChange = (sort) => {
  if (sort.prop) {
    sortProp.value = sort.prop
    sortOrder.value = sort.order
  } else {
    sortProp.value = null
    sortOrder.value = null
  }
  fetchScheduleList()
}

const fetchScheduleList = async () => {
  loading.value = true
  try {
    const data = await getScheduleList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value,
      sortField: sortProp.value,
      sortOrder: sortOrder.value
    })
    scheduleList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('获取值班表列表失败')
  } finally {
    loading.value = false
  }
}

const fetchEmployeeList = async () => {
  try {
    const data = await getEmployeeList(1, 1000, '') // 传递较大的pageSize以获取所有员工
    allEmployeeList.value = (data?.records || []).map(emp => ({
      key: emp.id,
      label: emp.employeeName,
      disabled: emp.status !== 1
    }))
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
}

const shiftApi = shiftConfigApi()

const fetchShiftList = async () => {
  try {
    const data = await shiftApi.getShiftConfigList({
      pageNum: 1,
      pageSize: 100, // 加载足够多的班次
      keyword: ''
    })
    allShiftList.value = (data?.records || []).map(shift => ({
      key: shift.id,
      label: shift.shiftName,
      disabled: shift.status !== 1
    }))
  } catch (error) {
    ElMessage.error('获取班次列表失败')
  }
}

const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  handleSearch(keyword)
  fetchScheduleList()
}

const handleExport = (exportParams) => {
  // 这里可以添加导出逻辑，例如调用后端API或使用前端库导出
  ElMessage.success(`导出${exportParams.format}格式成功`)
}

const handleSizeChange = (val) => {
  originalHandleSizeChange(val)
  fetchScheduleList()
}

const handleCurrentChange = (val) => {
  originalHandleCurrentChange(val)
  fetchScheduleList()
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
    status: 1,
    sortOrder: 0
  })
}

const handleSave = async () => {
  try {
    await scheduleFormRef.value.validate()
    dialogLoading.value = true
    
    // 处理用户输入，防止XSS攻击
    const safeForm = {
      ...scheduleForm,
      scheduleName: safeInput(scheduleForm.scheduleName),
      description: safeInput(scheduleForm.description)
    }
    
    if (safeForm.id) {
      await updateSchedule(safeForm)
    } else {
      await addSchedule(safeForm)
    }
    
    ElMessage.success(safeForm.id ? '编辑值班表成功' : '添加值班表成功')
    dialogVisible.value = false
    fetchScheduleList()
  } catch (error) {
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
    
    await deleteSchedule(id)
    ElMessage.success('删除值班表成功')
    fetchScheduleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除值班表失败')
    }
  }
}

const openEmployeeDialog = async (schedule) => {
  currentScheduleId.value = schedule.id
  try {
    const [employeeData, leaderData, shiftData] = await Promise.all([
      getScheduleEmployees(schedule.id),
      getScheduleLeaders(schedule.id),
      getScheduleShifts(schedule.id)
    ])
    selectedEmployeeList.value = employeeData || []
    selectedLeaderList.value = leaderData || []
    selectedShiftList.value = shiftData || []
  } catch (error) {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
