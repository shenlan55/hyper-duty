<template>
  <div class="record-container">
    <div class="page-header">
      <h2>值班记录</h2>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
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
        :data="pagedRecordList"
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
        <el-table-column prop="checkInTime" label="签到时间" width="180" />
        <el-table-column prop="checkOutTime" label="签退时间" width="180" />
        <el-table-column prop="dutyStatus" label="值班状态" width="120">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.dutyStatus)"
            >
              {{ getStatusName(scope.row.dutyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInRemark" label="签到备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="checkOutRemark" label="签退备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="overtimeHours" label="加班时长" width="100">
          <template #default="scope">
            {{ scope.row.overtimeHours || 0 }}小时
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template #default="scope">
            <el-tag
              :type="getApprovalType(scope.row.approvalStatus)"
            >
              {{ scope.row.approvalStatus || '待审批' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.dutyStatus === 0 || !scope.row.dutyStatus"
              type="success" 
              size="small" 
              @click="openCheckInDialog(scope.row)"
            >
              签到
            </el-button>
            <el-button
              v-else-if="scope.row.dutyStatus === 1"
              type="primary" 
              size="small" 
              @click="openCheckOutDialog(scope.row)"
            >
              签退
            </el-button>
            <el-button type="warning" size="small" @click="openEditDialog(scope.row)">
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
          :total="filteredRecordList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 签到对话框 -->
    <el-dialog
      v-model="checkInDialogVisible"
      title="值班签到"
      width="500px"
    >
      <el-form
        ref="checkInFormRef"
        :model="checkInForm"
        :rules="checkInRules"
        label-position="top"
      >
        <el-form-item label="值班人员">
          <el-input
            v-model="currentRecordEmployeeName"
            disabled
          />
        </el-form-item>
        <el-form-item label="值班日期">
          <el-input
            v-model="currentRecordDutyDate"
            disabled
          />
        </el-form-item>
        <el-form-item label="签到时间" prop="checkInTime">
          <el-date-picker
            v-model="checkInForm.checkInTime"
            type="datetime"
            placeholder="选择签到时间"
            style="width: 100%"
            default-time="00:00:00"
          />
        </el-form-item>
        <el-form-item label="签到备注" prop="checkInRemark">
          <el-input
            v-model="checkInForm.checkInRemark"
            placeholder="请输入签到备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkInDialogVisible = false">取消</el-button>
          <el-button type="success" :loading="checkInLoading" @click="handleCheckIn">
            确认签到
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 签退对话框 -->
    <el-dialog
      v-model="checkOutDialogVisible"
      title="值班签退"
      width="500px"
    >
      <el-form
        ref="checkOutFormRef"
        :model="checkOutForm"
        :rules="checkOutRules"
        label-position="top"
      >
        <el-form-item label="值班人员">
          <el-input
            v-model="currentRecordEmployeeName"
            disabled
          />
        </el-form-item>
        <el-form-item label="值班日期">
          <el-input
            v-model="currentRecordDutyDate"
            disabled
          />
        </el-form-item>
        <el-form-item label="签到时间">
          <el-input
            v-model="currentRecordCheckInTime"
            disabled
          />
        </el-form-item>
        <el-form-item label="签退时间" prop="checkOutTime">
          <el-date-picker
            v-model="checkOutForm.checkOutTime"
            type="datetime"
            placeholder="选择签退时间"
            style="width: 100%"
            default-time="23:59:59"
          />
        </el-form-item>
        <el-form-item label="加班时长（小时）" prop="overtimeHours">
          <el-input-number
            v-model="checkOutForm.overtimeHours"
            :min="0"
            :max="24"
            :step="0.5"
            placeholder="请输入加班时长"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="签退备注" prop="checkOutRemark">
          <el-input
            v-model="checkOutForm.checkOutRemark"
            placeholder="请输入签退备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkOutDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="checkOutLoading" @click="handleCheckOut">
            确认签退
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑值班记录对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑值班记录"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="值班状态" prop="dutyStatus">
              <el-select
                v-model="editForm.dutyStatus"
                placeholder="请选择值班状态"
                style="width: 100%"
              >
                <el-option label="未签到" :value="0" />
                <el-option label="已签到" :value="1" />
                <el-option label="已签退" :value="2" />
                <el-option label="请假" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批状态" prop="approvalStatus">
              <el-select
                v-model="editForm.approvalStatus"
                placeholder="请选择审批状态"
                style="width: 100%"
              >
                <el-option label="待审批" value="待审批" />
                <el-option label="已批准" value="已批准" />
                <el-option label="已拒绝" value="已拒绝" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="签到备注" prop="checkInRemark">
          <el-input
            v-model="editForm.checkInRemark"
            placeholder="请输入签到备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="签退备注" prop="checkOutRemark">
          <el-input
            v-model="editForm.checkOutRemark"
            placeholder="请输入签退备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="加班时长（小时）" prop="overtimeHours">
          <el-input-number
            v-model="editForm.overtimeHours"
            :min="0"
            :max="24"
            :step="0.5"
            placeholder="请输入加班时长"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="editForm.dutyStatus === 3" label="替补人员" prop="substituteEmployeeId">
          <el-radio-group v-model="substituteType" @change="handleSubstituteTypeChange">
            <el-radio :label="1">自动匹配</el-radio>
            <el-radio :label="2">手动选择</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editForm.dutyStatus === 3 && substituteType === 1" label="推荐替补人员">
          <el-select
            v-model="editForm.substituteEmployeeId"
            placeholder="请选择替补人员"
            style="width: 100%"
            :loading="substituteLoading"
          >
            <el-option
              v-for="employee in availableSubstitutes"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            >
              <span>{{ employee.employeeName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ getEmployeeDeptName(employee.deptId) }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="editForm.dutyStatus === 3 && substituteType === 2" label="选择替补人员" prop="substituteEmployeeId">
          <el-select
            v-model="editForm.substituteEmployeeId"
            placeholder="请选择替补人员"
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
        <el-form-item label="管理员备注" prop="managerRemark">
          <el-input
            v-model="editForm.managerRemark"
            placeholder="请输入管理员备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="handleEditSave">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRecordList,
  checkIn,
  checkOut,
  updateRecord,
  deleteRecord,
  getAvailableSubstitutes
} from '../../api/duty/record'
import { getEmployeeList } from '../../api/employee'
import { getAssignmentList } from '../../api/duty/assignment'
import { getDeptList } from '../../api/dept'

// 响应式数据
const searchQuery = ref('')
const loading = ref(false)
const checkInDialogVisible = ref(false)
const checkOutDialogVisible = ref(false)
const editDialogVisible = ref(false)
const checkInLoading = ref(false)
const checkOutLoading = ref(false)
const editLoading = ref(false)
const checkInFormRef = ref()
const checkOutFormRef = ref()
const editFormRef = ref()

// 当前记录信息
const currentRecord = ref(null)
const currentRecordEmployeeName = ref('')
const currentRecordDutyDate = ref('')
const currentRecordCheckInTime = ref('')

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 数据列表
const employeeList = ref([])
const assignmentList = ref([])
const recordList = ref([])

// 班次名称映射
const shiftNames = {
  1: '早班',
  2: '中班',
  3: '晚班',
  4: '全天'
}

// 值班状态映射
const dutyStatus = {
  0: '未签到',
  1: '已签到',
  2: '已签退',
  3: '请假'
}

// 状态类型映射
const statusType = {
  0: 'warning',
  1: 'info',
  2: 'success',
  3: 'danger'
}

// 审批状态类型映射
const approvalType = {
  '待审批': 'warning',
  '已批准': 'success',
  '已拒绝': 'danger'
}

// 表单数据
const checkInForm = reactive({
  checkInTime: null,
  checkInRemark: ''
})

const checkOutForm = reactive({
  checkOutTime: null,
  checkOutRemark: '',
  overtimeHours: 0
})

const editForm = reactive({
  id: null,
  dutyStatus: 0,
  checkInRemark: '',
  checkOutRemark: '',
  overtimeHours: 0,
  approvalStatus: '待审批',
  managerRemark: '',
  substituteEmployeeId: null,
  substituteType: 1
})

const substituteType = ref(1)
const availableSubstitutes = ref([])
const substituteLoading = ref(false)

const deptList = ref([])

// 表单验证规则
const checkInRules = {
  checkInTime: [
    { required: true, message: '请选择签到时间', trigger: 'blur' }
  ]
}

const checkOutRules = {
  checkOutTime: [
    { required: true, message: '请选择签退时间', trigger: 'blur' }
  ]
}

const editRules = {
  dutyStatus: [
    { required: true, message: '请选择值班状态', trigger: 'blur' }
  ],
  approvalStatus: [
    { required: true, message: '请选择审批状态', trigger: 'blur' }
  ]
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

// 获取状态名称
const getStatusName = (status) => {
  return dutyStatus[status] || '未知状态'
}

// 获取状态类型
const getStatusType = (status) => {
  return statusType[status] || 'info'
}

// 获取审批类型
const getApprovalType = (status) => {
  return approvalType[status] || 'info'
}

const getEmployeeDeptName = (deptId) => {
  const dept = deptList.value.find(d => d.id === deptId)
  return dept ? dept.deptName : '未知部门'
}

// 过滤后的值班记录列表
const filteredRecordList = computed(() => {
  let list = recordList.value
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(record => {
      const employeeName = getEmployeeName(record.employeeId).toLowerCase()
      return employeeName.includes(query)
    })
  }
  
  return list
})

// 分页后的值班记录列表
const pagedRecordList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecordList.value.slice(start, end)
})

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
const fetchAssignmentList = async () => {
  try {
    const response = await getAssignmentList()
    if (response.code === 200) {
      assignmentList.value = response.data
    }
  } catch (error) {
    console.error('获取值班安排列表失败:', error)
    ElMessage.error('获取值班安排列表失败')
  }
}

// 获取值班记录列表
const fetchRecordList = async () => {
  loading.value = true
  try {
    const response = await getRecordList()
    if (response.code === 200) {
      recordList.value = response.data
    }
  } catch (error) {
    console.error('获取值班记录列表失败:', error)
    ElMessage.error('获取值班记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
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

// 打开签到对话框
const openCheckInDialog = (record) => {
  currentRecord.value = record
  currentRecordEmployeeName.value = getEmployeeName(record.employeeId)
  currentRecordDutyDate.value = record.dutyDate
  
  checkInForm.checkInTime = new Date()
  checkInForm.checkInRemark = ''
  
  checkInDialogVisible.value = true
}

// 打开签退对话框
const openCheckOutDialog = (record) => {
  currentRecord.value = record
  currentRecordEmployeeName.value = getEmployeeName(record.employeeId)
  currentRecordDutyDate.value = record.dutyDate
  currentRecordCheckInTime.value = record.checkInTime
  
  checkOutForm.checkOutTime = new Date()
  checkOutForm.checkOutRemark = ''
  checkOutForm.overtimeHours = 0
  
  checkOutDialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = async (record) => {
  currentRecord.value = record
  Object.assign(editForm, record)
  substituteType.value = record.substituteType || 1
  
  if (record.dutyStatus === 3 && substituteType.value === 1) {
    await fetchAvailableSubstitutes(record.id)
  }
  
  editDialogVisible.value = true
}

// 处理签到
const handleCheckIn = async () => {
  try {
    await checkInFormRef.value.validate()
    checkInLoading.value = true
    
    const response = await checkIn(currentRecord.value.assignmentId, {
      employeeId: currentRecord.value.employeeId,
      checkInTime: checkInForm.checkInTime,
      checkInRemark: checkInForm.checkInRemark
    })
    
    if (response.code === 200) {
      ElMessage.success('签到成功')
      checkInDialogVisible.value = false
      fetchRecordList()
    } else {
      ElMessage.error(response.message || '签到失败')
    }
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  } finally {
    checkInLoading.value = false
  }
}

// 处理签退
const handleCheckOut = async () => {
  try {
    await checkOutFormRef.value.validate()
    checkOutLoading.value = true
    
    const response = await checkOut(currentRecord.value.id, {
      checkOutRemark: checkOutForm.checkOutRemark,
      overtimeHours: checkOutForm.overtimeHours
    })
    
    if (response.code === 200) {
      ElMessage.success('签退成功')
      checkOutDialogVisible.value = false
      fetchRecordList()
    } else {
      ElMessage.error(response.message || '签退失败')
    }
  } catch (error) {
    console.error('签退失败:', error)
    ElMessage.error('签退失败')
  } finally {
    checkOutLoading.value = false
  }
}

// 处理编辑保存
const handleEditSave = async () => {
  try {
    await editFormRef.value.validate()
    editLoading.value = true
    
    editForm.substituteType = substituteType.value
    
    const response = await updateRecord(editForm)
    
    if (response.code === 200) {
      ElMessage.success('编辑值班记录成功')
      editDialogVisible.value = false
      fetchRecordList()
    } else {
      ElMessage.error(response.message || '编辑值班记录失败')
    }
  } catch (error) {
    console.error('编辑值班记录失败:', error)
    ElMessage.error('编辑值班记录失败')
  } finally {
    editLoading.value = false
  }
}

// 删除值班记录
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteRecord(id)
    if (response.code === 200) {
      ElMessage.success('删除值班记录成功')
      fetchRecordList()
    } else {
      ElMessage.error(response.message || '删除值班记录失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除值班记录失败:', error)
      ElMessage.error('删除值班记录失败')
    }
  }
}

const handleSubstituteTypeChange = async (type) => {
  if (type === 1 && currentRecord.value) {
    await fetchAvailableSubstitutes(currentRecord.value.id)
  }
}

const fetchAvailableSubstitutes = async (recordId) => {
  substituteLoading.value = true
  try {
    const response = await getAvailableSubstitutes(recordId)
    if (response.code === 200) {
      availableSubstitutes.value = response.data
      if (availableSubstitutes.value.length > 0) {
        editForm.substituteEmployeeId = availableSubstitutes.value[0].id
      }
    }
  } catch (error) {
    console.error('获取替补人员失败:', error)
    ElMessage.error('获取替补人员失败')
  } finally {
    substituteLoading.value = false
  }
}

const fetchDeptList = async () => {
  try {
    const response = await getDeptList()
    if (response.code === 200) {
      deptList.value = response.data
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  }
}

// 生命周期钩子
onMounted(async () => {
  await fetchEmployeeList()
  await fetchAssignmentList()
  await fetchDeptList()
  await fetchRecordList()
})
</script>

<style scoped>
.record-container {
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