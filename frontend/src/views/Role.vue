<template>
  <div class="role-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加角色
          </el-button>
        </div>
      </template>
      
      <!-- 角色列表 -->
      <BaseTable
        v-loading="loading"
        :data="roleList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :show-search="true"
        :search-placeholder="'请输入角色名称或编码'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleSearch"
        @export="handleExport"
      >
        <template #status="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)"></el-switch>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #operation="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" size="small" @click="handleMenuAuth(row)">菜单授权</el-button>
          <el-button type="warning" size="small" @click="handleUserBind(row)">用户绑定</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>
    
    <!-- 角色表单对话框 -->
    <el-dialog v-model="dialogVisible" title="角色信息" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 菜单授权对话框 -->
    <el-dialog v-model="menuAuthVisible" title="角色菜单授权" width="600px">
      <div class="menu-auth-container">
        <el-tree
          :data="menuTree"
          show-checkbox
          node-key="id"
          :props="menuTreeProps"
          ref="menuTreeRef"
          default-expand-all
        ></el-tree>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="menuAuthVisible = false">取消</el-button>
          <el-button type="primary" @click="handleMenuAuthSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 用户绑定对话框 -->
    <el-dialog v-model="userBindVisible" title="角色用户绑定" width="600px">
      <div class="user-bind-container">
        <el-select
          v-model="selectedUserIds"
          multiple
          filterable
          remote
          reserve-keyword
          placeholder="请选择要绑定的用户"
          :remote-method="remoteUserSearch"
          :loading="userLoading"
          style="width: 100%"
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.username + ' (' + (user.employeeName || '未关联') + ')'"
            :value="user.id"
          ></el-option>
        </el-select>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userBindVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUserBindSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRole, addRole, updateRole, deleteRole, getRoleMenu, saveRoleMenu, getRoleUser, saveRoleUser } from '../api/role'
import { getMenuList } from '../api/menu'
import { getEmployeeList } from '../api/employee'
import { formatDateTime } from '../utils/dateUtils'
import { safeInput } from '../utils/xssUtil'
import BaseTable from '../components/BaseTable.vue'

// 角色列表数据
const roleList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 表格列配置
const columns = [
  { prop: 'id', label: '角色ID', width: '80', align: 'center' },
  { prop: 'roleName', label: '角色名称', width: '180' },
  { prop: 'roleCode', label: '角色编码', width: '180' },
  { prop: 'description', label: '角色描述' },
  {
    label: '状态',
    width: '100',
    align: 'center',
    slotName: 'status'
  },
  {
    label: '创建时间',
    width: '180',
    align: 'center',
    slotName: 'createTime'
  },
  {
    label: '操作',
    width: '320',
    align: 'center',
    slotName: 'operation'
  }
]

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 对话框状态
const dialogVisible = ref(false)
const formRef = ref(null)

// 菜单授权相关
const menuAuthVisible = ref(false)
const menuTreeRef = ref(null)
const menuTree = ref([])
const currentRoleId = ref(null)
const menuTreeProps = {
  label: 'menuName',
  children: 'children'
}

// 用户绑定相关
const userBindVisible = ref(false)
const userList = ref([])
const selectedUserIds = ref([])
const userLoading = ref(false)

// 加载角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    const data = await listRole({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    roleList.value = data.records || []
    total.value = data.total || 0
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('获取角色列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增角色
const handleAdd = () => {
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadRoleList()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 添加XSS防护
        const safeForm = {
          ...form,
          roleName: safeInput(form.roleName),
          roleCode: safeInput(form.roleCode),
          description: safeInput(form.description)
        }
        
        if (form.id) {
          // 更新角色
          await updateRole(safeForm)
        } else {
          // 添加角色
          await addRole(safeForm)
        }
        ElMessage.success(form.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadRoleList()
      } catch (error) {
        ElMessage.error((form.id ? '更新' : '添加') + '失败：' + error.message)
      }
    }
  })
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateRole(row)
  } catch (error) {
    ElMessage.error('更新状态失败：' + error.message)
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const data = await getMenuList()
    menuTree.value = data || []
  } catch (error) {
    ElMessage.error('获取菜单列表失败：' + error.message)
  }
}

// 处理菜单授权
const handleMenuAuth = async (row) => {
  currentRoleId.value = row.id
  menuAuthVisible.value = true
  
  // 加载菜单树
  await loadMenuTree()
  
  // 获取角色已有菜单
  try {
    const menuIds = await getRoleMenu(row.id)
    // 设置默认勾选
    if (menuTreeRef.value && menuIds.length > 0) {
      menuTreeRef.value.setCheckedKeys(menuIds)
    }
  } catch (error) {
    ElMessage.error('获取角色菜单失败：' + error.message)
  }
}

// 提交菜单授权
const handleMenuAuthSubmit = async () => {
  if (!menuTreeRef.value || !currentRoleId.value) return
  
  try {
    // 获取选中的菜单ID
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    
    await saveRoleMenu(currentRoleId.value, checkedKeys)
    ElMessage.success('菜单授权成功')
    menuAuthVisible.value = false
  } catch (error) {
    ElMessage.error('菜单授权失败：' + error.message)
  }
}

// 远程搜索用户
const remoteUserSearch = (query) => {
  if (userList.value.length === 0) {
    userLoading.value = true
    getEmployeeList(1, 1000, '').then(data => {
      // 过滤掉禁用的用户（status为0）和没有用户名的人员
      userList.value = data.records.filter(emp => emp.status === 1 && emp.username)
    }).catch(error => {
      ElMessage.error('获取用户列表失败：' + error.message)
    }).finally(() => {
      userLoading.value = false
    })
  }
}

// 处理用户绑定
const handleUserBind = async (row) => {
  currentRoleId.value = row.id
  userBindVisible.value = true
  selectedUserIds.value = []
  
  // 获取所有用户信息
  if (userList.value.length === 0) {
    try {
      userLoading.value = true
      const userData = await getEmployeeList(1, 1000, '')
      // 过滤掉禁用的用户（status为0）和没有用户名的人员
      userList.value = userData.records.filter(emp => emp.status === 1 && emp.username)
    } catch (error) {
      ElMessage.error('获取用户列表失败：' + error.message)
    } finally {
      userLoading.value = false
    }
  }
  
  // 获取角色已有用户
  try {
    const userIds = await getRoleUser(row.id)
    selectedUserIds.value = userIds
  } catch (error) {
    ElMessage.error('获取角色用户失败：' + error.message)
  }
}

// 提交用户绑定
const handleUserBindSubmit = async () => {
  if (!currentRoleId.value) return
  
  try {
    await saveRoleUser(currentRoleId.value, selectedUserIds.value)
    ElMessage.success('用户绑定成功')
    userBindVisible.value = false
  } catch (error) {
    ElMessage.error('用户绑定失败：' + error.message)
  }
}

// 分页变更
const handleCurrentChange = (page) => {
  currentPage.value = page
  pagination.currentPage = page
  loadRoleList()
}

// 分页大小变更
const handleSizeChange = (size) => {
  pageSize.value = size
  pagination.pageSize = size
  currentPage.value = 1
  pagination.currentPage = 1
  loadRoleList()
}

// 表格搜索
const handleSearch = (query) => {
  // 这里需要根据后端API支持的搜索参数进行调整
  // 目前我们假设后端API支持按角色名称和编码搜索
  currentPage.value = 1
  loadRoleList()
}

// 导出角色列表
const handleExport = () => {
  // 导出逻辑
  const exportData = roleList.value
  const headers = ['角色ID', '角色名称', '角色编码', '角色描述', '状态', '创建时间']
  const rows = exportData.map(row => [
    row.id,
    row.roleName,
    row.roleCode,
    row.description || '',
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
  link.setAttribute('download', `角色列表_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 页面挂载时加载角色列表
onMounted(() => {
  loadRoleList()
})
</script>

<style scoped>
.role-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>