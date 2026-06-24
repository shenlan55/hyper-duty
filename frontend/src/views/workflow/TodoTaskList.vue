<template>
  <div class="todo-task-list">
    <el-page-header title="待办任务">
      <template #extra>
        <el-button type="primary" @click="showBatchReassign">批量转办</el-button>
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
        <template #actions="{ row }">
          <el-button size="small" type="primary" @click="handleTask(row)">办理</el-button>
          <el-button size="small" @click="reassignTask(row)">转办</el-button>
          <el-button size="small" @click="delegateTask(row)">委托</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 办理任务对话框 -->
    <el-dialog v-model="taskDialogVisible" title="办理任务" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="taskForm.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTask">提交</el-button>
      </template>
    </el-dialog>

    <!-- 转办对话框 -->
    <el-dialog v-model="reassignDialogVisible" title="转办任务" width="1000px">
      <el-form :model="reassignForm" label-width="100px">
        <el-form-item label="转办人">
          <el-input v-model="reassignForm.userName" disabled placeholder="点击下方选择人员" />
        </el-form-item>
        <el-form-item>
          <PersonSelector
            v-model="selectedReassignPerson"
            @change="handleReassignPersonChange"
            style="height: 400px;"
          />
        </el-form-item>
        <el-form-item label="转办原因">
          <el-input v-model="reassignForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reassignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReassign">提交</el-button>
      </template>
    </el-dialog>

    <!-- 委托对话框 -->
    <el-dialog v-model="delegateDialogVisible" title="委托任务" width="1000px">
      <el-form :model="delegateForm" label-width="100px">
        <el-form-item label="委托人">
          <el-input v-model="delegateForm.userName" disabled placeholder="点击下方选择人员" />
        </el-form-item>
        <el-form-item>
          <PersonSelector
            v-model="selectedDelegatePerson"
            @change="handleDelegatePersonChange"
            style="height: 400px;"
          />
        </el-form-item>
        <el-form-item label="委托原因">
          <el-input v-model="delegateForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="delegateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDelegate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 批量转办对话框 -->
    <el-dialog v-model="batchReassignDialogVisible" title="批量转办" width="1000px">
      <el-form :model="batchReassignForm" label-width="100px">
        <el-form-item label="选择待转交的人">
          <el-select v-model="batchReassignForm.fromUserId" placeholder="请选择" style="width: 100%" clearable>
            <el-option
              v-for="emp in employeeList"
              :key="emp.id"
              :label="emp.employeeName"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转交给">
          <el-input v-model="batchReassignForm.toUserName" disabled placeholder="点击下方选择人员" />
        </el-form-item>
        <el-form-item>
          <PersonSelector
            v-model="selectedBatchReassignPerson"
            @change="handleBatchReassignPersonChange"
            style="height: 400px;"
          />
        </el-form-item>
        <el-form-item label="转办原因">
          <el-input v-model="batchReassignForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchReassignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchReassign">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import PersonSelector from '@/components/PersonSelector.vue'
import { getEmployeeList } from '@/api/employee'
import { pageTodoTask, completeTask, reassignTask, delegateTask, batchReassignTask } from '@/api/workflow/task'

export default {
  name: 'TodoTaskList',
  components: { BaseTable, PersonSelector },
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const employeeList = ref([])
    const currentTask = ref(null)

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
      { prop: 'createTime', label: '创建时间', minWidth: 180 },
      { prop: 'actions', label: '操作', width: 250, fixed: 'right' }
    ]

    // 对话框
    const taskDialogVisible = ref(false)
    const reassignDialogVisible = ref(false)
    const delegateDialogVisible = ref(false)
    const batchReassignDialogVisible = ref(false)

    // 选择的人员
    const selectedReassignPerson = ref([])
    const selectedDelegatePerson = ref([])
    const selectedBatchReassignPerson = ref([])

    // 表单数据
    const taskForm = reactive({
      name: '',
      comment: ''
    })

    const reassignForm = reactive({
      taskId: '',
      userId: null,
      userName: '',
      reason: ''
    })

    const delegateForm = reactive({
      taskId: '',
      userId: null,
      userName: '',
      reason: ''
    })

    const batchReassignForm = reactive({
      fromUserId: null,
      toUserId: null,
      toUserName: '',
      reason: ''
    })

    const loadData = async () => {
      loading.value = true
      try {
        const res = await pageTodoTask(pagination.currentPage, pagination.pageSize)
        tableData.value = res.records || []
        pagination.total = res.total || 0
      } catch (error) {
        ElMessage.error(error.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const loadEmployees = async () => {
      try {
        const res = await getEmployeeList(1, 1000)
        employeeList.value = res.records || []
      } catch (error) {
        console.error('加载人员列表失败', error)
      }
    }

    const handleTask = (row) => {
      currentTask.value = row
      taskForm.name = row.name
      taskForm.comment = ''
      taskDialogVisible.value = true
    }

    const submitTask = async () => {
      try {
        await completeTask({
          taskId: currentTask.value.id,
          comment: taskForm.comment
        })
        ElMessage.success('提交成功')
        taskDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '提交失败')
      }
    }

    const reassignTask = (row) => {
      currentTask.value = row
      reassignForm.taskId = row.id
      reassignForm.userId = null
      reassignForm.userName = ''
      reassignForm.reason = ''
      selectedReassignPerson.value = []
      reassignDialogVisible.value = true
    }

    const handleReassignPersonChange = (persons) => {
      if (persons && persons.length > 0) {
        const selected = persons[0]
        reassignForm.userId = selected.id
        reassignForm.userName = selected.employeeName
      } else {
        reassignForm.userId = null
        reassignForm.userName = ''
      }
    }

    const submitReassign = async () => {
      if (!reassignForm.userId) {
        ElMessage.warning('请选择转办人')
        return
      }

      try {
        await reassignTask(reassignForm)
        ElMessage.success('转办成功')
        reassignDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '转办失败')
      }
    }

    const delegateTask = (row) => {
      currentTask.value = row
      delegateForm.taskId = row.id
      delegateForm.userId = null
      delegateForm.userName = ''
      delegateForm.reason = ''
      selectedDelegatePerson.value = []
      delegateDialogVisible.value = true
    }

    const handleDelegatePersonChange = (persons) => {
      if (persons && persons.length > 0) {
        const selected = persons[0]
        delegateForm.userId = selected.id
        delegateForm.userName = selected.employeeName
      } else {
        delegateForm.userId = null
        delegateForm.userName = ''
      }
    }

    const submitDelegate = async () => {
      if (!delegateForm.userId) {
        ElMessage.warning('请选择委托人')
        return
      }

      try {
        await delegateTask(delegateForm)
        ElMessage.success('委托成功')
        delegateDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '委托失败')
      }
    }

    const showBatchReassign = () => {
      batchReassignForm.fromUserId = null
      batchReassignForm.toUserId = null
      batchReassignForm.toUserName = ''
      batchReassignForm.reason = ''
      selectedBatchReassignPerson.value = []
      batchReassignDialogVisible.value = true
    }

    const handleBatchReassignPersonChange = (persons) => {
      if (persons && persons.length > 0) {
        const selected = persons[0]
        batchReassignForm.toUserId = selected.id
        batchReassignForm.toUserName = selected.employeeName
      } else {
        batchReassignForm.toUserId = null
        batchReassignForm.toUserName = ''
      }
    }

    const submitBatchReassign = async () => {
      if (!batchReassignForm.fromUserId) {
        ElMessage.warning('请选择待转交的人')
        return
      }

      if (!batchReassignForm.toUserId) {
        ElMessage.warning('请选择接收人')
        return
      }

      try {
        await batchReassignTask(batchReassignForm)
        ElMessage.success('批量转办成功')
        batchReassignDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '批量转办失败')
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
      loadEmployees()
    })

    return {
      loading,
      tableData,
      employeeList,
      pagination,
      columns,
      taskDialogVisible,
      reassignDialogVisible,
      delegateDialogVisible,
      batchReassignDialogVisible,
      selectedReassignPerson,
      selectedDelegatePerson,
      selectedBatchReassignPerson,
      taskForm,
      reassignForm,
      delegateForm,
      batchReassignForm,
      loadData,
      handleTask,
      submitTask,
      reassignTask,
      handleReassignPersonChange,
      submitReassign,
      delegateTask,
      handleDelegatePersonChange,
      submitDelegate,
      showBatchReassign,
      handleBatchReassignPersonChange,
      submitBatchReassign,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.todo-task-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .todo-task-list {
    padding: 8px;
  }

  .mt-4 {
    margin-top: 10px;
  }

  .el-page-header {
    padding: 8px 0;
  }

  .el-page-header__title {
    font-size: 15px;
  }
}
</style>
