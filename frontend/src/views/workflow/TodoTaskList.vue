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
          <el-button size="small" type="warning" @click="openRejectDialog(row)">驳回</el-button>
          <el-dropdown size="small" trigger="click" @command="(cmd) => handleMoreAction(cmd, row)">
            <el-button size="small">
              更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="addSign">加签</el-dropdown-item>
                <el-dropdown-item command="removeSign">减签</el-dropdown-item>
                <el-dropdown-item command="recall" divided>取回</el-dropdown-item>
                <el-dropdown-item command="transfer">转签</el-dropdown-item>
                <el-dropdown-item command="urge" divided>催办</el-dropdown-item>
                <el-dropdown-item command="jump">自由跳转</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <!-- 驳回任务对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回任务" width="600px" :close-on-click-modal="false">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="驳回模式">
          <el-radio-group v-model="rejectForm.rejectType">
            <el-radio value="PREVIOUS">驳回到上一节点</el-radio>
            <el-radio value="INITIATOR">驳回到发起人</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 当选 PREVIOUS 且后端有历史节点可驳回时，显示提示信息 -->
        <el-form-item v-if="rejectForm.rejectType === 'PREVIOUS' && rejectTargets.length" label="目标节点">
          <el-tag v-for="t in rejectTargets.slice(0, 1)" :key="t.activityId" type="info" class="target-tag">
            {{ t.activityName || t.activityId }} · {{ t.assigneeName || t.assignee || '系统' }}
          </el-tag>
          <span v-if="rejectTargets.length > 1" class="target-hint">（共 {{ rejectTargets.length }} 个历史节点，将驳回到最近的一个）</span>
        </el-form-item>

        <el-alert
          v-if="rejectForm.rejectType === 'PREVIOUS' && !rejectTargets.length"
          type="warning"
          :closable="false"
          title="当前任务为首节点，无可驳回的上一步"
          show-icon
        />

        <el-form-item label="驳回原因" prop="reason" :rules="[{ required: true, message: '请输入驳回原因', trigger: 'blur' }]">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="rejectSubmitting" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 加签/减签对话框（共用） -->
    <el-dialog v-model="signDialogVisible" :title="signDialogTitle" width="1000px" :close-on-click-modal="false">
      <el-form :model="signForm" label-width="100px">
        <el-form-item label="选择人员">
          <el-input :model-value="signForm.userName" disabled placeholder="点击下方选择人员" />
        </el-form-item>
        <el-form-item>
          <PersonSelector
            v-model="selectedSignPerson"
            @change="handleSignPersonChange"
            style="height: 400px;"
          />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="signForm.reason" type="textarea" :rows="3" placeholder="请输入加签/减签原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="signSubmitting" @click="submitSign">确认</el-button>
      </template>
    </el-dialog>

    <!-- 取回对话框 -->
    <el-dialog v-model="recallDialogVisible" title="取回任务" width="500px" :close-on-click-modal="false">
      <el-alert
        type="info"
        :closable="false"
        title="取回规则：仅发起人 / 上一节点审批人可取回，流程将跳回上一节点由您重新处理"
        show-icon
        class="recall-tip"
      />
      <el-form :model="recallForm" label-width="100px" style="margin-top: 12px;">
        <el-form-item label="取回原因" prop="reason" :rules="[{ required: true, message: '请输入取回原因', trigger: 'blur' }]">
          <el-input v-model="recallForm.reason" type="textarea" :rows="4" placeholder="请输入取回原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recallDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="recallSubmitting" @click="submitRecall">确认取回</el-button>
      </template>
    </el-dialog>

    <!-- 自由跳转对话框 -->
    <el-dialog v-model="jumpDialogVisible" title="自由跳转" width="500px" :close-on-click-modal="false">
      <el-alert
        type="warning"
        :closable="false"
        title="警告：自由跳转是高权限操作，会强制把流程实例跳到指定节点，请确认后再操作"
        show-icon
        class="jump-tip"
      />
      <el-form :model="jumpForm" label-width="100px" style="margin-top: 12px;">
        <el-form-item label="当前任务">
          <el-input :model-value="jumpForm.taskId" disabled />
        </el-form-item>
        <el-form-item label="目标节点" prop="targetActivityId" :rules="[{ required: true, message: '请输入目标节点 activityId', trigger: 'blur' }]">
          <el-input v-model="jumpForm.targetActivityId" placeholder="如：UserTask_Approve" />
        </el-form-item>
        <el-form-item label="跳转原因" prop="reason" :rules="[{ required: true, message: '请输入跳转原因', trigger: 'blur' }]">
          <el-input v-model="jumpForm.reason" type="textarea" :rows="4" placeholder="请输入跳转原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="jumpDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="jumpSubmitting" @click="submitJump">确认跳转</el-button>
      </template>
    </el-dialog>

    <!-- 催办对话框 -->
    <el-dialog v-model="urgeDialogVisible" title="催办" width="1000px" :close-on-click-modal="false">
      <el-form :model="urgeForm" label-width="100px">
        <el-form-item label="催办人">
          <el-input :model-value="urgeForm.toUserName" disabled placeholder="点击下方选择接收人" />
        </el-form-item>
        <el-form-item>
          <PersonSelector
            v-model="selectedUrgePerson"
            @change="handleUrgePersonChange"
            style="height: 400px;"
          />
        </el-form-item>
        <el-form-item label="催办内容" prop="content" :rules="[{ required: true, message: '请输入催办内容', trigger: 'blur' }]">
          <el-input v-model="urgeForm.content" type="textarea" :rows="4" placeholder="如：请尽快处理本流程" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="urgeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="urgeSubmitting" @click="submitUrge">确认催办</el-button>
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
import { pageTodoTask, completeTask, reassignTask, delegateTask, batchReassignTask, listRejectTargets, rejectToPrevious, rejectToInitiator, addSign, removeSign, recallTask, jumpActivity } from '@/api/workflow/task'
import { urge } from '@/api/workflow/urge'

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
      { prop: 'actions', label: '操作', width: 440, fixed: 'right' }
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

    // 驳回相关状态
    const rejectDialogVisible = ref(false)
    const rejectSubmitting = ref(false)
    const rejectTargets = ref([]) // 可驳回目标节点列表（按历史倒序）
    const rejectForm = reactive({
      taskId: '',
      rejectType: 'PREVIOUS', // PREVIOUS / INITIATOR
      reason: ''
    })

    // 加签/减签状态
    const signDialogVisible = ref(false)
    const signDialogTitle = ref('加签') // "加签" / "减签"
    const signSubmitting = ref(false)
    const signMode = ref('add') // add / remove
    const selectedSignPerson = ref(null)
    const signForm = reactive({
      taskId: '',
      userId: null,
      userName: '',
      reason: ''
    })

    // 取回状态
    const recallDialogVisible = ref(false)
    const recallSubmitting = ref(false)
    const recallForm = reactive({
      taskId: '',
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

    // ====================== 驳回相关 ======================

    /**
     * 打开驳回弹窗：先拉取可驳回目标节点（PREVIOUS 模式下用）
     */
    const openRejectDialog = async (row) => {
      if (!row?.id) return
      currentTask.value = row
      rejectForm.taskId = row.id
      rejectForm.rejectType = 'PREVIOUS'
      rejectForm.reason = ''
      rejectTargets.value = []
      rejectDialogVisible.value = true
      try {
        const res = await listRejectTargets(row.id)
        rejectTargets.value = res || []
      } catch (error) {
        // 历史为空时也允许打开（驳回发起人不需要历史）
        rejectTargets.value = []
      }
    }

    /**
     * 提交驳回（按 rejectType 选不同 API）
     */
    const submitReject = async () => {
      if (!rejectForm.reason || !rejectForm.reason.trim()) {
        ElMessage.warning('请输入驳回原因')
        return
      }
      if (rejectForm.rejectType === 'PREVIOUS' && !rejectTargets.value.length) {
        ElMessage.warning('当前任务为首节点，无可驳回的上一步')
        return
      }
      rejectSubmitting.value = true
      try {
        if (rejectForm.rejectType === 'PREVIOUS') {
          await rejectToPrevious({
            taskId: rejectForm.taskId,
            rejectType: 'PREVIOUS',
            reason: rejectForm.reason.trim()
          })
          ElMessage.success('已驳回到上一节点')
        } else if (rejectForm.rejectType === 'INITIATOR') {
          await rejectToInitiator({
            taskId: rejectForm.taskId,
            rejectType: 'INITIATOR',
            reason: rejectForm.reason.trim()
          })
          ElMessage.success('已驳回到发起人')
        }
        rejectDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '驳回失败')
      } finally {
        rejectSubmitting.value = false
      }
    }

    // ====================== 加签/减签/取回/转签 ======================

    /**
     * "更多"下拉菜单触发
     */
    const handleMoreAction = (cmd, row) => {
      if (cmd === 'addSign') {
        openSignDialog(row, 'add')
      } else if (cmd === 'removeSign') {
        openSignDialog(row, 'remove')
      } else if (cmd === 'recall') {
        openRecallDialog(row)
      } else if (cmd === 'transfer') {
        // 转签 = 复用转办弹窗，但 reason 前缀标记为"转签"
        reassignTask(row)
        reassignForm.reason = '【转签】' + (reassignForm.reason || '')
      } else if (cmd === 'urge') {
        openUrgeDialog(row)
      } else if (cmd === 'jump') {
        openJumpDialog(row)
      }
    }

    /**
     * 打开加签/减签弹窗
     */
    const openSignDialog = (row, mode) => {
      signMode.value = mode
      signDialogTitle.value = mode === 'add' ? '加签' : '减签'
      signForm.taskId = row.id
      signForm.userId = null
      signForm.userName = ''
      signForm.reason = ''
      selectedSignPerson.value = null
      signDialogVisible.value = true
    }

    const handleSignPersonChange = (val) => {
      selectedSignPerson.value = val
      if (val) {
        signForm.userId = val.userId || val.id
        signForm.userName = val.employeeName || val.name || val.userName || ''
      } else {
        signForm.userId = null
        signForm.userName = ''
      }
    }

    const submitSign = async () => {
      if (!signForm.userId) {
        ElMessage.warning('请选择人员')
        return
      }
      signSubmitting.value = true
      try {
        if (signMode.value === 'add') {
          await addSign({
            taskId: signForm.taskId,
            userId: signForm.userId,
            reason: signForm.reason || '加签'
          })
          ElMessage.success('加签成功')
        } else {
          await removeSign({
            taskId: signForm.taskId,
            userId: signForm.userId,
            reason: signForm.reason || '减签'
          })
          ElMessage.success('减签成功')
        }
        signDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        signSubmitting.value = false
      }
    }

    /**
     * 打开取回弹窗
     */
    const openRecallDialog = (row) => {
      recallForm.taskId = row.id
      recallForm.reason = ''
      recallDialogVisible.value = true
    }

    const submitRecall = async () => {
      if (!recallForm.reason || !recallForm.reason.trim()) {
        ElMessage.warning('请输入取回原因')
        return
      }
      recallSubmitting.value = true
      try {
        await recallTask({
          taskId: recallForm.taskId,
          reason: recallForm.reason.trim()
        })
        ElMessage.success('已成功取回任务')
        recallDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '取回失败')
      } finally {
        recallSubmitting.value = false
      }
    }

    // ====================== 自由跳转 ======================

    /**
     * 打开自由跳转弹窗
     */
    const openJumpDialog = (row) => {
      jumpForm.taskId = row.id
      jumpForm.targetActivityId = ''
      jumpForm.reason = ''
      jumpDialogVisible.value = true
    }

    const submitJump = async () => {
      if (!jumpForm.targetActivityId || !jumpForm.targetActivityId.trim()) {
        ElMessage.warning('请输入目标节点 activityId')
        return
      }
      if (!jumpForm.reason || !jumpForm.reason.trim()) {
        ElMessage.warning('请输入跳转原因')
        return
      }
      jumpSubmitting.value = true
      try {
        await jumpActivity({
          taskId: jumpForm.taskId,
          targetActivityId: jumpForm.targetActivityId.trim(),
          reason: jumpForm.reason.trim()
        })
        ElMessage.success('自由跳转成功')
        jumpDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '自由跳转失败')
      } finally {
        jumpSubmitting.value = false
      }
    }

    // ====================== 催办 ======================

    /**
     * 打开催办弹窗
     */
    const openUrgeDialog = (row) => {
      urgeForm.taskId = row.id
      urgeForm.toUserId = null
      urgeForm.toUserName = ''
      urgeForm.content = ''
      selectedUrgePerson.value = null
      urgeDialogVisible.value = true
    }

    const handleUrgePersonChange = (val) => {
      selectedUrgePerson.value = val
      if (val) {
        urgeForm.toUserId = val.userId || val.id
        urgeForm.toUserName = val.employeeName || val.name || val.userName || ''
      } else {
        urgeForm.toUserId = null
        urgeForm.toUserName = ''
      }
    }

    const submitUrge = async () => {
      if (!urgeForm.toUserId) {
        ElMessage.warning('请选择催办接收人')
        return
      }
      if (!urgeForm.content || !urgeForm.content.trim()) {
        ElMessage.warning('请输入催办内容')
        return
      }
      urgeSubmitting.value = true
      try {
        await urge({
          taskId: urgeForm.taskId,
          toUserId: urgeForm.toUserId,
          content: urgeForm.content.trim()
        })
        ElMessage.success('催办成功')
        urgeDialogVisible.value = false
      } catch (error) {
        ElMessage.error(error.message || '催办失败')
      } finally {
        urgeSubmitting.value = false
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
      rejectDialogVisible,
      rejectSubmitting,
      rejectTargets,
      rejectForm,
      signDialogVisible,
      signDialogTitle,
      signSubmitting,
      selectedSignPerson,
      signForm,
      recallDialogVisible,
      recallSubmitting,
      recallForm,
      jumpDialogVisible,
      jumpSubmitting,
      jumpForm,
      urgeDialogVisible,
      urgeSubmitting,
      selectedUrgePerson,
      urgeForm,
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
      openRejectDialog,
      submitReject,
      handleMoreAction,
      handleSignPersonChange,
      submitSign,
      submitRecall,
      submitJump,
      handleUrgePersonChange,
      submitUrge,
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
