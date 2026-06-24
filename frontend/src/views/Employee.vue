<template>
  <div class="employee-container">
    <!-- 左右布局容器 -->
    <div class="employee-layout">
      <!-- 左侧部门树 -->
      <div class="dept-tree-panel">
        <el-card shadow="hover" class="tree-card">
          <template #header>
            <div class="tree-header">
              <span>部门列表</span>
              <el-button link type="primary" @click="handleRefreshDeptTree">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>

          <el-input
            v-model="deptSearchText"
            placeholder="搜索部门"
            clearable
            class="dept-search-input"
            :prefix-icon="Search"
          />

          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :props="deptTreeProps"
            :expand-on-click-node="false"
            :filter-node-method="filterDeptNode"
            :default-expand-all="true"
            :highlight-current="true"
            node-key="id"
            class="dept-tree"
            @node-click="handleDeptNodeClick"
          >
            <template #default="{ node, data }">
              <div class="custom-tree-node">
                <span class="node-label">{{ node.label }}</span>
                <el-badge
                  v-if="getDeptEmployeeCount(data.id) > 0"
                  :value="getDeptEmployeeCount(data.id)"
                  class="employee-count-badge"
                  type="info"
                />
              </div>
            </template>
          </el-tree>
        </el-card>
      </div>

      <!-- 右侧人员列表 -->
      <div class="employee-list-panel">
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
            :data="employeeList"
            :columns="columns"
            :show-pagination="true"
            :pagination="pagination"
            :backend-pagination="true"
            :show-search="true"
            :search-placeholder="'请输入人员姓名或编码'"
            :show-export="true"
            :show-column-control="true"
            :show-skeleton="true"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            @search="handleTableSearch"
            @export="handleExport"
          >
            <template #deptId="{ row }">
              {{ getDeptName(row.deptId) }}
            </template>
            <template #gender="{ row }">
              <el-tag
                :class="row.gender === 1 ? 'gender-tag-male' : row.gender === 2 ? 'gender-tag-female' : 'gender-tag-unknown'"
                size="small"
              >
                {{ genderLabel(row.gender) }}
              </el-tag>
            </template>
            <template #dictTypeId="{ row }">
              {{ getDictTypeName(row.dictTypeId) }}
            </template>
            <template #dictDataId="{ row }">
              {{ getDictDataLabel(row.dictDataId, row.dictTypeId) }}
            </template>
            <template #status="{ row }">
              <el-tag :type="commonStatusType(row.status)">
                {{ commonStatusLabel(row.status) }}
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
      </div>
    </div>

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
                <el-option
                  v-for="opt in genderOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="Number(opt.value)"
                />
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="employeeForm.sort"
                placeholder="请输入排序值"
                style="width: 100%"
                :min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="employeeForm.status">
            <el-radio
              v-for="opt in commonStatusOptions"
              :key="opt.value"
              :value="Number(opt.value)"
            >{{ opt.label }}</el-radio>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEmployeeList,
  addEmployee,
  updateEmployee,
  deleteEmployee
} from '../api/employee'
import { getDeptList, getDeptTree } from '../api/dept'
import { listDictType } from '../api/dictType'
import { getDictDataByType } from '../api/dictData'
import { formatDateTime } from '../utils/dateUtils'
import { safeInput } from '../utils/xssUtil'
import { useSearchPagination } from '../hooks/usePagination'
import BaseTable from '../components/BaseTable.vue'
import { useDict } from '../composables/useDict'

// 业务枚举：状态 / 性别 走字典
const { options: commonStatusOptions, labelOf: commonStatusLabel, tagTypeOf: commonStatusType, loadDict: loadCommonStatusDict } = useDict('common_status')
loadCommonStatusDict()
const { options: genderOptions, labelOf: genderLabel, loadDict: loadGenderDict } = useDict('gender')
loadGenderDict()

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加人员')
const employeeFormRef = ref()

// 部门树相关
const deptTreeRef = ref()
const deptTreeData = ref([])
const deptTreeProps = {
  children: 'children',
  label: 'deptName'
}
const deptSearchText = ref('')
const selectedDeptId = ref(null) // 当前选中的部门ID，null表示全部
const deptEmployeeCountMap = ref({}) // 部门-人数映射

// 部门列表（用于表单下拉框和显示）
const deptList = ref([])

// 表格列配置
const columns = [
  { prop: 'sort', label: '排序', width: '100' },
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
const {
  currentPage,
  pageSize,
  total,
  pagination,
  handleCurrentChange: originalHandleCurrentChange,
  handleSizeChange: originalHandleSizeChange,
  searchQuery,
  handleSearch,
  resetSearch
} = useSearchPagination()

// 处理页码变化
const handleCurrentChange = (val) => {
  originalHandleCurrentChange(val)
  fetchEmployeeList()
}

// 处理每页大小变化
const handleSizeChange = (val) => {
  originalHandleSizeChange(val)
  fetchEmployeeList()
}

// 人员列表
const employeeList = ref([])

// 字典类型数据
const dictTypeList = ref([])

// 字典数据（按字典类型ID分组）
const dictDataMap = ref(new Map())

// 字典数据（用于表单下拉框）
const dictDataList = ref([])

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
  sort: 0,
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

// 过滤后的人员列表（保留用于兼容，但实际筛选已移至后端）
const filteredEmployeeList = computed(() => {
  return employeeList.value
})

// ==================== 部门树相关方法 ====================

/**
 * 获取部门树数据
 * 调用getDeptTree接口获取完整的部门树形结构
 */
const fetchDeptTreeData = async () => {
  try {
    const data = await getDeptTree()
    deptTreeData.value = data || []
    // 同时获取扁平化的部门列表（用于表单下拉框）
    await fetchDeptList()
  } catch (error) {
    console.error('获取部门树失败:', error)
    ElMessage.error('获取部门树失败')
  }
}

/**
 * 部门树节点点击事件
 * @param {Object} data - 点击的节点数据对象
 */
const handleDeptNodeClick = (data) => {
  selectedDeptId.value = data.id
  // 重置到第一页并刷新人员列表
  currentPage.value = 1
  fetchEmployeeList()
}

/**
 * 部门树过滤方法
 * 用于前端搜索过滤部门节点
 * @param {String} value - 搜索关键词
 * @param {Object} data - 节点数据
 * @returns {Boolean} 是否匹配
 */
const filterDeptNode = (value, data) => {
  if (!value) return true
  return data.deptName.toLowerCase().includes(value.toLowerCase())
}

/**
 * 监听部门搜索文本变化，实时过滤树节点
 */
watch(deptSearchText, (val) => {
  deptTreeRef.value?.filter(val)
})

/**
 * 获取指定部门的人员数量
 * @param {Number} deptId - 部门ID
 * @returns {Number} 该部门的人数
 */
const getDeptEmployeeCount = (deptId) => {
  return deptEmployeeCountMap.value[deptId] || 0
}

/**
 * 刷新部门树数据
 */
const handleRefreshDeptTree = async () => {
  await fetchDeptTreeData()
  // 重新加载人员数量统计
  await fetchDeptEmployeeCounts()
  ElMessage.success('部门树已刷新')
}

/**
 * 获取各部门的人员数量统计
 * 用于在部门树上显示每个部门的人数badge
 */
const fetchDeptEmployeeCounts = async () => {
  try {
    const data = await getEmployeeList(1, 99999, '', null)
    const allEmployees = data.records || []
    
    // 统计每个部门的人数
    const countMap = {}
    for (const emp of allEmployees) {
      if (emp.deptId) {
        countMap[emp.deptId] = (countMap[emp.deptId] || 0) + 1
      }
    }
    
    deptEmployeeCountMap.value = countMap
  } catch (error) {
    console.error('获取部门人员统计失败:', error)
  }
}



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
    // 使用选中的部门ID进行筛选（null表示全部部门）
    const deptId = selectedDeptId.value || null
    const data = await getEmployeeList(currentPage.value, pageSize.value, searchQuery.value, deptId)
    employeeList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0 // 更新pagination.total
  } catch (error) {
    console.error('获取人员列表失败:', error)
    ElMessage.error('获取人员列表失败')
  } finally {
    loading.value = false
  }
}

// 按部门筛选（已由handleDeptNodeClick替代，保留用于兼容）
const handleDeptFilter = () => {
  currentPage.value = 1
  fetchEmployeeList()
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
    sort: 0,
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
const handleTableSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  searchQuery.value = keyword
  currentPage.value = 1
  fetchEmployeeList()
}

// 导出人员列表
const handleExport = () => {
  // 导出逻辑
  const exportData = employeeList.value
  const headers = ['人员姓名', '人员编码', '用户名', '所属部门', '手机号码', '邮箱', '性别', '字典类型', '字典数据', '状态', '创建时间']
  const rows = exportData.map(row => [
    row.employeeName,
    row.employeeCode,
    row.username,
    getDeptName(row.deptId),
    row.phone,
    row.email,
    genderLabel(row.gender),
    getDictTypeName(row.dictTypeId),
    getDictDataLabel(row.dictDataId, row.dictTypeId),
    commonStatusLabel(row.status),
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
  // 1. 先加载部门树
  await fetchDeptTreeData()
  // 2. 加载部门人员统计（用于显示badge）
  await fetchDeptEmployeeCounts()
  // 3. 加载字典类型和字典数据
  await fetchDictTypeList()
  // 4. 加载人员列表（默认显示全部）
  await fetchEmployeeList()
})
</script>

<style scoped>
.employee-container {
  padding: 10px;
}

/* 左右布局容器 */
.employee-layout {
  display: flex;
  gap: 16px;
  align-items: stretch; /* 让左右两侧高度一致 */
}

/* 左侧部门树面板 */
.dept-tree-panel {
  width: 280px;
  min-width: 280px;
  flex-shrink: 0;
}

.tree-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-card :deep(.el-card__header) {
  flex-shrink: 0;
}

.tree-card :deep(.el-card__body) {
  padding: 12px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
}

.dept-search-input {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.dept-tree {
  flex: 1; /* 让树填充剩余空间 */
  overflow-y: auto;
}

/* 自定义树节点样式 */
.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.employee-count-badge {
  flex-shrink: 0;
  margin-left: 8px;
}

/* 右侧人员列表面板 */
.employee-list-panel {
  flex: 1;
  min-width: 0; /* 防止flex子项溢出 */
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

/* 响应式适配：小屏幕时隐藏左侧树或改为可折叠 */
@media (max-width: 1200px) {
  .dept-tree-panel {
    width: 240px;
    min-width: 240px;
  }
}

@media (max-width: 768px) {
  .employee-layout {
    flex-direction: column;
  }

  .dept-tree-panel {
    width: 100%;
    min-width: 100%;
    max-height: 300px;
  }

  .employee-list-panel {
    width: 100%;
  }
}
</style>