<template>
  <div class="swap-request-container">
    <div class="page-header">
      <h2>调班管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        申请调班
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <el-table
        v-loading="loading"
        :data="requestList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="requestNo" label="申请编号" width="180" />
        <el-table-column prop="originalEmployeeName" label="原值班人员" width="120" />
        <el-table-column prop="targetEmployeeName" label="目标值班人员" width="120" />
        <el-table-column label="调班信息" width="250">
          <template #default="scope">
            {{ scope.row.swapDate }} {{ getShiftName(scope.row.swapShift) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调班原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="approvalStatus" label="审批状态" width="100">
          <template #default="scope">
            <el-tag :type="getApprovalStatusColor(scope.row.approvalStatus)">
              {{ getApprovalStatusName(scope.row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.approvalStatus === 'pending'"
              type="primary"
              size="small"
              @click="openConfirmDialog(scope.row)"
            >
              确认
            </el-button>
            <el-button
              v-if="scope.row.approvalStatus === 'pending'"
              type="danger"
              size="small"
              @click="handleDelete(scope.row.id)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="原值班人员" prop="originalEmployeeId">
          <el-select
            v-model="form.originalEmployeeId"
            placeholder="请选择原值班人员"
            style="width: 100%"
            filterable
            :filter-method="filterEmployee"
          >
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值班人员" prop="targetEmployeeId">
          <el-select
            v-model="form.targetEmployeeId"
            placeholder="请选择目标值班人员"
            style="width: 100%"
            filterable
            :filter-method="filterEmployee"
          >
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调班日期" prop="swapDate">
              <el-date-picker
                v-model="form.swapDate"
                type="date"
                placeholder="选择调班日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调班班次" prop="swapShift">
              <el-select v-model="form.swapShift" placeholder="请选择班次" style="width: 100%">
                <el-option label="早班" :value="1" />
                <el-option label="中班" :value="2" />
                <el-option label="晚班" :value="3" />
                <el-option label="全天" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调班原因" prop="reason">
          <el-input
            v-model="form.reason"
            placeholder="请输入调班原因"
            type="textarea"
            :rows="4"
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
      v-model="confirmDialogVisible"
      title="调班确认"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请编号">{{ currentSwapRequest.requestNo }}</el-descriptions-item>
        <el-descriptions-item label="原值班人员">{{ getEmployeeName(currentSwapRequest.originalEmployeeId) }}</el-descriptions-item>
        <el-descriptions-item label="目标值班人员">{{ getEmployeeName(currentSwapRequest.targetEmployeeId) }}</el-descriptions-item>
        <el-descriptions-item label="调班日期">{{ currentSwapRequest.swapDate }}</el-descriptions-item>
        <el-descriptions-item label="调班班次">{{ getShiftName(currentSwapRequest.swapShift) }}</el-descriptions-item>
        <el-descriptions-item label="调班原因">{{ currentSwapRequest.reason }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">
            确认调班
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMySwapRequests,
  submitSwapRequest,
  deleteSwapRequest,
  confirmSwapRequest
} from '../../api/duty/swapRequest'
import { getEmployeeList } from '../../api/employee'

const loading = ref(false)
const dialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const dialogLoading = ref(false)
const confirmLoading = ref(false)
const dialogTitle = ref('申请调班')
const formRef = ref()
const requestList = ref([])
const employeeList = ref([])
const currentSwapRequest = ref({})
const currentEmployeeId = ref(1)

const form = reactive({
  id: null,
  originalEmployeeId: null,
  targetEmployeeId: null,
  swapDate: null,
  swapShift: 1,
  reason: ''
})

const rules = {
  originalEmployeeId: [
    { required: true, message: '请选择原值班人员', trigger: 'blur' }
  ],
  targetEmployeeId: [
    { required: true, message: '请选择目标值班人员', trigger: 'blur' }
  ],
  swapDate: [
    { required: true, message: '请选择调班日期', trigger: 'blur' }
  ],
  swapShift: [
    { required: true, message: '请选择调班班次', trigger: 'blur' }
  ]
}

const shiftNames = {
  1: '早班',
  2: '中班',
  3: '晚班',
  4: '全天'
}

const approvalStatusMap = {
  'pending': '待审批',
  'approved': '已通过',
  'rejected': '已拒绝',
  'cancelled': '已取消'
}

const approvalStatusColorMap = {
  'pending': 'warning',
  'approved': 'success',
  'rejected': 'danger',
  'cancelled': 'info'
}

const getShiftName = (shift) => {
  return shiftNames[shift] || '未知'
}

const getApprovalStatusName = (status) => {
  return approvalStatusMap[status] || '未知'
}

const getApprovalStatusColor = (status) => {
  return approvalStatusColorMap[status] || 'info'
}

const getEmployeeName = (employeeId) => {
  const employee = employeeList.value.find(e => e.id === employeeId)
  return employee ? employee.employeeName : '未知'
}

const filterEmployee = (query) => {
  if (query) {
    return employeeList.value.filter(employee => 
      employee.employeeName.toLowerCase().includes(query.toLowerCase())
    )
  }
  return employeeList.value
}

const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      employeeList.value = response.data
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

const fetchMySwapRequests = async () => {
  loading.value = true
  try {
    const response = await getMySwapRequests(currentEmployeeId.value)
    if (response.code === 200) {
      requestList.value = response.data
    }
  } catch (error) {
    console.error('获取调班申请列表失败:', error)
    ElMessage.error('获取调班申请列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '申请调班'
  dialogVisible.value = true
}

const openConfirmDialog = (row) => {
  currentSwapRequest.value = row
  confirmDialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    originalEmployeeId: null,
    targetEmployeeId: null,
    swapDate: null,
    swapShift: 1,
    reason: ''
  })
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (form.id) {
      response = await submitSwapRequest(form)
    } else {
      response = await submitSwapRequest(form)
    }
    
    if (response.code === 200) {
      ElMessage.success('调班申请提交成功')
      dialogVisible.value = false
      fetchMySwapRequests()
    } else {
      ElMessage.error(response.message || '调班申请提交失败')
    }
  } catch (error) {
    console.error('提交调班申请失败:', error)
    ElMessage.error('提交调班申请失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleConfirm = async () => {
  try {
    confirmLoading.value = true
    
    const response = await confirmSwapRequest(
      currentSwapRequest.value.id,
      currentEmployeeId.value
    )
    
    if (response.code === 200) {
      ElMessage.success('调班确认成功')
      confirmDialogVisible.value = false
      fetchMySwapRequests()
    } else {
      ElMessage.error(response.message || '调班确认失败')
    }
  } catch (error) {
    console.error('调班确认失败:', error)
    ElMessage.error('调班确认失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤销该调班申请吗？', '撤销确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteSwapRequest(id)
    if (response.code === 200) {
      ElMessage.success('调班申请撤销成功')
      fetchMySwapRequests()
    } else {
      ElMessage.error(response.message || '调班申请撤销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销调班申请失败:', error)
      ElMessage.error('撤销调班申请失败')
    }
  }
}

onMounted(async () => {
  await fetchEmployeeList()
  await fetchMySwapRequests()
})
</script>

<style scoped>
.swap-request-container {
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
</style>
