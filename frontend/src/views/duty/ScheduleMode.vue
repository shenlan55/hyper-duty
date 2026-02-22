<template>
  <div class="schedule-mode-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>排班模式管理</span>
          <el-button type="primary" @click="handleAdd">新增排班模式</el-button>
        </div>
      </template>
      
      <BaseTable
        v-loading="loading"
        :data="modeList"
        :columns="columns"
        :show-pagination="true"
        :pagination="pagination"
        :backend-pagination="true"
        :show-search="true"
        :search-placeholder="'请输入模式名称或编码'"
        :show-export="true"
        :show-column-control="true"
        :show-skeleton="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @search="handleSearch"
        @export="handleExport"
      >
        <template #modeType="{ row }">
          <el-tag :type="getModeTypeTagType(row.modeType)">
            {{ getModeTypeName(row.modeType) }}
          </el-tag>
        </template>
        <template #status="{ row }">
          <el-switch
            v-model="row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
        <template #operation="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </BaseTable>
    </el-card>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1100px"
      destroy-on-close
    >
      <ScheduleModeEdit
        :mode-id="currentModeId"
        @save-success="handleSaveSuccess"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ScheduleModeEdit from './components/ScheduleModeEdit.vue'
import { scheduleModeApi } from '@/api/duty/scheduleMode'
import BaseTable from '@/components/BaseTable.vue'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

const loading = ref(false)
const modeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentModeId = ref(null)

const modeApi = scheduleModeApi()

const columns = [
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'modeName', label: '排班模式名称', minWidth: '150' },
  { prop: 'modeCode', label: '编码', width: '120' },
  { prop: 'modeType', label: '类型', width: '100' },
  { prop: 'status', label: '状态', width: '80' },
  { prop: 'sort', label: '排序', width: '80' },
  { prop: 'createTime', label: '创建时间', width: '180' },
  { type: 'operation', label: '操作', width: '180', fixed: 'right' }
]

const pagination = computed(() => {
  return {
    currentPage: currentPage.value,
    pageSize: pageSize.value,
    pageSizes: [10, 20, 50, 100],
    total: total.value
  }
})

// 获取排班模式列表
const fetchModeList = async () => {
  loading.value = true
  try {
    const data = await modeApi.getAllModes()
    // 确保status字段为字符串类型，与el-switch组件的active-value/inactive-value类型匹配
    let processedData = (data || []).map(item => ({
      ...item,
      status: item.status.toString()
    }))
    
    // 搜索过滤
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      processedData = processedData.filter(item => 
        item.modeName.toLowerCase().includes(query) || 
        item.modeCode.toLowerCase().includes(query)
      )
    }
    
    // 计算总条数
    total.value = processedData.length
    
    // 前端分页
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    modeList.value = processedData.slice(startIndex, endIndex)
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 新增排班模式
const handleAdd = () => {
  dialogTitle.value = '新增排班模式'
  currentModeId.value = null
  dialogVisible.value = true
}

// 编辑排班模式
const handleEdit = (row) => {
  dialogTitle.value = '编辑排班模式'
  currentModeId.value = row.id
  dialogVisible.value = true
}

// 删除排班模式
const handleDelete = async (id) => {
  try {
    await modeApi.delete(id)
    ElMessage.success('删除成功')
    fetchModeList()
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  // 保存当前状态用于恢复
  const originalStatus = row.status
  try {
    // 确保status为数字类型再发送到后端
    const updateData = {
      ...row,
      status: Number(row.status)
    }
    await modeApi.update(updateData)
  } catch (error) {
    // 恢复原状态
    row.status = originalStatus
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 保存成功回调
const handleSaveSuccess = () => {
  dialogVisible.value = false
  ElMessage.success('保存成功')
  fetchModeList()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchModeList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchModeList()
}

// 搜索处理
const handleSearch = (searchParams) => {
  const keyword = searchParams?.global || ''
  searchQuery.value = keyword
  currentPage.value = 1
  fetchModeList()
}

// 导出处理
const handleExport = () => {
  try {
    // 准备导出数据
    const exportData = modeList.value.map(item => ({
      'ID': item.id,
      '排班模式名称': item.modeName,
      '编码': item.modeCode,
      '类型': getModeTypeName(item.modeType),
      '状态': item.status === '1' ? '启用' : '禁用',
      '排序': item.sort,
      '创建时间': item.createTime
    }))
    
    // 创建工作表
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '排班模式')
    
    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    const dataBlob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    
    // 保存文件
    saveAs(dataBlob, `排班模式_${new Date().toISOString().slice(0, 10)}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 获取排班模式类型名称
const getModeTypeName = (type) => {
  const typeMap = {
    1: '轮班制',
    2: '综合制',
    3: '弹性制',
    4: '自定义'
  }
  return typeMap[type] || '未知'
}

// 获取排班模式类型标签类型
const getModeTypeTagType = (type) => {
  const typeMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info'
  }
  return typeMap[type] || 'default'
}

// 初始化
onMounted(() => {
  fetchModeList()
})
</script>

<style scoped>
.schedule-mode-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
