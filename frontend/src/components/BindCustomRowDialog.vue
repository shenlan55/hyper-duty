<template>
  <el-dialog
    v-model="visible"
    title="绑定表格数据"
    width="1500px"
    :close-on-click-modal="false"
    @close="reset"
  >
    <div class="dialog-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane
          v-for="table in tableList"
          :key="table.id"
          :label="table.tableName"
          :name="String(table.id)"
        >
          <div class="table-container">
            <el-table
              :data="getTableRows(table.id)"
              border
              style="width: 100%; margin-top: 10px;"
              @row-click="handleRowClick"
              highlight-current-row
            >
              <el-table-column
                v-for="column in getTableColumns(table.id)"
                :key="column.columnCode"
                :prop="column.columnCode"
                :label="column.columnName"
                :width="column.columnWidth"
                show-overflow-tooltip
              />
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="handleSelect(row, table.id)">选择</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="orderNoDialogVisible"
    title="录入单号"
    width="500px"
  >
    <el-form :model="orderNoForm" :rules="orderNoRules" ref="orderNoFormRef" label-width="80px" hide-required-asterisk>
      <el-form-item prop="orderNo">
        <template #label>
          <span style="color: #f56c6c; margin-right: 4px;">*</span>
          单号
        </template>
        <el-input v-model="orderNoForm.orderNo" placeholder="请输入单号" />
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="orderNoForm.title" placeholder="请输入标题" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="orderNoDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmBind">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCustomTablePage,
  getCustomTableColumns,
  getCustomTableRows,
  bindCustomRow
} from '@/api/customTable'

const props = defineProps({
  modelValue: Boolean,
  taskId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const tableList = ref([])
const tableColumnsMap = ref({})
const tableRowsMap = ref({})
const activeTab = ref('')
const orderNoDialogVisible = ref(false)
const orderNoForm = reactive({
  orderNo: '',
  title: ''
})
const orderNoFormRef = ref(null)
const orderNoRules = {
  orderNo: [{ required: true, message: '请输入单号', trigger: 'blur' }]
}
const pendingBindData = ref(null)

const loadTables = async () => {
  loading.value = true
  try {
    const data = await getCustomTablePage({ pageNum: 1, pageSize: 100, status: 1 })
    tableList.value = data.records || []
    if (tableList.value.length > 0) {
      activeTab.value = String(tableList.value[0].id)
    }
    for (const table of tableList.value) {
      const columns = await getCustomTableColumns(table.id)
      tableColumnsMap.value[table.id] = columns || []
      const rows = await getCustomTableRows(table.id)
      tableRowsMap.value[table.id] = rows.map(r => ({
        id: r.id,
        ...JSON.parse(r.rowData || '{}')
      }))
    }
  } catch (error) {
    ElMessage.error('加载表格数据失败')
  } finally {
    loading.value = false
  }
}

const getTableColumns = (tableId) => {
  return tableColumnsMap.value[tableId] || []
}

const getTableRows = (tableId) => {
  return tableRowsMap.value[tableId] || []
}

const handleRowClick = (row) => {
}

const handleSelect = (row, tableId) => {
  pendingBindData.value = { row, tableId }
  orderNoForm.orderNo = ''
  orderNoForm.title = ''
  orderNoDialogVisible.value = true
}

const confirmBind = async () => {
  try {
    await orderNoFormRef.value.validate()
    
    const { row, tableId } = pendingBindData.value
    await bindCustomRow(props.taskId, tableId, row.id, orderNoForm.orderNo, orderNoForm.title)
    ElMessage.success('绑定成功')
    orderNoDialogVisible.value = false
    visible.value = false
    emit('success')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('绑定失败')
    }
  }
}

const reset = () => {
  if (tableList.value.length > 0) {
    activeTab.value = String(tableList.value[0].id)
  }
}

watch(visible, (val) => {
  if (val) {
    loadTables()
  }
})

onMounted(() => {
})
</script>

<style scoped>
.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.dialog-content::-webkit-scrollbar {
  width: 8px;
}

.dialog-content::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

.dialog-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

.table-container {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;
}

.table-container::-webkit-scrollbar {
  width: 6px;
}

.table-container::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>
