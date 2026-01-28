<template>
  <div class="leave-approval-container">
    <div class="page-header">
      <h2>请假审批</h2>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick" style="margin-bottom: 20px">
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
      </el-tabs>
      <div class="header-actions">
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
        <el-button type="primary" @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="请假类型">
            <el-select v-model="filterForm.leaveType" placeholder="请选择请假类型" clearable style="width: 150px;">
              <el-option label="事假" :value="1" />
              <el-option label="病假" :value="2" />
              <el-option label="年假" :value="3" />
              <el-option label="调休" :value="4" />
              <el-option label="其他" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="审批状态" v-if="activeTab === 'approved'">
            <el-select v-model="filterForm.approvalStatus" placeholder="请选择审批状态" clearable style="width: 150px;">
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item label="请假时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
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
        <el-table-column prop="employeeName" label="申请人" width="120">
          <template #default="scope">
            {{ getEmployeeName(scope.row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="scope">
            <el-tag :type="getLeaveTypeColor(scope.row.leaveType)">
              {{ getLeaveTypeName(scope.row.leaveType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请假时间" width="250">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }} {{ scope.row.startTime || '' }} - {{ formatDate(scope.row.endDate) }} {{ scope.row.endTime || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="请假时长(小时)" width="130" />
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
              type="success"
              size="small"
              @click="openApproveDialog(scope.row)"
            >
              审批
            </el-button>
            <el-button type="primary" size="small" @click="openViewDialog(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="approveDialogVisible"
      title="请假审批"
      width="600px"
    >
      <el-form
        ref="approveFormRef"
        :model="approveForm"
        :rules="approveRules"
        label-position="top"
      >
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请编号">{{ currentRequest.requestNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ getEmployeeName(currentRequest.employeeId) }}</el-descriptions-item>
          <el-descriptions-item label="请假类型">
            <el-tag :type="getLeaveTypeColor(currentRequest.leaveType)">
              {{ getLeaveTypeName(currentRequest.leaveType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请假时长">{{ currentRequest.totalHours }}小时</el-descriptions-item>
          <el-descriptions-item label="请假时间">{{ currentRequest.startDate }} - {{ currentRequest.endDate }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRequest.shiftConfigIds" label="请假班次">{{ getShiftNames(currentRequest.shiftConfigIds) }}</el-descriptions-item>
          <el-descriptions-item label="请假原因" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="审批结果" prop="approvalStatus">
          <el-radio-group v-model="approveForm.approvalStatus" @change="handleApprovalStatusChange">
            <el-radio value="approved">通过</el-radio>
            <el-radio value="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approveForm.approvalStatus === 'approved'" label="排班处理" prop="scheduleAction">
          <el-radio-group v-model="approveForm.scheduleAction">
            <el-radio value="check">检查排班</el-radio>
            <el-radio value="substitute">选择顶岗人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approveForm.approvalStatus === 'approved' && approveForm.scheduleAction === 'substitute'" label="顶岗人员选择">
          <div v-if="substituteData.length === 0" class="text-center py-4">
            <el-button type="primary" @click="generateSubstituteSchedule">生成顶岗排班表</el-button>
          </div>
          <div v-else>
            <el-button type="primary" size="small" @click="selectAllSubstitutes">一键选择</el-button>
            <el-table :data="substituteData" style="width: 100%; margin-top: 10px">
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="shiftName" label="班次" width="120" />
              <el-table-column prop="originalEmployee" label="原值班人" width="120" />
              <el-table-column label="顶岗人员" width="200">
                <template #default="scope">
                  <el-select v-model="scope.row.substituteEmployeeId" placeholder="选择顶岗人员" style="width: 100%">
                    <el-option
                      v-for="employee in availableSubstitutes"
                      :key="employee.id"
                      :label="employee.employeeName"
                      :value="employee.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item v-if="approveForm.approvalStatus === 'approved' && approveForm.scheduleAction === 'check'" label="排班状态">
          <el-tag :type="scheduleStatus.type">{{ scheduleStatus.text }}</el-tag>
          <el-button v-if="scheduleStatus.type === 'success'" type="primary" size="small" @click="handleConfirmSchedule">
            确认排班完成
          </el-button>
          <el-button v-if="scheduleStatus.type === 'warning'" type="primary" size="small" @click="goToSchedule">
            去排班
          </el-button>
        </el-form-item>
        <el-form-item v-if="approveForm.approvalStatus === 'rejected'" label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="approveForm.rejectReason"
            placeholder="请输入拒绝原因"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="审批意见" prop="approvalOpinion">
          <el-input
            v-model="approveForm.approvalOpinion"
            placeholder="请输入审批意见"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="approveLoading" @click="handleApprove">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="请假详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号">{{ currentRequest.requestNo }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ getEmployeeName(currentRequest.employeeId) }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          <el-tag :type="getLeaveTypeColor(currentRequest.leaveType)">
            {{ getLeaveTypeName(currentRequest.leaveType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请假时长">{{ currentRequest.totalHours }}小时</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentRequest.startDate }} {{ currentRequest.startTime || '' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentRequest.endDate }} {{ currentRequest.endTime || '' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.shiftConfigIds" label="请假班次" :span="2">
          {{ getShiftNames(currentRequest.shiftConfigIds) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApprovalStatusColor(currentRequest.approvalStatus)">
            {{ getApprovalStatusName(currentRequest.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ formatDateTime(currentRequest.createTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.rejectReason" label="拒绝原因" :span="2">
          {{ currentRequest.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 顶岗信息表格 -->
      <div v-if="substituteInfoList.length > 0" style="margin-top: 20px">
        <h4>顶岗信息</h4>
        <el-table :data="substituteInfoList" style="width: 100%">
          <el-table-column prop="dutyDate" label="值班日期" width="120" />
          <el-table-column prop="shiftName" label="班次" width="120" />
          <el-table-column prop="originalEmployeeName" label="原值班人" width="120" />
          <el-table-column prop="substituteEmployeeName" label="顶岗人员" width="120" />
        </el-table>
      </div>
      <div v-else-if="currentRequest.approvalStatus === 'approved'" style="margin-top: 20px; text-align: center; color: #999">
        暂无顶岗信息
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPendingApprovals,
  getPendingApprovalsByScheduleId,
  getApprovedApprovals,
  getApprovedApprovalsByScheduleId,
  getPendingApprovalsPage,
  getApprovedApprovalsPage,
  approveLeaveRequest,
  confirmScheduleCompletion,
  checkEmployeeSchedule,
  getAvailableSubstitutes,
  getLeaveSubstitutes
} from '../../api/duty/leaveRequest'
import { getEmployeeList } from '../../api/employee'
import { getScheduleList } from '../../api/duty/schedule'
import {
  generateAutoSchedule,
  generateAutoScheduleByWorkHours,
  getEmployeeMonthlyWorkHours
} from '../../api/duty/autoSchedule'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const viewDialogVisible = ref(false)
const approveFormRef = ref()
const requestList = ref([])
const employeeList = ref([])
const scheduleList = ref([])
const selectedScheduleId = ref('')
const currentRequest = ref({})
const currentApproverId = ref(1)
const activeTab = ref('pending')
const shiftConfigList = ref([])
const shiftApi = shiftConfigApi()
const substituteInfoList = ref([])

// 分页相关
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选条件
const filterForm = reactive({
  leaveType: null,
  approvalStatus: '',
  dateRange: null
})

const approveForm = reactive({
  requestId: null,
  approverId: 1,
  approvalStatus: 'approved',
  rejectReason: '',
  approvalOpinion: '',
  scheduleAction: 'check',
  substituteData: [],
  availableSubstitutes: []
})

const substituteData = ref([])
const availableSubstitutes = ref([])

const approveRules = {
  approvalStatus: [
    { required: true, message: '请选择审批结果', trigger: 'blur' }
  ],
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

const scheduleStatus = reactive({
  type: 'info',
  text: '未检查'
})

const leaveTypeMap = {
  1: '事假',
  2: '病假',
  3: '年假',
  4: '调休',
  5: '其他'
}

const leaveTypeColorMap = {
  1: 'primary',
  2: 'warning',
  3: 'success',
  4: 'info',
  5: 'danger'
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

const getLeaveTypeName = (type) => {
  return leaveTypeMap[type] || '未知'
}

const getLeaveTypeColor = (type) => {
  return leaveTypeColorMap[type] || 'info'
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

const getShiftNames = (shiftConfigIds) => {
  if (!shiftConfigIds) return '未知'
  
  const shiftIds = typeof shiftConfigIds === 'string' ? shiftConfigIds.split(',') : shiftConfigIds
  const shiftNames = []
  
  shiftIds.forEach(shiftId => {
    const shift = shiftConfigList.value.find(s => s.id === Number(shiftId))
    if (shift) {
      shiftNames.push(shift.shiftName)
    }
  })
  
  return shiftNames.length > 0 ? shiftNames.join('、') : '未知'
}

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

const fetchEnabledShifts = async () => {
  try {
    const response = await shiftApi.getEnabledShifts()
    if (response.code === 200) {
      shiftConfigList.value = response.data
    }
  } catch (error) {
    console.error('获取班次配置列表失败:', error)
  }
}

const fetchPendingApprovals = async (tabName = activeTab.value) => {
  console.log('Fetching approvals, tabName:', tabName)
  loading.value = true
  try {
    let response
    const [startDate, endDate] = filterForm.dateRange || [null, null]
    
    if (tabName === 'pending') {
      console.log('Fetching pending approvals')
      response = await getPendingApprovalsPage(
        userStore.employeeId || 1,
        pagination.page,
        pagination.size,
        selectedScheduleId.value || null,
        filterForm.leaveType,
        startDate,
        endDate
      )
    } else {
      console.log('Fetching approved approvals')
      response = await getApprovedApprovalsPage(
        userStore.employeeId || 1,
        pagination.page,
        pagination.size,
        selectedScheduleId.value || null,
        filterForm.leaveType,
        filterForm.approvalStatus || null,
        startDate,
        endDate
      )
    }
    console.log('Response received:', response)
    if (response.code === 200) {
      requestList.value = response.data.records
      pagination.total = response.data.total
      console.log('Request list updated:', requestList.value)
    }
  } catch (error) {
    console.error('获取审批列表失败:', error)
    ElMessage.error('获取审批列表失败')
  } finally {
    loading.value = false
  }
}

const handleScheduleChange = () => {
  pagination.page = 1 // 重置页码
  fetchPendingApprovals()
}

const refreshList = () => {
  fetchPendingApprovals()
}

const handleTabClick = (tab) => {
  console.log('Tab clicked, tab name:', tab.props.name)
  pagination.page = 1 // 重置页码
  fetchPendingApprovals(tab.props.name)
}

// 分页大小变化处理
const handleSizeChange = (size) => {
  pagination.size = size
  fetchPendingApprovals()
}

// 页码变化处理
const handleCurrentChange = (current) => {
  pagination.page = current
  fetchPendingApprovals()
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1 // 重置页码
  fetchPendingApprovals()
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.leaveType = null
  filterForm.approvalStatus = ''
  filterForm.dateRange = null
  pagination.page = 1
  fetchPendingApprovals()
}

const openApproveDialog = (row) => {
  currentRequest.value = row
  approveForm.requestId = row.id
  approveForm.approverId = currentApproverId.value
  approveForm.approvalStatus = 'approved'
  approveForm.rejectReason = ''
  approveForm.approvalOpinion = ''
  approveDialogVisible.value = true
}

const openViewDialog = async (row) => {
  currentRequest.value = row
  viewDialogVisible.value = true
  
  // 获取顶岗信息
  await fetchSubstituteInfo(row.id)
}

const handleApprove = async () => {
  try {
    await approveFormRef.value.validate()
    approveLoading.value = true
    
    if (approveForm.approvalStatus === 'approved' && approveForm.scheduleAction === 'check') {
      await checkSchedule()
      return
    }
    
    if (approveForm.approvalStatus === 'approved' && approveForm.scheduleAction === 'substitute') {
      // 验证是否所有顶岗人员都已选择
      const hasEmptySubstitute = substituteData.value.some(item => !item.substituteEmployeeId)
      if (hasEmptySubstitute) {
        ElMessage.error('请为所有班次选择顶岗人员')
        approveLoading.value = false
        return
      }
    }
    
    const response = await approveLeaveRequest(
      approveForm.requestId,
      approveForm.approverId,
      approveForm.approvalStatus,
      approveForm.approvalStatus === 'rejected' ? approveForm.rejectReason : approveForm.approvalOpinion,
      approveForm.scheduleAction,
      substituteData.value // 传递顶岗人员数据
    )
    
    if (response.code === 200) {
      ElMessage.success('审批成功')
      approveDialogVisible.value = false
      fetchPendingApprovals()
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
  } finally {
    approveLoading.value = false
  }
}

const handleApprovalStatusChange = (status) => {
  if (status === 'rejected') {
    approveForm.scheduleAction = 'check'
  } else {
    approveForm.scheduleAction = 'check'
  }
}

const generateSubstituteSchedule = async () => {
  try {
    // 获取请假期间的所有日期和班次
    const startDate = currentRequest.value.startDate
    const endDate = currentRequest.value.endDate
    const shiftConfigIds = currentRequest.value.shiftConfigIds ? currentRequest.value.shiftConfigIds.split(',') : []
    
    // 生成日期范围
    const dates = []
    let current = new Date(startDate)
    const end = new Date(endDate)
    while (current <= end) {
      dates.push(current.toISOString().split('T')[0])
      current.setDate(current.getDate() + 1)
    }
    
    // 获取可用的顶岗人员（排除请假期间请假的人）
    await fetchAvailableSubstitutes(startDate, endDate)
    
    // 生成顶岗排班表
    const data = []
    dates.forEach(date => {
      shiftConfigIds.forEach(shiftId => {
        const shift = shiftConfigList.value.find(s => s.id === Number(shiftId))
        if (shift) {
          data.push({
            date,
            shiftId: Number(shiftId),
            shiftName: shift.shiftName,
            originalEmployee: getEmployeeName(currentRequest.value.employeeId),
            substituteEmployeeId: null
          })
        }
      })
    })
    
    substituteData.value = data
  } catch (error) {
    console.error('生成顶岗排班表失败:', error)
    ElMessage.error('生成顶岗排班表失败')
  }
}

const fetchAvailableSubstitutes = async (startDate, endDate) => {
  try {
    // 调用后端API获取可用的顶岗人员
    const response = await getAvailableSubstitutes(
      currentRequest.value.scheduleId,
      startDate,
      endDate,
      currentRequest.value.employeeId
    )
    if (response.code === 200) {
      availableSubstitutes.value = response.data
    }
  } catch (error) {
    console.error('获取可用顶岗人员失败:', error)
    ElMessage.error('获取可用顶岗人员失败')
  }
}

const selectAllSubstitutes = () => {
  // 一键选择逻辑：选择第一个可用的顶岗人员填入所有班次
  if (availableSubstitutes.value.length > 0) {
    const firstSubstituteId = availableSubstitutes.value[0].id
    substituteData.value.forEach(item => {
      item.substituteEmployeeId = firstSubstituteId
    })
    ElMessage.success('已为所有班次选择顶岗人员')
  }
}

const checkSchedule = async () => {
  try {
    const response = await checkEmployeeSchedule(currentRequest.value.employeeId, currentRequest.value.startDate, currentRequest.value.endDate)
    
    if (response.code === 200) {
      if (response.data.hasSchedule) {
        scheduleStatus.type = 'warning'
        scheduleStatus.text = '已有排班，建议选择顶岗人员'
        ElMessage.warning('申请人请假期间已有排班，建议选择顶岗人员')
      } else {
        scheduleStatus.type = 'info'
        scheduleStatus.text = '无排班，需要选择顶岗人员'
        ElMessage.info('申请人请假期间无排班，需要选择顶岗人员')
      }
    }
  } catch (error) {
    console.error('检查排班失败:', error)
    ElMessage.error('检查排班失败')
  }
}

const goToSchedule = () => {
  const router = useRouter()
  router.push({
    path: '/duty/assignment',
    query: {
      scheduleId: selectedScheduleId.value,
      date: currentRequest.value.startDate
    }
  })
}

const handleConfirmSchedule = async () => {
  try {
    const response = await confirmScheduleCompletion(approveForm.requestId, approveForm.approverId)
    
    if (response.code === 200) {
      ElMessage.success('排班完成确认成功')
      approveDialogVisible.value = false
      fetchPendingApprovals()
    } else {
      ElMessage.error(response.message || '确认失败')
    }
  } catch (error) {
    console.error('确认排班完成失败:', error)
    ElMessage.error('确认排班完成失败')
  }
}

const fetchSubstituteInfo = async (leaveRequestId) => {
  try {
    const response = await getLeaveSubstitutes(leaveRequestId)
    if (response.code === 200) {
      // 处理顶岗信息数据
      substituteInfoList.value = response.data.map(item => ({
        dutyDate: item.dutyDate,
        shiftName: getShiftNames([item.shiftConfigId]),
        originalEmployeeName: getEmployeeName(item.originalEmployeeId),
        substituteEmployeeName: getEmployeeName(item.substituteEmployeeId)
      }))
    }
  } catch (error) {
    console.error('获取顶岗信息失败:', error)
    substituteInfoList.value = []
  }
}

onMounted(async () => {
  await fetchEmployeeList()
  await fetchScheduleList()
  await fetchEnabledShifts()
  await fetchPendingApprovals()
})
</script>

<style scoped>
.leave-approval-container {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.schedule-select {
  width: 250px;
}

.content-card {
  margin-bottom: 10px;
}

.filter-container {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
