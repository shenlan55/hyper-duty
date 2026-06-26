<template>
  <div class="record-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>加班记录</span>
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
            <el-button type="primary" @click="openCreateDialog">
              新建加班记录
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 标签页组件 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mb-4">
        <el-tab-pane label="加班人员记录" name="record">
          <BaseTable
            v-loading="loading || !shiftConfigsLoaded"
            :data="recordList"
            :columns="recordColumns"
            :show-pagination="true"
            :pagination="{
              currentPage: pagination.currentPage,
              pageSize: pagination.pageSize,
              pageSizes: pagination.pageSizes,
              total: pagination.total
            }"
            :backend-pagination="true"
            :show-search="true"
            :search-placeholder="'请输入值班日期'"
            :show-column-control="true"
            :show-export="true"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            @search="handleTableSearch"
            @export="handleExport"
          >
            <template #scheduleName="{ row }">
              {{ row.scheduleName || '未知值班表' }}
            </template>
            <template #dutyDate="{ row }">
              {{ formatDate(row.dutyDate) }}
            </template>
            <template #dutyShiftName="{ row }">
              <el-tag :type="'info'">
                {{ row.dutyShiftName || getShiftName(row.dutyShift) }}
              </el-tag>
            </template>
            <template #employeeName="{ row }">
              {{ row.employeeName || '未知人员' }}
            </template>
            <template #overtimeHours="{ row }">
              {{ row.overtimeHours || 0 }}小时
            </template>
            <template #approvalStatus="{ row }">
              <el-tag
                :type="getApprovalType(row.approvalStatus)"
              >
                {{ getApprovalStatusText(row.approvalStatus) || '待审批' }}
              </el-tag>
            </template>
            <template #operation="{ row }">
              <el-button 
                v-if="!isApprovedStatus(row.approvalStatus)"
                type="warning" 
                size="small" 
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="!isApprovedStatus(row.approvalStatus)"
                type="danger" 
                size="small" 
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </BaseTable>
        </el-tab-pane>
        <el-tab-pane label="加班审批" name="approval" :disabled="!isDutyManager">
          <BaseTable
            v-loading="approvalLoading || !shiftConfigsLoaded"
            :data="filteredApprovalList"
            :columns="approvalColumns"
            :show-pagination="true"
            :pagination="{...approvalPagination, total: filteredApprovalTotal}"
            :show-search="true"
            :search-placeholder="'请输入值班日期'"
            :show-column-control="true"
            :show-export="true"
            @size-change="handleApprovalSizeChange"
            @current-change="handleApprovalCurrentChange"
            @search="handleApprovalTableSearch"
            @export="handleApprovalExport"
          >
            <template #scheduleName="{ row }">
              {{ row.scheduleName || '未知值班表' }}
            </template>
            <template #dutyDate="{ row }">
              {{ formatDate(row.dutyDate) }}
            </template>
            <template #dutyShiftName="{ row }">
              <el-tag :type="'info'">
                {{ row.dutyShiftName || getShiftName(row.dutyShift) }}
              </el-tag>
            </template>
            <template #employeeName="{ row }">
              {{ row.employeeName || '未知人员' }}
            </template>
            <template #overtimeHours="{ row }">
              {{ row.overtimeHours || 0 }}小时
            </template>
            <template #approvalStatus="{ row }">
              <el-tag
                :type="getApprovalType(row.approvalStatus)"
              >
                {{ getApprovalStatusText(row.approvalStatus) || '待审批' }}
              </el-tag>
            </template>
            <template #operation="{ row }">
              <el-button 
                v-if="isDutyManager && row.approvalStatus !== '已批准'"
                type="warning" 
                size="small" 
                @click="openEditDialog(row)"
              >
                审批
              </el-button>
              <el-button 
                v-if="!isApprovedStatus(row.approvalStatus)"
                type="danger" 
                size="small" 
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </BaseTable>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新建/编辑加班记录对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="createForm.id ? '编辑加班记录' : '新建加班记录'"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-position="top"
      >
        <el-form-item label="值班表" prop="scheduleId" :disabled="createForm.id || dialogMode === 'approval'">
          <el-select
            v-model="createForm.scheduleId"
            placeholder="请选择值班表"
            style="width: 100%"
            @change="handleScheduleChange"
            :loading="assignmentsLoading"
            :disabled="createForm.id || dialogMode === 'approval'"
          >
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="schedule.scheduleName"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班日期" prop="dutyDate" :disabled="createForm.id || dialogMode === 'approval'">
          <el-date-picker
            v-model="createForm.dutyDate"
            type="date"
            placeholder="选择值班日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            :disabled="createForm.id || dialogMode === 'approval'"
          />
        </el-form-item>
        <el-form-item label="班次" prop="dutyShift" :disabled="createForm.id || dialogMode === 'approval'">
          <el-select
            v-model="createForm.dutyShift"
            placeholder="请选择班次"
            style="width: 100%"
            @change="handleShiftChange"
            :loading="assignmentsLoading"
            :disabled="createForm.id || dialogMode === 'approval'"
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
        <el-form-item label="加班时长（小时）" prop="overtimeHours">
          <el-input-number
            v-model="createForm.overtimeHours"
            :min="0"
            :max="24"
            :step="0.5"
            placeholder="请输入加班时长"
            style="width: 100%"
            :disabled="dialogMode === 'approval'"
          />
        </el-form-item>
        
        <el-form-item label="加班原因" prop="remark">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入加班原因"
            :disabled="dialogMode === 'approval'"
          />
        </el-form-item>

        <!-- 审批模式下的字段 -->
        <template v-if="dialogMode === 'approval'">
          <el-form-item label="审批状态" prop="approvalStatus">
            <el-select
              v-model="createForm.approvalStatus"
              placeholder="请选择审批状态"
              style="width: 100%"
            >
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
        </template>
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
  addRecord,
  updateRecord,
  deleteRecord
} from '../../api/duty/record'
import { getEmployeeList } from '../../api/employee'
import { getAssignmentsByScheduleId } from '../../api/duty/assignment'
import { getAllSchedules, getScheduleLeaders, getScheduleEmployees, getScheduleShifts } from '../../api/duty/schedule'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'
import BaseTable from '../../components/BaseTable.vue'
import { safeInput } from '../../utils/xssUtil'
import { useRoute } from 'vue-router'
import { useSearchPagination } from '../../hooks/usePagination'
import { APPROVAL_STATUS_LABEL, APPROVAL_STATUS_TAG, SHIFT_TYPE_FALLBACK_LABEL } from '../../constants/duty'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const approvalLoading = ref(false)
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref()

// 标签页相关
const activeTab = ref('record')

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

// 选中的值班表ID
const selectedScheduleId = ref(null)

// 值班长列表
const scheduleLeaders = ref({})

// 分页配置 - 记录页
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

// 分页配置 - 审批页
const {
  currentPage: approvalCurrentPage,
  pageSize: approvalPageSize,
  total: approvalTotal,
  pagination: approvalPagination,
  handleCurrentChange: originalApprovalHandleCurrentChange,
  handleSizeChange: originalApprovalHandleSizeChange,
  searchQuery: approvalSearchQuery,
  handleSearch: handleApprovalSearch
} = useSearchPagination()

// 获取值班表的值班长列表
const fetchScheduleLeaders = async (scheduleId) => {
  if (!scheduleId) return []
  
  try {
    const data = await getScheduleLeaders(scheduleId)
    scheduleLeaders.value[scheduleId] = data
    return data
  } catch (error) {
    // console.error('获取值班长列表失败:', error)
  }
  return []
}

// 是否是值班长（根据选择的值班表判断）
const isDutyManager = computed(() => {
  if (!selectedScheduleId.value || !userStore.employeeId) {
    return false
  }
  
  // 检查当前用户是否在值班长列表中
  const leaders = scheduleLeaders.value[selectedScheduleId.value] || []
  const currentEmployeeId = parseInt(userStore.employeeId) || 0
  
  let isLeader = false
  
  // 处理值班长列表是ID数组的情况
  if (leaders.length > 0 && typeof leaders[0] === 'number') {
    isLeader = leaders.some(leaderId => {
      const id = parseInt(leaderId) || 0
      return id === currentEmployeeId
    })
  } 
  // 处理值班长列表是对象数组的情况
  else {
    isLeader = leaders.some(leader => {
      // 确保类型匹配
      const leaderId = parseInt(leader.id) || parseInt(leader.employeeId) || 0
      return leaderId === currentEmployeeId
    })
  }
  
  return isLeader
})

// 当前记录信息
const currentRecord = ref(null)

// 表格列配置
const recordColumns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'scheduleName', label: '值班表', minWidth: '150' },
  { prop: 'dutyDate', label: '值班日期', width: '150' },
  { prop: 'dutyShiftName', label: '班次', width: '100' },
  { prop: 'employeeName', label: '值班人员', minWidth: '150' },
  { prop: 'overtimeHours', label: '加班时长', width: '100' },
  { prop: 'remark', label: '加班原因', minWidth: '200' },
  { prop: 'approvalStatus', label: '审批状态', width: '120' },
  { type: 'operation', label: '操作', width: '180', fixed: 'right' }
]

const approvalColumns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'scheduleName', label: '值班表', minWidth: '150' },
  { prop: 'dutyDate', label: '值班日期', width: '150' },
  { prop: 'dutyShiftName', label: '班次', width: '100' },
  { prop: 'employeeName', label: '值班人员', minWidth: '150' },
  { prop: 'overtimeHours', label: '加班时长', width: '100' },
  { prop: 'remark', label: '加班原因', minWidth: '200' },
  { prop: 'approvalStatus', label: '审批状态', width: '120' },
  { type: 'operation', label: '操作', width: '160', fixed: 'right' }
]

// 分页变更处理
const handleCurrentChange = (val) => {
  originalHandleCurrentChange(val)
  fetchRecordList()
}

const handleSizeChange = (val) => {
  originalHandleSizeChange(val)
  fetchRecordList()
}

// 审批页面分页变更处理
const handleApprovalCurrentChange = (val) => {
  originalApprovalHandleCurrentChange(val)
  fetchRecordList()
}

const handleApprovalSizeChange = (val) => {
  originalApprovalHandleSizeChange(val)
  fetchRecordList()
}

// 计算过滤后的记录总数
const filteredRecordTotal = computed(() => {
  return filteredRecordList.value.length
})

const filteredApprovalTotal = computed(() => {
  return filteredApprovalList.value.length
})

// 表格搜索处理
const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  handleSearch(keyword)
  fetchRecordList()
}

// 审批表格搜索处理
const handleApprovalTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  handleApprovalSearch(keyword)
  fetchRecordList()
}

// 数据列表
const employeeList = ref([])
const recordList = ref([])

// 班次名称映射（动态）
const shiftNames = ref({})

// 保存班次名称
const saveShiftName = (id, name) => {
  // 确保id是数字类型
  const idNum = parseInt(id)
  if (!isNaN(idNum)) {
    shiftNames.value[idNum] = name

  }
}

// 检查用户是否是值班长
const checkIfDutyManager = async () => {
  // 这里需要根据实际情况实现，假设用户信息中包含值班长标识
  // 暂时使用模拟数据，实际项目中需要从后端获取
  // 由于isDutyManager现在是计算属性，不需要手动设置值
  // 计算属性会根据selectedScheduleId自动判断
}



// 审批状态 label/type 走 constants/duty.js（统一管理）

// 标准化状态为英文代码
const normalizeApprovalStatus = (status) => {
  if (!status) return 'pending'
  // 兼容中英文双向
  if (APPROVAL_STATUS_LABEL[status]) return status
  const found = Object.entries(APPROVAL_STATUS_LABEL).find(([, label]) => label === status)
  return found ? found[0] : status
}

// 判断是否是已批准状态
const isApprovedStatus = (status) => {
  return normalizeApprovalStatus(status) === 'approved'
}

// 判断是否是待审批状态
const isPendingStatus = (status) => {
  return normalizeApprovalStatus(status) === 'pending'
}

const createForm = reactive({
  id: null,
  scheduleId: null,
  dutyDate: null,
  dutyShift: null,
  isOvertime: true, // 默认是加班
  overtimeHours: 0,
  remark: '',
  approvalStatus: 'pending'
})



// 新建值班记录表单验证规则
const createRules = {
  scheduleId: [
    { required: true, message: '请选择值班表', trigger: 'blur' }
  ],
  dutyDate: [
    { required: true, message: '请选择值班日期', trigger: ['blur', 'change'] }
  ],
  dutyShift: [
    { required: true, message: '请选择班次', trigger: ['blur', 'change'] }
  ],
  overtimeHours: [
    { required: true, message: '请输入加班时长', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请输入加班原因', trigger: 'blur' }
  ],
  approvalStatus: [
    { required: true, message: '请选择审批状态', trigger: 'change', validator: (rule, value, callback) => {
      if (dialogMode.value === 'approval' && !value) {
        callback(new Error('请选择审批状态'))
      } else {
        callback()
      }
    }}
  ]
}



// 获取员工姓名
const getEmployeeName = (employeeId) => {
  if (!employeeId) return '未知人员'
  const targetId = parseInt(employeeId) || 0
  // 优先检查是否是当前登录用户
  if (targetId === userStore.employeeId && userStore.employeeName) {
    return userStore.employeeName
  }
  const employee = employeeList.value.find(e => parseInt(e.id) === targetId)
  return employee ? (employee.employeeName || employee.employeename || employee.name || '未知人员') : '未知人员'
}

// 获取状态名称


// 获取审批状态中文显示（label 走 constants/duty.js）
const getApprovalStatusText = (status) => {
  if (!status) return '待审批'
  // 先尝试直接命中英文编码
  if (APPROVAL_STATUS_LABEL[status]) return APPROVAL_STATUS_LABEL[status]
  // 兼容中文输入
  const found = Object.entries(APPROVAL_STATUS_LABEL).find(([, label]) => label === status)
  return found ? found[1] : status
}

// 获取审批类型（type 走 constants/duty.js）
const getApprovalType = (status) => {
  if (!status) return 'info'
  if (APPROVAL_STATUS_TAG[status]) return APPROVAL_STATUS_TAG[status]
  const found = Object.entries(APPROVAL_STATUS_TAG).find(([, label]) => label === status)
  return found ? found[1] : 'info'
}

const getEmployeeDeptName = (deptId) => {
  const dept = deptList.value.find(d => d.id === deptId)
  return dept ? dept.deptName : '未知部门'
}

// 审批页面过滤后的值班记录列表
const filteredApprovalList = computed(() => {
  let list = recordList.value
  
  // 只显示审批状态为待审批的记录
  list = list.filter(record => isPendingStatus(record.approvalStatus))
  
  return list
})

// 获取员工列表
const fetchEmployeeList = async () => {
  try {
    const data = await getEmployeeList()
    employeeList.value = data?.records || []
  } catch (error) {
    // console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}



// 获取值班记录列表
const fetchRecordList = async () => {
  loading.value = true
  try {
    // 先获取班次配置列表
    await fetchShiftConfigs()
    
    // 获取所有值班安排
    let allAssignments = []
    try {
      // 根据选择的值班表获取值班安排
      if (selectedScheduleId.value) {
        const assignmentsResponse = await getAssignmentsByScheduleId(selectedScheduleId.value)
        allAssignments = assignmentsResponse || []
      } else {
        // 使用模拟数据
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
      }
    } catch (error) {
      // console.error('获取值班安排失败:', error)
    }
    
    // 值班长获取所有记录，普通用户只获取自己的记录
        let allRecords = []
        if (userStore.employeeId) {
          if (isDutyManager.value) {
            // 值班长获取所有记录，支持分页
            allRecords = await getRecordList({
              pageNum: activeTab.value === 'record' ? currentPage.value : approvalCurrentPage.value,
              pageSize: activeTab.value === 'record' ? pageSize.value : approvalPageSize.value,
              keyword: activeTab.value === 'record' ? searchQuery.value : approvalSearchQuery.value,
              scheduleId: selectedScheduleId.value,
              date: activeTab.value === 'record' ? searchQuery.value : approvalSearchQuery.value
            })
          } else {
            // 普通用户获取自己的记录，支持分页
            allRecords = await getRecordsByEmployeeId({
              employeeId: userStore.employeeId,
              pageNum: activeTab.value === 'record' ? currentPage.value : approvalCurrentPage.value,
              pageSize: activeTab.value === 'record' ? pageSize.value : approvalPageSize.value,
              keyword: activeTab.value === 'record' ? searchQuery.value : approvalSearchQuery.value,
              scheduleId: selectedScheduleId.value,
              date: activeTab.value === 'record' ? searchQuery.value : approvalSearchQuery.value
            })
          }
      
      // 处理分页数据
        if (allRecords && allRecords.records) {
          recordList.value = allRecords.records || []
          // 更新分页总数
          if (activeTab.value === 'record') {
            total.value = allRecords.total || 0
            pagination.total = allRecords.total || 0
          } else {
            approvalTotal.value = allRecords.total || 0
            approvalPagination.total = allRecords.total || 0
          }
        } else {
          recordList.value = allRecords || []
          // 当没有records属性时，使用数组长度作为总记录数
          if (Array.isArray(allRecords)) {
            if (activeTab.value === 'record') {
              total.value = allRecords.length
              pagination.total = allRecords.length
            } else {
              approvalTotal.value = allRecords.length
              approvalPagination.total = allRecords.length
            }
          }
        }
      
      // 遍历值班记录，获取值班安排详情
      recordList.value.forEach(record => {
        // 优先使用记录自身的字段
        if (record.scheduleId) {
          const schedule = scheduleList.value.find(s => s.id === record.scheduleId)
          record.scheduleName = schedule ? schedule.scheduleName : '未知值班表'
        } else if (record.assignmentId) {
          // 如果记录没有scheduleId，尝试从值班安排中查找
          const assignment = allAssignments.find(a => a.id === record.assignmentId)
          if (assignment) {
            // 设置值班日期和班次
            record.dutyDate = record.dutyDate || assignment.dutyDate
            record.dutyShift = record.dutyShift || (assignment.shiftConfigId || assignment.dutyShift)
            // 设置值班表信息
            record.scheduleId = assignment.scheduleId
            const schedule = scheduleList.value.find(s => s.id === assignment.scheduleId)
            record.scheduleName = schedule ? schedule.scheduleName : '未知值班表'
          }
        }
        
        if (record.dutyShift) {
          // 尝试从班次配置中查找
          const shiftConfig = shiftConfigs.value.find(config => config.id === record.dutyShift)
          if (shiftConfig && shiftConfig.shiftName) {
            saveShiftName(record.dutyShift, shiftConfig.shiftName)
          } else {
            // 使用默认格式，确保能够处理所有班次值
            saveShiftName(record.dutyShift, `班次${record.dutyShift}`)
          }
        }
      })
    } else {
      // 用户未登录，使用空数据
      recordList.value = []
      total.value = 0
      pagination.total = 0
      approvalTotal.value = 0
      approvalPagination.total = 0
    }
  } catch (error) {
    // console.error('获取值班记录列表失败:', error)
    ElMessage.error('获取值班记录列表失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换处理
const handleTabChange = (tabName) => {
  // 切换标签页时重置相关状态并重新加载数据
  if (tabName === 'record') {
    currentPage.value = 1
    pagination.currentPage = 1
  } else if (tabName === 'approval') {
    approvalCurrentPage.value = 1
    approvalPagination.currentPage = 1
  }
  // 重新加载数据
  fetchRecordList()
}



// 打开签到对话框


// 打开编辑对话框
const openEditDialog = async (record) => {
  // 检查审批状态，如果已批准则不允许编辑
  if (isApprovedStatus(record.approvalStatus)) {
    ElMessage.warning('已批准的记录不能编辑')
    return
  }
  
  currentRecord.value = record
  // 先复制基本字段
  createForm.id = record.id
  createForm.scheduleId = record.scheduleId
  createForm.dutyDate = record.dutyDate
  createForm.dutyShift = record.dutyShift
  createForm.overtimeHours = record.overtimeHours
  createForm.remark = record.remark || ''
  
  // 检查是否是审批模式（值班长且在审批标签页）
  if (isDutyManager.value && activeTab.value === 'approval') {
    dialogMode.value = 'approval'
    // 审批模式下设置默认值为已批准，不覆盖
    createForm.approvalStatus = 'approved'
  } else {
    dialogMode.value = 'edit'
    createForm.approvalStatus = record.approvalStatus || 'pending'
  }
  
  // 初始化可用班次，确保编辑时班次显示中文名称
  if (record.scheduleId && record.dutyDate) {
    try {
      // 先获取班次配置列表
      await fetchShiftConfigs()
      
      // 直接调用后端API获取值班表下的所有值班安排
      const assignments = await getAssignmentsByScheduleId(record.scheduleId)
      
      const dateStr = formatDate(record.dutyDate)
      
      // 过滤出该日期下的所有值班安排
      const dateAssignments = assignments.filter(assignment => {
        const assignmentDate = formatDate(assignment.dutyDate)
        return assignmentDate === dateStr
      })
      
      // 转换为班次选项格式
      const shiftMap = new Map()
      dateAssignments.forEach(assignment => {
          // 检查是否是加班班次（用于标记）
          let isOvertime = false
          
          // 优先使用shiftConfigId查找班次配置
          if (assignment.shiftConfigId) {
            const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
            if (shiftConfig) {
              // 正确处理isOvertimeShift字段（整数类型0或1）
              isOvertime = parseInt(shiftConfig.isOvertimeShift) === 1
            }
          } else if (assignment.dutyShift) {
            // 如果没有shiftConfigId，使用dutyShift查找班次配置
            const shift = parseInt(assignment.dutyShift) || 0
            if (shift > 0) {
              const shiftConfig = shiftConfigs.value.find(config => config.id === shift)
              if (shiftConfig) {
                // 正确处理isOvertimeShift字段（整数类型0或1）
                isOvertime = parseInt(shiftConfig.isOvertimeShift) === 1
              }
            }
          }
          
          // 如果从班次配置中没有找到，尝试从值班安排中获取
          if (!isOvertime && assignment.isOvertime !== undefined) {
            // 处理值班安排中的isOvertime字段
            if (typeof assignment.isOvertime === 'string') {
              isOvertime = assignment.isOvertime === '是'
            } else if (typeof assignment.isOvertime === 'number') {
              isOvertime = assignment.isOvertime === 1
            } else {
              isOvertime = Boolean(assignment.isOvertime)
            }
          }
          
          // 优先使用shiftConfigId获取班次名称
          if (assignment.shiftConfigId) {
            const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
            let shiftName = '未知班次'
            
            if (shiftConfig) {
              shiftName = shiftConfig.shiftName || getShiftName(assignment.shiftConfigId)
            } else {
              // 如果班次配置不存在，尝试从其他值班安排中查找
              const otherAssignment = dateAssignments.find(a => a.shiftConfigId === assignment.shiftConfigId && a.shiftName)
              if (otherAssignment && otherAssignment.shiftName) {
                shiftName = otherAssignment.shiftName
              } else {
                shiftName = getShiftName(assignment.shiftConfigId)
              }
            }
            
            // 如果是加班班次，标记一下
            if (isOvertime) {
              shiftName += '(加班)'
            }
            
            // 去重
            if (!shiftMap.has(assignment.shiftConfigId)) {
              shiftMap.set(assignment.shiftConfigId, {
                label: shiftName,
                value: assignment.shiftConfigId,
                isOvertime: isOvertime
              })
              // 保存班次名称
              saveShiftName(assignment.shiftConfigId, shiftConfig ? shiftConfig.shiftName : getShiftName(assignment.shiftConfigId))
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
                  shiftName = getShiftName(shift)
                }
              }
              
              // 如果是加班班次，标记一下
              if (isOvertime) {
                shiftName += '(加班)'
              }
              
              // 去重
              if (!shiftMap.has(shift)) {
                shiftMap.set(shift, {
                  label: shiftName,
                  value: shift,
                  isOvertime: isOvertime
                })
                // 保存班次名称
                saveShiftName(shift, shiftConfig ? shiftConfig.shiftName : getShiftName(shift))
              }
            }
          }
        })

      // 将Map转换为数组
      availableShifts.value = Array.from(shiftMap.values())

      // 兜底：编辑时如果 currentRecord.dutyShift 不在 availableShifts 中（因为该班次不在当前值班表的
      // 当天 assignments 里），手动追加一条 option，避免 el-select 显示 v-model 原值（如 "17"）
      const fallbackRecord = currentRecord.value
      if (fallbackRecord && fallbackRecord.dutyShift != null) {
        const currentShiftId = parseInt(fallbackRecord.dutyShift)
        if (!isNaN(currentShiftId) && !availableShifts.value.some(s => parseInt(s.value) === currentShiftId)) {
          // 优先从 shiftConfigs 查找名称，再回退到 getShiftName
          const config = shiftConfigs.value.find(c => parseInt(c.id) === currentShiftId)
          const shiftName = (config && config.shiftName) ? config.shiftName : getShiftName(currentShiftId)
          availableShifts.value.push({
            label: shiftName,
            value: currentShiftId,
            isOvertime: config ? (parseInt(config.isOvertimeShift) === 1) : true
          })
        }
      }
    } catch (error) {
      // console.error('获取值班安排失败:', error)
    }
  }
  
  createDialogVisible.value = true
}



// 删除值班记录
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteRecord(id)
    ElMessage.success('删除值班记录成功')
    fetchRecordList()
  } catch (error) {
    if (error !== 'cancel') {
      // console.error('删除值班记录失败:', error)
      ElMessage.error('删除值班记录失败')
    }
  }
}

// 跟踪当前对话框模式：'create'(新建) | 'edit'(编辑) | 'approval'(审批)
const dialogMode = ref('create')

// 打开新建值班记录对话框
const openCreateDialog = () => {
  createForm.scheduleId = null
  createForm.dutyDate = null
  createForm.dutyShift = null
  createForm.remark = ''
  createForm.id = null  // 确保是新建模式
  
  // 设置为新建模式
  dialogMode.value = 'create'
  
  createDialogVisible.value = true
}

// 处理值班表选择变化
const handleScheduleChange = async (scheduleId) => {
  if (scheduleId) {
    assignmentsLoading.value = true
    try {
      // 获取班次配置列表
      await fetchShiftConfigs()
      
      // 获取值班表下的值班安排（用于后续查找）
      const assignments = await getAssignmentsByScheduleId(scheduleId)
      
      // 完全放开日期选择，不做任何限制
      availableDates.value = [] // 清空，不再用于限制
      
      // 检查用户是否登录
      if (!userStore.employeeId) {
        ElMessage.warning('请先登录系统')
        return
      }
      
      // 确保班次配置加载成功
      if (shiftConfigs.value.length === 0) {
        ElMessage.error('班次配置加载失败，请刷新页面重试')
        return
      }
      
      // 重置日期和班次选择
      createForm.dutyDate = null
      createForm.dutyShift = null
      availableShifts.value = []
      
      // 获取值班表的值班长列表
      await fetchScheduleLeaders(scheduleId)
    } catch (error) {
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
  
  // 重新获取值班记录列表
  await fetchRecordList()
}

// 禁用日期选择器中不可用的日期
const disabledDate = (time) => {
  const dateStr = formatDate(time)
  // 检查是否在可用日期列表中
  const isAvailable = availableDates.value && Array.isArray(availableDates.value) ? availableDates.value.includes(dateStr) : false
  return !isAvailable
}

// 检查日期是否可用（用于新建加班记录对话框）
const checkDateAvailability = (time) => {
  const dateStr = formatDate(time)
  // 检查是否在可用日期列表中
  const isAvailable = availableDates.value && Array.isArray(availableDates.value) ? availableDates.value.includes(dateStr) : false
  return !isAvailable
}

// 班次配置列表
const shiftConfigs = ref([])
const shiftConfigsLoaded = ref(false)
const shiftApi = shiftConfigApi()

// 获取班次名称
const getShiftName = (shift) => {
  if (!shift) {
    return '默认班次'
  }
  
  // 确保shift是数字类型
  const shiftNum = parseInt(shift) || 0
  
  // 从班次配置列表中查找
  const shiftConfig = shiftConfigs.value.find(config => config.id === shiftNum)
  if (shiftConfig && shiftConfig.shiftName) {
    return shiftConfig.shiftName
  }
  
  // 直接返回 fallback 班次名称（早/中/晚/全天，GOC/SRE 班次由 sys_shift_config 提供）
  if (SHIFT_TYPE_FALLBACK_LABEL[shiftNum]) {
    return SHIFT_TYPE_FALLBACK_LABEL[shiftNum]
  }
  
  // 如果没有找到，使用默认格式
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
  }
  
  return null
}

// 获取班次配置列表
const fetchShiftConfigs = async () => {
  try {
    const shiftConfigsData = await shiftApi.getShiftConfigList({
      pageNum: 1,
      pageSize: 100, // 加载足够多的班次配置
      keyword: ''
    })
    
    // 确保数据是分页对象
    if (shiftConfigsData && shiftConfigsData.records) {
      shiftConfigs.value = shiftConfigsData.records
      
      // 保存班次名称
      shiftConfigs.value.forEach(config => {
        saveShiftName(config.id, config.shiftName)
      })
    } else {
      shiftConfigs.value = []
    }
  } catch (error) {
    ElMessage.error('获取班次配置失败')
    shiftConfigs.value = []
  } finally {
    shiftConfigsLoaded.value = true
  }
}

// 处理日期选择变化
const handleDateChange = async (date) => {
  if (date && createForm.scheduleId) {
    const dateStr = formatDate(date)
    
    try {
      // 先获取班次配置列表
      await fetchShiftConfigs()
      
      // 直接调用后端API获取值班表下的所有值班安排
      const assignments = await getAssignmentsByScheduleId(createForm.scheduleId)
      
      // 过滤出该日期下的当前用户的值班安排
      const userEmployeeId = parseInt(userStore.employeeId) || 0
      const dateAssignments = assignments.filter(assignment => {
        const assignmentDate = formatDate(assignment.dutyDate)
        const assignmentEmployeeId = parseInt(assignment.employeeId) || 0
        return assignmentDate === dateStr && assignmentEmployeeId === userEmployeeId
      })
      
      // 转换为班次选项格式
      if (dateAssignments.length > 0) {
        // 如果有值班安排，显示该日期下的班次
        const shiftMap = new Map()
        dateAssignments.forEach(assignment => {
          // 检查是否是加班班次（用于标记）
          let isOvertime = false
          
          // 优先使用shiftConfigId查找班次配置
          if (assignment.shiftConfigId) {
            const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
            if (shiftConfig) {
              // 正确处理isOvertimeShift字段（整数类型0或1）
              isOvertime = parseInt(shiftConfig.isOvertimeShift) === 1
            }
          } else if (assignment.dutyShift) {
            // 如果没有shiftConfigId，使用dutyShift查找班次配置
            const shift = parseInt(assignment.dutyShift) || 0
            if (shift > 0) {
              const shiftConfig = shiftConfigs.value.find(config => config.id === shift)
              if (shiftConfig) {
                // 正确处理isOvertimeShift字段（整数类型0或1）
                isOvertime = parseInt(shiftConfig.isOvertimeShift) === 1
              }
            }
          }
          
          // 如果从班次配置中没有找到，尝试从值班安排中获取
          if (!isOvertime && assignment.isOvertime !== undefined) {
            // 处理值班安排中的isOvertime字段
            if (typeof assignment.isOvertime === 'string') {
              isOvertime = assignment.isOvertime === '是'
            } else if (typeof assignment.isOvertime === 'number') {
              isOvertime = assignment.isOvertime === 1
            } else {
              isOvertime = Boolean(assignment.isOvertime)
            }
          }
          
          // 优先使用shiftConfigId获取班次名称
          if (assignment.shiftConfigId) {
            const shiftConfig = shiftConfigs.value.find(config => config.id === assignment.shiftConfigId)
            let shiftName = '未知班次'
            
            if (shiftConfig) {
              shiftName = shiftConfig.shiftName || getShiftName(assignment.shiftConfigId)
            } else {
              // 如果班次配置不存在，尝试从其他值班安排中查找
              const otherAssignment = dateAssignments.find(a => a.shiftConfigId === assignment.shiftConfigId && a.shiftName)
              if (otherAssignment && otherAssignment.shiftName) {
                shiftName = otherAssignment.shiftName
              } else {
                shiftName = getShiftName(assignment.shiftConfigId)
              }
            }
            
            // 如果是加班班次，标记一下
            if (isOvertime) {
              shiftName += '(加班)'
            }
            
            // 去重
            if (!shiftMap.has(assignment.shiftConfigId)) {
              shiftMap.set(assignment.shiftConfigId, {
                label: shiftName,
                value: assignment.shiftConfigId,
                isOvertime: isOvertime
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
                  shiftName = getShiftName(shift)
                }
              }
              
              // 如果是加班班次，标记一下
              if (isOvertime) {
                shiftName += '(加班)'
              }
              
              // 去重
              if (!shiftMap.has(shift)) {
                shiftMap.set(shift, {
                  label: shiftName,
                  value: shift,
                  isOvertime: isOvertime
                })
                // 保存班次名称
                saveShiftName(shift, shiftConfig ? shiftConfig.shiftName : `班次${shift}`)
              }
            }
          }
        })
        
        // 将Map转换为数组
        availableShifts.value = Array.from(shiftMap.values())
      } else {
        // 如果没有值班安排，获取该值班表配置的班次
        try {
          const scheduleShiftIds = await getScheduleShifts(createForm.scheduleId)
          // 只显示该值班表配置的班次
          availableShifts.value = shiftConfigs.value
            .filter(config => scheduleShiftIds.includes(config.id))
            .map(config => {
              const isOvertime = parseInt(config.isOvertimeShift) === 1
              let shiftName = config.shiftName
              if (isOvertime) {
                shiftName += '(加班)'
              }
              return {
                label: shiftName,
                value: config.id,
                isOvertime: isOvertime
              }
            })
        } catch (err) {
          // 如果获取值班表配置班次失败，显示所有可用班次
          console.error('获取值班表配置班次失败:', err)
          availableShifts.value = shiftConfigs.value.map(config => {
            const isOvertime = parseInt(config.isOvertimeShift) === 1
            let shiftName = config.shiftName
            if (isOvertime) {
              shiftName += '(加班)'
            }
            return {
              label: shiftName,
              value: config.id,
              isOvertime: isOvertime
            }
          })
        }
      }
      
      // 重置班次选择
      createForm.dutyShift = null
      createForm.isOvertime = false
    } catch (error) {
      // console.error('获取值班安排失败:', error)
      ElMessage.error('获取值班安排失败')
      // 发生错误时尝试获取值班表配置的班次，否则显示所有可用班次
      try {
        const scheduleShiftIds = await getScheduleShifts(createForm.scheduleId)
        availableShifts.value = shiftConfigs.value
          .filter(config => scheduleShiftIds.includes(config.id))
          .map(config => {
            const isOvertime = parseInt(config.isOvertimeShift) === 1
            let shiftName = config.shiftName
            if (isOvertime) {
              shiftName += '(加班)'
            }
            return {
              label: shiftName,
              value: config.id,
              isOvertime: isOvertime
            }
          })
      } catch (err) {
        availableShifts.value = shiftConfigs.value.map(config => {
          const isOvertime = parseInt(config.isOvertimeShift) === 1
          let shiftName = config.shiftName
          if (isOvertime) {
            shiftName += '(加班)'
          }
          return {
            label: shiftName,
            value: config.id,
            isOvertime: isOvertime
          }
        })
      }
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



// 处理新建/编辑值班记录
const handleCreate = async () => {
  try {
    await createFormRef.value.validate()
    createLoading.value = true
    
    // 如果是新建值班记录
    if (!createForm.id) {
      // 查找对应的值班安排
      const dateStr = formatDate(createForm.dutyDate)
      
      // 获取值班表下的所有值班安排
      const assignments = await getAssignmentsByScheduleId(createForm.scheduleId)
      
      // 查找对应的值班安排
      const assignment = assignments.find(a => {
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
        return assignmentDate === dateStr && assignmentShift === formDutyShift && assignmentEmployeeId === userEmployeeId
      })
      
      // 构建请求参数
      let recordData
      if (assignment) {
        recordData = {
          assignmentId: assignment.id,
          employeeId: userStore.employeeId,
          overtimeHours: createForm.overtimeHours,
          remark: createForm.remark,
          approvalStatus: 'pending' // 新建时默认待审批
        }
      } else {
        // 如果没有对应的值班安排，直接使用 scheduleId, dutyDate, dutyShift
        recordData = {
          scheduleId: createForm.scheduleId,
          dutyDate: createForm.dutyDate,
          dutyShift: parseInt(createForm.dutyShift) || 0,
          employeeId: userStore.employeeId,
          overtimeHours: createForm.overtimeHours,
          remark: createForm.remark,
          approvalStatus: 'pending' // 新建时默认待审批
        }
      }
      
      await addRecord(recordData)
      
      ElMessage.success('新建加班记录成功')
      createDialogVisible.value = false
      fetchRecordList()
    } else if (dialogMode.value === 'approval') {
      // 审批模式 - 只修改审批状态
      console.log('审批模式，发送数据:', {
        id: createForm.id,
        approvalStatus: createForm.approvalStatus,
        overtimeHours: createForm.overtimeHours
      })
      const recordData = {
        id: createForm.id,
        approvalStatus: createForm.approvalStatus,
        overtimeHours: createForm.overtimeHours
      }
      
      await updateRecord(recordData)
      
      ElMessage.success('审批成功')
      createDialogVisible.value = false
      fetchRecordList()
    } else {
      // 编辑模式
      const recordData = {
        id: createForm.id,
        overtimeHours: createForm.overtimeHours,
        remark: createForm.remark
      }
      
      await updateRecord(recordData)
      
      ElMessage.success('编辑加班记录成功')
      createDialogVisible.value = false
      fetchRecordList()
    }
  } catch (error) {
    // console.error('处理值班记录失败:', error)
    ElMessage.error('处理值班记录失败')
  } finally {
    createLoading.value = false
  }
}



// 获取值班表列表
const fetchScheduleList = async () => {
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
    
    // 如果有值班表，默认选择第一个
    if (scheduleList.value.length > 0 && !selectedScheduleId.value) {
      selectedScheduleId.value = scheduleList.value[0].id
      // 获取默认值班表的值班长列表
      await fetchScheduleLeaders(selectedScheduleId.value)
      // 获取默认值班表的记录列表
      await fetchRecordList()
    }
  } catch (error) {
    // console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  }
}

// 处理导出
const handleExport = async () => {
  try {
    loading.value = true
    
    // 获取所有记录（不分页）
    let allRecords = []
    if (userStore.employeeId) {
      if (isDutyManager.value) {
        // 值班长获取所有记录
        allRecords = await getRecordList({ pageNum: 1, pageSize: 1000, scheduleId: selectedScheduleId.value })
      } else {
        // 普通用户获取自己的记录
        allRecords = await getRecordsByEmployeeId({ employeeId: userStore.employeeId, pageNum: 1, pageSize: 1000, scheduleId: selectedScheduleId.value })
      }
    } else {
      // 无员工ID时获取所有记录
      allRecords = await getRecordList({ pageNum: 1, pageSize: 1000, scheduleId: selectedScheduleId.value })
    }
    
    const records = allRecords.records || []
    
    // 转换数据格式
    const exportData = records.map(record => {
      return {
        '值班表': record.scheduleName || '未知值班表',
        '值班日期': formatDate(record.dutyDate),
        '班次': getShiftName(record.dutyShift),
        '值班人员': getEmployeeName(record.employeeId),
        '加班时长': `${record.overtimeHours || 0}小时`,
        '加班原因': record.remark || '',
        '审批状态': getApprovalStatusText(record.approvalStatus) || '待审批'
      }
    })
    
    // 导出为Excel
    import('xlsx').then(XLSX => {
      const worksheet = XLSX.utils.json_to_sheet(exportData)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '加班记录')
      XLSX.writeFile(workbook, `加班记录_${formatDate(new Date())}.xlsx`)
      ElMessage.success('导出成功')
    })
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 处理加班审批导出
const handleApprovalExport = async () => {
  try {
    approvalLoading.value = true
    
    // 获取所有审批记录（不分页）
    const allRecords = await getRecordList({ pageNum: 1, pageSize: 1000, scheduleId: selectedScheduleId.value })
    const records = allRecords.records || []
    
    // 转换数据格式
    const exportData = records.map(record => {
      return {
        '值班表': record.scheduleName || '未知值班表',
        '值班日期': formatDate(record.dutyDate),
        '班次': getShiftName(record.dutyShift),
        '值班人员': getEmployeeName(record.employeeId),
        '加班时长': `${record.overtimeHours || 0}小时`,
        '加班原因': record.remark || '',
        '审批状态': getApprovalStatusText(record.approvalStatus) || '待审批'
      }
    })
    
    // 导出为Excel
    import('xlsx').then(XLSX => {
      const worksheet = XLSX.utils.json_to_sheet(exportData)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '加班审批')
      XLSX.writeFile(workbook, `加班审批_${formatDate(new Date())}.xlsx`)
      ElMessage.success('导出成功')
    })
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    approvalLoading.value = false
  }
}

// 生命周期钩子
onMounted(async () => {
  // 先获取班次配置列表
  await fetchShiftConfigs()
  // 再获取其他数据
  await fetchEmployeeList()
  
  // 先处理路由参数，设置selectedScheduleId
  let routeScheduleId = null
  if (route.query.scheduleId) {
    routeScheduleId = parseInt(route.query.scheduleId)
  }
  
  // 如果有路由参数，先设置selectedScheduleId
  if (routeScheduleId) {
    selectedScheduleId.value = routeScheduleId
  }
  
  // 获取值班表列表，设置默认值班表并获取值班长列表
  await fetchScheduleList()
  
  // 处理activeTab参数
  if (route.query.activeTab) {
    activeTab.value = route.query.activeTab
  }
  
  // 如果有路由参数，获取对应值班表的值班长列表和记录列表
  if (routeScheduleId) {
    await fetchScheduleLeaders(routeScheduleId)
    await fetchRecordList()
  }
  // 否则，fetchScheduleList会自动设置默认值班表并调用fetchRecordList
})
</script>

<style scoped>
.record-container {
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
  min-width: 250px;
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

/* 移动端适配 */
@media (max-width: 767px) {
  .duty-record-container {
    padding: 0;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }

  .search-input {
    width: 100% !important;
  }

  .pagination-container {
    justify-content: center;
  }
}
</style>