<template>
  <div class="user-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="请输入用户名或人员姓名"
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="pagedUserList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="关联人员" min-width="150">
          <template #default="scope">
            {{ scope.row.employeeName || '未关联' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
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
          :total="filteredUserList.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="关联人员" prop="employeeId">
          <el-select
            v-model="userForm.employeeId"
            placeholder="请选择关联人员"
            style="width: 100%"
          >
            <el-option
              v-for="employee in employeeList"
              :key="employee.id"
              :label="employee.employeeName"
              :value="employee.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="dialogLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getEmployeeList } from '../api/employee'
import { getUserList, addUser, updateUser, deleteUser } from '../api/user'
import { formatDateTime } from '../utils/dateUtils'

// 搜索查询
const searchQuery = ref('')

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 加载状态
const loading = ref(false)
const dialogLoading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref(null)

// 人员列表（用于关联选择）
const employeeList = ref([])

// 用户数据
const userList = ref([])

// 表单数据
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  employeeId: null,
  status: 1
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: userForm.id === null, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ],
  employeeId: [
    { required: true, message: '请选择关联人员', trigger: 'blur' }
  ]
}

// 过滤后的用户列表
const filteredUserList = computed(() => {
  let list = userList.value
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(user => 
      user.username.toLowerCase().includes(query) || 
      (user.employeeName && user.employeeName.toLowerCase().includes(query))
    )
  }
  
  return list
})

// 分页后的用户列表
const pagedUserList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUserList.value.slice(start, end)
})

// 获取人员列表
const fetchEmployeeList = async () => {
  try {
    const response = await getEmployeeList()
    if (response.code === 200) {
      employeeList.value = response.data
    }
  } catch (error) {
    console.error('获取人员列表失败:', error)
    ElMessage.error('获取人员列表失败')
  }
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const response = await getUserList()
    if (response.code === 200) {
      userList.value = response.data
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
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
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (user) => {
  Object.assign(userForm, user)
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    employeeId: null,
    status: 1
  })
}

// 保存用户
const handleSave = async () => {
  try {
    await userFormRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (userForm.id) {
      // 编辑用户
      response = await updateUser(userForm)
    } else {
      // 添加用户
      response = await addUser(userForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(userForm.id ? '编辑用户成功' : '添加用户成功')
      dialogVisible.value = false
      fetchUserList()
    } else {
      ElMessage.error(response.message || (userForm.id ? '编辑用户失败' : '添加用户失败'))
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存用户失败')
  } finally {
    dialogLoading.value = false
  }
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteUser(id)
    if (response.code === 200) {
      ElMessage.success('删除用户成功')
      fetchUserList()
    } else {
      ElMessage.error(response.message || '删除用户失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 生命周期钩子
onMounted(async () => {
  await fetchEmployeeList()
  await fetchUserList()
})
</script>

<style scoped>
.user-container {
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

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>
