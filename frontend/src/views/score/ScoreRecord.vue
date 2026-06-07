<template>
  <div class="score-record">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分记录</span>
          <el-button type="primary" @click="openDialog">
            <el-icon><Plus /></el-icon>
            录入积分
          </el-button>
        </div>
      </template>

      <!-- 筛选区 -->
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="员工">
          <el-input
            v-model="filterEmployeeName"
            placeholder="点击选择员工"
            readonly
            @click="filterEmployeeDialogVisible = true"
            style="width: 200px; cursor: pointer;"
            clearable
            @clear="clearFilterEmployee"
          >
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="queryForm.periodYear" :min="2024" :max="2099" />
        </el-form-item>
        <el-form-item label="月份">
          <el-select v-model="queryForm.periodMonth" placeholder="选择月份" clearable style="width: 120px">
            <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="recordList" v-loading="loading" border stripe>
        <el-table-column prop="employeeName" label="员工" width="120" />
        <el-table-column prop="eventName" label="事件" min-width="150" />
        <el-table-column prop="score" label="分值" width="80">
          <template #default="{ row }">
            <span :class="row.score >= 0 ? 'score-plus' : 'score-minus'">
              {{ row.score >= 0 ? '+' + row.score : row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="periodYear" label="所属月份" width="120">
          <template #default="{ row }">{{ row.periodYear }}-{{ String(row.periodMonth).padStart(2, '0') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @change="fetchList"
        class="pagination"
      />
    </el-card>

    <!-- 录入对话框 -->
    <el-dialog v-model="dialogVisible" title="录入积分" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="员工" prop="employeeId">
          <el-input
            v-model="formEmployeeName"
            placeholder="点击选择员工"
            readonly
            @click="formEmployeeDialogVisible = true"
            style="cursor: pointer;"
          >
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="积分事件" prop="eventId">
          <el-select v-model="form.eventId" placeholder="选择事件" @change="onEventChange" style="width: 100%">
            <el-option v-for="evt in eventList" :key="evt.id" :label="evt.eventName" :value="evt.id">
              <span>{{ evt.eventName }}</span>
              <el-tag size="small" :type="evt.eventType === 1 ? 'success' : 'danger'" style="margin-left: 8px">
                {{ evt.eventType === 1 ? '加分' : '扣分' }}
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="-100" :max="100" />
        </el-form-item>
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细描述加分/扣分原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 筛选区员工选择对话框 -->
    <el-dialog v-model="filterEmployeeDialogVisible" title="选择员工" width="900px" max-width="90vw">
      <div style="padding: 10px;">
        <PersonSelector v-model="filterSelectedEmployees" style="height: 500px;" />
      </div>
      <template #footer>
        <el-button @click="filterEmployeeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFilterEmployee">确定</el-button>
      </template>
    </el-dialog>

    <!-- 录入表单员工选择对话框 -->
    <el-dialog v-model="formEmployeeDialogVisible" title="选择员工" width="900px" max-width="90vw">
      <div style="padding: 10px;">
        <PersonSelector v-model="formSelectedEmployees" style="height: 500px;" />
      </div>
      <template #footer>
        <el-button @click="formEmployeeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFormEmployee">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UserFilled } from '@element-plus/icons-vue'
import PersonSelector from '@/components/PersonSelector.vue'
import { getScoreRecords, createScoreRecord, deleteScoreRecord } from '../../api/score/index.js'
import { getScoreEvents } from '../../api/score/index.js'

const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const eventList = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

// 筛选区员工选择
const filterEmployeeName = ref('')
const filterEmployeeDialogVisible = ref(false)
const filterSelectedEmployees = ref([])

// 录入表单员工选择
const formEmployeeName = ref('')
const formEmployeeDialogVisible = ref(false)
const formSelectedEmployees = ref([])

const queryForm = ref({
  page: 1,
  pageSize: 10,
  employeeId: null,
  periodYear: new Date().getFullYear(),
  periodMonth: null
})

const form = ref({
  employeeId: null,
  eventId: null,
  score: 0,
  recordDate: '',
  description: ''
})

const rules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  eventId: [{ required: true, message: '请选择积分事件', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...queryForm.value }
    const res = await getScoreRecords(params)
    recordList.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const fetchEvents = async () => {
  try {
    const res = await getScoreEvents()
    eventList.value = (res || []).filter(e => e.status === 1)
  } catch (e) { /* ignore */ }
}

// 筛选区员工确认
const confirmFilterEmployee = () => {
  if (filterSelectedEmployees.value.length > 0) {
    const emp = filterSelectedEmployees.value[0]
    queryForm.value.employeeId = emp.id
    filterEmployeeName.value = emp.employeeName
  }
  filterEmployeeDialogVisible.value = false
}

// 清除筛选员工
const clearFilterEmployee = () => {
  queryForm.value.employeeId = null
  filterEmployeeName.value = ''
}

// 录入表单员工确认
const confirmFormEmployee = () => {
  if (formSelectedEmployees.value.length > 0) {
    const emp = formSelectedEmployees.value[0]
    form.value.employeeId = emp.id
    formEmployeeName.value = emp.employeeName
  }
  formEmployeeDialogVisible.value = false
}

const resetQuery = () => {
  queryForm.value = { page: 1, pageSize: 10, employeeId: null, periodYear: new Date().getFullYear(), periodMonth: null }
  filterEmployeeName.value = ''
  fetchList()
}

const openDialog = () => {
  form.value = { employeeId: null, eventId: null, score: 0, recordDate: '', description: '' }
  formEmployeeName.value = ''
  formSelectedEmployees.value = []
  dialogVisible.value = true
}

const onEventChange = (eventId) => {
  const evt = eventList.value.find(e => e.id === eventId)
  if (evt) {
    form.value.score = evt.eventType === 2 ? -Math.abs(evt.defaultScore) : Math.abs(evt.defaultScore)
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createScoreRecord(form.value)
    ElMessage.success('录入成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteScoreRecord(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
}

onMounted(() => {
  fetchList()
  fetchEvents()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.score-plus {
  color: #67c23a;
  font-weight: bold;
}
.score-minus {
  color: #f56c6c;
  font-weight: bold;
}
</style>