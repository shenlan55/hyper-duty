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
      <BaseTable
        v-loading="loading"
        :data="dictTypeList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :show-search="true"
        :search-placeholder="'请输入字典名称或编码'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleSearch"
        @export="handleExport"
      >
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
          <el-button type="info" size="small" @click="handleViewData(row)">
            字典数据
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </BaseTable>
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
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入字典描述" />
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
import BaseTable from '../components/BaseTable.vue'

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

// 表格列配置
const columns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'dictName', label: '字典名称', minWidth: '150' },
  { prop: 'dictCode', label: '字典编码', width: '180' },
  { prop: 'description', label: '字典描述', minWidth: '200' },
  { prop: 'status', label: '状态', width: '100', slotName: 'status' },
  { prop: 'createTime', label: '创建时间', width: '180', slotName: 'createTime' },
  { label: '操作', width: '200', fixed: 'right', slotName: 'operation' }
]

// 分页配置
const pagination = computed(() => {
  return {
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    pageSizes: [10, 20, 50, 100],
    total: total.value
  }
})

const loadDictTypeList = async () => {
  loading.value = true
  try {
    const data = await listDictType({ pageNum: 1, pageSize: 1000 })
    let filteredData = data.records || []
    
    // 根据搜索查询过滤数据
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredData = filteredData.filter(item => 
        item.dictName.toLowerCase().includes(query) || 
        item.dictCode.toLowerCase().includes(query)
      )
    }
    
    dictTypeList.value = filteredData
    total.value = filteredData.length
  } catch (error) {
    ElMessage.error('加载字典类型列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = (query) => {
  searchQuery.value = query
  currentPage.value = 1
  loadDictTypeList()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleExport = () => {
  // 导出逻辑
  const exportData = dictTypeList.value
  const headers = ['ID', '字典名称', '字典编码', '字典描述', '状态', '创建时间']
  const rows = exportData.map(item => [
    item.id,
    item.dictName,
    item.dictCode,
    item.description || '',
    item.status === 1 ? '启用' : '禁用',
    formatDateTime(item.createTime)
  ])
  
  // 这里可以使用xlsx库或其他方式导出，暂时使用简单的CSV导出
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `字典类型_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
        if (isEdit) {
          await updateDictType(form)
        } else {
          await addDictType(form)
        }
        ElMessage.success(isEdit ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadDictTypeList()
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
    await deleteDictType(id)
    ElMessage.success('删除成功')
    loadDictTypeList()
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
  padding: 10px;
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