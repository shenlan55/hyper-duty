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
      <div class="filter-form">
        <el-form :inline="true" :model="filterForm" class="mb-4">
          <el-form-item label="审批状态">
            <el-select v-model="filterForm.approvalStatus" placeholder="请选择审批状态" style="width: 150px;">
              <el-option label="全部" value="" />
              <el-option label="待审批" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item label="值班表">
            <el-select v-model="filterForm.scheduleId" placeholder="请选择值班表" style="width: 200px;">
              <el-option
                v-for="schedule in scheduleList"
                :key="schedule.id"
                :label="schedule.scheduleName"
                :value="schedule.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="filterForm.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="filterForm.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 180px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="loading"
        :data="requestList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="requestNo" label="申请编号" width="180" />
        <el-table-column prop="scheduleName" label="值班表" width="150" show-overflow-tooltip />
        <el-table-column prop="originalEmployeeName" label="原值班人员" width="120" />
        <el-table-column prop="targetEmployeeName" label="目标值班人员" width="120" />
        <el-table-column label="调班信息" width="250">
          <template #default="scope">
            {{ formatDate(scope.row.swapDate) }} {{ getShiftName(scope.row.swapShift) }}
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
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
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
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="值班表" prop="scheduleId">
          <el-select
            v-model="form.scheduleId"
            placeholder="请选择值班表"
            style="width: 100%"
            @change="handleScheduleChange"
          >
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="schedule.scheduleName"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="调班原因" prop="reason">
          <el-input
            v-model="form.reason"
            placeholder="请输入调班原因"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item label="调班详情" prop="swapDetails">
          <div v-for="(detail, index) in form.swapDetails" :key="index" class="swap-detail-item">
            <el-divider :content-position="'left'">调班组合 {{ index + 1 }}</el-divider>
            <!-- 原值班人员信息 -->
            <div class="swap-person-row">
              <h4 class="person-title">原值班人员</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.originalEmployeeId`" :rules="[{ required: true, message: '请选择原值班人员', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">值班人员</span>
                    </template>
                    <el-select
                      v-model="detail.originalEmployeeId"
                      placeholder="请选择原值班人员"
                      style="width: 100%"
                      :disabled="!isLeader"
                      filterable
                    >
                      <el-option
                        v-for="employee in availableEmployeeList"
                        :key="employee.id"
                        :label="employee.employeeName"
                        :value="employee.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.originalSwapDate`" :rules="[{ required: true, message: '请选择调班日期', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">调班日期</span>
                    </template>
                    <el-date-picker
                      v-model="detail.originalSwapDate"
                      type="date"
                      placeholder="选择日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.originalSwapShift`" :rules="[{ required: true, message: '请选择调班班次', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">调班班次</span>
                    </template>
                    <el-select v-model="detail.originalSwapShift" placeholder="请选择班次" style="width: 100%">
                      <el-option
                        v-for="shiftConfig in shiftConfigList.filter(s => availableShiftIds.length === 0 || availableShiftIds.includes(s.id))"
                        :key="shiftConfig.id"
                        :label="shiftConfig.shiftName"
                        :value="shiftConfig.shiftType"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <!-- 目标值班人员信息 -->
            <div class="swap-person-row">
              <h4 class="person-title">目标值班人员</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.targetEmployeeId`" :rules="[{ required: true, message: '请选择目标值班人员', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">值班人员</span>
                    </template>
                    <el-select
                      v-model="detail.targetEmployeeId"
                      placeholder="请选择目标值班人员"
                      style="width: 100%"
                      filterable
                    >
                      <el-option
                        v-for="employee in availableEmployeeList"
                        :key="employee.id"
                        :label="employee.employeeName"
                        :value="employee.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.targetSwapDate`" :rules="[{ required: true, message: '请选择调班日期', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">调班日期</span>
                    </template>
                    <el-date-picker
                      v-model="detail.targetSwapDate"
                      type="date"
                      placeholder="选择日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item :prop="`swapDetails.${index}.targetSwapShift`" :rules="[{ required: true, message: '请选择调班班次', trigger: 'blur' }]">
                    <template #label>
                      <span class="detail-label">调班班次</span>
                    </template>
                    <el-select v-model="detail.targetSwapShift" placeholder="请选择班次" style="width: 100%">
                      <el-option
                        v-for="shiftConfig in shiftConfigList.filter(s => availableShiftIds.length === 0 || availableShiftIds.includes(s.id))"
                        :key="shiftConfig.id"
                        :label="shiftConfig.shiftName"
                        :value="shiftConfig.shiftType"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            <el-button 
              v-if="form.swapDetails.length > 1"
              type="danger" 
              size="small" 
              @click="removeSwapDetail(index)"
              style="margin-top: 10px;"
            >
              删除调班组合
            </el-button>
          </div>
          
          <el-button type="primary" size="small" @click="addSwapDetail" style="margin-top: 10px;">
            添加调班组合
          </el-button>
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
        <el-descriptions-item label="值班表">{{ getScheduleName(currentSwapRequest.scheduleId) }}</el-descriptions-item>
        <el-descriptions-item label="原值班人员">{{ getEmployeeName(currentSwapRequest.originalEmployeeId) }}</el-descriptions-item>
        <el-descriptions-item label="目标值班人员">{{ getEmployeeName(currentSwapRequest.targetEmployeeId) }}</el-descriptions-item>
        <el-descriptions-item label="调班日期">{{ formatDate(currentSwapRequest.swapDate) }}</el-descriptions-item>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMySwapRequests,
  submitSwapRequest,
  deleteSwapRequest,
  confirmSwapRequest,
  getMySwapRequestsPage
} from '../../api/duty/swapRequest'
import { getEmployeeList } from '../../api/employee'
import { getScheduleList, getScheduleEmployees, getScheduleShifts } from '../../api/duty/schedule'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { getUserInfo } from '../../utils/auth'

const shiftApi = shiftConfigApi()

const loading = ref(false)
const dialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const dialogLoading = ref(false)
const confirmLoading = ref(false)
const dialogTitle = ref('申请调班')
const formRef = ref()
const requestList = ref([])
const employeeList = ref([])
const shiftConfigList = ref([])
const scheduleList = ref([])
const availableEmployeeList = ref([])
// 当前值班表可用的班次ID列表
const availableShiftIds = ref([])
const currentSwapRequest = ref({})
const currentEmployeeId = ref(1)
const isLeader = ref(false)

// 获取当前用户信息
const userInfo = getUserInfo()
if (userInfo) {
  currentEmployeeId.value = userInfo.employeeId
  isLeader.value = userInfo.roles.includes('值班长')
}

const form = reactive({
  id: null,
  scheduleId: null,
  reason: '',
  swapDetails: [
    {
      originalEmployeeId: null,
      originalSwapDate: null,
      originalSwapShift: null,
      targetEmployeeId: null,
      targetSwapDate: null,
      targetSwapShift: null
    }
  ]
})

const filterForm = reactive({
  approvalStatus: '',
  scheduleId: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const total = ref(0)

const rules = {
  scheduleId: [
    { required: true, message: '请选择值班表', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调班原因', trigger: 'blur' }
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
  const shiftConfig = shiftConfigList.value.find(config => config.shiftType === shift)
  if (shiftConfig) {
    return shiftConfig.shiftName
  }
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

const getScheduleName = (scheduleId) => {
  const schedule = scheduleList.value.find(s => s.id === scheduleId)
  return schedule ? schedule.scheduleName : '未知'
}

const filterEmployee = (query) => {
  if (query) {
    return availableEmployeeList.value.filter(employee => 
      employee.employeeName.toLowerCase().includes(query.toLowerCase())
    )
  }
  return availableEmployeeList.value
}

const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      employeeList.value = response.data.filter(emp => emp.status === 1)
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

const fetchShiftConfigList = async () => {
  try {
    const response = await shiftApi.getShiftConfigList()
    if (response.code === 200) {
      shiftConfigList.value = response.data.filter(config => config.status === 1)
    }
  } catch (error) {
    console.error('获取班次配置列表失败:', error)
  }
}

const fetchScheduleList = async () => {
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data.filter(schedule => schedule.status === 1)
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
  }
}

const fetchAvailableEmployees = async (scheduleId) => {
  try {
    if (scheduleId) {
      const response = await getScheduleEmployees(scheduleId)
      if (response.code === 200) {
        const employeeIds = response.data
        // 根据员工ID列表，从employeeList中获取对应的员工对象
        availableEmployeeList.value = employeeList.value.filter(emp => 
          employeeIds.includes(emp.id)
        )
      }
    } else {
      availableEmployeeList.value = []
    }
  } catch (error) {
    console.error('获取值班表人员失败:', error)
  }
}

const fetchMySwapRequests = async () => {
  loading.value = true
  try {
    const response = await getMySwapRequestsPage(
      currentEmployeeId.value,
      pagination.currentPage,
      pagination.pageSize,
      filterForm.approvalStatus,
      filterForm.scheduleId,
      filterForm.startDate,
      filterForm.endDate
    )
    if (response.code === 200) {
      requestList.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取调班申请列表失败:', error)
    ElMessage.error('获取调班申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.currentPage = 1
  fetchMySwapRequests()
}

const resetFilter = () => {
  filterForm.approvalStatus = ''
  filterForm.scheduleId = null
  filterForm.startDate = null
  filterForm.endDate = null
  pagination.currentPage = 1
  fetchMySwapRequests()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchMySwapRequests()
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
  fetchMySwapRequests()
}

const handleScheduleChange = async (scheduleId) => {
  await fetchAvailableEmployees(scheduleId)
  
  // 清空之前的班次选择
  form.swapDetails.forEach(detail => {
    detail.originalSwapShift = null
    detail.targetSwapShift = null
  })
  
  if (scheduleId) {
    try {
      // 获取值班表绑定的班次列表
      const response = await getScheduleShifts(scheduleId)
      if (response.code === 200) {
        availableShiftIds.value = response.data || []
      }
    } catch (error) {
      console.error('获取值班表班次失败:', error)
      ElMessage.error('获取值班表班次失败')
      availableShiftIds.value = []
    }
  } else {
    availableShiftIds.value = []
  }
  
  // 非值班长只能选择自己
  if (!isLeader.value) {
    form.swapDetails.forEach(detail => {
      detail.originalEmployeeId = currentEmployeeId.value
    })
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
    scheduleId: null,
    reason: '',
    swapDetails: [
      {
        originalEmployeeId: isLeader.value ? null : currentEmployeeId.value,
        originalSwapDate: null,
        originalSwapShift: null,
        targetEmployeeId: null,
        targetSwapDate: null,
        targetSwapShift: null
      }
    ]
  })
  availableEmployeeList.value = []
  availableShiftIds.value = []
}

const addSwapDetail = () => {
  form.swapDetails.push({
    originalEmployeeId: isLeader.value ? null : currentEmployeeId.value,
    originalSwapDate: null,
    originalSwapShift: null,
    targetEmployeeId: null,
    targetSwapDate: null,
    targetSwapShift: null
  })
}

const removeSwapDetail = (index) => {
  if (form.swapDetails.length > 1) {
    form.swapDetails.splice(index, 1)
  }
}

const handleSave = async () => {
  try {
    // 先验证基本字段
    await formRef.value.validate()
    
    // 手动验证调班详情
    for (let i = 0; i < form.swapDetails.length; i++) {
      const detail = form.swapDetails[i]
      if (!detail.originalEmployeeId) {
        ElMessage.error(`调班组合 ${i + 1}：请选择原值班人员`)
        return
      }
      if (!detail.originalSwapDate) {
        ElMessage.error(`调班组合 ${i + 1}：请选择原值班日期`)
        return
      }
      if (!detail.originalSwapShift) {
        ElMessage.error(`调班组合 ${i + 1}：请选择原值班班次`)
        return
      }
      if (!detail.targetEmployeeId) {
        ElMessage.error(`调班组合 ${i + 1}：请选择目标值班人员`)
        return
      }
      if (!detail.targetSwapDate) {
        ElMessage.error(`调班组合 ${i + 1}：请选择目标值班日期`)
        return
      }
      if (!detail.targetSwapShift) {
        ElMessage.error(`调班组合 ${i + 1}：请选择目标值班班次`)
        return
      }
    }
    
    dialogLoading.value = true
    
    // 构建调班申请数据
    const swapRequests = form.swapDetails.map(detail => ({
      scheduleId: form.scheduleId,
      originalEmployeeId: detail.originalEmployeeId,
      targetEmployeeId: detail.targetEmployeeId,
      swapDate: detail.originalSwapDate,
      swapShift: detail.originalSwapShift,
      reason: form.reason
    }))
    
    // 提交多个调班申请
    let allSuccess = true
    for (const swapRequest of swapRequests) {
      const response = await submitSwapRequest(swapRequest)
      if (response.code !== 200) {
        allSuccess = false
        ElMessage.error(response.message || '调班申请提交失败')
        break
      }
    }
    
    if (allSuccess) {
      ElMessage.success('调班申请提交成功')
      dialogVisible.value = false
      fetchMySwapRequests()
    }
  } catch (error) {
    console.error('提交调班申请失败:', error)
    // 不显示通用错误，因为表单验证错误会单独处理
  } finally {
    dialogLoading.value = false
  }
}

const handleConfirm = async () => {
  try {
    confirmLoading.value = true
    
    const response = await confirmSwapRequest(
      currentSwapRequest.value.id,
      currentEmployeeId.value,
      'approved'
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
  await fetchShiftConfigList()
  await fetchScheduleList()
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

.swap-detail-item {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.detail-label {
  font-weight: 500;
  color: #303133;
}

.person-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.swap-person-row {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-divider {
  margin: 10px 0;
}

.el-form-item {
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .swap-detail-item {
    padding: 10px;
  }
  
  .el-col {
    margin-bottom: 10px;
  }
}
</style>
