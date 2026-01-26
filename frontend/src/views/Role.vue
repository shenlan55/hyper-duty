<template>
  <div class="role-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">添加角色</el-button>
        </div>
      </template>
      
      <!-- 角色列表 -->
      <el-table :data="roleList" stripe style="width: 100%">
        <el-table-column prop="id" label="角色ID" width="80" align="center"></el-table-column>
        <el-table-column prop="roleName" label="角色名称" width="180"></el-table-column>
        <el-table-column prop="roleCode" label="角色编码" width="180"></el-table-column>
        <el-table-column prop="description" label="角色描述"></el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleMenuAuth(scope.row)">菜单授权</el-button>
            <el-button type="warning" size="small" @click="handleUserBind(scope.row)">用户绑定</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
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
import { getUserList } from '../api/user'
import { formatDateTime } from '../utils/dateUtils'

// 角色列表数据
const roleList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

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
  try {
    const response = await listRole({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (response.code === 200) {
      roleList.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error('获取角色列表失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败：' + error.message)
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
      const response = await deleteRole(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadRoleList()
      } else {
        ElMessage.error('删除失败：' + response.message)
      }
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
        let response
        if (form.id) {
          // 更新角色
          response = await updateRole(form)
        } else {
          // 添加角色
          response = await addRole(form)
        }
        if (response.code === 200) {
          ElMessage.success(form.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadRoleList()
        } else {
          ElMessage.error((form.id ? '更新' : '添加') + '失败：' + response.message)
        }
      } catch (error) {
        ElMessage.error((form.id ? '更新' : '添加') + '失败：' + error.message)
      }
    }
  })
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    const response = await updateRole(row)
    if (response.code !== 200) {
      ElMessage.error('更新状态失败：' + response.message)
      // 恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    ElMessage.error('更新状态失败：' + error.message)
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const response = await getMenuList()
    if (response.code === 200) {
      menuTree.value = response.data
    } else {
      ElMessage.error('获取菜单列表失败：' + response.message)
    }
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
    const response = await getRoleMenu(row.id)
    if (response.code === 200) {
      const menuIds = response.data
      // 设置默认勾选
      if (menuTreeRef.value && menuIds.length > 0) {
        menuTreeRef.value.setCheckedKeys(menuIds)
      }
    } else {
      ElMessage.error('获取角色菜单失败：' + response.message)
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
    
    const response = await saveRoleMenu(currentRoleId.value, checkedKeys)
    
    if (response.code === 200) {
      ElMessage.success('菜单授权成功')
      menuAuthVisible.value = false
    } else {
      ElMessage.error('菜单授权失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('菜单授权失败：' + error.message)
  }
}

// 远程搜索用户
const remoteUserSearch = (query) => {
  if (userList.value.length === 0) {
    userLoading.value = true
    getUserList().then(response => {
      if (response.code === 200) {
        userList.value = response.data
      } else {
        ElMessage.error('获取用户列表失败：' + response.message)
      }
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
      const userResponse = await getUserList()
      if (userResponse.code === 200) {
        userList.value = userResponse.data
      } else {
        ElMessage.error('获取用户列表失败：' + userResponse.message)
      }
    } catch (error) {
      ElMessage.error('获取用户列表失败：' + error.message)
    } finally {
      userLoading.value = false
    }
  }
  
  // 获取角色已有用户
  try {
    const response = await getRoleUser(row.id)
    if (response.code === 200) {
      selectedUserIds.value = response.data
    } else {
      ElMessage.error('获取角色用户失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('获取角色用户失败：' + error.message)
  }
}

// 提交用户绑定
const handleUserBindSubmit = async () => {
  if (!currentRoleId.value) return
  
  try {
    const response = await saveRoleUser(currentRoleId.value, selectedUserIds.value)
    
    if (response.code === 200) {
      ElMessage.success('用户绑定成功')
      userBindVisible.value = false
    } else {
      ElMessage.error('用户绑定失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('用户绑定失败：' + error.message)
  }
}

// 分页变更
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRoleList()
}

// 页面挂载时加载角色列表
onMounted(() => {
  loadRoleList()
})
</script>

<style scoped>
.role-container {
  padding: 20px;
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