<template>
  <div class="leave-request-container">
    <div class="page-header">
      <h2>请假申请</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        申请请假
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openViewDialog(scope.row)">
              查看
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
        <el-form-item label="值班表" prop="scheduleId">
          <el-select v-model="form.scheduleId" placeholder="请选择值班表" style="width: 100%">
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="schedule.scheduleName"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
                <el-option label="事假" :value="1" />
                <el-option label="病假" :value="2" />
                <el-option label="年假" :value="3" />
                <el-option label="调休" :value="4" />
                <el-option label="其他" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请假时长(小时)" prop="totalHours">
              <el-input-number
                v-model="form.totalHours"
                :min="0.5"
                :max="168"
                :step="0.5"
                placeholder="请输入请假时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="HH:mm:ss"
                value-format="HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="form.endTime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="HH:mm:ss"
                value-format="HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="form.reason"
            placeholder="请输入请假原因"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            action="#"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
            :limit="3"
            :on-exceed="handleExceed"
            :file-list="fileList"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="handleSave">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="请假详情"
      width="900px"
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
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApprovalStatusColor(currentRequest.approvalStatus)">
            {{ getApprovalStatusName(currentRequest.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.currentApproverId" label="当前审批人">
          {{ getEmployeeName(currentRequest.currentApproverId) }}
        </el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ formatDateTime(currentRequest.createTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.substituteEmployeeId" label="替补人员">
          {{ getEmployeeName(currentRequest.substituteEmployeeId) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRequest.rejectReason" label="拒绝原因" :span="2">
          {{ currentRequest.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div v-if="currentRequest.approvalRecords && currentRequest.approvalRecords.length > 0" class="approval-records">
        <h3>审批记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in currentRequest.approvalRecords"
            :key="index"
            :timestamp="formatDateTime(record.createTime)"
            placement="top"
            :type="record.approvalStatus === 'approved' ? 'success' : record.approvalStatus === 'rejected' ? 'danger' : 'primary'"
          >
            <el-card>
              <h4>{{ getApproverName(record.approverId) }}</h4>
              <p><strong>审批结果：</strong>
                <el-tag :type="getApprovalStatusColor(record.approvalStatus)">
                  {{ getApprovalStatusName(record.approvalStatus) }}
                </el-tag>
              </p>
              <p v-if="record.approvalOpinion"><strong>审批意见：</strong>{{ record.approvalOpinion }}</p>
              <p v-if="record.rejectReason"><strong>拒绝原因：</strong>{{ record.rejectReason }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <div v-if="currentRequest.approvalStatus === 'rejected'" class="re-submit-section">
        <el-button type="primary" @click="handleReSubmit">
          重新提交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyLeaveRequests,
  submitLeaveRequest,
  deleteLeaveRequest,
  getApprovalRecords
} from '../../api/duty/leaveRequest'
import { getEmployeeList } from '../../api/employee'
import { getScheduleList } from '../../api/duty/schedule'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('申请请假')
const formRef = ref()
const requestList = ref([])
const employeeList = ref([])
const scheduleList = ref([])
const currentRequest = ref({})
const fileList = ref([])

const form = reactive({
  id: null,
  employeeId: null,
  scheduleId: null,
  leaveType: 1,
  startDate: null,
  endDate: null,
  startTime: null,
  endTime: null,
  totalHours: 8,
  reason: '',
  attachmentUrl: ''
})

const rules = {
  scheduleId: [
    { required: true, message: '请选择值班表', trigger: 'blur' }
  ],
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'blur' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'blur' }
  ],
  totalHours: [
    { required: true, message: '请输入请假时长', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' }
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

const getApproverName = (approverId) => {
  const employee = employeeList.value.find(e => e.id === approverId)
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

const fetchScheduleList = async () => {
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
  }
}

const fetchMyLeaveRequests = async () => {
  loading.value = true
  try {
    const response = await getMyLeaveRequests(userStore.employeeId)
    if (response.code === 200) {
      requestList.value = response.data
    }
  } catch (error) {
    console.error('获取请假申请列表失败:', error)
    ElMessage.error('获取请假申请列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '申请请假'
  dialogVisible.value = true
}

const openViewDialog = async (row) => {
  currentRequest.value = row
  
  if (row.approvalStatus === 'rejected' || row.approvalStatus === 'approved') {
    await fetchApprovalRecords(row.id)
  }
  
  viewDialogVisible.value = true
}

const fetchApprovalRecords = async (requestId) => {
  try {
    const response = await getApprovalRecords(requestId)
    if (response.code === 200) {
      currentRequest.value.approvalRecords = response.data
    }
  } catch (error) {
    console.error('获取审批记录失败:', error)
  }
}

const handleReSubmit = () => {
  resetForm()
  dialogTitle.value = '重新提交请假'
  dialogVisible.value = true
  viewDialogVisible.value = false
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    employeeId: userStore.employeeId,
    scheduleId: null,
    leaveType: 1,
    startDate: null,
    endDate: null,
    startTime: null,
    endTime: null,
    totalHours: 8,
    reason: '',
    attachmentUrl: ''
  })
  fileList.value = []
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    dialogLoading.value = true
    
    const response = await submitLeaveRequest(form)
    
    if (response.code === 200) {
      ElMessage.success('请假申请提交成功')
      dialogVisible.value = false
      fetchMyLeaveRequests()
    } else {
      ElMessage.error(response.message || '请假申请提交失败')
    }
  } catch (error) {
    console.error('提交请假申请失败:', error)
    ElMessage.error('提交请假申请失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤销该请假申请吗？', '撤销确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteLeaveRequest(id)
    if (response.code === 200) {
      ElMessage.success('请假申请撤销成功')
      fetchMyLeaveRequests()
    } else {
      ElMessage.error(response.message || '请假申请撤销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销请假申请失败:', error)
      ElMessage.error('撤销请假申请失败')
    }
  }
}

const handlePreview = (file) => {
  console.log(file)
}

const handleRemove = (file, fileList) => {
  console.log(file, fileList)
}

const beforeRemove = (file) => {
  return ElMessageBox.confirm(`确定移除 ${file.name}？`)
}

const handleExceed = (files) => {
  ElMessage.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件`)
}

onMounted(async () => {
  await fetchEmployeeList()
  await fetchScheduleList()
  await fetchMyLeaveRequests()
})
</script>

<style scoped>
.leave-request-container {
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

.approval-records {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.approval-records h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.approval-records .el-card {
  margin-bottom: 10px;
}

.approval-records .el-card h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.approval-records .el-card p {
  margin: 5px 0;
  color: #606266;
}

.re-submit-section {
  margin-top: 20px;
  text-align: center;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 4px;
}
</style>
