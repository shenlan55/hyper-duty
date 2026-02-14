<template>
  <div class="assignment-container">
    <div class="page-header">
      <h2>值班安排</h2>
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
        <el-button type="primary" @click="openBatchDialog" :disabled="!selectedScheduleId || !isLeader">
          <el-icon><Plus /></el-icon>
          批量排班
        </el-button>
        <el-button type="danger" @click="openClearDialog" :disabled="!selectedScheduleId || !isLeader">
          <el-icon><Delete /></el-icon>
          批量清空
        </el-button>
        <el-button @click="openExportDialog" :disabled="!selectedScheduleId">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="content-card">
      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell" :class="getCalendarCellClass(data.day)" @click="handleCellClick(data.day)" style="cursor: pointer;">
            <div class="date-number">{{ data.day.split('-').slice(2).join('-') }}</div>
            <div v-if="isHoliday(data.day)" class="holiday-info">
              {{ getHolidayName(data.day) }}
            </div>
            <div class="duty-list">
              <div
                v-for="assignment in getAssignmentsByDate(data.day)"
                :key="assignment.id"
                class="duty-item"
                :class="{ 'clickable': isLeader, 'invalid-duty': assignment.status === 0 }"
                @click.stop="isLeader ? openEditDialog(assignment) : null"
              >
                <el-tag :type="getShiftTypeColor(assignment.dutyShift)" size="small">
                  {{ getShiftName(assignment.dutyShift) }}
                </el-tag>
                <span class="employee-name" :class="{ 'invalid-employee': assignment.status === 0 }">
                  {{ getEmployeeName(assignment.employeeId) }}
                </span>
              </div>
            </div>
            <el-button
              v-if="selectedScheduleId && isLeader"
              type="primary"
              size="small"
              text
              @click.stop="openAddDialog(data.day)"
              class="add-btn"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="async () => {
        // 恢复原始的排班员工列表
        await fetchScheduleEmployees(selectedScheduleId.value, false)
      }"
    >
      <el-form
        ref="assignmentFormRef"
        :model="assignmentForm"
        :rules="assignmentRules"
        label-position="top"
      >
        <el-form-item label="值班日期" prop="dutyDate">
          <el-date-picker
            v-model="assignmentForm.dutyDate"
            type="date"
            placeholder="选择值班日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="班次" prop="dutyShift">
          <el-select
            v-model="assignmentForm.dutyShift"
            placeholder="请选择班次"
            style="width: 100%"
            @change="handleShiftChange"
          >
            <el-option
              v-for="shift in availableShiftList"
              :key="shift.id"
              :label="shift.shiftName"
              :value="shift.id"
            >
              <span>{{ shift.shiftName }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                {{ shift.startTime }} - {{ shift.endTime }}
              </span>
            </el-option>
          </el-select>
          <el-alert
            v-if="availableShiftList.length === 0"
            type="warning"
            message="当前值班表未配置可选班次，请联系管理员设置"
            show-icon
            :closable="false"
            style="margin-top: 8px"
          />
        </el-form-item>
        <el-form-item label="值班人员" prop="employeeIds">
          <el-select
            v-model="assignmentForm.employeeIds"
            placeholder="请选择值班人员"
            style="width: 100%"
            multiple
            filterable
          >
            <el-option
              v-for="employee in scheduleEmployeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="assignmentForm.status">
            <el-radio :value="1">有效</el-radio>
            <el-radio :value="0">无效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="assignmentForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
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
      v-model="batchDialogVisible"
      title="批量排班"
      width="700px"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchRules"
        label-position="top"
      >
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="排班方式" prop="scheduleType">
          <el-radio-group v-model="batchForm.scheduleType">
            <el-radio :value="1">轮换排班</el-radio>
            <el-radio :value="2">固定排班</el-radio>
            <el-radio :value="3">排班模式</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期类型" prop="dateType">
          <el-checkbox-group v-model="batchForm.dateType">
            <el-checkbox label="workday">工作日（包括调休）</el-checkbox>
            <el-checkbox label="weekend">休息日（周末）</el-checkbox>
            <el-checkbox label="holiday">节假日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="排班模式" prop="scheduleModeId" v-if="batchForm.scheduleType === 3">
          <el-select
            v-model="batchForm.scheduleModeId"
            placeholder="请选择排班模式"
            style="width: 100%"
          >
            <el-option
              v-for="mode in scheduleModeList"
              :key="mode.id"
              :label="mode.modeName"
              :value="mode.id"
            >
              <span>{{ mode.modeName }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px;">{{ mode.description }}</span>
            </el-option>
          </el-select>
          <el-alert
            v-if="scheduleModeList.length === 0"
            type="warning"
            message="暂无可用的排班模式，请联系管理员配置"
            show-icon
            :closable="false"
            style="margin-top: 8px"
          />
        </el-form-item>
        <el-form-item label="班次" prop="dutyShift" v-if="batchForm.scheduleType !== 3">
          <el-select
            v-model="batchForm.dutyShift"
            placeholder="请选择班次"
            style="width: 100%"
          >
            <el-option
              v-for="shift in availableShiftList"
              :key="shift.id"
              :label="shift.shiftName"
              :value="shift.id"
            >
              <span>{{ shift.shiftName }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                {{ shift.startTime }} - {{ shift.endTime }}
              </span>
            </el-option>
          </el-select>
          <el-alert
            v-if="availableShiftList.length === 0"
            type="warning"
            message="当前值班表未配置可选班次，请联系管理员设置"
            show-icon
            :closable="false"
            style="margin-top: 8px"
          />
        </el-form-item>
        <el-form-item label="班次人数" prop="shiftEmployeeCount" v-if="batchForm.scheduleType === 1">
          <el-input-number
            v-model="batchForm.shiftEmployeeCount"
            :min="1"
            :max="scheduleEmployeeList.length"
            :step="1"
            placeholder="请输入每个班次的人数"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="值班人员" prop="employeeIds">
          <el-select
            v-model="batchForm.employeeIds"
            placeholder="请选择值班人员"
            style="width: 100%"
            multiple
            filterable
            @change="handleEmployeeSelectChange"
          >
            <!-- 全选选项 -->
            <el-option
              :key="'all'"
              :label="'全选'"
              :value="'all'"
            />
            <el-option
              v-for="employee in scheduleEmployeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="batchForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchDialogLoading" @click="handleBatchSave">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="clearDialogVisible"
      title="批量清空排班"
      width="500px"
    >
      <el-form
        ref="clearFormRef"
        :model="clearForm"
        :rules="clearRules"
        label-position="top"
      >
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="clearForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-alert
          title="警告"
          type="warning"
          description="清空操作不可恢复，请谨慎操作！"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="clearDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="clearDialogLoading" @click="handleClearSave">
            确认清空
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 当天值班人员详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailDialogTitle"
      width="1200px"
      height="800px"
    >
      <div v-if="selectedDateAssignments.length > 0">
        <el-tabs type="border-card">
          <el-tab-pane label="值班详情">
            <el-table :data="selectedDateAssignments" border stripe>
              <el-table-column prop="shiftName" label="班次" width="120">
                <template #default="scope">
                  <el-tag :type="getShiftTypeColor(scope.row.dutyShift)" size="small">
                    {{ scope.row.shiftName }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="employeeName" label="值班人员" width="150" />
              <el-table-column prop="deptName" label="所在部门" width="150" />
              <el-table-column prop="phone" label="联系电话" width="150" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 1 ? '有效' : '无效' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" />
              <el-table-column label="操作" width="100" v-if="isLeader">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="openEditDetailDialog(scope.row)">
                    修改
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="统计信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="日期">{{ selectedDetailDate }}</el-descriptions-item>
              <el-descriptions-item label="总值班人数">{{ selectedDateAssignments.length }}</el-descriptions-item>
              <el-descriptions-item label="有效值班">{{ selectedDateAssignments.filter(item => item.status === 1).length }}</el-descriptions-item>
              <el-descriptions-item label="无效值班">{{ selectedDateAssignments.filter(item => item.status === 0).length }}</el-descriptions-item>
              <el-descriptions-item label="节假日" :span="2">
                {{ isHoliday(selectedDetailDate) ? getHolidayName(selectedDetailDate) : '非节假日' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div v-else class="no-assignment">
        <el-empty description="当天无值班安排" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导出值班安排对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="导出值班安排"
      width="500px"
    >
      <el-form
        ref="exportFormRef"
        :model="exportForm"
        :rules="exportRules"
        label-position="top"
      >
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExport" :loading="exportDialogLoading">
            确认导出
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑值班详情对话框 -->
    <el-dialog
      v-model="editDetailDialogVisible"
      title="编辑值班详情"
      width="500px"
    >
      <el-form
        ref="editDetailFormRef"
        :model="editDetailForm"
        label-position="top"
      >
        <el-form-item label="值班人员">
          <el-input v-model="editDetailForm.employeeName" disabled />
        </el-form-item>
        <el-form-item label="班次">
          <el-input v-model="editDetailForm.shiftName" disabled />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editDetailForm.status">
            <el-radio label="1">有效</el-radio>
            <el-radio label="0">无效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editDetailForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDetailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveDetail" :loading="editDetailDialogLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAssignmentList,
  getAssignmentsByScheduleId,
  addAssignment,
  updateAssignment,
  deleteAssignment,
  deleteBatchAssignments
} from '../../api/duty/assignment'
import { getScheduleList, getScheduleEmployees, getScheduleLeaders, getScheduleShifts, getScheduleModeList, generateScheduleByMode } from '../../api/duty/schedule'
import { getEmployeeList } from '../../api/employee'
import { getDeptList } from '../../api/dept'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import { holidayApi } from '../../api/duty/holiday'
import { getEmployeeLeaveInfo as getEmployeeLeaveInfoAPI, getSubstitutesByEmployees } from '../../api/duty/leaveRequest'
import dayjs from 'dayjs'
import { formatDate, formatDateTime } from '../../utils/dateUtils'
import { useUserStore } from '../../stores/user'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

const userStore = useUserStore()

const selectedScheduleId = ref('')
const currentDate = ref(new Date())
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加值班安排')
const assignmentFormRef = ref()

const batchDialogVisible = ref(false)
const batchDialogLoading = ref(false)
const batchFormRef = ref()

const scheduleList = ref([])
const allEmployeeList = ref([])
const scheduleEmployeeList = ref([])
const scheduleLeaderList = ref([])
const shiftConfigList = ref([])
const availableShiftList = ref([])
const assignmentList = ref([])
const scheduleModeList = ref([])
const deptList = ref([])

const assignmentForm = reactive({
  id: null,
  scheduleId: null,
  dutyDate: null,
  dutyShift: null,
  employeeIds: [],
  status: 1,
  remark: ''
})

const batchForm = reactive({
  dateRange: null,
  scheduleType: 1,
  dutyShift: null,
  shiftEmployeeCount: 1, // 每个班次的人数，默认为1
  employeeIds: [],
  remark: '',
  scheduleModeId: null,
  dateType: ['workday'] // 默认选择工作日
})

const clearDialogVisible = ref(false)
const clearDialogLoading = ref(false)
const clearFormRef = ref()

const clearForm = reactive({
  dateRange: null
})

// 导出值班安排对话框相关
const exportDialogVisible = ref(false)
const exportDialogLoading = ref(false)
const exportFormRef = ref()

const exportForm = reactive({
  dateRange: null
})

const exportRules = {
  dateRange: [
    {
      type: 'array',
      required: true,
      message: '请选择日期范围',
      trigger: 'change'
    }
  ]
}

// 编辑值班详情对话框相关
const editDetailDialogVisible = ref(false)
const editDetailDialogLoading = ref(false)
const editDetailFormRef = ref()

const editDetailForm = reactive({
  id: null,
  employeeName: '',
  shiftName: '',
  status: 1,
  remark: ''
})

// 当天值班人员详情弹窗相关
const detailDialogVisible = ref(false)
const detailDialogTitle = ref('')
const selectedDetailDate = ref('')
const selectedDateAssignments = ref([])

// 批量排班全选相关
const selectAllEmployees = ref(false)

const assignmentRules = {
  dutyDate: [
    { required: true, message: '请选择值班日期', trigger: 'blur' }
  ],
  dutyShift: [
    { required: true, message: '请选择班次', trigger: 'blur' }
  ],
  employeeIds: [
    { required: true, message: '请选择值班人员', trigger: 'blur' },
    { type: 'array', min: 1, message: '至少选择一个值班人员', trigger: 'blur' }
  ]
}

const batchRules = {
  dateRange: [
    { required: true, message: '请选择日期范围', trigger: 'blur' }
  ],
  dateType: [
    { required: true, message: '请至少选择一种日期类型', trigger: 'blur' },
    { required: true, message: '请至少选择一种日期类型', trigger: 'change' }
  ],
  dutyShift: [
    { required: true, message: '请选择班次', trigger: 'blur' },
    { required: true, message: '请选择班次', trigger: 'change' }
  ],
  employeeIds: [
    { required: true, message: '请选择值班人员', trigger: 'blur' },
    { required: true, message: '请选择值班人员', trigger: 'change' }
  ],
  scheduleModeId: [
    { required: true, message: '请选择排班模式', trigger: 'blur' },
    { required: true, message: '请选择排班模式', trigger: 'change' }
  ]
}

const clearRules = {
  dateRange: [
    { required: true, message: '请选择日期范围', trigger: 'blur' }
  ]
}

const shiftNames = computed(() => {
  const map = {}
  shiftConfigList.value.forEach(shift => {
    map[shift.id] = shift.shiftName
  })
  return map
})

const getShiftName = (shiftId) => {
  return shiftNames.value[shiftId] || '未知班次'
}

const getShiftTypeColor = (shiftId) => {
  const shift = shiftConfigList.value.find(s => s.id === shiftId)
  if (!shift) return 'info'
  const colorMap = {
    0: 'primary',
    1: 'success',
    2: 'info',
    3: 'warning',
    4: 'primary',
    5: 'danger'
  }
  return colorMap[shift.shiftType] || 'info'
}

const getEmployeeName = (employeeId) => {
  const employee = allEmployeeList.value.find(e => e.id === employeeId)
  return employee ? employee.employeeName : '未知人员'
}

// 根据员工ID获取员工信息
const getEmployeeInfo = (employeeId) => {
  return allEmployeeList.value.find(e => e.id === employeeId) || {}
}

// 根据部门ID获取部门名称
const getDeptName = (deptId) => {
  const dept = deptList.value.find(d => d.id === deptId)
  return dept ? dept.deptName : '未知部门'
}

const getAssignmentsByDate = (date) => {
  // 先过滤出当天的值班安排
  const assignments = assignmentList.value.filter(assignment => assignment.dutyDate === date)
  // 排序：状态为1（有效）的排在前面，状态为0（无效）的排在后面
  return assignments.sort((a, b) => {
    if (a.status === b.status) return 0
    return a.status > b.status ? -1 : 1
  })
}

const isHoliday = (date) => {
  return !!holidayMap.value[date]
}

const getHolidayInfo = (date) => {
  return holidayMap.value[date]
}

const getCalendarCellClass = (date) => {
  const holidayInfo = getHolidayInfo(date)
  if (holidayInfo) {
    if (holidayInfo.isWorkday === 1) {
      return 'workday-holiday'
    } else {
      return 'regular-holiday'
    }
  }
  return ''
}

const getHolidayName = (date) => {
  const holidayInfo = getHolidayInfo(date)
  return holidayInfo ? holidayInfo.holidayName : ''
}

// 处理日历单元格点击事件，显示当天值班人员详情
const handleCellClick = (date) => {
  selectedDetailDate.value = date
  detailDialogTitle.value = `${date} 值班人员详情`
  
  // 获取当天的值班安排并格式化数据
  const assignments = getAssignmentsByDate(date)
  selectedDateAssignments.value = assignments.map(assignment => {
    const employeeInfo = getEmployeeInfo(assignment.employeeId)
    return {
      ...assignment,
      shiftName: getShiftName(assignment.dutyShift),
      employeeName: getEmployeeName(assignment.employeeId),
      deptName: getDeptName(employeeInfo.deptId),
      phone: employeeInfo.phone || '无'
    }
  })
  
  detailDialogVisible.value = true
}

const hasAssignment = (date) => {
  return assignmentList.value.some(assignment => assignment.dutyDate === date)
}

const fetchScheduleList = async () => {
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data
    }
  } catch (error) {
    // console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  }
}

const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      allEmployeeList.value = response.data
    }
  } catch (error) {
    // console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}

// 获取部门列表
const fetchDeptList = async () => {
  try {
    const response = await getDeptList()
    if (response.code === 200) {
      deptList.value = response.data
    }
  } catch (error) {
    // console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  }
}

const shiftApi = shiftConfigApi()
const holidayService = holidayApi()

const holidaysList = ref([])
const holidayMap = ref({})

const fetchShiftConfigList = async () => {
  try {
    const response = await shiftApi.getShiftConfigList()
    if (response.code === 200) {
      shiftConfigList.value = response.data.filter(shift => shift.status === 1)
    }
  } catch (error) {
    // console.error('获取班次配置列表失败:', error)
    ElMessage.error('获取班次配置列表失败')
  }
}

const fetchHolidaysList = async (startDate, endDate) => {
  try {
    // console.log('开始获取节假日列表:', startDate, endDate)
    const response = await holidayService.getHolidaysInRange(startDate, endDate)
    // console.log('节假日API响应:', response)
    if (response.code === 200) {
      holidaysList.value = response.data
      // console.log('获取到的节假日数据:', response.data)
      // 构建节假日映射，方便快速查询
      const map = {}
      response.data.forEach(holiday => {
        map[holiday.holidayDate] = holiday
      })
      holidayMap.value = map
      // console.log('构建的节假日映射:', map)
    }
  } catch (error) {
    // console.error('获取节假日列表失败:', error)
    // 节假日获取失败不影响主功能，只在控制台报错
    holidaysList.value = []
    holidayMap.value = {}
  }
}

const fetchScheduleModeList = async () => {
  try {
    const response = await getScheduleModeList()
    if (response.code === 200) {
      scheduleModeList.value = response.data.filter(mode => mode.status === 1)
    }
  } catch (error) {
    // console.error('获取排班模式列表失败:', error)
    ElMessage.error('获取排班模式列表失败')
  }
}

const fetchScheduleEmployees = async (scheduleId, showError = true) => {
  try {
    if (!scheduleId) {
      if (showError) {
        ElMessage.error('缺少值班表信息，无法获取值班人员')
      }
      return []
    }
    const [employeeRes, leaderRes] = await Promise.all([
      getScheduleEmployees(scheduleId),
      getScheduleLeaders(scheduleId)
    ])
    if (employeeRes.code === 200) {
      const employeeIds = employeeRes.data || []
      scheduleEmployeeList.value = allEmployeeList.value.filter(emp => 
        employeeIds.includes(emp.id)
      )
    }
    if (leaderRes.code === 200) {
      scheduleLeaderList.value = leaderRes.data || []
    }
    return scheduleEmployeeList.value
  } catch (error) {
    // console.error('获取值班人员失败:', error)
    ElMessage.error('获取值班人员失败')
    return []
  }
}

const fetchScheduleShifts = async (scheduleId) => {
  try {
    if (!scheduleId) {
      availableShiftList.value = []
      return
    }
    const response = await getScheduleShifts(scheduleId)
    if (response.code === 200) {
      const shiftIds = response.data || []
      // 过滤出当前值班表可用的班次
      availableShiftList.value = shiftConfigList.value.filter(shift => 
        shiftIds.includes(shift.id)
      )
    }
  } catch (error) {
    // console.error('获取值班表班次失败:', error)
    ElMessage.error('获取值班表班次失败')
  }
}

const isLeader = computed(() => {
  // 转换为相同类型后再比较，避免字符串和数字类型不匹配的问题
  return scheduleLeaderList.value.some(leaderId => {
    return String(leaderId) === String(userStore.employeeId)
  })
})

const fetchAssignmentList = async (scheduleId = null) => {
  loading.value = true
  try {
    let response
    if (scheduleId) {
      response = await getAssignmentsByScheduleId(scheduleId)
    } else {
      response = await getAssignmentList()
    }
    
    if (response.code === 200) {
      assignmentList.value = response.data
    }
  } catch (error) {
    // console.error('获取值班安排列表失败:', error)
    ElMessage.error('获取值班安排列表失败')
  } finally {
    loading.value = false
  }
}

const handleScheduleChange = async (scheduleId) => {
  if (scheduleId) {
    await fetchScheduleEmployees(scheduleId)
    await fetchScheduleShifts(scheduleId)
    await fetchAssignmentList(scheduleId)
  } else {
    scheduleEmployeeList.value = []
    availableShiftList.value = []
    assignmentList.value = []
    await fetchScheduleEmployees(scheduleId, false)
  }
}

const getMonthRange = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth()
  // 使用 dayjs 处理日期，避免时区问题
  const startDate = dayjs(date).startOf('month')
  const endDate = dayjs(date).endOf('month')
  return {
    start: startDate.format('YYYY-MM-DD'),
    end: endDate.format('YYYY-MM-DD')
  }
}

const updateHolidaysByMonth = async () => {
  const { start, end } = getMonthRange(currentDate.value)
  await fetchHolidaysList(start, end)
}

watch(currentDate, () => {
  updateHolidaysByMonth()
})

const openAddDialog = (date) => {
  resetForm()
  assignmentForm.scheduleId = selectedScheduleId.value
  assignmentForm.dutyDate = date
  dialogTitle.value = '添加值班安排'
  
  // 先恢复原始的排班员工列表
  if (selectedScheduleId.value) {
    fetchScheduleEmployees(selectedScheduleId.value)
  }
  
  dialogVisible.value = true
}

const openEditDialog = (assignment) => {
  if (!isLeader.value) {
    ElMessage.warning('只有值班长才能编辑值班安排')
    return
  }
  // 先设置selectedScheduleId，避免触发handleShiftChange时使用undefined
  if (!assignment.scheduleId) {
    ElMessage.error('值班安排缺少值班表信息，无法编辑')
    return
  }
  selectedScheduleId.value = assignment.scheduleId
  // 再复制其他属性
  Object.assign(assignmentForm, {
    id: assignment.id,
    scheduleId: assignment.scheduleId,
    dutyDate: assignment.dutyDate,
    dutyShift: assignment.dutyShift,
    employeeIds: [assignment.employeeId], // 将单个employeeId转换为数组
    status: assignment.status,
    remark: assignment.remark
  })
  dialogTitle.value = '编辑值班安排'
  dialogVisible.value = true
}

const resetForm = () => {
  if (assignmentFormRef.value) {
    assignmentFormRef.value.resetFields()
  }
  Object.assign(assignmentForm, {
    id: null,
    scheduleId: selectedScheduleId.value,
    dutyDate: null,
    dutyShift: null,
    employeeIds: [],
    status: 1,
    remark: ''
  })
}

// 监听班次变化，重新过滤可用员工
const handleShiftChange = async () => {
  if (!assignmentForm.dutyDate || !assignmentForm.dutyShift) return
  if (!selectedScheduleId.value) {
    ElMessage.error('缺少值班表信息，无法获取可用员工')
    return
  }
  
  // 获取原始的排班员工列表
  const originalEmployees = await fetchScheduleEmployees(selectedScheduleId.value)
  
  // 获取当天请假的员工列表
  const leaveInfo = await fetchEmployeeLeaveInfo(
    scheduleEmployeeList.value.map(emp => emp.id),
    assignmentForm.dutyDate,
    assignmentForm.dutyDate
  )
  
  // 过滤出当天在该班次没有请假的员工
  const availableEmployees = scheduleEmployeeList.value.filter(emp => {
    return !isEmployeeOnLeave(
      emp.id,
      assignmentForm.dutyDate,
      selectedScheduleId.value,
      assignmentForm.dutyShift,
      leaveInfo
    )
  })
  
  // 更新排班员工列表为可用员工
  scheduleEmployeeList.value = availableEmployees
}

const handleSave = async () => {
  try {
    await assignmentFormRef.value.validate()
    dialogLoading.value = true
    
    // 检查是否选择了值班人员
    if (!assignmentForm.employeeIds || assignmentForm.employeeIds.length === 0) {
      ElMessage.warning('请选择值班人员')
      dialogLoading.value = false
      return
    }
    
    let successCount = 0
    
    if (assignmentForm.id) {
      // 编辑模式：只更新当前记录（只能编辑一个值班人员）
      const singleAssignment = {
        id: assignmentForm.id,
        scheduleId: assignmentForm.scheduleId,
        dutyDate: assignmentForm.dutyDate,
        dutyShift: assignmentForm.dutyShift,
        employeeId: assignmentForm.employeeIds[0], // 只使用第一个选择的员工ID
        status: assignmentForm.status,
        remark: assignmentForm.remark
      }
      const response = await updateAssignment(singleAssignment)
      if (response.code === 200) {
        successCount++
      }
    } else {
      // 添加模式：为每个员工创建新记录
      for (const employeeId of assignmentForm.employeeIds) {
        // 创建单个排班记录
        const singleAssignment = {
          id: null,
          scheduleId: assignmentForm.scheduleId,
          dutyDate: assignmentForm.dutyDate,
          dutyShift: assignmentForm.dutyShift,
          employeeId: employeeId, // 使用单个employeeId
          status: assignmentForm.status,
          remark: assignmentForm.remark
        }
        
        const response = await addAssignment(singleAssignment)
        if (response.code === 200) {
          successCount++
        }
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(assignmentForm.id ? '编辑值班安排成功' : `添加值班安排成功，共添加 ${successCount} 条记录`)
      dialogVisible.value = false
      // 恢复原始的排班员工列表
      await fetchScheduleEmployees(assignmentForm.scheduleId)
      fetchAssignmentList(assignmentForm.scheduleId)
    } else {
      ElMessage.error(assignmentForm.id ? '编辑值班安排失败' : '添加值班安排失败')
    }
  } catch (error) {
    // console.error('保存值班安排失败:', error)
    ElMessage.error('保存值班安排失败')
  } finally {
    dialogLoading.value = false
  }
}

const openExportDialog = () => {
  // 默认设置为当前月份的开始和结束日期
  exportForm.dateRange = [
    dayjs(currentDate.value).startOf('month').format('YYYY-MM-DD'),
    dayjs(currentDate.value).endOf('month').format('YYYY-MM-DD')
  ]
  exportDialogVisible.value = true
}

const handleExport = async () => {
  try {
    await exportFormRef.value.validate()
    exportDialogLoading.value = true
    
    if (!selectedScheduleId.value) {
      ElMessage.error('请先选择值班表')
      return
    }
    
    // 获取当前值班表的名称
    const currentSchedule = scheduleList.value.find(s => s.id === selectedScheduleId.value)
    const scheduleName = currentSchedule ? currentSchedule.scheduleName : '值班安排'
    
    // 获取用户选择的时间范围
    const [startDate, endDate] = exportForm.dateRange
    
    // 准备导出数据
    const exportData = assignmentList.value
      .filter(item => {
        return item.scheduleId === selectedScheduleId.value && 
               item.dutyDate >= startDate && 
               item.dutyDate <= endDate
      })
      .map(item => {
        return {
          '值班日期': item.dutyDate,
          '班次': getShiftName(item.dutyShift),
          '值班人员': getEmployeeName(item.employeeId),
          '状态': item.status === 1 ? '有效' : '无效',
          '备注': item.remark || ''
        }
      })
    
    // 创建工作表
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '值班安排')
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    const dataBlob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    
    // 保存文件
    saveAs(dataBlob, `${scheduleName}_${startDate}_${endDate}.xlsx`)
    
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch (error) {
    // console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportDialogLoading.value = false
  }
}

const openEditDetailDialog = (row) => {
  // 填充编辑表单数据
  Object.assign(editDetailForm, {
    id: row.id,
    employeeName: row.employeeName,
    shiftName: row.shiftName,
    status: row.status,
    remark: row.remark || ''
  })
  editDetailDialogVisible.value = true
}

const handleSaveDetail = async () => {
  try {
    editDetailDialogLoading.value = true
    
    // 确保status是数字类型
    const statusValue = Number(editDetailForm.status)
    
    // 准备更新数据
    const updateData = {
      id: editDetailForm.id,
      status: statusValue,
      remark: editDetailForm.remark
    }
    
    // 调用更新接口
    const response = await updateAssignment(updateData)
    
    if (response.code === 200) {
      // 更新本地数据
      const index = assignmentList.value.findIndex(item => item.id === editDetailForm.id)
      if (index !== -1) {
        assignmentList.value[index].status = statusValue
        assignmentList.value[index].remark = editDetailForm.remark
      }
      
      // 更新详情弹窗中的数据
      const detailIndex = selectedDateAssignments.value.findIndex(item => item.id === editDetailForm.id)
      if (detailIndex !== -1) {
        // 创建新对象以确保Vue能够检测到变化
        const updatedItem = {
          ...selectedDateAssignments.value[detailIndex],
          status: statusValue,
          remark: editDetailForm.remark
        }
        // 替换整个数组项
        selectedDateAssignments.value.splice(detailIndex, 1, updatedItem)
      }
      
      editDetailDialogVisible.value = false
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    // console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    editDetailDialogLoading.value = false
  }
}

const openBatchDialog = () => {
  Object.assign(batchForm, {
    dateRange: null,
    scheduleType: 1,
    dutyShift: null,
    employeeIds: [],
    remark: '',
    scheduleModeId: null,
    dateType: ['workday'] // 默认选择工作日
  })
  batchDialogVisible.value = true
}

// 处理人员选择变化
const handleEmployeeSelectChange = (values) => {
  if (values.includes('all')) {
    // 如果包含全选选项，选择所有值班人员，不包含'all'本身
    batchForm.employeeIds = scheduleEmployeeList.value.map(emp => emp.id)
  }
}

// 获取符合条件的日期列表
const getFilteredDates = async (startDate, endDate, dateTypes) => {
  try {
    // 获取日期范围内的所有日期
    const allDates = getDatesInRange(startDate, endDate)
    // 获取日期范围内的节假日信息
    await fetchHolidaysList(startDate, endDate)
    
    const filteredDates = []
    for (const date of allDates) {
      const dateObj = dayjs(date)
      const dayOfWeek = dateObj.day()
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
      const holidayInfo = holidayMap.value[date]
      const isHoliday = holidayInfo && holidayInfo.isWorkday === 0
      const isWorkdayHoliday = holidayInfo && holidayInfo.isWorkday === 1
      
      // 判断日期是否符合选择的日期类型
      let shouldInclude = false
      
      if (dateTypes.includes('workday')) {
        // 工作日包括调休和非周末非节假日
        if (isWorkdayHoliday || (!isWeekend && !isHoliday)) {
          shouldInclude = true
        }
      }
      
      if (dateTypes.includes('weekend')) {
        // 休息日包括周末且不是调休且不是节假日
        if (isWeekend && !isWorkdayHoliday && !isHoliday) {
          shouldInclude = true
        }
      }
      
      if (dateTypes.includes('holiday')) {
        // 节假日包括法定节假日且不是调休
        if (isHoliday) {
          shouldInclude = true
        }
      }
      
      if (shouldInclude) {
        filteredDates.push(date)
      }
    }
    
    return filteredDates
  } catch (error) {
    // console.error('筛选日期失败:', error)
    return getDatesInRange(startDate, endDate) // 失败时返回所有日期
  }
}

// 获取员工在指定日期范围内的请假信息
const fetchEmployeeLeaveInfo = async (employeeIds, startDate, endDate) => {
  try {
    if (!employeeIds || employeeIds.length === 0) {
      return {}
    }
    
    // 调用后端API获取请假信息
    const response = await getEmployeeLeaveInfoAPI(employeeIds, startDate, endDate)
    
    if (response.code === 200) {
      // 直接返回处理后的请假信息，不需要转换格式
      return response.data
    }
    
    return {}
  } catch (error) {
    // console.error('获取请假信息失败:', error)
    return {}
  }
}

// 检查员工在指定日期、值班表和班次是否请假
const isEmployeeOnLeave = (employeeId, date, scheduleId, dutyShift, leaveInfo) => {
  const employeeLeaves = leaveInfo[employeeId]
  if (!employeeLeaves) {
    return false
  }
  
  const dateLeaves = employeeLeaves[date]
  if (!dateLeaves) {
    return false
  }
  
  // 检查当前值班表的请假记录
  const scheduleIdStr = scheduleId.toString()
  const scheduleLeaves = dateLeaves[scheduleIdStr]
  if (!scheduleLeaves) {
    return false
  }
  
  // 检查当前班次是否在请假的班次列表中
  return scheduleLeaves.includes(dutyShift)
}

// 获取所有请假人员的顶岗信息
const fetchAllSubstituteInfo = async (employeeIds, startDate, endDate) => {
  try {
    if (!employeeIds || employeeIds.length === 0) {
      return new Map()
    }
    
    // 调用后端API获取顶岗信息
    const response = await getSubstitutesByEmployees(employeeIds, startDate, endDate)
    
    if (response.code === 200) {
      const substitutes = response.data
      const substituteMap = new Map()
      
      // 构建顶岗信息映射，键格式为：employeeId_date_dutyShift
      substitutes.forEach(substitute => {
        const key = `${substitute.originalEmployeeId}_${substitute.dutyDate}_${substitute.shiftConfigId}`
        substituteMap.set(key, substitute.substituteEmployeeId)
      })
      
      return substituteMap
    }
  } catch (error) {
    // console.error('获取顶岗信息失败:', error)
  }
  return new Map()
}

const handleBatchSave = async () => {
  try {
    await batchFormRef.value.validate()
    batchDialogLoading.value = true
    
    // 检查是否选择了员工
    if (batchForm.employeeIds.length === 0) {
      ElMessage.warning('请选择值班人员')
      batchDialogLoading.value = false
      return
    }
    
    const [startDate, endDate] = batchForm.dateRange
    // 获取符合条件的日期列表
    const filteredDates = await getFilteredDates(startDate, endDate, batchForm.dateType)
    
    if (filteredDates.length === 0) {
      ElMessage.warning('所选日期范围内没有符合条件的日期')
      batchDialogLoading.value = false
      return
    }
    
    // 获取员工请假信息
    const leaveInfo = await fetchEmployeeLeaveInfo(batchForm.employeeIds, startDate, endDate)
    
    if (batchForm.scheduleType === 3) {
          // 使用排班模式生成排班
          const response = await generateScheduleByMode(
            selectedScheduleId.value,
            startDate,
            endDate,
            batchForm.scheduleModeId,
            {
              employeeIds: batchForm.employeeIds,
              remark: batchForm.remark,
              dateType: batchForm.dateType,
              filteredDates: filteredDates,
              leaveInfo: leaveInfo,
              scheduleId: selectedScheduleId.value
            }
          )
          
          if (response.code === 200) {
            // 保存生成的排班数据到数据库
            const assignments = response.data
            for (const assignment of assignments) {
              // 添加remark字段
              assignment.remark = batchForm.remark
              await addAssignment(assignment)
            }
            ElMessage.success(`批量排班成功，共添加 ${assignments.length} 条记录`)
          } else {
            ElMessage.error('批量排班失败: ' + (response.message || '未知错误'))
            return
          }
        } else {
          // 传统排班方式
          const assignments = []
          let rotationIndex = 0 // 维护全局的轮换索引
          
          // 首先获取所有请假人员的顶岗信息
          const substituteInfo = await fetchAllSubstituteInfo(batchForm.employeeIds, startDate, endDate)
          
          for (const date of filteredDates) {
            // 首先处理请假人员的顶岗替换
            let substituteMap = new Map()
            
            // 检查每个员工是否请假，如果请假且有顶岗人员，则记录顶岗关系
            for (const employeeId of batchForm.employeeIds) {
              if (isEmployeeOnLeave(employeeId, date, selectedScheduleId.value, batchForm.dutyShift, leaveInfo)) {
                // 查找该员工在当天该班次的顶岗人员
                const substituteKey = `${employeeId}_${date}_${batchForm.dutyShift}`
                const substituteEmployeeId = substituteInfo.get(substituteKey)
                if (substituteEmployeeId) {
                  substituteMap.set(employeeId, substituteEmployeeId)
                }
              }
            }
            
            // 过滤出当天没有请假的员工，加上顶岗人员
            const dayAvailableEmployees = []
            const usedSubstitutes = new Set()
            
            // 首先添加顶岗人员
            for (const [originalEmployeeId, substituteEmployeeId] of substituteMap.entries()) {
              if (!usedSubstitutes.has(substituteEmployeeId)) {
                dayAvailableEmployees.push(substituteEmployeeId)
                usedSubstitutes.add(substituteEmployeeId)
              }
            }
            
            // 然后添加其他没有请假的员工
            for (const employeeId of batchForm.employeeIds) {
              if (!isEmployeeOnLeave(employeeId, date, selectedScheduleId.value, batchForm.dutyShift, leaveInfo) && 
                  !usedSubstitutes.has(employeeId)) {
                dayAvailableEmployees.push(employeeId)
              }
            }
            
            if (dayAvailableEmployees.length === 0) {
              // 当天没有可用员工，跳过
              // console.log(`日期 ${date} 没有可用员工，跳过排班`)
              continue
            }
            
            if (batchForm.scheduleType === 1) {
              // 轮换排班模式
              // 保持原始轮换顺序，同时处理请假和顶岗人员的情况
              let selectedEmployeeIds = [];
              let tempRotationIndex = rotationIndex;
              
              // 需要选择的人数
              const needSelectCount = batchForm.shiftEmployeeCount;
              
              // 按原始员工列表的顺序查找合适的员工
              for (let i = 0; i < batchForm.employeeIds.length && selectedEmployeeIds.length < needSelectCount; i++) {
                // 计算当前轮换位置
                const currentIndex = (tempRotationIndex + i) % batchForm.employeeIds.length;
                const currentEmployeeId = batchForm.employeeIds[currentIndex];
                
                // 检查当前员工是否请假
                const isCurrentEmployeeOnLeave = isEmployeeOnLeave(
                  currentEmployeeId, 
                  date, 
                  selectedScheduleId.value, 
                  batchForm.dutyShift, 
                  leaveInfo
                );
                
                if (!isCurrentEmployeeOnLeave) {
                  // 当前员工未请假，可以直接使用
                  selectedEmployeeIds.push(currentEmployeeId);
                  tempRotationIndex = (currentIndex + 1) % batchForm.employeeIds.length;
                } else {
                  // 当前员工请假，检查是否有顶岗人员
                  const substituteKey = `${currentEmployeeId}_${date}_${batchForm.dutyShift}`;
                  const substituteEmployeeId = substituteInfo.get(substituteKey);
                  
                  if (substituteEmployeeId) {
                    // 有顶岗人员，使用顶岗人员
                    selectedEmployeeIds.push(substituteEmployeeId);
                    tempRotationIndex = (currentIndex + 1) % batchForm.employeeIds.length;
                  }
                }
              }
              
              // 如果找到了合适的员工，进行排班
              if (selectedEmployeeIds.length > 0) {
                // 更新全局轮换索引
                rotationIndex = tempRotationIndex;
                
                // 为每个选中的员工创建排班记录
                selectedEmployeeIds.forEach(employeeId => {
                  assignments.push({
                    scheduleId: selectedScheduleId.value,
                    dutyDate: date,
                    dutyShift: batchForm.dutyShift,
                    employeeId: employeeId,
                    status: 1,
                    remark: batchForm.remark
                  });
                });
              }
            } else {
              // 固定排班模式
              // 限制每个班次的人数
              const needSelectCount = batchForm.scheduleType === 1 ? batchForm.shiftEmployeeCount : dayAvailableEmployees.length;
              const selectedEmployees = dayAvailableEmployees.slice(0, needSelectCount);
              
              selectedEmployees.forEach(employeeId => {
                assignments.push({
                  scheduleId: selectedScheduleId.value,
                  dutyDate: date,
                  dutyShift: batchForm.dutyShift,
                  employeeId: employeeId,
                  status: 1,
                  remark: batchForm.remark
                })
              })
            }
          }
          
          if (assignments.length === 0) {
            ElMessage.warning('所选日期范围内没有可用员工进行排班')
            batchDialogLoading.value = false
            return
          }
          
          for (const assignment of assignments) {
            await addAssignment(assignment)
          }
          
          ElMessage.success(`批量排班成功，共添加 ${assignments.length} 条记录`)
        }
    
    batchDialogVisible.value = false
    // 刷新排班数据
    await fetchAssignmentList(selectedScheduleId.value)
  } catch (error) {
    // console.error('批量排班失败:', error)
    ElMessage.error('批量排班失败')
  } finally {
    batchDialogLoading.value = false
  }
}

const openClearDialog = () => {
  Object.assign(clearForm, {
    dateRange: null
  })
  clearDialogVisible.value = true
}

const handleClearSave = async () => {
  try {
    await clearFormRef.value.validate()
    
    await ElMessageBox.confirm(
      `确定要清空 ${clearForm.dateRange[0]} 至 ${clearForm.dateRange[1]} 的所有排班吗？此操作不可恢复！`,
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    clearDialogLoading.value = true
    const [startDate, endDate] = clearForm.dateRange
    
    await deleteBatchAssignments(selectedScheduleId.value, startDate, endDate)
    
    ElMessage.success('批量清空成功')
    clearDialogVisible.value = false
    fetchAssignmentList(selectedScheduleId.value)
  } catch (error) {
    if (error !== 'cancel') {
      // console.error('批量清空失败:', error)
      ElMessage.error('批量清空失败')
    }
  } finally {
    clearDialogLoading.value = false
  }
}

const getDatesInRange = (startDate, endDate) => {
  const dates = []
  let current = dayjs(startDate)
  const end = dayjs(endDate)
  
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    dates.push(current.format('YYYY-MM-DD'))
    current = current.add(1, 'day')
  }
  
  return dates
}

 /**
 * 检查用户是否在指定值班表中
 * @param {Array} employeeIds - 值班表员工ID列表
 * @returns {boolean} - 用户是否在值班表中
 */
const isUserInSchedule = (employeeIds) => {
  return employeeIds.includes(userStore.employeeId)
}

/**
 * 查找用户所在的值班表
 * @param {Array} schedules - 值班表列表
 * @returns {Object|null} - 用户所在的值班表或null
 */
const findUserSchedule = async (schedules) => {
  for (const schedule of schedules) {
    try {
      const response = await getScheduleEmployees(schedule.id)
      if (response.code === 200 && response.data) {
        const employeeIds = response.data
        if (isUserInSchedule(employeeIds)) {
          return schedule
        }
      }
    } catch (error) {
      // console.error(`获取值班表${schedule.id}员工失败:`, error)
    }
  }
  return null
}

onMounted(async () => {
  await fetchScheduleList()
  await fetchEmployeeList()
  await fetchDeptList()
  await fetchShiftConfigList()
  await fetchScheduleModeList()
  await updateHolidaysByMonth()
  
  // 默认选择值班表逻辑
  if (scheduleList.value.length > 0) {
    // 暂时简化处理，后续可根据用户角色进行判断
    // 先尝试查找用户所在的值班表
    const userSchedule = await findUserSchedule(scheduleList.value)
    if (userSchedule) {
      // 用户在某个值班表中，默认选择该值班表
      selectedScheduleId.value = userSchedule.id
      await handleScheduleChange(userSchedule.id)
    } else {
      // 用户不在任何值班表中，默认选择第一个值班表
      selectedScheduleId.value = scheduleList.value[0].id
      await handleScheduleChange(scheduleList.value[0].id)
    }
  }
})
</script>

<style scoped>
.assignment-container {
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

.content-card {
  margin-bottom: 10px;
}

.calendar-cell {
  height: 100%;
  padding: 5px;
  display: flex;
  flex-direction: column;
}

.date-number {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #606266;
}

.duty-list {
  flex: 1;
  overflow-y: auto;
}

/* 美化duty-list的滚动条样式 */
:deep(.duty-list) {
  scrollbar-width: thin;
  scrollbar-color: #e1e1e1 #f9f9f9;
}

:deep(.duty-list::-webkit-scrollbar) {
  width: 4px;
  height: 4px;
}

:deep(.duty-list::-webkit-scrollbar-track) {
  background: #f9f9f9;
  border-radius: 2px;
}

:deep(.duty-list::-webkit-scrollbar-thumb) {
  background: #e1e1e1;
  border-radius: 2px;
  transition: background 0.3s ease;
}

:deep(.duty-list::-webkit-scrollbar-thumb:hover) {
  background: #d1d1d1;
}

.duty-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 2px 0;
  transition: background-color 0.2s;
}

.duty-item.clickable {
  cursor: pointer;
}

.duty-item.clickable:hover {
  background-color: #f5f7fa;
}

.employee-name {
  font-size: 12px;
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 无效状态的人员样式 */
.invalid-employee {
  text-decoration: line-through;
  color: #909399;
}

.invalid-duty {
  opacity: 0.7;
}

.add-btn {
  margin-top: auto;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2px 0;
}

:deep(.el-calendar-table .el-calendar-day) {
  height: 120px;
}

:deep(.el-calendar-table td.is-selected .el-calendar-day) {
  background-color: #ecf5ff;
}

/* 节假日样式 */
.calendar-cell.regular-holiday {
  background-color: #fef0f0;
}

.calendar-cell.workday-holiday {
  background-color: #f0f9eb;
}

.holiday-info {
  font-size: 10px;
  color: #f56c6c;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.calendar-cell.workday-holiday .holiday-info {
  color: #67c23a;
}
</style>
