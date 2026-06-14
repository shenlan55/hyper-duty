<template>
  <div class="shift-config-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>班次配置</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加班次
          </el-button>
        </div>
      </template>
      
      <BaseTable
        v-loading="loading"
        :data="shiftConfigList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :backend-pagination="true"
        :show-search="true"
        :search-placeholder="'请输入班次名称或编码'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleTableSearch"
        @export="handleExport"
      >
        <template #shiftType="{ row }">
          <el-tag :type="getShiftTypeColor(row.shiftType)">
            {{ getShiftTypeName(row.shiftType) }}
          </el-tag>
        </template>
        <template #endTime="{ row }">
          {{ row.endTime }}<span v-if="row.isCrossDay" style="color: #F56C6C; margin-left: 5px;">(次日)</span>
        </template>
        <template #isOvertimeShift="{ row }">
          <el-tag :type="yesNoType(row.isOvertimeShift)">
            {{ yesNoLabel(row.isOvertimeShift) }}
          </el-tag>
        </template>
        <template #status="{ row }">
          <el-tag :type="commonStatusType(row.status)">
            {{ commonStatusLabel(row.status) }}
          </el-tag>
        </template>
        <template #operation="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ commonStatusLabel(row.status === 1 ? '0' : '1') }}
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班次名称" prop="shiftName">
              <el-input v-model="form.shiftName" placeholder="请输入班次名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班次编码" prop="shiftCode">
              <el-input v-model="form.shiftCode" placeholder="请输入班次编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班次类型" prop="shiftType">
              <el-select v-model="form.shiftType" placeholder="请选择班次类型" style="width: 100%">
                <el-option
                  v-for="opt in shiftTypeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="Number(opt.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否加班班次" prop="isOvertimeShift">
              <el-radio-group v-model="form.isOvertimeShift">
                <el-radio
                  v-for="opt in yesNoOptions"
                  :key="opt.value"
                  :value="Number(opt.value)"
                >{{ opt.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班时间" prop="startTime">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择上班时间"
                style="width: 100%"
                format="HH:mm:ss"
                value-format="HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班时间" prop="endTime">
              <el-time-picker
                v-model="form.endTime"
                placeholder="选择下班时间"
                style="width: 100%"
                format="HH:mm:ss"
                value-format="HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否跨天">
              <el-switch v-model="form.isCrossDay" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时长(小时)" prop="durationHours">
              <el-input-number
                v-model="form.durationHours"
                :min="0"
                :max="24"
                :step="0.5"
                placeholder="请输入时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              v-if="form.isOvertimeShift === 1"
              label="加班时长(小时)"
              prop="overtimeHours"
            >
              <el-input-number
                v-model="form.overtimeHours"
                :min="0"
                :max="24"
                :step="0.5"
                placeholder="日常加班时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.isOvertimeShift === 1" :gutter="20">
          <el-col :span="12">
            <el-form-item label="休息日加班时长(小时)" prop="weekendOvertimeHours">
              <el-input-number
                v-model="form.weekendOvertimeHours"
                :min="0"
                :max="24"
                :step="0.5"
                placeholder="留空则使用日常加班时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法定节假日加班时长(小时)" prop="holidayOvertimeHours">
              <el-input-number
                v-model="form.holidayOvertimeHours"
                :min="0"
                :max="24"
                :step="0.5"
                placeholder="留空则使用日常加班时长"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="休息日规则" prop="restDayRule">
          <el-input
            v-model="form.restDayRule"
            placeholder="请输入休息日规则"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="opt in commonStatusOptions"
              :key="opt.value"
              :value="Number(opt.value)"
            >{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="999"
            :step="1"
            placeholder="请输入排序数字"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="互斥班次">
          <el-select
            v-model="form.mutexShiftIds"
            multiple
            placeholder="请选择互斥班次"
            style="width: 100%"
          >
            <el-option
              v-for="shift in shiftConfigList"
              :key="shift.id"
              :label="shift.shiftName"
              :value="shift.id"
              :disabled="shift.id === form.id"
            />
          </el-select>
          <div class="el-form-item__help">互斥班次表示同一天不能同时应用的班次</div>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shiftConfigApi } from '../../api/duty/shiftConfig'
import BaseTable from '../../components/BaseTable.vue'
import { useSearchPagination } from '../../hooks/usePagination'
import { useDict } from '../../composables/useDict'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'
import { safeInput } from '../../utils/xssUtil'

const shiftApi = shiftConfigApi()

// 业务枚举：班次类型 / 状态 / 是否 走字典
const { options: shiftTypeOptions, labelOf: shiftTypeLabel, tagTypeOf: shiftTypeType, loadDict: loadShiftTypeDict } = useDict('shift_type')
loadShiftTypeDict()
const { options: commonStatusOptions, labelOf: commonStatusLabel, tagTypeOf: commonStatusType, loadDict: loadCommonStatusDict } = useDict('common_status')
loadCommonStatusDict()
const { options: yesNoOptions, labelOf: yesNoLabel, tagTypeOf: yesNoType, loadDict: loadYesNoDict } = useDict('yes_no')
loadYesNoDict()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加班次')
const formRef = ref()
const shiftConfigList = ref([])

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

const form = reactive({
  id: null,
  shiftName: '',
  shiftCode: '',
  shiftType: 1,
  startTime: '',
  endTime: '',
  isCrossDay: false,
  durationHours: 8,
  breakHours: 0,
  overtimeHours: 0,
  weekendOvertimeHours: null,
  holidayOvertimeHours: null,
  isOvertimeShift: 0,
  status: 1,
  sort: 0,
  remark: '',
  mutexShiftIds: []
})

const columns = [
  { prop: 'sort', label: '排序', width: '80' },
  { prop: 'shiftName', label: '班次名称', width: '150' },
  { prop: 'shiftCode', label: '班次编码', width: '120' },
  { prop: 'shiftType', label: '班次类型', width: '100' },
  { prop: 'startTime', label: '上班时间', width: '120' },
  { prop: 'endTime', label: '下班时间', width: '120' },
  { prop: 'durationHours', label: '时长(小时)', width: '120' },
  { prop: 'overtimeHours', label: '加班时长(小时)', width: '140' },
  { prop: 'weekendOvertimeHours', label: '休息日加班(小时)', width: '160' },
  { prop: 'holidayOvertimeHours', label: '节假日加班(小时)', width: '160' },
  { prop: 'isOvertimeShift', label: '是否加班班次', width: '120' },
  { prop: 'status', label: '状态', width: '100' },
  { prop: 'remark', label: '备注', minWidth: '200' },
  { type: 'operation', label: '操作', width: '200', fixed: 'right' }
]

const rules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' }
  ],
  shiftCode: [
    { required: true, message: '请输入班次编码', trigger: 'blur' }
  ],
  shiftType: [
    { required: true, message: '请选择班次类型', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择上班时间', trigger: 'blur' }
  ],
  endTime: [
    { required: true, message: '请选择下班时间', trigger: 'blur' }
  ],
  durationHours: [
    { required: true, message: '请输入时长', trigger: 'blur' }
  ]
}

const getShiftTypeName = (type) => {
  return shiftTypeLabel(type)
}

const getShiftTypeColor = (type) => {
  return shiftTypeType(type) || 'info'
}

const fetchShiftConfigList = async () => {
  loading.value = true
  try {
    const data = await shiftApi.getShiftConfigList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value
    })
    shiftConfigList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('获取班次配置列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '添加班次'
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  // 先深拷贝对象
  const formData = JSON.parse(JSON.stringify(row))
  // 将isCrossDay从数字类型转换为布尔类型
  formData.isCrossDay = formData.isCrossDay === 1
  // 确保mutexShiftIds存在
  if (!formData.mutexShiftIds) {
    formData.mutexShiftIds = []
  }
  Object.assign(form, formData)
  dialogTitle.value = '编辑班次'
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    shiftName: '',
    shiftCode: '',
    shiftType: 1,
    startTime: '',
    endTime: '',
    isCrossDay: false,
    durationHours: 8,
    breakHours: 0,
    overtimeHours: 0,
    weekendOvertimeHours: null,
    holidayOvertimeHours: null,
    isOvertimeShift: 0,
    status: 1,
    sort: 0,
    remark: '',
    mutexShiftIds: []
  })
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    dialogLoading.value = true
    
    // 添加XSS防护
    const safeFormData = {
      ...form,
      shiftName: safeInput(form.shiftName),
      shiftCode: safeInput(form.shiftCode),
      restDayRule: safeInput(form.restDayRule),
      remark: safeInput(form.remark),
      isCrossDay: form.isCrossDay ? 1 : 0
    }
    
    if (form.id) {
      await shiftApi.updateShiftConfig(safeFormData)
    } else {
      await shiftApi.addShiftConfig(safeFormData)
    }
    
    ElMessage.success(form.id ? '编辑班次成功' : '添加班次成功')
    dialogVisible.value = false
    fetchShiftConfigList()
  } catch (error) {
    ElMessage.error('保存班次配置失败')
  } finally {
    dialogLoading.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await shiftApi.updateShiftConfigStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchShiftConfigList()
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该班次配置吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await shiftApi.deleteShiftConfig(id)
    ElMessage.success('删除班次配置成功')
    fetchShiftConfigList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除班次配置失败')
    }
  }
}

// 分页处理
const handleSizeChange = (size) => {
  originalHandleSizeChange(size)
  fetchShiftConfigList()
}

const handleCurrentChange = (current) => {
  originalHandleCurrentChange(current)
  fetchShiftConfigList()
}

// 搜索处理
const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  handleSearch(keyword)
  fetchShiftConfigList()
}

// 导出处理
const handleExport = () => {
  try {
    // 准备导出数据
    const exportData = shiftConfigList.value.map(item => ({
      'ID': item.id,
      '班次名称': item.shiftName,
      '班次编码': item.shiftCode,
      '班次类型': getShiftTypeName(item.shiftType),
      '上班时间': item.startTime,
      '下班时间': item.endTime + (item.isCrossDay ? ' (次日)' : ''),
      '时长(小时)': item.durationHours,
      '加班时长(小时)': item.overtimeHours,
      '休息日加班时长(小时)': item.weekendOvertimeHours,
      '法定节假日加班时长(小时)': item.holidayOvertimeHours,
      '是否加班班次': yesNoLabel(item.isOvertimeShift),
      '状态': commonStatusLabel(item.status),
      '备注': item.remark
    }))
    
    // 创建工作表
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '班次配置')
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    const dataBlob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    
    // 保存文件
    saveAs(dataBlob, `班次配置_${new Date().toISOString().slice(0, 10)}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchShiftConfigList()
})
</script>

<style scoped>
.shift-config-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
