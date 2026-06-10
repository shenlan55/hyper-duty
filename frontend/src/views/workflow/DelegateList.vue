<template>
  <div class="delegate-list">
    <el-page-header title="委托配置">
      <template #extra>
        <el-button type="primary" @click="handleAdd">新增委托</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4">
      <BaseTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #status="{ row }">
          <el-tag :type="commonStatusType(row.status)">
            {{ commonStatusLabel(row.status) }}
          </el-tag>
        </template>
        <template #actions="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="委托人" prop="assignee">
          <el-input v-model="form.assignee" placeholder="请输入委托人" />
        </el-form-item>
        <el-form-item label="受托人" prop="attorney">
          <el-input v-model="form.attorney" placeholder="请输入受托人" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="opt in commonStatusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="Number(opt.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { useDict } from '@/composables/useDict'

// 业务枚举：状态 走字典
const { options: commonStatusOptions, labelOf: commonStatusLabel, tagTypeOf: commonStatusType, loadDict: loadCommonStatusDict } = useDict('common_status')
loadCommonStatusDict()

const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'assignee', label: '委托人', minWidth: 150 },
  { prop: 'attorney', label: '受托人', minWidth: 150 },
  { prop: 'startTime', label: '开始时间', width: 180 },
  { prop: 'endTime', label: '结束时间', width: 180 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'actions', label: '操作', width: 180, fixed: 'right', slot: 'actions' }
]

const form = reactive({
  id: null,
  assignee: '',
  attorney: '',
  startTime: null,
  endTime: null,
  status: 1,
  remark: ''
})

const rules = {
  assignee: [{ required: true, message: '请输入委托人', trigger: 'blur' }],
  attorney: [{ required: true, message: '请输入受托人', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑委托' : '新增委托')

const loadData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    assignee: '',
    attorney: '',
    startTime: null,
    endTime: null,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该委托吗？', '提示', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.delegate-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}
</style>
