<template>
  <div class="employee-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>人员管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加人员
          </el-button>
        </div>
      </template>
      
      <BaseTable
        v-loading="loading"
        :data="filteredEmployeeList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :show-search="true"
        :search-placeholder="'请输入人员姓名或编码'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleSearch"
        @export="handleExport"
      >
        <template #toolbar>
          <el-select
            v-model="deptFilter"
            placeholder="按部门筛选"
            clearable
            class="dept-filter"
            @change="handleDeptFilter"
            filterable
          >
            <el-option label="全部部门" value="" />
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </template>
        <template #deptId="{ row }">
          {{ getDeptName(row.deptId) }}
        </template>
        <template #gender="{ row }">
          <el-tag 
            :class="row.gender === 1 ? 'gender-tag-male' : row.gender === 2 ? 'gender-tag-female' : 'gender-tag-unknown'"
            size="small"
          >
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </el-tag>
        </template>
        <template #dictTypeId="{ row }">
          {{ getDictTypeName(row.dictTypeId) }}
        </template>
        <template #dictDataId="{ row }">
          {{ getDictDataLabel(row.dictDataId, row.dictTypeId) }}
        </template>
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #operation="{ row }">
          <el-button type="primary" size="small" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
    </BaseTable>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="employeeFormRef"
        :model="employeeForm"
        :rules="employeeRules"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人员姓名" prop="employeeName">
              <el-input
                v-model="employeeForm.employeeName"
                placeholder="请输入人员姓名"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人员编码" prop="employeeCode">
              <el-input
                v-model="employeeForm.employeeCode"
                placeholder="请输入人员编码"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="employeeForm.username"
                placeholder="请输入用户名"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" :required="!employeeForm.id" prop="password">
              <el-input
                v-model="employeeForm.password"
                type="password"
                placeholder="请输入密码"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-select
                v-model="employeeForm.deptId"
                placeholder="请选择所属部门"
                style="width: 100%"
              >
                <el-option
                  v-for="dept in deptList"
                  :key="dept.id"
                  :label="dept.deptName"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input
                v-model="employeeForm.phone"
                placeholder="请输入手机号码"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="employeeForm.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select
                v-model="employeeForm.gender"
                placeholder="请选择性别"
                style="width: 100%"
              >
                <el-option label="未知" :value="0" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="字典类型" prop="dictTypeId">
              <el-select
                v-model="employeeForm.dictTypeId"
                placeholder="请选择字典类型"
                style="width: 100%"
                clearable
                @change="handleDictTypeChange"
              >
                <el-option
                  v-for="dictType in dictTypeList"
                  :key="dictType.id"
                  :label="dictType.dictName"
                  :value="dictType.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字典数据" prop="dictDataId">
              <el-select
                v-model="employeeForm.dictDataId"
                placeholder="请选择字典数据"
                style="width: 100%"
                clearable
                :disabled="!employeeForm.dictTypeId"
              >
                <el-option
                  v-for="dictData in dictDataList"
                  :key="dictData.id"
                  :label="dictData.dictLabel"
                  :value="dictData.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="employeeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEmployeeList,
  addEmployee,
  updateEmployee,
  deleteEmployee
} from '../api/employee'
import { getDeptList } from '../api/dept'
import { listDictType } from '../api/dictType'
import { getDictDataByType } from '../api/dictData'
import { formatDateTime } from '../utils/dateUtils'
import { safeInput } from '../utils/xssUtil'
import BaseTable from '../components/BaseTable.vue'

// 响应式数据
const searchQuery = ref('')
const deptFilter = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加人员')
const employeeFormRef = ref()

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 表格列配置
const columns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'employeeName', label: '人员姓名', minWidth: '150' },
  { prop: 'employeeCode', label: '人员编码', width: '150' },
  { prop: 'username', label: '用户名', minWidth: '150' },
  { prop: 'deptId', label: '所属部门', minWidth: '180' },
  { prop: 'phone', label: '手机号码', width: '150' },
  { prop: 'email', label: '邮箱', minWidth: '200' },
  { prop: 'gender', label: '性别', width: '100' },
  { prop: 'dictTypeId', label: '字典类型', minWidth: '150' },
  { prop: 'dictDataId', label: '字典数据', minWidth: '150' },
  { prop: 'status', label: '状态', width: '100' },
  { prop: 'createTime', label: '创建时间', width: '180' },
  { type: 'operation', label: '操作', width: '180', fixed: 'right' }
]

// 分页配置
const pagination = computed(() => {
  return {
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    pageSizes: [10, 20, 50, 100],
    total: filteredEmployeeList.value.length
  }
})

// 部门数据
const deptList = ref([])

// 字典类型数据
const dictTypeList = ref([])

// 字典数据（按字典类型ID分组）
const dictDataMap = ref(new Map())

// 字典数据（用于表单下拉框）
const dictDataList = ref([])

// 人员数据
const employeeList = ref([])

// 表单数据
const employeeForm = reactive({
  id: null,
  employeeName: '',
  employeeCode: '',
  username: '',
  password: '',
  deptId: null,
  phone: '',
  email: '',
  gender: 0,
  dictTypeId: null,
  dictDataId: null,
  status: 1
})

// 表单验证规则
const employeeRules = {
  employeeName: [
    { required: true, message: '请输入人员姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '人员姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  employeeCode: [
    { required: true, message: '请输入人员编码', trigger: 'blur' },
    { min: 2, max: 20, message: '人员编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: (rule, value, callback) => {
        if (!employeeForm.id && !value) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      }, trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择所属部门', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 过滤后的人员列表
const filteredEmployeeList = computed(() => {
  let list = employeeList.value
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(emp => 
      emp.employeeName.toLowerCase().includes(query) || 
      emp.employeeCode.toLowerCase().includes(query) ||
      (emp.username && emp.username.toLowerCase().includes(query))
    )
  }
  
  // 按部门筛选
  if (deptFilter.value) {
    list = list.filter(emp => emp.deptId === deptFilter.value)
  }
  
  return list
})



// 获取部门名称
const getDeptName = (deptId) => {
  const dept = deptList.value.find(d => d.id === deptId)
  return dept ? dept.deptName : ''
}

// 获取字典类型名称
const getDictTypeName = (dictTypeId) => {
  const dictType = dictTypeList.value.find(d => d.id === dictTypeId)
  return dictType ? dictType.dictName : ''
}

// 获取字典数据标签
const getDictDataLabel = (dictDataId, dictTypeId) => {
  if (!dictDataId || !dictTypeId) return ''
  const dictDataList = dictDataMap.value.get(dictTypeId)
  if (!dictDataList) return ''
  const dictData = dictDataList.find(d => d.id === dictDataId)
  return dictData ? dictData.dictLabel : ''
}

// 获取部门列表
const fetchDeptList = async () => {
  try {
    const data = await getDeptList()
    deptList.value = data || []
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  }
}

// 获取字典类型列表
const fetchDictTypeList = async () => {
  try {
    const data = await listDictType({ pageNum: 1, pageSize: 1000 })
    dictTypeList.value = data.records || []
    
    // 预加载所有字典类型的字典数据
    for (const dictType of dictTypeList.value) {
      await fetchDictDataByType(dictType.id)
    }
  } catch (error) {
    console.error('获取字典类型列表失败:', error)
  }
}

// 获取字典数据（按字典类型）
const fetchDictDataByType = async (dictTypeId) => {
  try {
    const data = await getDictDataByType(dictTypeId)
    dictDataMap.value.set(dictTypeId, data || [])
  } catch (error) {
    console.error('获取字典数据失败:', error)
  }
}

// 获取字典数据（用于表单下拉框）
const fetchDictDataList = async (dictTypeId) => {
  try {
    const data = await getDictDataByType(dictTypeId)
    dictDataList.value = data || []
  } catch (error) {
    console.error('获取字典数据失败:', error)
  }
}

// 字典类型变化时加载对应的字典数据
const handleDictTypeChange = (dictTypeId) => {
  if (dictTypeId) {
    fetchDictDataList(dictTypeId)
  } else {
    dictDataList.value = []
    // 当字典类型被清除时，同时清空字典数据ID
    employeeForm.dictDataId = null
  }
}

// 获取人员列表
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const data = await getEmployeeList()
    employeeList.value = data || []
  } catch (error) {
    console.error('获取人员列表失败:', error)
    ElMessage.error('获取人员列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 按部门筛选
const handleDeptFilter = () => {
  currentPage.value = 1
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 打开添加对话框
const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '添加人员'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (employee) => {
  Object.assign(employeeForm, employee)
  // 清空密码字段，避免加载加密密码
  employeeForm.password = ''
  dialogTitle.value = '编辑人员'
  dialogVisible.value = true
  
  // 如果有字典类型，加载对应的字典数据
  if (employee.dictTypeId) {
    fetchDictDataList(employee.dictTypeId)
  }
}

// 重置表单
const resetForm = () => {
  if (employeeFormRef.value) {
    employeeFormRef.value.resetFields()
  }
  Object.assign(employeeForm, {
    id: null,
    employeeName: '',
    employeeCode: '',
    username: '',
    password: '',
    deptId: null,
    phone: '',
    email: '',
    gender: 0,
    dictTypeId: null,
    dictDataId: null,
    status: 1
  })
  dictDataList.value = []
}

// 保存人员
const handleSave = async () => {
  try {
    await employeeFormRef.value.validate()
    dialogLoading.value = true
    
    // 处理用户输入，防止XSS攻击
    const safeForm = {
      ...employeeForm,
      employeeName: safeInput(employeeForm.employeeName),
      employeeCode: safeInput(employeeForm.employeeCode),
      username: safeInput(employeeForm.username),
      phone: safeInput(employeeForm.phone),
      email: safeInput(employeeForm.email)
    }
    
    if (employeeForm.id) {
      // 编辑人员
      await updateEmployee(safeForm)
      ElMessage.success('编辑人员成功')
    } else {
      // 添加人员
      await addEmployee(safeForm)
      ElMessage.success('添加人员成功')
    }
    
    dialogVisible.value = false
    fetchEmployeeList()
  } catch (error) {
    console.error('保存人员失败:', error)
    ElMessage.error('保存人员失败')
  } finally {
    dialogLoading.value = false
  }
}

// 删除人员
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该人员吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteEmployee(id)
    ElMessage.success('删除人员成功')
    fetchEmployeeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除人员失败:', error)
      ElMessage.error('删除人员失败')
    }
  }
}

// 表格搜索
const handleTableSearch = (query) => {
  searchQuery.value = query
  currentPage.value = 1
}

// 导出人员列表
const handleExport = () => {
  // 导出逻辑
  const exportData = filteredEmployeeList.value
  const headers = ['人员姓名', '人员编码', '用户名', '所属部门', '手机号码', '邮箱', '性别', '字典类型', '字典数据', '状态', '创建时间']
  const rows = exportData.map(row => [
    row.employeeName,
    row.employeeCode,
    row.username,
    getDeptName(row.deptId),
    row.phone,
    row.email,
    row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知',
    getDictTypeName(row.dictTypeId),
    getDictDataLabel(row.dictDataId, row.dictTypeId),
    row.status === 1 ? '启用' : '禁用',
    formatDateTime(row.createTime)
  ])
  
  // CSV导出实现
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `人员列表_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 生命周期钩子
onMounted(async () => {
  await fetchDeptList()
  await fetchDictTypeList()
  await fetchEmployeeList()
})
</script>

<style scoped>
.employee-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.dept-filter {
  width: 200px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.gender-tag-male {
  background-color: #ECF5FF;
  color: #409EFF;
  border-color: #D9ECFF;
}

.gender-tag-female {
  background-color: #FEF0F0;
  color: #F56C6C;
  border-color: #FDE2E2;
}

.gender-tag-unknown {
  background-color: #F4F4F5;
  color: #909399;
  border-color: #E9E9EB;
 }
</style>