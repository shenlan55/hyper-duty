<template>
  <div class="process-list">
    <el-page-header title="流程定义">
      <template #extra>
        <el-button type="primary" @click="goToDesigner">设计流程</el-button>
        <el-button @click="syncProcessDefinition">同步流程</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程KEY">
          <el-input v-model="searchForm.processKey" placeholder="请输入流程KEY" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <BaseTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #actions="{ row }">
          <el-button size="small" @click="viewProcess(row)">查看</el-button>
          <el-button size="small" @click="bindFormDialog(row)">绑定表单</el-button>
          <el-button size="small" type="danger" @click="deleteProcess(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 绑定表单对话框 -->
    <el-dialog
      :title="'绑定表单 - ' + bindFormData.name"
      v-model="bindFormVisible"
      width="500px"
    >
      <el-form :model="bindFormData" label-width="100px">
        <el-form-item label="表单">
          <el-select v-model="bindFormData.formId" placeholder="请选择表单">
            <el-option :label="'- 无 -'" :value="''" />
            <el-option v-for="form in formList" :key="form.id" :label="form.name" :value="form.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBindForm">确定绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { pageProcessDefinition, deleteDeployment, syncProcessDefinition, bindFormToProcess } from '@/api/workflow/process'
import { listForm } from '@/api/workflow/form'

export default {
  name: 'ProcessList',
  components: { BaseTable },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const tableData = ref([])
    const formList = ref([])
    const bindFormVisible = ref(false)

    const searchForm = reactive({
      processKey: ''
    })

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
      total: 0
    })

    const bindFormData = reactive({
      key: '',
      name: '',
      formId: ''
    })

    const columns = [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'key', label: '流程KEY', minWidth: 180 },
      { prop: 'name', label: '流程名称', minWidth: 180 },
      { prop: 'version', label: '版本', width: 80 },
      { prop: 'resourceName', label: '资源名称', minWidth: 200 },
      { prop: 'actions', label: '操作', width: 250, fixed: 'right' }
    ]

    const loadData = async () => {
      loading.value = true
      try {
        const res = await pageProcessDefinition(pagination.currentPage, pagination.pageSize, searchForm.processKey)
        tableData.value = res.records || []
        pagination.total = res.total || 0
      } catch (error) {
        ElMessage.error(error.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const loadFormList = async () => {
      try {
        const res = await listForm({ pageNum: 1, pageSize: 100 })
        formList.value = res.records || []
      } catch (error) {
        ElMessage.error('加载表单列表失败')
      }
    }

    const resetSearch = () => {
      searchForm.processKey = ''
      pagination.currentPage = 1
      loadData()
    }

    const goToDesigner = () => {
      router.push('/workflow/designer')
    }

    const syncProcessDefinition = async () => {
      try {
        await syncProcessDefinition()
        ElMessage.success('同步成功')
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '同步失败')
      }
    }

    const bindFormDialog = (row) => {
      bindFormData.key = row.key
      bindFormData.name = row.name
      bindFormData.formId = row.formId || ''
      bindFormVisible.value = true
    }

    const handleBindForm = async () => {
      try {
        await bindFormToProcess({
          processKey: bindFormData.key,
          formId: bindFormData.formId || null
        })
        ElMessage.success('绑定成功')
        bindFormVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '绑定失败')
      }
    }

    const viewProcess = (row) => {
      router.push({
        path: '/workflow/designer',
        query: { processDefinitionId: row.id }
      })
    }

    const deleteProcess = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该流程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteDeployment(row.deploymentId)
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
      loadFormList()
    })

    return {
      loading,
      tableData,
      formList,
      bindFormVisible,
      searchForm,
      pagination,
      bindFormData,
      columns,
      loadData,
      resetSearch,
      goToDesigner,
      syncProcessDefinition,
      bindFormDialog,
      handleBindForm,
      viewProcess,
      deleteProcess,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.process-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
