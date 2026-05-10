<template>
  <div class="form-list">
    <el-page-header title="表单管理">
      <template #extra>
        <el-button type="primary" @click="handleAdd">新增表单</el-button>
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
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #actions="{ row }">
          <el-button size="small" type="primary" @click="handleDesign(row)">设计表单</el-button>
          <el-button size="small" @click="handlePreview(row)">预览</el-button>
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
        <el-form-item label="表单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入表单编码" />
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-select v-model="form.formType" placeholder="请选择">
            <el-option label="动态表单" value="dynamic" />
            <el-option label="自定义表单" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 表单设计器对话框（使用 FormCreate Designer） -->
    <el-dialog
      :title="'表单设计 - ' + designForm.name"
      v-model="designerDialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div class="designer-container">
        <fc-designer
          v-if="designerDialogVisible"
          ref="designerApi"
          :rule="designerRule"
          :option="designerOption"
        />
      </div>
      <template #footer>
        <el-button @click="designerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 表单预览对话框 -->
    <el-dialog
      :title="'表单预览 - ' + previewForm.name"
      v-model="previewDialogVisible"
      width="800px"
    >
      <div class="preview-container">
        <form-create
          v-if="previewRule.length > 0"
          :rule="previewRule"
          :option="previewOption"
          v-model="previewFormData"
          v-model:api="previewFormApi"
        />
        <el-empty v-else description="暂无表单配置" />
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { pageForm, getForm, createForm, updateForm, deleteForm } from '@/api/workflow/form'
import designer from '@form-create/designer'

const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const dialogVisible = ref(false)
const designerDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const isEdit = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '表单名称', minWidth: 180 },
  { prop: 'code', label: '表单编码', minWidth: 180 },
  { prop: 'formType', label: '表单类型', width: 120 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'actions', label: '操作', width: 450, fixed: 'right', slot: 'actions' }
]

const form = reactive({
  id: null,
  name: '',
  code: '',
  formType: 'dynamic',
  status: 1,
  remark: ''
})

const designForm = ref({
  id: null,
  name: ''
})
const designerApi = ref({})
const designerRule = ref([])
const designerOption = ref({})

const previewForm = ref({
  id: null,
  name: ''
})
const previewRule = ref([])
const previewFormData = ref({})
const previewFormApi = ref({})
const previewOption = ref({
  submitBtn: false,
  resetBtn: false
})

const rules = {
  name: [{ required: true, message: '请输入表单名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入表单编码', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑表单' : '新增表单')

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageForm({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
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
    name: '',
    code: '',
    formType: 'dynamic',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getForm(row.id)
    Object.assign(form, res)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      await updateForm(form)
      ElMessage.success('更新成功')
    } else {
      await createForm(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleDesign = async (row) => {
  designForm.value = { id: row.id, name: row.name }
  try {
    const res = await getForm(row.id)
    if (res.formContent) {
      try {
        designerRule.value = JSON.parse(res.formContent)
      } catch (e) {
        designerRule.value = []
      }
    } else {
      designerRule.value = []
    }
    designerDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取表单失败')
  }
}

const handleSaveForm = async () => {
  try {
    const rule = designerApi.value.getRule()
    await updateForm({
      id: designForm.value.id,
      formContent: JSON.stringify(rule)
    })
    ElMessage.success('保存成功')
    designerDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handlePreview = async (row) => {
  previewForm.value = { id: row.id, name: row.name }
  try {
    const res = await getForm(row.id)
    if (res.formContent) {
      try {
        previewRule.value = JSON.parse(res.formContent)
      } catch (e) {
        previewRule.value = []
      }
    } else {
      previewRule.value = []
    }
    previewDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取表单失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该表单吗？', '提示', {
      type: 'warning'
    })
    
    await deleteForm(row.id)
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
.form-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.designer-container {
  height: calc(100vh - 200px);
}

.preview-container {
  padding: 20px;
}
</style>
