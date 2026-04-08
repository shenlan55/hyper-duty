<template>
  <div class="custom-table-container">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">自定义表格</span>
          <el-button type="primary" @click="openTableDialog">新建表格</el-button>
        </div>
      </template>

      <BaseTable
        :columns="tableColumns"
        :data="tableList"
        :loading="loading"
        :pagination="{
          currentPage: pagination.currentPage,
          pageSize: pagination.pageSize,
          pageSizes: pagination.pageSizes,
          total: pagination.total
        }"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #actions="{ row }">
          <el-button type="primary" size="small" @click="viewTable(row)">查看</el-button>
          <el-button type="primary" size="small" @click="openTableDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteTable(row.id)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="tableDialogVisible"
      :title="isEdit ? '编辑表格' : '新建表格'"
      width="1200px"
      @close="resetTableForm"
    >
      <el-form :model="tableForm" :rules="tableRules" ref="tableFormRef" label-width="100px">
        <el-form-item label="表格名称" prop="tableName">
          <el-input v-model="tableForm.tableName" placeholder="请输入表格名称" />
        </el-form-item>
        <el-form-item label="表格编码" prop="tableCode">
          <el-input v-model="tableForm.tableCode" placeholder="请输入表格编码" />
        </el-form-item>
        <el-form-item label="表格描述" prop="description">
          <el-input v-model="tableForm.description" type="textarea" :rows="2" placeholder="请输入表格描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="tableForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div class="column-config">
        <div class="column-header">
          <span>列配置</span>
          <el-button type="primary" size="small" @click="addColumn">添加列</el-button>
        </div>
        <div class="column-list">
          <div v-for="(column, index) in tableForm.columns" :key="index" class="column-item">
            <el-form :model="column" label-width="80px" size="small">
              <el-row :gutter="15">
                <el-col :span="5">
                  <el-form-item label="列名称">
                    <el-input v-model="column.columnName" placeholder="列名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="列编码">
                    <el-input v-model="column.columnCode" placeholder="列编码" />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="类型">
                    <el-select v-model="column.columnType" placeholder="类型" style="width: 100%;">
                      <el-option label="文本" value="text" />
                      <el-option label="数字" value="number" />
                      <el-option label="日期" value="date" />
                      <el-option label="下拉" value="select" />
                      <el-option label="人员" value="person" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="2">
                  <el-form-item label="必填">
                    <el-switch v-model="column.required" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="排序">
                    <el-input-number v-model="column.sortOrder" :min="0" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item>
                    <el-button type="danger" size="small" @click="removeColumn(index)">删除</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row v-if="column.columnType === 'select'" :gutter="10">
                <el-col :span="24">
                  <el-form-item label="下拉选项">
                    <el-input v-model="column.options" type="textarea" :rows="2" placeholder='JSON格式，例如：[{"label":"选项1","value":"1"},{"label":"选项2","value":"2"}]' />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="tableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTable">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" :title="currentTable?.tableName" width="1200px">
      <div class="view-dialog-content">
        <div class="view-header">
          <span>数据管理</span>
          <div style="display: flex; gap: 10px;">
            <el-button type="primary" size="small" @click="addNewRow">添加数据</el-button>
            <el-button type="success" size="small" @click="saveAllRows">保存</el-button>
          </div>
        </div>
        <el-table :data="rowList" :row-key="(row, index) => getRowKey(row, index)" border style="width: 100%; margin-top: 10px;">
          <el-table-column v-for="column in currentColumns" :key="'col-' + column.columnCode" :label="column.columnName" :width="column.columnWidth">
            <template #default="{ row, $index }">
              <el-input 
                v-if="column.columnType === 'text'" 
                :key="'text-' + getRowKey(row, $index) + '-' + column.columnCode"
                v-model="row[column.columnCode]" 
                :placeholder="`请输入${column.columnName}`" 
                size="small" 
              />
              <el-input-number 
                v-else-if="column.columnType === 'number'" 
                :key="'num-' + getRowKey(row, $index) + '-' + column.columnCode"
                v-model="row[column.columnCode]" 
                :placeholder="`请输入${column.columnName}`" 
                style="width: 100%;" 
                size="small" 
              />
              <el-date-picker 
                v-else-if="column.columnType === 'date'" 
                :key="'date-' + getRowKey(row, $index) + '-' + column.columnCode"
                v-model="row[column.columnCode]" 
                type="date" 
                :placeholder="`请选择${column.columnName}`" 
                style="width: 100%;" 
                value-format="YYYY-MM-DD" 
                size="small" 
              />
              <el-select 
                v-else-if="column.columnType === 'select'" 
                :key="'sel-' + getRowKey(row, $index) + '-' + column.columnCode"
                v-model="row[column.columnCode]" 
                :placeholder="`请选择${column.columnName}`" 
                style="width: 100%;" 
                size="small"
              >
                <el-option v-for="opt in parseOptions(column.options)" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-select 
                v-else-if="column.columnType === 'person'" 
                :key="'per-' + getRowKey(row, $index) + '-' + column.columnCode"
                v-model="row[column.columnCode]" 
                :placeholder="`请选择${column.columnName}`" 
                style="width: 100%;" 
                filterable 
                size="small"
              >
                <el-option v-for="emp in employeeList" :key="emp.id" :label="emp.employeeName" :value="emp.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ $index }">
              <el-button type="danger" size="small" @click="deleteRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import {
  getCustomTablePage,
  createCustomTable,
  updateCustomTable,
  deleteCustomTable,
  getCustomTableColumns,
  getCustomTableRows,
  createTableRow,
  updateTableRow,
  deleteTableRow
} from '@/api/customTable'
import { getEmployeeList } from '@/api/employee'
import { formatDateTime } from '@/utils/dateUtils'

const tableColumns = [
  { prop: 'tableName', label: '表格名称', width: 180 },
  { prop: 'tableCode', label: '表格编码', width: 150 },
  { prop: 'description', label: '描述', minWidth: 200 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180, slot: 'createTime' },
  { prop: 'actions', label: '操作', width: 240, slot: 'actions', fixed: 'right' }
]

const loading = ref(false)
const tableList = ref([])
const employeeList = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const tableDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const tableFormRef = ref(null)

const tableForm = reactive({
  id: null,
  tableName: '',
  tableCode: '',
  description: '',
  status: 1,
  columns: []
})

const tableRules = {
  tableName: [{ required: true, message: '请输入表格名称', trigger: 'blur' }],
  tableCode: [{ required: true, message: '请输入表格编码', trigger: 'blur' }]
}

const currentTable = ref(null)
const currentColumns = ref([])
const rowList = ref([])
const originalRowList = ref([])

const loadTables = async () => {
  loading.value = true
  try {
    const data = await getCustomTablePage({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    tableList.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载表格列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadTables()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadTables()
}

const openTableDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(tableForm, {
      id: row.id,
      tableName: row.tableName,
      tableCode: row.tableCode,
      description: row.description,
      status: row.status,
      columns: []
    })
    getColumns(row.id)
  } else {
     isEdit.value = false
    tableForm.id = null
    tableForm.tableName = ''
    tableForm.tableCode = ''
    tableForm.description = ''
    tableForm.status = 1
    tableForm.columns = []
    tableFormRef.value?.resetFields()
  }
  tableDialogVisible.value = true
}

const getColumns = async (tableId) => {
  if (!tableId) {
    return
  }
  try {
    const data = await getCustomTableColumns(tableId)
    tableForm.columns = (data || []).map((col, index) => ({
      columnName: col.columnName,
      columnCode: col.columnCode || `col_${Date.now()}_${index}`,
      columnType: col.columnType,
      columnWidth: col.columnWidth,
      required: col.required,
      sortOrder: col.sortOrder,
      options: col.options && col.options !== '' ? col.options : null
    }))
  } catch (error) {
    ElMessage.error('加载列配置失败')
  }
}

const addColumn = () => {
  const index = tableForm.columns.length
  tableForm.columns.push({
    columnName: '',
    columnCode: `col_${Date.now()}_${index}`,
    columnType: 'text',
    columnWidth: 150,
    required: 0,
    sortOrder: index,
    options: null
  })
}

const removeColumn = (index) => {
  tableForm.columns.splice(index, 1)
}

const saveTable = async () => {
  try {
    await tableFormRef.value.validate()
    const processedColumns = tableForm.columns.map(col => ({
      columnName: col.columnName,
      columnCode: col.columnCode,
      columnType: col.columnType,
      columnWidth: col.columnWidth,
      required: col.required,
      sortOrder: col.sortOrder,
      options: col.options && col.options.trim() !== '' ? col.options : null
    }))
    const data = {
      table: {
        id: tableForm.id,
        tableName: tableForm.tableName,
        tableCode: tableForm.tableCode,
        description: tableForm.description,
        status: tableForm.status
      },
      columns: processedColumns
    }
    const isUpdate = isEdit.value && tableForm.id
    if (isUpdate) {
      await updateCustomTable(data)
      ElMessage.success('更新成功')
    } else {
      await createCustomTable(data)
      ElMessage.success('创建成功')
    }
    tableDialogVisible.value = false
    loadTables()
  } catch (error) {
    const isUpdate = isEdit.value && tableForm.id
    if (error !== false) {
      ElMessage.error(isUpdate ? '更新失败' : '创建失败')
    }
  }
}

const deleteTable = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个表格吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCustomTable(id)
    ElMessage.success('删除成功')
    loadTables()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewTable = async (row) => {
  if (!row || !row.id) {
    return
  }
  currentTable.value = row
  try {
    const columns = await getCustomTableColumns(row.id)
    currentColumns.value = (columns || []).map((col, index) => ({
      ...col,
      columnCode: col.columnCode || `col_${Date.now()}_${index}`
    }))
    const rows = await getCustomTableRows(row.id)
    rowList.value = rows.map(r => {
      const rowData = JSON.parse(r.rowData || '{}')
      const newRow = {}
      newRow.id = r.id
      currentColumns.value.forEach(col => {
        newRow[col.columnCode] = rowData[col.columnCode] !== undefined ? rowData[col.columnCode] : ''
      })
      return newRow
    })
    originalRowList.value = rowList.value.map(row => {
      const copy = {}
      Object.keys(row).forEach(key => {
        copy[key] = row[key]
      })
      return copy
    })
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const addNewRow = () => {
  const newRow = {}
  newRow._tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 16)}`
  currentColumns.value.forEach(column => {
    newRow[column.columnCode] = ''
  })
  const rowCopy = {}
  Object.keys(newRow).forEach(key => {
    rowCopy[key] = newRow[key]
  })
  rowList.value.push(rowCopy)
}

const saveAllRows = async () => {
  try {
    const savePromises = []

    for (const row of rowList.value) {
      const rowId = row.id
      const rowData = {}
      
      currentColumns.value.forEach(column => {
        if (row[column.columnCode] !== undefined) {
          rowData[column.columnCode] = row[column.columnCode]
        }
      })

      if (rowId) {
        const originalRow = originalRowList.value.find(r => r.id === rowId)
        const rowCopy = { ...row }
        delete rowCopy._tempId
        const isModified = JSON.stringify(originalRow) !== JSON.stringify(rowCopy)
        if (isModified) {
          savePromises.push(updateTableRow(rowId, JSON.stringify(rowData)))
        }
      } else {
        savePromises.push(createTableRow(currentTable.value.id, JSON.stringify(rowData)))
      }
    }

    await Promise.all(savePromises)
    ElMessage.success('保存成功')
    await viewTable(currentTable.value)
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const deleteRow = async (index) => {
  const row = rowList.value[index]
  if (row.id) {
    try {
      await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteTableRow(row.id)
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
        return
      } else {
        return
      }
    }
  }
  rowList.value.splice(index, 1)
}

const getRowKey = (row, index) => {
  if (row.id) {
    return `row_${row.id}`
  }
  if (row._tempId) {
    return row._tempId
  }
  return `temp_idx_${index}_${Date.now()}`
}

const parseOptions = (optionsStr) => {
  try {
    return JSON.parse(optionsStr || '[]')
  } catch {
    return []
  }
}

const resetTableForm = () => {
  isEdit.value = false
  Object.assign(tableForm, {
    id: null,
    tableName: '',
    tableCode: '',
    description: '',
    status: 1,
    columns: []
  })
  tableFormRef.value?.resetFields()
}

const loadEmployees = async () => {
  try {
    const data = await getEmployeeList()
    employeeList.value = data || []
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

onMounted(() => {
  loadTables()
  loadEmployees()
})
</script>

<style scoped>
.custom-table-container {
  padding: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.column-config {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}

.column-list {
  max-height: 400px;
  overflow-y: auto;
}

.column-item {
  background: #f5f7fa;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.view-dialog-content {
  padding: 10px 0;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
