<template>
  <div class="leave-approval-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>请假审批</span>
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
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick" style="margin-bottom: 20px">
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
      </el-tabs>
      
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
            <el-button type="primary" @click="handleFilterSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <BaseTable
        v-loading="loading"
        :data="requestList"
        :columns="approvalColumns"
        :show-pagination="true"
        :pagination="pagination"
        :backend-pagination="true"
        :show-search="true"
        :search-placeholder="'请输入申请编号或申请人'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleTableSearch"
        @export="handleExport"
      >
        <template #employeeName="{ row }">
          {{ getEmployeeName(row.employeeId) }}
        </template>
        <template #leaveType="{ row }">
          <el-tag :type="getLeaveTypeColor(row.leaveType)">
            {{ getLeaveTypeName(row.leaveType) }}
          </el-tag>
        </template>
        <template #leaveTime="{ row }">
          {{ formatDate(row.startDate) }} {{ row.startTime || '' }} - {{ formatDate(row.endDate) }} {{ row.endTime || '' }}
        </template>
        <template #approvalStatus="{ row }">
          <el-tag :type="getApprovalStatusColor(row.approvalStatus)">
            {{ getApprovalStatusName(row.approvalStatus) }}
          </el-tag>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #operation="{ row }">
          <el-button
            v-if="row.approvalStatus === 'pending'"
            type="success"
            size="small"
            @click="openApproveDialog(row)"
          >
            审批
          </el-button>
          <el-button type="primary" size="small" @click="openViewDialog(row)">
            查看
          </el-button>
        </template>
      </BaseTable>
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
          <el-descriptions-item label="值班表">{{ getScheduleName(currentRequest.scheduleId) }}</el-descriptions-item>
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
        <el-form-item v-if="approveForm.approvalStatus === 'approved'" label="排班处理">
          <el-button type="primary" @click="checkSchedule">检查排班状态</el-button>
          <el-switch
            v-model="approveForm.excludeSameDayShifts"
            active-text="不允许当日已有班次的人员顶岗"
            inactive-text="允许当日已有班次的人员顶岗"
            style="margin-left: 20px"
          />
          <div v-if="scheduleChecked" style="margin-top: 10px">
            <el-tag :type="scheduleStatus.type">{{ scheduleStatus.text }}</el-tag>
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
                    <el-select 
                      v-model="scope.row.substituteEmployeeId" 
                      placeholder="选择顶岗人员" 
                      style="width: 100%"
                      @visible-change="(visible) => visible && loadSubstitutesForRow(scope.row)"
                    >
                      <el-option
                        v-for="employee in getAvailableSubstitutesForRow(scope.row)"
                        :key="employee.id"
                        :label="employee.employeeName"
                        :value="employee.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
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
import { useRouter, useRoute } from 'vue-router'
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
  getLeaveSubstitutes,
  autoSelectSubstitutes
} from '../../api/duty/leaveRequest'
import { getEmployeeList } from '../../api/employee'
import { getAllSchedules, getScheduleEmployees, getScheduleEmployeesWithDetails } from '../../api/duty/schedule'
import { getAssignmentsByScheduleId } from '../../api/duty/assignment'
import {
  generateAutoSchedule,
  generateAutoScheduleByWorkHours,
  getEmployeeMonthlyWorkHours
} from '../../api/duty/autoSchedule'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'
import BaseTable from '../../components/BaseTable.vue'
import { useSearchPagination } from '../../hooks/usePagination'

const userStore = useUserStore()
const route = useRoute()

const loading = ref(false)
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const viewDialogVisible = ref(false)
const approveFormRef = ref()
const requestList = ref([])
const employeeList = ref([]) // 保留以保持兼容性
const scheduleEmployeeList = ref([]) // 存储值班表的员工详细信息
const scheduleList = ref([])
const selectedScheduleId = ref('')
const currentRequest = ref({})
const currentApproverId = ref(userStore.employeeId || 1)
const activeTab = ref('pending')
const shiftConfigList = ref([])
const shiftApi = shiftConfigApi()
const substituteInfoList = ref([])

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
  availableSubstitutes: [],
  excludeSameDayShifts: true
})

const substituteData = ref([])
const availableSubstitutes = ref([])
const scheduleChecked = ref(false)

const approveRules = {
  approvalStatus: [
    { required: true, message: '请选择审批结果', trigger: 'blur' }
  ],
  rejectReason: [
    {
      validator: (rule, value, callback) => {
        if (approveForm.approvalStatus === 'rejected' && (!value || value.trim() === '')) {
          callback(new Error('请输入拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
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

// 表格列配置
const approvalColumns = [
  { prop: 'requestNo', label: '申请编号', width: '180' },
  { prop: 'employeeName', label: '申请人', width: '120', slotName: 'employeeName' },
  { prop: 'leaveType', label: '请假类型', width: '100', slotName: 'leaveType' },
  { label: '请假时间', width: '250', slotName: 'leaveTime' },
  { prop: 'totalHours', label: '请假时长(小时)', width: '130' },
  { prop: 'approvalStatus', label: '审批状态', width: '100', slotName: 'approvalStatus' },
  { prop: 'createTime', label: '申请时间', width: '180', slotName: 'createTime' },
  { label: '操作', width: '200', fixed: 'right', slotName: 'operation' }
]

// 导出处理
const handleExport = () => {
  // 导出逻辑
  const exportData = requestList.value
  const headers = ['申请编号', '申请人', '请假类型', '请假时间', '请假时长(小时)', '审批状态', '申请时间']
  const rows = exportData.map(item => [
    item.requestNo,
    getEmployeeName(item.employeeId),
    getLeaveTypeName(item.leaveType),
    `${formatDate(item.startDate)} ${item.startTime || ''} - ${formatDate(item.endDate)} ${item.endTime || ''}`,
    item.totalHours,
    getApprovalStatusName(item.approvalStatus),
    formatDateTime(item.createTime)
  ])
  
  // 这里可以使用xlsx库或其他方式导出，暂时使用简单的CSV导出
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `请假审批_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
  const employee = scheduleEmployeeList.value.find(e => e.id === employeeId) || employeeList.value.find(e => e.id === employeeId)
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

const getScheduleName = (scheduleId) => {
  if (!scheduleId) return '未知'
  
  const schedule = scheduleList.value.find(s => s.id === scheduleId)
  return schedule ? schedule.scheduleName : '未知'
}

const fetchEmployeeList = async () => {
  // 现在不再需要从所有员工中过滤，直接从值班表获取员工详细信息
  // 该方法保留以保持兼容性
  return
}

const fetchScheduleList = async (targetScheduleId = null) => {
  try {
    const data = await getAllSchedules()
    
    // 过滤出用户所在的值班表
    let userSchedules = []
    if (userStore.employeeId) {
      for (const schedule of data || []) {
        try {
          // 检查用户是否在值班表中
          const employees = await getScheduleEmployees(schedule.id)
          if (employees && employees.includes(userStore.employeeId)) {
            userSchedules.push(schedule)
          }
        } catch (error) {
          console.error('获取值班表员工失败:', error)
        }
      }
    }
    
    // 如果没有找到用户所在的值班表，或者用户未登录，使用所有值班表
    if (userSchedules.length === 0) {
      userSchedules = data || []
    }
    
    // 获取值班表列表（后端已经按id排序）
    scheduleList.value = userSchedules
    
    // 如果有目标 scheduleId，优先使用目标 scheduleId
    if (targetScheduleId) {
      const targetSchedule = scheduleList.value.find(s => s.id === targetScheduleId)
      if (targetSchedule) {
        selectedScheduleId.value = targetScheduleId
        // 获取目标值班表的员工详细信息
        try {
          const employeeDetails = await getScheduleEmployeesWithDetails(targetScheduleId)
          scheduleEmployeeList.value = (employeeDetails || []).map(emp => ({
            id: emp.id,
            employeeName: emp.employee_name,
            employeeCode: emp.employee_code,
            deptId: emp.dept_id,
            status: emp.status
          }))
        } catch (error) {
          console.error('获取值班表员工详情失败:', error)
        }
        return
      }
    }
    
    // 如果有值班表，默认选择第一个
    if (scheduleList.value.length > 0 && !selectedScheduleId.value) {
      selectedScheduleId.value = scheduleList.value[0].id
      // 获取默认值班表的员工详细信息
      try {
        const employeeDetails = await getScheduleEmployeesWithDetails(selectedScheduleId.value)
        scheduleEmployeeList.value = (employeeDetails || []).map(emp => ({
          id: emp.id,
          employeeName: emp.employee_name,
          employeeCode: emp.employee_code,
          deptId: emp.dept_id,
          status: emp.status
        }))
      } catch (error) {
        console.error('获取值班表员工详情失败:', error)
      }
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  }
}

const fetchEnabledShifts = async () => {
  try {
    const data = await shiftApi.getEnabledShifts()
    shiftConfigList.value = data || []
  } catch (error) {
    console.error('获取班次配置列表失败:', error)
  }
}

const fetchPendingApprovals = async (tabName = activeTab.value) => {
  // console.log('Fetching approvals, tabName:', tabName)
  loading.value = true
  try {
    let data
    const [startDate, endDate] = filterForm.dateRange || [null, null]
    
    if (tabName === 'pending') {
      // console.log('Fetching pending approvals')
      data = await getPendingApprovalsPage(
        userStore.employeeId || 1,
        currentPage.value,
        pageSize.value,
        selectedScheduleId.value || null,
        filterForm.leaveType,
        startDate,
        endDate,
        searchQuery.value
      )
    } else {
      // console.log('Fetching approved approvals')
      data = await getApprovedApprovalsPage(
        userStore.employeeId || 1,
        currentPage.value,
        pageSize.value,
        selectedScheduleId.value || null,
        filterForm.leaveType,
        filterForm.approvalStatus || null,
        startDate,
        endDate,
        searchQuery.value
      )
    }
    // console.log('Response received:', data)
    requestList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0
    // console.log('Request list updated:', requestList.value)
  } catch (error) {
    console.error('获取审批列表失败:', error)
    ElMessage.error('获取审批列表失败')
  } finally {
    loading.value = false
  }
}

const handleScheduleChange = () => {
  currentPage.value = 1 // 重置页码
  pagination.currentPage = 1
  fetchPendingApprovals()
}

const refreshList = () => {
  fetchPendingApprovals()
}

const handleTabClick = (tab) => {
  // console.log('Tab clicked, tab name:', tab.props.name)
  currentPage.value = 1 // 重置页码
  pagination.currentPage = 1
  fetchPendingApprovals(tab.props.name)
}

// 分页变更处理
const handleCurrentChange = (val) => {
  originalHandleCurrentChange(val)
  fetchPendingApprovals()
}

const handleSizeChange = (val) => {
  originalHandleSizeChange(val)
  fetchPendingApprovals()
}

// 表格搜索处理
const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  searchQuery.value = keyword
  currentPage.value = 1
  fetchPendingApprovals()
}

// 搜索处理
const handleFilterSearch = () => {
  // 重置页码
  currentPage.value = 1
  pagination.currentPage = 1
  // 重新获取数据，使用筛选条件
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
  // 重置检查状态和顶岗数据
  scheduleChecked.value = false
  substituteData.value = []
  scheduleStatus.type = 'info'
  scheduleStatus.text = '未检查'
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
    
    if (approveForm.approvalStatus === 'approved') {
      // 验证是否已经检查了排班状态
      if (!scheduleChecked.value) {
        ElMessage.warning('请先检查排班状态')
        approveLoading.value = false
        return
      }
      
      // 验证是否所有顶岗人员都已选择
      const hasEmptySubstitute = substituteData.value.some(item => !item.substituteEmployeeId)
      if (hasEmptySubstitute) {
        ElMessage.error('请为所有班次选择顶岗人员')
        approveLoading.value = false
        return
      }
    }
    
    await approveLeaveRequest(
      approveForm.requestId,
      approveForm.approverId,
      approveForm.approvalStatus,
      approveForm.approvalStatus === 'rejected' ? approveForm.rejectReason : approveForm.approvalOpinion,
      approveForm.approvalStatus === 'rejected' ? 'reject' : 'substitute', // 拒绝时使用reject
      approveForm.approvalStatus === 'rejected' ? [] : substituteData.value, // 拒绝时不传递顶岗数据
      approveForm.excludeSameDayShifts // 传递是否排除当日已有班次的人员
    )
    
    ElMessage.success('审批成功')
    approveDialogVisible.value = false
    fetchPendingApprovals()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
  } finally {
    approveLoading.value = false
  }
}

const handleApprovalStatusChange = (status) => {
  // 重置检查状态
  scheduleChecked.value = false
  substituteData.value = []
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
    
    // 处理选择"否"的情况：合并相同值班表相同日期相同班次的记录
    if (!approveForm.excludeSameDayShifts) {
      const mergedData = []
      const seen = new Set()
      
      data.forEach(item => {
        const key = `${item.date}_${item.shiftId}`
        if (!seen.has(key)) {
          seen.add(key)
          mergedData.push(item)
        }
      })
      
      substituteData.value = mergedData
    } else {
      substituteData.value = data
    }
  } catch (error) {
    console.error('生成顶岗排班表失败:', error)
    ElMessage.error('生成顶岗排班表失败')
  }
}

const fetchAvailableSubstitutes = async (startDate, endDate) => {
  try {
    // 调用后端API获取可用的顶岗人员
    const data = await getAvailableSubstitutes(
      currentRequest.value.scheduleId,
      startDate,
      endDate,
      currentRequest.value.employeeId
    )
    availableSubstitutes.value = data || []
  } catch (error) {
    console.error('获取可用顶岗人员失败:', error)
    ElMessage.error('获取可用顶岗人员失败')
  }
}

const selectAllSubstitutes = async () => {
  // 调用后端API进行一键选择顶岗人员
  try {
    const data = await autoSelectSubstitutes({
      requestId: currentRequest.value.id,
      substituteData: substituteData.value
    })
    
    // 更新前端顶岗数据
    substituteData.value = data || []
    
    // 重新加载每个班次的可用人员列表，确保下拉框能正确显示姓名
    for (const item of substituteData.value) {
      await loadSubstitutesForRow(item)
    }
    
    ElMessage.success('已为所有班次选择顶岗人员')
  } catch (error) {
    console.error('一键选择失败:', error)
    ElMessage.error('一键选择失败')
  }
}

const rowSubstitutes = ref({})

const loadSubstitutesForRow = async (row) => {
  try {
    const key = `${row.date}_${row.shiftId}`
    if (!rowSubstitutes.value[key] || approveForm.excludeSameDayShifts) {
      // 调用后端API获取该行的可用顶岗人员
      const data = await getAvailableSubstitutes(
        currentRequest.value.scheduleId,
        row.date,
        row.date,
        currentRequest.value.employeeId,
        approveForm.excludeSameDayShifts,
        row.shiftId
      )
      rowSubstitutes.value[key] = data || []
    }
  } catch (error) {
    console.error('加载顶岗人员失败:', error)
  }
}

const getAvailableSubstitutesForRow = (row) => {
  const key = `${row.date}_${row.shiftId}`
  return rowSubstitutes.value[key] || []
}

const checkSchedule = async () => {
  try {
    const scheduleId = currentRequest.value.scheduleId
    
    // 获取值班表的员工详细信息
    try {
      const employeeDetails = await getScheduleEmployeesWithDetails(scheduleId)
      scheduleEmployeeList.value = (employeeDetails || []).map(emp => ({
        id: emp.id,
        employeeName: emp.employee_name,
        employeeCode: emp.employee_code,
        deptId: emp.dept_id,
        status: emp.status
      }))
    } catch (error) {
      console.error('获取值班表员工详情失败:', error)
    }
    
    const data = await checkEmployeeSchedule(currentRequest.value.employeeId, currentRequest.value.startDate, currentRequest.value.endDate, scheduleId)
    
    if (data.hasSchedule) {
      scheduleStatus.type = 'warning'
      scheduleStatus.text = '已有排班，请选择顶岗人员'
      ElMessage.warning('申请人请假期间已有排班，请选择顶岗人员')
    } else {
      scheduleStatus.type = 'info'
      scheduleStatus.text = '无排班，需要选择顶岗人员'
      ElMessage.info('申请人请假期间无排班，需要选择顶岗人员')
    }
    // 设置检查状态为已检查
    scheduleChecked.value = true
    // 生成顶岗排班表
    await generateSubstituteSchedule()
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
    await confirmScheduleCompletion(approveForm.requestId, approveForm.approverId)
    
    ElMessage.success('排班完成确认成功')
    approveDialogVisible.value = false
    fetchPendingApprovals()
  } catch (error) {
    console.error('确认排班完成失败:', error)
    ElMessage.error('确认排班完成失败')
  }
}

const fetchSubstituteInfo = async (leaveRequestId) => {
  try {
    const data = await getLeaveSubstitutes(leaveRequestId)
    // 处理顶岗信息数据
    substituteInfoList.value = (data || []).map(item => ({
      dutyDate: item.dutyDate,
      shiftName: getShiftNames([item.shiftConfigId]),
      originalEmployeeName: getEmployeeName(item.originalEmployeeId),
      substituteEmployeeName: getEmployeeName(item.substituteEmployeeId)
    }))
  } catch (error) {
    console.error('获取顶岗信息失败:', error)
    substituteInfoList.value = []
  }
}

onMounted(async () => {
  // 从路由参数中读取 activeTab 和 scheduleId
  const queryActiveTab = route.query?.activeTab
  const queryScheduleId = route.query?.scheduleId ? Number(route.query.scheduleId) : null
  
  // 设置 activeTab
  if (queryActiveTab === 'pending' || queryActiveTab === 'approved') {
    activeTab.value = queryActiveTab
  }
  
  await fetchEmployeeList()
  await fetchScheduleList(queryScheduleId)
  await fetchEnabledShifts()
  await fetchPendingApprovals()
})
</script>

<style scoped>
.leave-approval-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.schedule-select {
  width: 250px;
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
