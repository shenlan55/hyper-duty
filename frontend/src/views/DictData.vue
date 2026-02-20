<template>
  <div class="dict-data-container">
    <div class="page-header">
      <h2>字典数据管理</h2>
      <div class="header-actions">
        <el-select
          v-model="selectedDictTypeId"
          placeholder="请选择字典类型"
          clearable
          @change="handleDictTypeChange"
          style="width: 250px; margin-right: 10px"
        >
          <el-option
            v-for="dictType in dictTypeList"
            :key="dictType.id"
            :label="dictType.dictName"
            :value="dictType.id"
          />
        </el-select>
        <el-button type="primary" @click="openAddDialog" :disabled="!selectedDictTypeId">
          <el-icon><Plus /></el-icon>
          添加字典数据
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="content-card">
      <BaseTable
        v-loading="loading"
        :data="dictDataList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :show-search="true"
        :search-placeholder="'请输入字典标签或键值'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleSearch"
        @export="handleExport"
      >
        <template #isDefault="{ row }">
          <el-tag :type="row.isDefault === 1 ? 'success' : 'info'">
            {{ row.isDefault === 1 ? '是' : '否' }}
          </el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="form.cssClass" placeholder="请输入样式属性" />
        </el-form-item>
        <el-form-item label="表格回显样式" prop="listClass">
          <el-input v-model="form.listClass" placeholder="请输入表格回显样式" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  listDictData,
  getDictDataByType,
  addDictData,
  updateDictData,
  deleteDictData
} from '../api/dictData'
import { listDictType } from '../api/dictType'
import { formatDateTime } from '../utils/dateUtils'
import BaseTable from '../components/BaseTable.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('')
const selectedDictTypeId = ref('')
const dictTypeList = ref([])
const dictDataList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const formRef = ref(null)

const form = reactive({
  id: null,
  dictTypeId: null,
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  cssClass: '',
  listClass: '',
  isDefault: 0,
  status: 1,
  remark: ''
})

const rules = {
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入字典键值', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  dictSort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 表格列配置
const columns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'dictLabel', label: '字典标签', minWidth: '150' },
  { prop: 'dictValue', label: '字典键值', width: '180' },
  { prop: 'dictSort', label: '排序', width: '100' },
  { prop: 'cssClass', label: '样式属性', width: '150' },
  { prop: 'listClass', label: '表格回显样式', width: '150' },
  { prop: 'isDefault', label: '是否默认', width: '100', slotName: 'isDefault' },
  { prop: 'status', label: '状态', width: '100', slotName: 'status' },
  { prop: 'remark', label: '备注', minWidth: '200' },
  { prop: 'createTime', label: '创建时间', width: '180', slotName: 'createTime' },
  { label: '操作', width: '150', fixed: 'right', slotName: 'operation' }
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
  try {
    const data = await listDictType({ pageNum: 1, pageSize: 1000 })
    dictTypeList.value = data.records || []
  } catch (error) {
    ElMessage.error('加载字典类型列表失败')
  }
}

const loadDictDataList = async () => {
  if (!selectedDictTypeId.value) {
    dictDataList.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const data = await getDictDataByType(selectedDictTypeId.value)
    let filteredData = data || []
    
    // 根据搜索查询过滤数据
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredData = filteredData.filter(item => 
        item.dictLabel.toLowerCase().includes(query) || 
        item.dictValue.toLowerCase().includes(query)
      )
    }
    
    dictDataList.value = filteredData
    total.value = filteredData.length
  } catch (error) {
    ElMessage.error('加载字典数据列表失败')
  } finally {
    loading.value = false
  }
}

const handleDictTypeChange = () => {
  currentPage.value = 1
  searchQuery.value = ''
  loadDictDataList()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleSearch = (query) => {
  searchQuery.value = query
  currentPage.value = 1
}

const handleExport = () => {
  // 导出逻辑
  const exportData = dictDataList.value
  const headers = ['ID', '字典标签', '字典键值', '排序', '样式属性', '表格回显样式', '是否默认', '状态', '备注', '创建时间']
  const rows = exportData.map(item => [
    item.id,
    item.dictLabel,
    item.dictValue,
    item.dictSort,
    item.cssClass,
    item.listClass,
    item.isDefault === 1 ? '是' : '否',
    item.status === 1 ? '启用' : '禁用',
    item.remark || '',
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
  link.setAttribute('download', `字典数据_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const openAddDialog = () => {
  dialogTitle.value = '添加字典数据'
  Object.assign(form, {
    id: null,
    dictTypeId: selectedDictTypeId.value,
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    cssClass: '',
    listClass: '',
    isDefault: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑字典数据'
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
          await updateDictData(form)
        } else {
          await addDictData(form)
        }
        ElMessage.success(isEdit ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadDictDataList()
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
    await ElMessageBox.confirm('确定要删除该字典数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDictData(id)
    ElMessage.success('删除成功')
    loadDictDataList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  loadDictTypeList()
})
</script>

<style scoped>
.dict-data-container {
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

.header-actions {
  display: flex;
  align-items: center;
}

.content-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>