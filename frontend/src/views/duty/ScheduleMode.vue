<template>
  <div class="schedule-mode-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>排班模式管理</span>
          <el-button type="primary" @click="handleAdd">新增排班模式</el-button>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="modeList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="modeName" label="排班模式名称" min-width="150" />
        <el-table-column prop="modeCode" label="编码" width="120" />
        <el-table-column prop="modeType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getModeTypeTagType(scope.row.modeType)">
              {{ getModeTypeName(scope.row.modeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="1"
              inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
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
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
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

const loading = ref(false)
const modeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentModeId = ref(null)

const modeApi = scheduleModeApi()

// 获取排班模式列表
const fetchModeList = async () => {
  loading.value = true
  try {
    const response = await modeApi.getAllModes()
    if (response.code === 200) {
      // 确保status字段为字符串类型，与el-switch组件的active-value/inactive-value类型匹配
      modeList.value = response.data.map(item => ({
        ...item,
        status: item.status.toString()
      }))
      total.value = response.data.length
    } else {
      ElMessage.error('获取排班模式列表失败')
    }
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
    const response = await modeApi.delete(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchModeList()
    } else {
      ElMessage.error('删除失败')
    }
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
    const response = await modeApi.update(updateData)
    if (response.code !== 200) {
      // 恢复原状态
      row.status = originalStatus
      ElMessage.error('状态更新失败')
    }
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
}

const handleCurrentChange = (current) => {
  currentPage.value = current
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
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
