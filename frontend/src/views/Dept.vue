<template>
  <div class="dept-container">
    <div class="page-header">
      <h2>部门管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加部门
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="dept-tabs">
      <!-- 部门列表 -->
      <el-tab-pane label="部门列表" name="list">
        <el-card shadow="hover" class="content-card">
          <div class="table-toolbar">
            <el-input
              v-model="searchQuery"
              placeholder="请输入部门名称或编码"
              prefix-icon="Search"
              clearable
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <el-table
            v-loading="loading"
            :data="filteredDeptList"
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="deptName" label="部门名称" min-width="180" />
            <el-table-column prop="deptCode" label="部门编码" width="150" />
            <el-table-column prop="parentId" label="上级部门ID" width="120" />
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
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
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredDeptList.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 部门树 -->
      <el-tab-pane label="部门树" name="tree">
        <el-card shadow="hover" class="content-card">
          <el-tree
            :data="deptTreeData"
            :props="treeProps"
            :expand-on-click-node="false"
            :default-expand-all="true"
            node-key="id"
            ref="deptTree"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-node-actions">
                  <el-button type="text" size="small" @click="openEditDialog(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="text" size="small" @click="handleDelete(data.id)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑部门对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="deptFormRef"
        :model="deptForm"
        :rules="deptRules"
        label-position="top"
      >
        <el-form-item label="部门名称" prop="deptName">
          <el-input
            v-model="deptForm.deptName"
            placeholder="请输入部门名称"
          />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input
            v-model="deptForm.deptCode"
            placeholder="请输入部门编码"
          />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-select
            v-model="deptForm.parentId"
            placeholder="请选择上级部门"
          >
            <el-option label="无上级部门" value="0" />
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="deptForm.sort"
            :min="0"
            :max="100"
            placeholder="请输入排序"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="deptForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
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
  getDeptList,
  getDeptTree,
  addDept,
  updateDept,
  deleteDept
} from '../api/dept'

// 响应式数据
const activeTab = ref('list')
const searchQuery = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加部门')
const deptFormRef = ref()
const deptTree = ref()

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 表单数据
const deptForm = reactive({
  id: null,
  deptName: '',
  deptCode: '',
  parentId: 0,
  sort: 0,
  status: 1
})

// 表单验证规则
const deptRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  deptCode: [
    { required: true, message: '请输入部门编码', trigger: 'blur' },
    { min: 2, max: 20, message: '部门编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 部门数据
const deptList = ref([])
const deptTreeData = ref([])

// 树结构配置
const treeProps = {
  children: 'children',
  label: 'deptName'
}

// 过滤后的部门列表
const filteredDeptList = computed(() => {
  if (!searchQuery.value) {
    return deptList.value
  }
  return deptList.value.filter(dept => 
    dept.deptName.includes(searchQuery.value) || 
    dept.deptCode.includes(searchQuery.value)
  )
})

// 获取部门列表
const fetchDeptList = async () => {
  loading.value = true
  try {
    const response = await getDeptList()
    if (response.code === 200) {
      deptList.value = response.data
      // 构建树结构数据
      buildDeptTree(response.data)
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

// 构建部门树
const buildDeptTree = (data) => {
  // 这里简化处理，实际应该递归构建树结构
  deptTreeData.value = data
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
  dialogTitle.value = '添加部门'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (dept) => {
  Object.assign(deptForm, dept)
  dialogTitle.value = '编辑部门'
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (deptFormRef.value) {
    deptFormRef.value.resetFields()
  }
  Object.assign(deptForm, {
    id: null,
    deptName: '',
    deptCode: '',
    parentId: 0,
    sort: 0,
    status: 1
  })
}

// 保存部门
const handleSave = async () => {
  try {
    await deptFormRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (deptForm.id) {
      // 编辑部门
      response = await updateDept(deptForm)
    } else {
      // 添加部门
      response = await addDept(deptForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(deptForm.id ? '编辑部门成功' : '添加部门成功')
      dialogVisible.value = false
      fetchDeptList()
    } else {
      ElMessage.error(response.message || (deptForm.id ? '编辑部门失败' : '添加部门失败'))
    }
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('保存部门失败')
  } finally {
    dialogLoading.value = false
  }
}

// 删除部门
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该部门吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteDept(id)
    if (response.code === 200) {
      ElMessage.success('删除部门成功')
      fetchDeptList()
    } else {
      ElMessage.error(response.message || '删除部门失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      ElMessage.error('删除部门失败')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchDeptList()
})
</script>

<style scoped>
.dept-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.dept-tabs {
  margin-bottom: 20px;
}

.content-card {
  margin-bottom: 20px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.tree-node-actions {
  visibility: hidden;
}

.tree-node:hover .tree-node-actions {
  visibility: visible;
}
</style>