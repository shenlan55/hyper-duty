<template>
  <div class="score-event">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分事件管理</span>
          <el-button type="primary" @click="openDialog(null)">
            <el-icon><Plus /></el-icon>
            新增事件
          </el-button>
        </div>
      </template>

      <el-table :data="eventList" v-loading="loading" border stripe>
        <el-table-column prop="eventName" label="事件名称" min-width="150" />
        <el-table-column prop="eventType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.eventType === 1 ? 'success' : 'danger'">
              {{ row.eventType === 1 ? '加分' : '扣分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultScore" label="默认分值" width="100" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑事件' : '新增事件'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="事件名称" prop="eventName">
          <el-input v-model="form.eventName" placeholder="如：团队技术分享" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-radio-group v-model="form.eventType">
            <el-radio :value="1">加分</el-radio>
            <el-radio :value="2">扣分</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认分值" prop="defaultScore">
          <el-input-number v-model="form.defaultScore" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如：赋能提效/培训分享/抽检" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getScoreEvents, createScoreEvent, updateScoreEvent, deleteScoreEvent } from '../../api/score/index.js'

const loading = ref(false)
const eventList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = ref({
  eventName: '',
  eventType: 1,
  defaultScore: 5,
  category: '',
  sort: 0,
  status: 1,
  remark: ''
})

const rules = {
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  defaultScore: [{ required: true, message: '请输入默认分值', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getScoreEvents()
    eventList.value = res || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { eventName: '', eventType: 1, defaultScore: 5, category: '', sort: 0, status: 1, remark: '' }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateScoreEvent(form.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await createScoreEvent(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除事件"${row.eventName}"吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteScoreEvent(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
}

onMounted(fetchList)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>