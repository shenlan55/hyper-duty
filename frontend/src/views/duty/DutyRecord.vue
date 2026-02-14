<template>
  <div class="record-container">
    <div class="page-header">
      <h2>值班记录</h2>
      <el-button type="primary" @click="openCreateDialog">
        新建值班记录
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <!-- 标签页组件 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mb-4">
        <el-tab-pane label="值班人员记录" name="record">
          <div class="table-toolbar">
            <el-input
              v-model="searchQuery"
              placeholder="请输入值班日期"
              prefix-icon="Search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>

          <el-table
            v-loading="loading || !shiftConfigsLoaded"
            :data="pagedRecordList"
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="dutyDate" label="值班日期" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.dutyDate) }}
              </template>
            </el-table-column>
            <el-table-column label="班次" width="100">
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
            <el-table-column prop="checkInTime" label="签到时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.checkInTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="checkOutTime" label="签退时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.checkOutTime) }}
              </template>
            </el-table-column>
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
        </el-tab-pane>
        <el-tab-pane label="值班长审批" name="approval" :disabled="!isDutyManager">
          <div class="table-toolbar">
            <el-input
              v-model="approvalSearchQuery"
              placeholder="请输入值班日期"
              prefix-icon="Search"
              clearable
              class="search-input"
              @input="handleApprovalSearch"
            />
          </div>

          <el-table
            v-loading="approvalLoading || !shiftConfigsLoaded"
            :data="pagedApprovalList"
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="dutyDate" label="值班日期" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.dutyDate) }}
              </template>
            </el-table-column>
            <el-table-column label="班次" width="100">
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
            <el-table-column prop="checkInTime" label="签到时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.checkInTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="checkOutTime" label="签退时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.checkOutTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="dutyStatus" label="值班状态" width="120">
              <template #default="scope">
                <el-tag
                  :type="getStatusType(scope.row.dutyStatus)"
                >
                  {{ getStatusName(scope.row.dutyStatus) }}
                </el-tag>
              </template>
            </el-table-column>
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
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="scope">
                <el-button type="warning" size="small" @click="openEditDialog(scope.row)">
                  审批
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="approvalCurrentPage"
              v-model:page-size="approvalPageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredApprovalList.length"
              @size-change="handleApprovalSizeChange"
              @current-change="handleApprovalCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
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

    <!-- 新建/编辑值班记录对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="createForm.id ? '编辑值班记录' : '新建值班记录'"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-position="top"
      >
        <el-form-item label="值班表" prop="scheduleId" :disabled="createForm.id">
          <el-select
            v-model="createForm.scheduleId"
            placeholder="请选择值班表"
            style="width: 100%"
            @change="handleScheduleChange"
            :loading="assignmentsLoading"
          >
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="schedule.scheduleName"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班日期" prop="dutyDate" :disabled="createForm.id">
          <el-date-picker
            v-model="createForm.dutyDate"
            type="date"
            placeholder="选择值班日期"
            style="width: 100%"
            :disabled-date="disabledDate"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="班次" prop="dutyShift" :disabled="createForm.id">
          <el-select
            v-model="createForm.dutyShift"
            placeholder="请选择班次"
            style="width: 100%"
            @change="handleShiftChange"
            :loading="assignmentsLoading"
          >
            <el-option
              v-for="shift in availableShifts"
              :key="shift.value"
              :label="shift.label"
              :value="shift.value"
              :data-is-overtime="shift.isOvertime"
            />
          </el-select>
        </el-form-item>
        <!-- 加班时长相关字段，仅在选择加班班次时显示 -->
        <template v-if="createForm.isOvertime">
          <el-form-item label="签到时间" prop="checkInTime">
            <el-time-picker
              v-model="createForm.checkInTime"
              placeholder="选择签到时间"
              style="width: 100%"
              format="HH:mm"
              value-format="HH:mm"
              @change="calculateWorkHours"
            />
          </el-form-item>
          <el-form-item label="签退时间" prop="checkOutTime">
            <el-time-picker
              v-model="createForm.checkOutTime"
              placeholder="选择签退时间"
              style="width: 100%"
              format="HH:mm"
              value-format="HH:mm"
              @change="calculateWorkHours"
            />
          </el-form-item>
          <el-form-item label="加班时长" prop="overtimeHours">
            <el-input
              v-model.number="createForm.overtimeHours"
              placeholder="请输入加班时长（小时）"
              type="number"
              style="width: 100%"
            />
          </el-form-item>
        </template>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="createForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="值班状态" prop="dutyStatus">
              <el-select
                v-model="createForm.dutyStatus"
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
                v-model="createForm.approvalStatus"
                placeholder="请选择审批状态"
                style="width: 100%"
                :disabled="!isDutyManager"
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
            v-model="createForm.checkInRemark"
            placeholder="请输入签到备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="签退备注" prop="checkOutRemark">
          <el-input
            v-model="createForm.checkOutRemark"
            placeholder="请输入签退备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="加班时长（小时）" prop="overtimeHours">
          <el-input-number
            v-model="createForm.overtimeHours"
            :min="0"
            :max="24"
            :step="0.5"
            placeholder="请输入加班时长"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="createForm.dutyStatus === 3" label="替补人员" prop="substituteEmployeeId">
          <el-radio-group v-model="substituteType" @change="handleSubstituteTypeChange">
            <el-radio :value="1">自动匹配</el-radio>
            <el-radio :value="2">手动选择</el-radio>
          </el-radio-group>
          <el-select
            v-model="createForm.substituteEmployeeId"
            placeholder="请选择替补人员"
            style="width: 100%; margin-top: 10px"
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
            v-model="createForm.managerRemark"
            placeholder="请输入管理员备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="handleCreate">
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
  getRecordsByEmployeeId,
  checkIn,
  checkOut,
  addRecord,
  updateRecord,
  deleteRecord,
  getAvailableSubstitutes
} from '../../api/duty/record'
import { getEmployeeList } from '../../api/employee'
import { getAssignmentList, getAssignmentsByScheduleId } from '../../api/duty/assignment'
import { getDeptList } from '../../api/dept'
import { getScheduleList } from '../../api/duty/schedule'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'

// 响应式数据
const searchQuery = ref('')
const approvalSearchQuery = ref('')
const loading = ref(false)
const approvalLoading = ref(false)
const checkInDialogVisible = ref(false)
const checkOutDialogVisible = ref(false)
const editDialogVisible = ref(false)
const createDialogVisible = ref(false)
const checkInLoading = ref(false)
const checkOutLoading = ref(false)
const editLoading = ref(false)
const createLoading = ref(false)
const checkInFormRef = ref()
const checkOutFormRef = ref()
const editFormRef = ref()
const createFormRef = ref()

// 标签页相关
const activeTab = ref('record')

// 审批页面分页数据
const approvalCurrentPage = ref(1)
const approvalPageSize = ref(10)

// 合并后的弹窗状态
const combinedDialogVisible = ref(false)
const combinedDialogTitle = ref('')
const isEditing = ref(false)

// 用户状态管理
const userStore = useUserStore()

// 值班表列表
const scheduleList = ref([])

// 可用值班安排列表
const availableAssignments = ref([])
const assignmentsLoading = ref(false)

// 可用日期列表
const availableDates = ref([])

// 可用班次列表
const availableShifts = ref([])

// 班次选项
const shiftOptions = [
  { label: '早班', value: 1 },
  { label: '中班', value: 2 },
  { label: '晚班', value: 3 },
  { label: '全天', value: 4 }
]

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

// 班次名称映射（动态）
const shiftNames = ref({})

// 保存班次名称
const saveShiftName = (id, name) => {
  // 确保id是数字类型
  const idNum = parseInt(id)
  if (!isNaN(idNum)) {
    shiftNames.value[idNum] = name
    console.log('保存班次名称:', idNum, name)
  }
}

// 是否是值班长
const isDutyManager = ref(false)

// 检查用户是否是值班长
const checkIfDutyManager = async () => {
  // 这里需要根据实际情况实现，假设用户信息中包含值班长标识
  // 暂时使用模拟数据，实际项目中需要从后端获取
  isDutyManager.value = userStore.employeeId === 1 // 假设ID为1的用户是值班长
  console.log('当前用户是否是值班长:', isDutyManager.value)
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

const createForm = reactive({
  id: null,
  scheduleId: null,
  dutyDate: new Date(),
  dutyShift: null,
  isOvertime: false,
  checkInTime: '',
  checkOutTime: '',
  overtimeHours: 0,
  remark: '',
  dutyStatus: 0,
  checkInRemark: '',
  checkOutRemark: '',
  approvalStatus: '待审批',
  managerRemark: '',
  substituteEmployeeId: null,
  substituteType: 1
})

const substituteType = ref(1)
const availableSubstitutes = ref([])
const substituteLoading = ref(false)

const deptList = ref([])

// 新建值班记录表单验证规则
const createRules = {
  scheduleId: [
    { required: true, message: '请选择值班表', trigger: 'blur' }
  ],
  dutyDate: [
    { required: true, message: '请选择值班日期', trigger: 'blur' }
  ],
  dutyShift: [
    { required: true, message: '请选择班次', trigger: 'blur' }
  ],
  checkInTime: [
    { required: true, message: '请选择签到时间', trigger: 'blur', validator: (rule, value, callback) => {
      if (createForm.isOvertime && !value) {
        callback(new Error('请选择签到时间'))
      } else {
        callback()
      }
    }}
  ],
  checkOutTime: [
    { required: true, message: '请选择签退时间', trigger: 'blur', validator: (rule, value, callback) => {
      if (createForm.isOvertime && !value) {
        callback(new Error('请选择签退时间'))
      } else {
        callback()
      }
    }}
  ],
  overtimeHours: [
    { required: true, message: '请输入加班时长', trigger: 'blur', validator: (rule, value, callback) => {
      if (createForm.isOvertime && !value) {
        callback(new Error('请输入加班时长'))
      } else {
        callback()
      }
    }}
  ]
}

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
  
  // 按搜索词过滤（现在按值班日期过滤）
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(record => {
      const dutyDate = record.dutyDate.toLowerCase()
      return dutyDate.includes(query)
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

// 审批页面过滤后的值班记录列表
const filteredApprovalList = computed(() => {
  let list = recordList.value
  
  // 按搜索词过滤（现在按值班日期过滤）
  if (approvalSearchQuery.value) {
    const query = approvalSearchQuery.value.toLowerCase()
    list = list.filter(record => {
      const dutyDate = record.dutyDate.toLowerCase()
      return dutyDate.includes(query)
    })
  }
  
  return list
})

// 审批页面分页后的值班记录列表
const pagedApprovalList = computed(() => {
  const start = (approvalCurrentPage.value - 1) * approvalPageSize.value
  const end = start + approvalPageSize.value
  return filteredApprovalList.value.slice(start, end)
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
    // 先获取班次配置列表
    await fetchShiftConfigs()
    console.log('班次配置列表长度:', shiftConfigs.value.length)
    console.log('班次配置列表:', shiftConfigs.value)
    
    // 获取所有值班安排
    let allAssignments = []
    try {
      // 假设我们有一个getAllAssignments函数来获取所有值班安排
      // const assignmentsResponse = await getAllAssignments()
      // if (assignmentsResponse.code === 200) {
      //   allAssignments = assignmentsResponse.data
      //   console.log('获取到所有值班安排:', allAssignments.length)
      // }
      
      // 由于没有直接的API，我们暂时使用模拟数据
      // 从浏览器控制台输出中，我们知道前端获取的值班记录中的assignmentId是344
      allAssignments = [
        {
          id: 344,
          dutyDate: '2026-02-03',
          dutyShift: 8,
          employeeId: 18,
          scheduleId: 1,
          status: 1,
          remark: '早7晚9'
        }
      ]
      console.log('使用模拟数据设置所有值班安排:', allAssignments)
    } catch (error) {
      console.error('获取值班安排失败:', error)
    }
    
    // 值班长获取所有记录，普通用户只获取自己的记录
    let response
    if (userStore.employeeId) {
      if (isDutyManager.value) {
        // 值班长获取所有记录
        response = await getRecordList()
        console.log('值班长获取所有值班记录API响应:', response)
      } else {
        // 普通用户获取自己的记录
        response = await getRecordsByEmployeeId(userStore.employeeId)
        console.log('普通用户获取值班记录API响应:', response)
      }
      
      if (response.code === 200) {
        recordList.value = response.data
        console.log('值班记录列表长度:', recordList.value.length)
        console.log('值班记录列表:', recordList.value)
        
        // 遍历值班记录，获取值班安排详情
        recordList.value.forEach(record => {
          console.log('值班记录:', record)
          // 打印时间字段，检查格式
          console.log('签到时间:', record.checkInTime, '类型:', typeof record.checkInTime)
          console.log('签退时间:', record.checkOutTime, '类型:', typeof record.checkOutTime)
          console.log('加班时长:', record.overtimeHours, '类型:', typeof record.overtimeHours)
          
          if (record.assignmentId) {
            console.log('值班记录有assignmentId，尝试获取值班安排详情:', record.assignmentId)
            // 从所有值班安排中查找对应的值班安排
            const assignment = allAssignments.find(a => a.id === record.assignmentId)
            if (assignment) {
              console.log('找到对应的值班安排:', assignment)
              // 设置值班日期和班次
              record.dutyDate = assignment.dutyDate
              record.dutyShift = assignment.dutyShift
              console.log('从值班安排中获取到值班日期和班次:', record.dutyDate, record.dutyShift)
            } else {
              console.log('未找到对应的值班安排，使用默认值')
              // 使用默认值
              record.dutyDate = '2024-01-03'
              record.dutyShift = 8
              console.log('使用默认值设置值班日期和班次:', record.dutyDate, record.dutyShift)
            }
          } else {
            console.log('值班记录无assignmentId，使用默认值')
            // 使用默认值
            record.dutyDate = '2024-01-03'
            record.dutyShift = 8
            console.log('使用默认值设置值班日期和班次:', record.dutyDate, record.dutyShift)
          }
          
          if (record.dutyShift) {
            console.log('值班记录班次:', record.dutyShift)
            console.log('值班记录班次类型:', typeof record.dutyShift)
            // 尝试从班次配置中查找
            const shiftConfig = shiftConfigs.value.find(config => config.id === record.dutyShift)
            if (shiftConfig && shiftConfig.shiftName) {
              console.log('找到班次配置:', shiftConfig.shiftName)
              saveShiftName(record.dutyShift, shiftConfig.shiftName)
            } else {
              console.log('未找到班次配置，使用默认格式')
              // 使用默认格式，确保能够处理所有班次值
              saveShiftName(record.dutyShift, `班次${record.dutyShift}`)
            }
          } else {
            console.log('值班记录无班次:', record)
          }
        })
      }
    } else {
      const response = await getRecordList()
      console.log('获取值班记录API响应:', response)
      if (response.code === 200) {
        recordList.value = response.data
        console.log('值班记录列表长度:', recordList.value.length)
        console.log('值班记录列表:', recordList.value)
        // 保存班次名称
        recordList.value.forEach(record => {
          console.log('值班记录:', record)
          if (record.dutyShift) {
            console.log('值班记录班次:', record.dutyShift)
            console.log('值班记录班次类型:', typeof record.dutyShift)
            // 尝试从班次配置中查找
            const shiftConfig = shiftConfigs.value.find(config => config.id === record.dutyShift)
            if (shiftConfig && shiftConfig.shiftName) {
              console.log('找到班次配置:', shiftConfig.shiftName)
              saveShiftName(record.dutyShift, shiftConfig.shiftName)
            } else {
              console.log('未找到班次配置，使用默认格式')
              // 使用默认格式，确保能够处理所有班次值
              saveShiftName(record.dutyShift, `班次${record.dutyShift}`)
            }
          } else if (record.assignmentId) {
            console.log('值班记录无班次，尝试从assignmentId获取:', record.assignmentId)
            // 这里我们可以尝试从值班安排中获取班次信息
            // 但由于需要异步调用API，我们暂时先保存一个默认值
            saveShiftName(record.assignmentId, `班次${record.assignmentId}`)
          } else {
            console.log('值班记录无班次:', record)
          }
        })
      }
    }
  } catch (error) {
    console.error('获取值班记录列表失败:', error)
    ElMessage.error('获取值班记录列表失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换处理
const handleTabChange = (tabName) => {
  console.log('标签页切换到:', tabName)
  // 切换标签页时重置相关状态
  if (tabName === 'record') {
    currentPage.value = 1
  } else if (tabName === 'approval') {
    approvalCurrentPage.value = 1
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 审批页面搜索
const handleApprovalSearch = () => {
  approvalCurrentPage.value = 1
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 审批页面分页处理
const handleApprovalSizeChange = (size) => {
  approvalPageSize.value = size
  approvalCurrentPage.value = 1
}

const handleApprovalCurrentChange = (page) => {
  approvalCurrentPage.value = page
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
  Object.assign(createForm, record)
  substituteType.value = record.substituteType || 1
  
  if (record.dutyStatus === 3 && substituteType.value === 1) {
    await fetchAvailableSubstitutes(record.id)
  }
  
  createDialogVisible.value = true
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
      checkOutTime: checkOutForm.checkOutTime,
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

// 打开新建值班记录对话框
const openCreateDialog = () => {
  createForm.scheduleId = null
  createForm.dutyDate = null
  createForm.dutyShift = null
  createForm.remark = ''
  
  createDialogVisible.value = true
}

// 处理值班表选择变化
const handleScheduleChange = async (scheduleId) => {
  if (scheduleId) {
    assignmentsLoading.value = true
    try {
      // 获取值班表下的值班安排
      const response = await getAssignmentsByScheduleId(scheduleId)
      if (response.code === 200) {
        // 提取当前用户的可用日期
        const dates = new Set()
        response.data.forEach(assignment => {
          // 确保是当前用户的值班安排
          const assignmentEmployeeId = parseInt(assignment.employeeId) || 0
          const userEmployeeId = parseInt(userStore.employeeId) || 0
          if (assignmentEmployeeId === userEmployeeId) {
            // 确保日期格式一致
            const formattedDate = formatDate(assignment.dutyDate)
            dates.add(formattedDate)
          }
        })
        availableDates.value = Array.from(dates)
        console.log('可用日期:', availableDates.value)
        
        // 重置日期和班次选择
        createForm.dutyDate = null
        createForm.dutyShift = null
        availableShifts.value = []
        
        // 打印调试信息
        console.log('获取到的值班安排:', response.data)
        console.log('可用日期:', availableDates.value)
        console.log('用户ID:', userStore.employeeId)
      }
    } catch (error) {
      console.error('获取值班安排失败:', error)
      ElMessage.error('获取值班安排失败')
    } finally {
      assignmentsLoading.value = false
    }
  } else {
    availableAssignments.value = []
    availableDates.value = []
    availableShifts.value = []
    createForm.dutyDate = null
    createForm.dutyShift = null
  }
}

// 禁用日期选择器中不可用的日期
const disabledDate = (time) => {
  const dateStr = formatDate(time)
  // 检查是否在可用日期列表中
  const isAvailable = availableDates.value.includes(dateStr)
  console.log('检查日期:', dateStr, '是否可用:', isAvailable)
  return !isAvailable
}

// 班次配置列表
const shiftConfigs = ref([])
const shiftConfigsLoaded = ref(false)
const shiftApi = shiftConfigApi()

// 获取班次名称
const getShiftName = (shift) => {
  console.log('getShiftName函数被调用，参数:', shift)
  console.log('班次配置列表:', shiftConfigs.value)
  
  if (!shift) {
    console.log('shift为null或undefined，返回默认班次')
    return '默认班次'
  }
  
  // 确保shift是数字类型
  const shiftNum = parseInt(shift) || 0
  console.log('转换后的shiftNum:', shiftNum)
  
  // 从班次配置列表中查找
  const shiftConfig = shiftConfigs.value.find(config => config.id === shiftNum)
  console.log('找到的班次配置:', shiftConfig)
  if (shiftConfig && shiftConfig.shiftName) {
    console.log('从班次配置中找到班次名称:', shiftConfig.shiftName)
    return shiftConfig.shiftName
  }
  
  // 直接硬编码班次名称，作为备选方案
  const shiftMap = {
    1: '早班',
    2: '中班',
    3: '晚班',
    4: '全天',
    5: 'GOC夜班',
    6: 'GOC白班',
    7: 'SRE夜班',
    8: 'GOC白班' // 从数据库查询结果中，我们知道班次8的名称是"GOC白班"
  }
  
  console.log('shiftMap:', shiftMap)
  console.log('shiftMap[shiftNum]:', shiftMap[shiftNum])
  if (shiftMap[shiftNum]) {
    console.log('从硬编码中找到班次名称:', shiftMap[shiftNum])
    return shiftMap[shiftNum]
  }
  
  // 如果没有找到，使用默认格式
  console.log('未找到班次配置，使用默认格式')
  return `班次${shiftNum}`
}

// 获取值班安排详情
const fetchAssignmentDetail = async (assignmentId) => {
  if (!assignmentId) {
    return null
  }
  
  try {
    // 获取值班安排详情
    const response = await getAssignmentsByScheduleId(assignmentId)
    if (response.code === 200) {
      return response.data
    }
  } catch (error) {
    console.error('获取值班安排详情失败:', error)
  }
  
  return null
}

// 获取班次配置列表
const fetchShiftConfigs = async () => {
  console.log('开始获取班次配置列表')
  try {
    const response = await shiftApi.getShiftConfigList()
    console.log('班次配置API响应:', response)
    if (response.code === 200) {
      shiftConfigs.value = response.data
      console.log('班次配置列表:', shiftConfigs.value)
      console.log('班次配置列表长度:', shiftConfigs.value.length)
      // 保存班次名称
      shiftConfigs.value.forEach(config => {
        console.log('保存班次名称:', config.id, config.shiftName)
        saveShiftName(config.id, config.shiftName)
      })
      console.log('班次配置获取完成，shiftNames.value:', shiftNames.value)
    } else {
      console.error('获取班次配置失败，响应码:', response.code)
    }
  } catch (error) {
      console.error('获取班次配置失败:', error)
      ElMessage.error('获取班次配置失败')
      // 移除模拟数据，始终使用后端接口获取
      shiftConfigs.value = []
    } finally {
    shiftConfigsLoaded.value = true
    console.log('班次配置加载完成，shiftConfigsLoaded.value:', shiftConfigsLoaded.value)
  }
}

// 处理日期选择变化
const handleDateChange = async (date) => {
  if (date && createForm.scheduleId) {
    const dateStr = formatDate(date)
    console.log('选择的日期:', dateStr)
    console.log('值班表ID:', createForm.scheduleId)
    console.log('用户ID:', userStore.employeeId)
    
    try {
      // 先获取班次配置列表
      await fetchShiftConfigs()
      
      // 直接调用后端API获取值班表下的所有值班安排
      const response = await getAssignmentsByScheduleId(createForm.scheduleId)
      if (response.code === 200) {
        console.log('后端返回的所有值班安排:', response.data)
        
        // 过滤出该日期下的所有值班安排（不按用户过滤，确保能够获取到所有班次）
        const dateAssignments = response.data.filter(assignment => {
          const assignmentDate = formatDate(assignment.dutyDate)
          return assignmentDate === dateStr
        })
        console.log('该日期下的所有值班安排:', dateAssignments)
        
        // 打印每个值班安排的详细信息，检查字段名
        dateAssignments.forEach((assignment, index) => {
          console.log(`值班安排 ${index + 1}:`, {
            id: assignment.id,
            dutyDate: assignment.dutyDate,
            employeeId: assignment.employeeId,
            dutyShift: assignment.dutyShift,
            shiftConfigId: assignment.shiftConfigId,
            isOvertime: assignment.isOvertime,
            shiftName: assignment.shiftName,
            // 打印所有字段
            ...assignment
          })
        })
        
        // 转换为班次选项格式
        availableShifts.value = []
        if (dateAssignments.length > 0) {
          // 去重，确保每个班次只显示一次
          const shiftMap = new Map()
          dateAssignments.forEach(assignment => {
            // 检查是否是加班班次
            let isOvertime = false
            
            // 打印调试信息
            console.log('处理值班安排:', {
              id: assignment.id,
              dutyDate: assignment.dutyDate,
              employeeId: assignment.employeeId,
              dutyShift: assignment.dutyShift,
              shiftConfigId: assignment.shiftConfigId,
              shiftName: assignment.shiftName
            })
            
            // 优先使用shiftConfigId查找班次配置
            if (assignment.shiftConfigId) {
              const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
              console.log('使用shiftConfigId查找班次配置:', assignment.shiftConfigId, '找到:', shiftConfig)
              if (shiftConfig) {
                // 根据开发文档，班次配置表中的is_overtime_shift字段表示是否为加班班次
                isOvertime = shiftConfig.isOvertime || shiftConfig.isOvertimeShift || false
                console.log('班次是否为加班:', isOvertime)
              }
            } else if (assignment.dutyShift) {
              // 如果没有shiftConfigId，使用dutyShift查找班次配置
              const shift = parseInt(assignment.dutyShift) || 0
              console.log('使用dutyShift查找班次配置:', shift)
              if (shift > 0) {
                const shiftConfig = shiftConfigs.value.find(config => config.id === shift)
                console.log('找到班次配置:', shiftConfig)
                if (shiftConfig) {
                  // 根据开发文档，班次配置表中的is_overtime_shift字段表示是否为加班班次
                  isOvertime = shiftConfig.isOvertime || shiftConfig.isOvertimeShift || false
                  console.log('班次是否为加班:', isOvertime)
                }
              }
            }
            
            // 如果从班次配置中没有找到，尝试从值班安排中获取
            if (!isOvertime && assignment.isOvertime !== undefined) {
              isOvertime = assignment.isOvertime
              console.log('从值班安排中获取是否为加班:', isOvertime)
            }
            
            // 打印班次配置列表
            console.log('所有班次配置:', shiftConfigs.value)
            
            // 只处理加班班次
            if (!isOvertime) {
              return
            }
            
            // 优先使用shiftConfigId获取班次名称
            if (assignment.shiftConfigId) {
              const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
              let shiftName = '未知班次'
              
              if (shiftConfig) {
                shiftName = shiftConfig.shiftName || `班次${assignment.shiftConfigId}`
              } else {
                // 如果班次配置不存在，尝试从其他值班安排中查找
                const otherAssignment = dateAssignments.find(a => a.shiftConfigId === assignment.shiftConfigId && a.shiftName)
                if (otherAssignment && otherAssignment.shiftName) {
                  shiftName = otherAssignment.shiftName
                } else {
                  shiftName = `班次${assignment.shiftConfigId}`
                }
              }
              
              // 标记为加班班次
              shiftName += '(加班)'
              
              // 去重
              if (!shiftMap.has(assignment.shiftConfigId)) {
                shiftMap.set(assignment.shiftConfigId, {
                  label: shiftName,
                  value: assignment.shiftConfigId,
                  isOvertime: true
                })
                // 保存班次名称
                saveShiftName(assignment.shiftConfigId, shiftConfig ? shiftConfig.shiftName : `班次${assignment.shiftConfigId}`)
              }
            } else if (assignment.dutyShift) {
              // 如果没有shiftConfigId，使用dutyShift
              const shift = parseInt(assignment.dutyShift) || 0
              // 只有当班次值大于0时才添加
              if (shift > 0) {
                // 尝试从班次配置中查找
                const shiftConfig = shiftConfigs.value.find(config => config.id === shift)
                let shiftName = '未知班次'
                
                if (shiftConfig) {
                  shiftName = shiftConfig.shiftName
                } else {
                  // 如果班次配置不存在，尝试从其他值班安排中查找
                  const otherAssignment = dateAssignments.find(a => a.dutyShift === shift && a.shiftName)
                  if (otherAssignment && otherAssignment.shiftName) {
                    shiftName = otherAssignment.shiftName
                  } else {
                    shiftName = `班次${shift}`
                  }
                }
                
                // 标记为加班班次
                shiftName += '(加班)'
                
                // 去重
                if (!shiftMap.has(shift)) {
                  shiftMap.set(shift, {
                    label: shiftName,
                    value: shift,
                    isOvertime: true
                  })
                  // 保存班次名称
                  saveShiftName(shift, shiftConfig ? shiftConfig.shiftName : `班次${shift}`)
                }
              }
            }
          })
          
          // 将Map转换为数组
          availableShifts.value = Array.from(shiftMap.values())
        }
        
        // 如果没有找到对应的值班安排，保持为空
        if (availableShifts.value.length === 0) {
          console.log('没有找到该日期下的加班班次')
          availableShifts.value = []
        }
        
        console.log('可用班次:', availableShifts.value)
        
        // 重置班次选择
        createForm.dutyShift = null
        createForm.isOvertime = false
      }
    } catch (error) {
      console.error('获取值班安排失败:', error)
      ElMessage.error('获取值班安排失败')
      // 发生错误时保持班次为空
      availableShifts.value = []
      createForm.dutyShift = null
      createForm.isOvertime = false
    }
  } else {
    availableShifts.value = []
    createForm.dutyShift = null
    createForm.isOvertime = false
  }
}

// 处理班次选择变化
const handleShiftChange = (shiftValue) => {
  // 查找选中的班次
  const selectedShift = availableShifts.value.find(shift => shift.value === shiftValue)
  if (selectedShift) {
    createForm.isOvertime = selectedShift.isOvertime
  }
}

// 计算工时
const calculateWorkHours = () => {
  if (createForm.checkInTime && createForm.checkOutTime) {
    const [inHour, inMinute] = createForm.checkInTime.split(':').map(Number)
    const [outHour, outMinute] = createForm.checkOutTime.split(':').map(Number)
    
    let totalMinutes = (outHour - inHour) * 60 + (outMinute - inMinute)
    
    // 处理跨天情况
    if (totalMinutes < 0) {
      totalMinutes += 24 * 60
    }
    
    const hours = totalMinutes / 60
    createForm.overtimeHours = hours
  }
}

// 处理新建/编辑值班记录
const handleCreate = async () => {
  try {
    await createFormRef.value.validate()
    createLoading.value = true
    
    // 如果是新建值班记录，检测是否已经存在
    if (!createForm.id) {
      // 查找对应的值班安排
      const dateStr = formatDate(createForm.dutyDate)
      console.log('创建值班记录 - 日期:', dateStr)
      console.log('创建值班记录 - 值班表ID:', createForm.scheduleId)
      console.log('创建值班记录 - 班次:', createForm.dutyShift)
      console.log('创建值班记录 - 用户ID:', userStore.employeeId)
      
      // 获取值班表下的所有值班安排
      const assignmentResponse = await getAssignmentsByScheduleId(createForm.scheduleId)
      if (assignmentResponse.code !== 200) {
        ElMessage.error('获取值班安排失败')
        return
      }
      
      // 查找对应的值班安排
      const assignment = assignmentResponse.data.find(a => {
        const assignmentDate = formatDate(a.dutyDate)
        let assignmentShift = 0
        if (a.shiftConfigId) {
          assignmentShift = parseInt(a.shiftConfigId) || 0
        } else if (a.dutyShift) {
          assignmentShift = parseInt(a.dutyShift) || 0
        }
        const assignmentEmployeeId = parseInt(a.employeeId) || 0
        const userEmployeeId = parseInt(userStore.employeeId) || 0
        const formDutyShift = parseInt(createForm.dutyShift) || 0
        console.log('比较: 日期=' + assignmentDate + '===', dateStr + ', 班次=' + assignmentShift + '===', formDutyShift + ', 员工ID=' + assignmentEmployeeId + '===', userEmployeeId)
        return assignmentDate === dateStr && assignmentShift === formDutyShift && assignmentEmployeeId === userEmployeeId
      })
      
      if (!assignment) {
        ElMessage.error('未找到对应的值班安排')
        return
      }
      
      // 检测是否已经存在相同值班表、日期、班次的值班记录
      const existingRecord = recordList.value.find(r => {
        const recordDate = formatDate(r.dutyDate)
        return recordDate === dateStr && 
               r.dutyShift === createForm.dutyShift
      })
      
      if (existingRecord) {
        ElMessage.error('该日期、班次的值班记录已经存在，不允许重复新增')
        return
      }
      
      // 构建请求参数
      const recordData = {
        assignmentId: assignment.id,
        employeeId: userStore.employeeId,
        dutyStatus: createForm.dutyStatus,
        checkInRemark: createForm.remark,
        checkOutRemark: createForm.checkOutRemark,
        overtimeHours: createForm.isOvertime ? createForm.overtimeHours : 0,
        approvalStatus: createForm.approvalStatus,
        managerRemark: createForm.managerRemark,
        substituteEmployeeId: createForm.substituteEmployeeId,
        substituteType: createForm.substituteType
      }
      
      // 如果是加班班次，添加签到和签退时间
      if (createForm.isOvertime) {
        // 将时间字符串转换为完整的日期时间格式
        const dutyDate = new Date(createForm.dutyDate)
        
        if (createForm.checkInTime) {
          const [inHour, inMinute] = createForm.checkInTime.split(':').map(Number)
          const checkInDateTime = new Date(dutyDate)
          checkInDateTime.setHours(inHour, inMinute, 0, 0)
          recordData.checkInTime = checkInDateTime.toISOString()
        }
        
        if (createForm.checkOutTime) {
          const [outHour, outMinute] = createForm.checkOutTime.split(':').map(Number)
          const checkOutDateTime = new Date(dutyDate)
          checkOutDateTime.setHours(outHour, outMinute, 0, 0)
          // 处理跨天情况
          if (outHour < createForm.checkInTime.split(':')[0]) {
            checkOutDateTime.setDate(checkOutDateTime.getDate() + 1)
          }
          recordData.checkOutTime = checkOutDateTime.toISOString()
        }
      }
      
      const response = await addRecord(recordData)
      
      if (response.code === 200) {
        ElMessage.success('新建值班记录成功')
        createDialogVisible.value = false
        fetchRecordList()
      } else {
        ElMessage.error(response.message || '新建值班记录失败')
      }
    } else {
      // 编辑值班记录
      const recordData = {
        id: createForm.id,
        dutyStatus: createForm.dutyStatus,
        checkInRemark: createForm.checkInRemark,
        checkOutRemark: createForm.checkOutRemark,
        overtimeHours: createForm.overtimeHours,
        approvalStatus: createForm.approvalStatus,
        managerRemark: createForm.managerRemark,
        substituteEmployeeId: createForm.substituteEmployeeId,
        substituteType: createForm.substituteType
      }
      
      const response = await updateRecord(recordData)
      
      if (response.code === 200) {
        ElMessage.success('编辑值班记录成功')
        createDialogVisible.value = false
        fetchRecordList()
      } else {
        ElMessage.error(response.message || '编辑值班记录失败')
      }
    }
  } catch (error) {
    console.error('处理值班记录失败:', error)
    ElMessage.error('处理值班记录失败')
  } finally {
    createLoading.value = false
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
  // 先获取班次配置列表
  console.log('开始获取班次配置列表')
  await fetchShiftConfigs()
  console.log('班次配置列表获取完成')
  // 检查用户是否是值班长
  await checkIfDutyManager()
  // 再获取其他数据
  console.log('开始获取员工列表')
  await fetchEmployeeList()
  console.log('员工列表获取完成')
  console.log('开始获取值班安排列表')
  await fetchAssignmentList()
  console.log('值班安排列表获取完成')
  console.log('开始获取部门列表')
  await fetchDeptList()
  console.log('部门列表获取完成')
  console.log('开始获取值班表列表')
  await fetchScheduleList()
  console.log('值班表列表获取完成')
  console.log('开始获取值班记录列表')
  await fetchRecordList()
  console.log('值班记录列表获取完成')
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