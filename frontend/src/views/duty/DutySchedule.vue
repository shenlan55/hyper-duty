<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>值班表管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加值班表
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="请输入值班表名称"
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="pagedScheduleList"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="scheduleName" label="值班表名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
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
          :total="filteredScheduleList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑值班表对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        label-position="top"
      >
        <el-form-item label="值班表名称" prop="scheduleName">
          <el-input
            v-model="scheduleForm.scheduleName"
            placeholder="请输入值班表名称"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="scheduleForm.description"
            placeholder="请输入值班表描述"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="scheduleForm.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="scheduleForm.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="scheduleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getScheduleList,
  addSchedule,
  updateSchedule,
  deleteSchedule
} from '../../api/duty/schedule'
import { formatDate, formatDateTime } from '../../utils/dateUtils'

// 响应式数据
const searchQuery = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogTitle = ref('添加值班表')
const scheduleFormRef = ref()

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)

// 值班表数据
const scheduleList = ref([])

// 表单数据
const scheduleForm = reactive({
  id: null,
  scheduleName: '',
  description: '',
  startDate: null,
  endDate: null,
  status: 1
})

// 表单验证规则
const scheduleRules = {
  scheduleName: [
    { required: true, message: '请输入值班表名称', trigger: 'blur' },
    { min: 2, max: 100, message: '值班表名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'blur' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'blur' }
  ]
}

// 过滤后的值班表列表
const filteredScheduleList = computed(() => {
  let list = scheduleList.value
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(schedule => 
      schedule.scheduleName.toLowerCase().includes(query)
    )
  }
  
  return list
})

// 分页后的值班表列表
const pagedScheduleList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredScheduleList.value.slice(start, end)
})

// 获取值班表列表
const fetchScheduleList = async () => {
  loading.value = true
  try {
    const response = await getScheduleList()
    if (response.code === 200) {
      scheduleList.value = response.data
    }
  } catch (error) {
    console.error('获取值班表列表失败:', error)
    ElMessage.error('获取值班表列表失败')
  } finally {
    loading.value = false
  }
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
  dialogTitle.value = '添加值班表'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (schedule) => {
  Object.assign(scheduleForm, schedule)
  dialogTitle.value = '编辑值班表'
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields()
  }
  Object.assign(scheduleForm, {
    id: null,
    scheduleName: '',
    description: '',
    startDate: null,
    endDate: null,
    status: 1
  })
}

// 保存值班表
const handleSave = async () => {
  try {
    await scheduleFormRef.value.validate()
    dialogLoading.value = true
    
    let response
    if (scheduleForm.id) {
      // 编辑值班表
      response = await updateSchedule(scheduleForm)
    } else {
      // 添加值班表
      response = await addSchedule(scheduleForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(scheduleForm.id ? '编辑值班表成功' : '添加值班表成功')
      dialogVisible.value = false
      fetchScheduleList()
    } else {
      ElMessage.error(response.message || (scheduleForm.id ? '编辑值班表失败' : '添加值班表失败'))
    }
  } catch (error) {
    console.error('保存值班表失败:', error)
    ElMessage.error('保存值班表失败')
  } finally {
    dialogLoading.value = false
  }
}

// 删除值班表
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班表吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteSchedule(id)
    if (response.code === 200) {
      ElMessage.success('删除值班表成功')
      fetchScheduleList()
    } else {
      ElMessage.error(response.message || '删除值班表失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除值班表失败:', error)
      ElMessage.error('删除值班表失败')
    }
  }
}

// 生命周期钩子
onMounted(async () => {
  await fetchScheduleList()
})
</script>

<style scoped>
.schedule-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.content-card {
  margin-bottom: 10px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>