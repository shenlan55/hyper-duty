<template>
  <div class="dict-type-container">
    <div class="page-header">
      <h2>字典类型管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加字典类型
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="请输入字典名称或编码"
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="pagedDictTypeList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dictName" label="字典名称" min-width="150" />
        <el-table-column prop="dictCode" label="字典编码" width="180" />
        <el-table-column prop="description" label="字典描述" min-width="200" show-overflow-tooltip />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleViewData(scope.row)">
              字典数据
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="form.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="form.dictCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入字典描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import {
  listDictType,
  addDictType,
  updateDictType,
  deleteDictType
} from '../api/dictType'
import { formatDateTime } from '../utils/dateUtils'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const searchQuery = ref('')
const dictTypeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formRef = ref(null)

const form = reactive({
  id: null,
  dictName: '',
  dictCode: '',
  description: '',
  status: 1
})

const rules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const pagedDictTypeList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return dictTypeList.value.slice(start, end)
})

const loadDictTypeList = async () => {
  loading.value = true
  try {
    const response = await listDictType({ pageNum: 1, pageSize: 1000 })
    if (response.code === 200) {
      dictTypeList.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载字典类型列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const openAddDialog = () => {
  dialogTitle.value = '添加字典类型'
  Object.assign(form, {
    id: null,
    dictName: '',
    dictCode: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑字典类型'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      dialogLoading.value = true
      try {
        const isEdit = form.id !== null
        const response = isEdit ? await updateDictType(form) : await addDictType(form)
        if (response.code === 200) {
          ElMessage.success(isEdit ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadDictTypeList()
        }
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        dialogLoading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该字典类型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await deleteDictType(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadDictTypeList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleViewData = (row) => {
  ElMessage.info('字典数据功能即将开放')
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  loadDictTypeList()
})
</script>

<style scoped>
.dict-type-container {
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
  font-size: 24px;
  font-weight: 500;
}

.content-card {
  margin-bottom: 20px;
}

.table-toolbar {
  display: flex;
  gap: 10px;
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
</style>