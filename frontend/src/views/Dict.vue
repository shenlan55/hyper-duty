<template>
  <div class="dict-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <el-button type="primary" @click="openTypeDialog(null)">
            <el-icon><Plus /></el-icon>
            添加字典类型
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="dict-type-panel">
            <div class="panel-header">
              <span>字典类型</span>
              <el-input
                v-model="typeSearchQuery"
                placeholder="搜索字典类型"
                prefix-icon="Search"
                clearable
                size="small"
                style="width: 150px"
              />
            </div>
            <div class="type-list">
              <div
                v-for="dictType in filteredDictTypes"
                :key="dictType.id"
                :class="['type-item', { active: selectedDictTypeId === dictType.id }]"
                @click="selectDictType(dictType)"
              >
                <div class="type-name">{{ dictType.dictName }}</div>
                <div class="type-code">{{ dictType.dictCode }}</div>
                <div class="type-actions">
                  <el-button
                    type="primary"
                    size="small"
                    link
                    @click.stop="openTypeDialog(dictType)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    link
                    @click.stop="handleDeleteType(dictType)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="18">
          <div class="dict-data-panel">
            <div class="panel-header">
              <span>字典数据</span>
              <el-button
                type="primary"
                size="small"
                :disabled="!selectedDictTypeId"
                @click="openDataDialog(null)"
              >
                <el-icon><Plus /></el-icon>
                添加字典数据
              </el-button>
            </div>
            <div v-if="!selectedDictTypeId" class="empty-state">
              <el-empty description="请选择左侧字典类型查看字典数据" />
            </div>
            <BaseTable
              v-else
              v-loading="dataLoading"
              :data="dictDataList"
              :columns="dataColumns"
              :show-pagination="false"
              :show-search="true"
              :search-placeholder="'请输入字典标签或键值'"
              :show-export="true"
              :show-column-control="true"
              :show-skeleton="true"
              border
              @search="handleDataSearch"
              @export="handleDataExport"
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
                <el-button type="primary" size="small" @click="openDataDialog(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteData(row.id)">
                  删除
                </el-button>
              </template>
            </BaseTable>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px">
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典描述" prop="description">
          <el-input v-model="typeForm.description" type="textarea" :rows="3" placeholder="请输入字典描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="typeDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="typeDialogLoading" @click="handleSaveType">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="600px">
      <el-form :model="dataForm" :rules="dataRules" ref="dataFormRef" label-width="120px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="dataForm.cssClass" placeholder="请输入样式属性" />
        </el-form-item>
        <el-form-item label="表格回显样式" prop="listClass">
          <el-input v-model="dataForm.listClass" placeholder="请输入表格回显样式" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-switch v-model="dataForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dataDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dataDialogLoading" @click="handleSaveData">
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
import {
  listDictData,
  getDictDataByType,
  addDictData,
  updateDictData,
  deleteDictData
} from '../api/dictData'
import { formatDateTime } from '../utils/dateUtils'
import { safeInput } from '../utils/xssUtil'
import BaseTable from '../components/BaseTable.vue'

const typeLoading = ref(false)
const dataLoading = ref(false)
const typeDialogVisible = ref(false)
const typeDialogLoading = ref(false)
const dataDialogVisible = ref(false)
const dataDialogLoading = ref(false)
const typeDialogTitle = ref('')
const dataDialogTitle = ref('')
const typeSearchQuery = ref('')
const dictTypeList = ref([])
const dictDataList = ref([])
const selectedDictTypeId = ref('')
const typeFormRef = ref(null)
const dataFormRef = ref(null)

const typeForm = reactive({
  id: null,
  dictName: '',
  dictCode: '',
  description: '',
  status: 1
})

const dataForm = reactive({
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

// 字典数据表格列配置
const dataColumns = [
  { prop: 'dictLabel', label: '字典标签', minWidth: '150' },
  { prop: 'dictValue', label: '字典键值', width: '180' },
  { prop: 'dictSort', label: '排序', width: '100' },
  { prop: 'cssClass', label: '样式属性', width: '150', showOverflowTooltip: true },
  { prop: 'listClass', label: '表格回显样式', width: '150', showOverflowTooltip: true },
  {
    label: '是否默认',
    width: '100',
    slotName: 'isDefault'
  },
  {
    label: '状态',
    width: '100',
    slotName: 'status'
  },
  { prop: 'remark', label: '备注', minWidth: '200', showOverflowTooltip: true },
  {
    label: '创建时间',
    width: '180',
    slotName: 'createTime'
  },
  {
    label: '操作',
    width: '150',
    fixed: 'right',
    slotName: 'operation'
  }
]

const typeRules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const dataRules = {
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

const filteredDictTypes = computed(() => {
  if (!typeSearchQuery.value) {
    return dictTypeList.value
  }
  const query = typeSearchQuery.value.toLowerCase()
  return dictTypeList.value.filter(
    type =>
      type.dictName.toLowerCase().includes(query) ||
      type.dictCode.toLowerCase().includes(query)
  )
})

const loadDictTypeList = async () => {
  typeLoading.value = true
  try {
    const data = await listDictType({ pageNum: 1, pageSize: 1000 })
    dictTypeList.value = data.records || []
  } catch (error) {
    ElMessage.error('加载字典类型列表失败')
  } finally {
    typeLoading.value = false
  }
}

const loadDictDataList = async () => {
  if (!selectedDictTypeId.value) {
    dictDataList.value = []
    return
  }

  dataLoading.value = true
  try {
    const data = await getDictDataByType(selectedDictTypeId.value)
    dictDataList.value = data || []
  } catch (error) {
    ElMessage.error('加载字典数据列表失败')
  } finally {
    dataLoading.value = false
  }
}

const selectDictType = (dictType) => {
  selectedDictTypeId.value = dictType.id
  loadDictDataList()
}

const openTypeDialog = (row = null) => {
  typeDialogTitle.value = row ? '编辑字典类型' : '添加字典类型'
  if (row) {
    Object.assign(typeForm, row)
  } else {
    typeForm.id = null
    typeForm.dictName = ''
    typeForm.dictCode = ''
    typeForm.description = ''
    typeForm.status = 1
    delete typeForm.createTime
    delete typeForm.updateTime
  }
  typeDialogVisible.value = true
}

const openDataDialog = (row = null) => {
  dataDialogTitle.value = row ? '编辑字典数据' : '添加字典数据'
  if (row) {
    Object.assign(dataForm, row)
  } else {
    dataForm.id = null
    dataForm.dictTypeId = selectedDictTypeId.value
    dataForm.dictLabel = ''
    dataForm.dictValue = ''
    dataForm.dictSort = 0
    dataForm.cssClass = ''
    dataForm.listClass = ''
    dataForm.isDefault = 0
    dataForm.status = 1
    dataForm.remark = ''
    delete dataForm.createTime
    delete dataForm.updateTime
  }
  dataDialogVisible.value = true
}

const handleSaveType = async () => {
  if (!typeFormRef.value) return
  
  await typeFormRef.value.validate(async (valid) => {
    if (valid) {
      typeDialogLoading.value = true
      try {
        // 添加XSS防护
        const safeTypeForm = {
          ...typeForm,
          dictName: safeInput(typeForm.dictName),
          dictCode: safeInput(typeForm.dictCode),
          description: safeInput(typeForm.description)
        }
        
        const isEdit = typeForm.id !== null
        if (isEdit) {
          await updateDictType(safeTypeForm)
        } else {
          await addDictType(safeTypeForm)
        }
        ElMessage.success(isEdit ? '更新成功' : '添加成功')
        typeDialogVisible.value = false
        loadDictTypeList()
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        typeDialogLoading.value = false
      }
    }
  })
}

const handleDeleteType = async (dictType) => {
  try {
    await ElMessageBox.confirm(`确定要删除字典类型"${dictType.dictName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDictType(dictType.id)
    ElMessage.success('删除成功')
    if (selectedDictTypeId.value === dictType.id) {
      selectedDictTypeId.value = ''
      dictDataList.value = []
    }
    loadDictTypeList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSaveData = async () => {
  if (!dataFormRef.value) return
  
  await dataFormRef.value.validate(async (valid) => {
    if (valid) {
      dataDialogLoading.value = true
      try {
        // 添加XSS防护
        const dataToSave = {
          ...dataForm,
          dictLabel: safeInput(dataForm.dictLabel),
          dictValue: safeInput(dataForm.dictValue),
          cssClass: safeInput(dataForm.cssClass),
          listClass: safeInput(dataForm.listClass),
          remark: safeInput(dataForm.remark)
        }
        
        if (!dataForm.id) {
          dataToSave.dictTypeId = selectedDictTypeId.value
        }
        
        if (dataForm.id) {
          await updateDictData(dataToSave)
        } else {
          await addDictData(dataToSave)
        }
        ElMessage.success(dataForm.id ? '更新成功' : '添加成功')
        dataDialogVisible.value = false
        loadDictDataList()
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        dataDialogLoading.value = false
      }
    }
  })
}

const handleDeleteData = async (id) => {
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

const handleDataSearch = (searchParams) => {
  const searchTerm = searchParams.global.toLowerCase()
  if (!searchTerm) {
    loadDictDataList()
    return
  }
  
  // 过滤字典数据
  dictDataList.value = dictDataList.value.filter(item => 
    item.dictLabel.toLowerCase().includes(searchTerm) || 
    item.dictValue.toLowerCase().includes(searchTerm)
  )
}

const handleDataExport = (exportParams) => {
  // 这里可以添加导出逻辑，例如调用后端API或使用前端库导出
  ElMessage.success(`导出${exportParams.format}格式成功`)
}

onMounted(() => {
  loadDictTypeList()
})
</script>

<style scoped>
.dict-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dict-type-panel,
.dict-data-panel {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 10px;
}

.panel-header span {
  font-size: 16px;
  font-weight: 500;
}

.type-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.type-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.type-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.type-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.type-code {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.type-actions {
  display: none;
  gap: 8px;
}

.type-item:hover .type-actions {
  display: flex;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}
</style>