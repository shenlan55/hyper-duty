<template>
  <div class="shift-config-container">
    <div class="page-header">
      <h2>班次配置</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加班次
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <el-table
        v-loading="loading"
        :data="shiftConfigList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="shiftName" label="班次名称" width="150" />
        <el-table-column prop="shiftCode" label="班次编码" width="120" />
        <el-table-column prop="shiftType" label="班次类型" width="100">
          <template #default="scope">
            <el-tag :type="getShiftTypeColor(scope.row.shiftType)">
              {{ getShiftTypeName(scope.row.shiftType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="上班时间" width="120" />
        <el-table-column prop="endTime" label="下班时间" width="120">
          <template #default="scope">
            {{ scope.row.endTime }}<span v-if="scope.row.isCrossDay" style="color: #F56C6C; margin-left: 5px;">(次日)</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationHours" label="时长(小时)" width="120" />
        <el-table-column prop="breakHours" label="休息时长(小时)" width="140" />
        <el-table-column prop="isOvertimeShift" label="是否加班班次" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.isOvertimeShift === 1 ? 'warning' : 'info'">
              {{ scope.row.isOvertimeShift === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
                <el-option label="白班" :value="0" />
                <el-option label="早班" :value="1" />
                <el-option label="中班" :value="2" />
                <el-option label="晚班" :value="3" />
                <el-option label="全天" :value="4" />
                <el-option label="夜班" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否加班班次" prop="isOvertimeShift">
              <el-radio-group v-model="form.isOvertimeShift">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
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
            <el-form-item label="休息时长(小时)" prop="breakHours">
              <el-input-number
                v-model="form.breakHours"
                :min="0"
                :max="8"
                :step="0.5"
                placeholder="请输入休息时长"
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
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shiftConfigApi } from '../../api/duty/shiftConfig'

const shiftApi = shiftConfigApi()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加班次')
const formRef = ref()
const shiftConfigList = ref([])

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
  isOvertimeShift: 0,
  status: 1,
  remark: '',
  mutexShiftIds: []
})

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

const shiftTypeMap = {
  0: '白班',
  1: '早班',
  2: '中班',
  3: '晚班',
  4: '全天',
  5: '夜班'
}

const shiftTypeColorMap = {
  0: 'primary',
  1: 'success',
  2: 'info',
  3: 'warning',
  4: 'primary',
  5: 'danger'
}

const getShiftTypeName = (type) => {
  return shiftTypeMap[type] || '未知'
}

const getShiftTypeColor = (type) => {
  return shiftTypeColorMap[type] || 'info'
}

const fetchShiftConfigList = async () => {
  loading.value = true
  try {
    const response = await shiftApi.getShiftConfigList()
    if (response.code === 200) {
      shiftConfigList.value = response.data
    }
  } catch (error) {
    console.error('获取班次配置列表失败:', error)
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
    isOvertimeShift: 0,
    status: 1,
    remark: '',
    mutexShiftIds: []
  })
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    dialogLoading.value = true
    
    const formData = {
      ...form,
      isCrossDay: form.isCrossDay ? 1 : 0
    }
    
    let response
    if (form.id) {
      response = await shiftApi.updateShiftConfig(formData)
    } else {
      response = await shiftApi.addShiftConfig(formData)
    }
    
    if (response.code === 200) {
      ElMessage.success(form.id ? '编辑班次成功' : '添加班次成功')
      dialogVisible.value = false
      fetchShiftConfigList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('保存班次配置失败:', error)
    ElMessage.error('保存班次配置失败')
  } finally {
    dialogLoading.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const response = await shiftApi.updateShiftConfigStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success('状态更新成功')
      fetchShiftConfigList()
    } else {
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    console.error('更新状态失败:', error)
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
    
    const response = await shiftApi.deleteShiftConfig(id)
    if (response.code === 200) {
      ElMessage.success('删除班次配置成功')
      fetchShiftConfigList()
    } else {
      ElMessage.error(response.message || '删除班次配置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除班次配置失败:', error)
      ElMessage.error('删除班次配置失败')
    }
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
