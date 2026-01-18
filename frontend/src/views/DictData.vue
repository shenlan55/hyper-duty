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
      <el-table
        v-loading="loading"
        :data="pagedDictDataList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dictLabel" label="字典标签" min-width="150" />
        <el-table-column prop="dictValue" label="字典键值" width="180" />
        <el-table-column prop="dictSort" label="排序" width="100" />
        <el-table-column prop="cssClass" label="样式属性" width="150" show-overflow-tooltip />
        <el-table-column prop="listClass" label="表格回显样式" width="150" show-overflow-tooltip />
        <el-table-column prop="isDefault" label="是否默认" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isDefault === 1 ? 'success' : 'info'">
              {{ scope.row.isDefault === 1 ? '是' : '否' }}
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
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
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

const pagedDictDataList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return dictDataList.value.slice(start, end)
})

const loadDictTypeList = async () => {
  try {
    const response = await listDictType({ pageNum: 1, pageSize: 1000 })
    if (response.code === 200) {
      dictTypeList.value = response.data.records
    }
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
    const response = await getDictDataByType(selectedDictTypeId.value)
    if (response.code === 200) {
      dictDataList.value = response.data
      total.value = response.data.length
    }
  } catch (error) {
    ElMessage.error('加载字典数据列表失败')
  } finally {
    loading.value = false
  }
}

const handleDictTypeChange = () => {
  currentPage.value = 1
  loadDictDataList()
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
        const response = isEdit ? await updateDictData(form) : await addDictData(form)
        if (response.code === 200) {
          ElMessage.success(isEdit ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadDictDataList()
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
    await ElMessageBox.confirm('确定要删除该字典数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await deleteDictData(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadDictDataList()
    }
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