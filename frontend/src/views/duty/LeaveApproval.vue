<template>
  <div class="leave-approval-container">
    <div class="page-header">
      <h2>请假审批</h2>
      <el-button type="primary" @click="refreshList">
        <el-icon><Refresh /></el-icon>
        刷新列表
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
            {{ scope.row.startDate }} {{ scope.row.startTime }} - {{ scope.row.endDate }} {{ scope.row.endTime }}
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
        <el-table-column prop="createTime" label="申请时间" width="180" />
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
          <el-descriptions-item label="请假原因" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="审批结果" prop="approvalStatus">
          <el-radio-group v-model="approveForm.approvalStatus">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
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
        <el-descriptions-item label="开始时间">{{ currentRequest.startDate }} {{ currentRequest.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentRequest.endDate }} {{ currentRequest.endTime }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApprovalStatusColor(currentRequest.approvalStatus)">
            {{ getApprovalStatusName(currentRequest.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ currentRequest.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.substituteEmployeeId" label="替补人员">
          {{ getEmployeeName(currentRequest.substituteEmployeeId) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.rejectReason" label="拒绝原因" :span="2">
          {{ currentRequest.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPendingApprovals,
  approveLeaveRequest
} from '../../api/duty/leaveRequest'
import { getEmployeeList } from '../../api/employee'

const loading = ref(false)
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const viewDialogVisible = ref(false)
const approveFormRef = ref()
const requestList = ref([])
const employeeList = ref([])
const currentRequest = ref({})
const currentApproverId = ref(1)

const approveForm = reactive({
  requestId: null,
  approverId: 1,
  approvalStatus: 'approved',
  rejectReason: '',
  approvalOpinion: ''
})

const approveRules = {
  approvalStatus: [
    { required: true, message: '请选择审批结果', trigger: 'blur' }
  ],
  rejectReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

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

const fetchPendingApprovals = async () => {
  loading.value = true
  try {
    const response = await getPendingApprovals(currentApproverId.value)
    if (response.code === 200) {
      requestList.value = response.data
    }
  } catch (error) {
    console.error('获取待审批列表失败:', error)
    ElMessage.error('获取待审批列表失败')
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
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

const openViewDialog = (row) => {
  currentRequest.value = row
  viewDialogVisible.value = true
}

const handleApprove = async () => {
  try {
    await approveFormRef.value.validate()
    approveLoading.value = true
    
    const response = await approveLeaveRequest(
      approveForm.requestId,
      approveForm.approverId,
      approveForm.approvalStatus,
      approveForm.approvalStatus === 'rejected' ? approveForm.rejectReason : approveForm.approvalOpinion
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

onMounted(async () => {
  await fetchEmployeeList()
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

.content-card {
  margin-bottom: 10px;
}
</style>
