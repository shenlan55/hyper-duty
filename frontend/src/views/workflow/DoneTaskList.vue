<template>
  <div class="done-task-list">
    <el-page-header title="已办任务" />

    <el-card class="mt-4">
      <BaseTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #actions="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
        </template>
      </BaseTable>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { pageDoneTask } from '@/api/workflow/task'

export default {
  name: 'DoneTaskList',
  components: { BaseTable },
  setup() {
    const loading = ref(false)
    const tableData = ref([])

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
      total: 0
    })

    const columns = [
      { prop: 'id', label: '任务ID', width: 120 },
      { prop: 'name', label: '任务名称', minWidth: 180 },
      { prop: 'processDefinitionId', label: '流程ID', minWidth: 200 },
      { prop: 'startTime', label: '开始时间', minWidth: 180 },
      { prop: 'endTime', label: '结束时间', minWidth: 180 },
      { prop: 'actions', label: '操作', width: 150, fixed: 'right' }
    ]

    const loadData = async () => {
      loading.value = true
      try {
        const res = await pageDoneTask(pagination.currentPage, pagination.pageSize)
        tableData.value = res.records || []
        pagination.total = res.total || 0
      } catch (error) {
        ElMessage.error(error.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const viewDetail = (row) => {
      ElMessage.info('查看任务详情：' + row.id)
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

    return {
      loading,
      tableData,
      pagination,
      columns,
      loadData,
      viewDetail,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.done-task-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .done-task-list {
    padding: 8px;
  }

  .mt-4 {
    margin-top: 10px;
  }

  .el-page-header {
    padding: 8px 0;
  }
}
</style>
