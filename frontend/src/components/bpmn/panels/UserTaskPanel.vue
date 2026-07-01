<!--
  UserTask 节点属性面板
  - 由 BpmnDesigner / PropertyPanel 引用
  - 负责 UserTask 专属：处理人类型、9 种处理人配置、多实例、抄送、关联表单、DueDate、Priority
  - 表单状态由父组件 BpmnDesigner 通过 props.userTaskForm 传入（共享 reactive 对象）
  - 所有"写回 bpmn 元素"的副作用通过父组件 props.updateElementProp 触发
-->
<template>
  <el-form label-position="top" size="small">
    <!-- 1. 处理人类型 -->
    <el-divider>环节处理人</el-divider>
    <el-form-item label="处理人类型">
      <el-select
        v-model="userTaskForm.handlerType"
        placeholder="请选择处理人类型"
        style="width: 100%"
        @change="onHandlerTypeChange"
      >
        <el-option
          v-for="opt in handlerTypeOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <div class="hint">切换处理人类型会自动加载对应配置表单</div>
    </el-form-item>

    <!-- ASSIGNEE 指定人员 -->
    <template v-if="userTaskForm.handlerType === 'ASSIGNEE'">
      <el-form-item label="指定处理人">
        <div class="picker-row">
          <el-input v-model="userTaskForm.assigneeLabel" placeholder="点击右侧按钮选择" readonly />
          <el-button type="primary" size="small" @click="openDialog('assignee')">选择</el-button>
          <el-button v-if="userTaskForm.assigneeLabel" size="small" @click="clearAssignee">清空</el-button>
        </div>
      </el-form-item>
    </template>

    <!-- CANDIDATE_USERS 候选人 -->
    <template v-if="userTaskForm.handlerType === 'CANDIDATE_USERS'">
      <el-form-item label="候选人（多选）">
        <div class="picker-row">
          <el-input v-model="userTaskForm.candidateUsersLabel" placeholder="点击右侧按钮选择" readonly />
          <el-button type="primary" size="small" @click="openDialog('candidateUsers')">选择</el-button>
          <el-button v-if="userTaskForm.candidateUsersLabel" size="small" @click="clearCandidateUsers">清空</el-button>
        </div>
      </el-form-item>
    </template>

    <!-- CANDIDATE_GROUPS 候选角色 -->
    <template v-if="userTaskForm.handlerType === 'CANDIDATE_GROUPS'">
      <el-form-item label="候选角色（多选）">
        <div class="picker-row">
          <el-input v-model="userTaskForm.candidateGroupsLabel" placeholder="点击右侧按钮选择" readonly />
          <el-button type="primary" size="small" @click="openDialog('candidateGroups')">选择</el-button>
          <el-button v-if="userTaskForm.candidateGroupsLabel" size="small" @click="clearCandidateGroups">清空</el-button>
        </div>
      </el-form-item>
    </template>

    <!-- INITIATOR 发起人 -->
    <template v-if="userTaskForm.handlerType === 'INITIATOR'">
      <el-alert type="info" :closable="false" show-icon>
        流程发起人即为处理人，运行时自动取流程 startUserId。
      </el-alert>
    </template>

    <!-- DEPT_LEADER 发起人部门负责人 -->
    <template v-if="userTaskForm.handlerType === 'DEPT_LEADER'">
      <el-form-item label="部门范围">
        <el-radio-group v-model="userTaskForm.deptLeaderScope">
          <el-radio value="current">发起人所在部门</el-radio>
          <el-radio value="parent">发起人上级部门</el-radio>
          <el-radio value="root">顶级部门</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-alert type="info" :closable="false" show-icon>
        运行时通过流程变量 deptLeaderUserIds 解析，任务监听器注入。
      </el-alert>
    </template>

    <!-- ROLE_LEADER 角色负责人 -->
    <template v-if="userTaskForm.handlerType === 'ROLE_LEADER'">
      <el-form-item label="目标角色">
        <div class="picker-row">
          <el-input v-model="userTaskForm.roleLeaderLabel" readonly placeholder="点击选择角色" />
          <el-button type="primary" size="small" @click="openDialog('roleLeader')">选择</el-button>
          <el-button v-if="userTaskForm.roleLeaderLabel" size="small" @click="clearRoleLeader">清空</el-button>
        </div>
      </el-form-item>
    </template>

    <!-- PREV_ASSIGNEE 上一步处理人 -->
    <template v-if="userTaskForm.handlerType === 'PREV_ASSIGNEE'">
      <el-alert type="info" :closable="false" show-icon>
        上一节点的处理人，运行时通过流程变量 prevAssignee 注入。
      </el-alert>
    </template>

    <!-- FORM_FIELD 表单内人员字段 -->
    <template v-if="userTaskForm.handlerType === 'FORM_FIELD'">
      <el-form-item label="表单字段名">
        <el-input v-model="userTaskForm.formFieldName" placeholder="例如：managerId" />
        <div class="hint">运行时取业务表单中该字段值作为处理人</div>
      </el-form-item>
    </template>

    <!-- VARIABLE 流程变量 -->
    <template v-if="userTaskForm.handlerType === 'VARIABLE'">
      <el-form-item label="流程变量名">
        <el-input v-model="userTaskForm.varName" placeholder="例如：nextHandler" />
        <div class="hint">运行时取流程变量值作为处理人（变量值为用户名）</div>
      </el-form-item>
    </template>

    <!-- 2. 多实例会签 / 或签 -->
    <el-divider>多人审批方式</el-divider>
    <el-form-item label="审批方式">
      <el-select v-model="userTaskForm.multiInstanceType" style="width: 100%" @change="emitMultiInstance">
        <el-option label="单人审批" value="none" />
        <el-option label="会签（全部同意）" value="countersign" />
        <el-option label="或签（任一同意）" value="or_sign" />
        <el-option label="顺序会签" value="sequential" />
        <el-option label="按比例通过" value="ratio" />
      </el-select>
    </el-form-item>

    <el-form-item v-if="userTaskForm.multiInstanceType === 'ratio'" label="通过比例（%）">
      <el-input-number
        v-model="userTaskForm.multiInstanceRatio"
        :min="1"
        :max="100"
        controls-position="right"
      />
    </el-form-item>

    <!-- 3. 节点抄送 -->
    <el-divider>节点抄送</el-divider>
    <el-form-item label="触发时机">
      <el-radio-group v-model="userTaskForm.ccTriggerOn">
        <el-radio value="node_end">节点完成后</el-radio>
        <el-radio value="node_start">节点开始时</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="抄送人（多选）">
      <div class="picker-row">
        <el-input v-model="userTaskForm.ccUsersLabel" readonly placeholder="点击选择抄送人" />
        <el-button type="primary" size="small" @click="openDialog('ccUsers')">选择</el-button>
        <el-button v-if="userTaskForm.ccUsersLabel" size="small" @click="clearCcUsers">清空</el-button>
      </div>
    </el-form-item>
    <el-form-item label="抄送人姓名">
      <el-input v-model="userTaskForm.ccNames" placeholder="可选，附加抄送说明" />
    </el-form-item>

    <!-- 4. 关联表单 / 其它 -->
    <el-divider>关联表单</el-divider>
    <el-form-item label="Form Key（表单标识）">
      <el-input
        v-model="userTaskForm.formKey"
        placeholder="例如：leave_form"
        @change="updateElementProp('formKey', userTaskForm.formKey)"
      />
    </el-form-item>

    <el-form-item label="Due Date（到期时间表达式）">
      <el-input
        v-model="userTaskForm.dueDate"
        placeholder="例如：${date} 或 P3D"
        @change="updateElementProp('dueDate', userTaskForm.dueDate)"
      />
    </el-form-item>

    <el-form-item label="Priority（优先级）">
      <el-input-number
        v-model="userTaskForm.priority"
        :min="0"
        :max="100"
        controls-position="right"
        @change="updateElementProp('priority', userTaskForm.priority)"
      />
    </el-form-item>
  </el-form>

  <!-- 选人弹窗（Assignee） -->
  <SelectorDialog
    v-model="dialogs.assignee"
    title="选择 Assignee（单人）"
    :options="employeeOptions"
    :columns="employeeColumns"
    value-key="username"
    :selected="userTaskForm.assigneeValue"
    @confirm="onAssigneeConfirm"
  />

  <!-- 选人弹窗（CandidateUsers 多选） -->
  <SelectorDialog
    v-model="dialogs.candidateUsers"
    title="选择候选人（多选）"
    :options="employeeOptions"
    :columns="employeeColumns"
    value-key="username"
    :multiple="true"
    :selected="userTaskForm.candidateUsersValue"
    @confirm="onCandidateUsersConfirm"
  />

  <!-- 选角色弹窗（CandidateGroups 多选） -->
  <SelectorDialog
    v-model="dialogs.candidateGroups"
    title="选择候选角色（多选）"
    :options="roleOptions"
    :columns="roleColumns"
    value-key="roleCode"
    :multiple="true"
    :selected="userTaskForm.candidateGroupsValue"
    @confirm="onCandidateGroupsConfirm"
  />

  <!-- 选角色弹窗（RoleLeader） -->
  <SelectorDialog
    v-model="dialogs.roleLeader"
    title="选择目标角色"
    :options="roleOptions"
    :columns="roleColumns"
    value-key="roleCode"
    :selected="userTaskForm.roleLeaderCode"
    @confirm="onRoleLeaderConfirm"
  />

  <!-- 选人弹窗（CcUsers 多选） -->
  <SelectorDialog
    v-model="dialogs.ccUsers"
    title="选择抄送人（多选）"
    :options="employeeOptions"
    :columns="employeeColumns"
    value-key="username"
    :multiple="true"
    :selected="userTaskForm.ccUserNames"
    @confirm="onCcUsersConfirm"
  />
</template>

<script>
import { reactive } from 'vue'
import SelectorDialog from '@/components/SelectorDialog.vue'

export default {
  name: 'UserTaskPanel',
  components: { SelectorDialog },
  props: {
    /** 与父组件 BpmnDesigner 共享的 UserTask 表单 reactive 对象 */
    userTaskForm: { type: Object, required: true },
    /** 父组件提供的处理人类型选项 */
    handlerTypeOptions: { type: Array, required: true },
    /** 父组件提供的员工列表（用于 SelectorDialog） */
    employeeOptions: { type: Array, default: () => [] },
    /** 父组件提供的角色列表 */
    roleOptions: { type: Array, default: () => [] },
    /** SelectorDialog 表格列定义 */
    employeeColumns: { type: Array, required: true },
    roleColumns: { type: Array, required: true },
    /** 父组件提供的 bpmn 元素属性写入函数：updateElementProp(key, value) */
    updateElementProp: { type: Function, required: true }
  },
  emits: ['handler-type-change', 'multi-instance-change'],
  setup(props, { emit }) {
    // 5 个弹窗显隐状态
    const dialogs = reactive({
      assignee: false,
      candidateUsers: false,
      candidateGroups: false,
      roleLeader: false,
      ccUsers: false
    })

    /** 打开指定弹窗 */
    const openDialog = (key) => {
      dialogs[key] = true
    }

    // ===== 员工 / 角色 label 解析（username/roleCode → 名称） =====
    const resolveEmployeeLabel = (username) => {
      if (!username) return ''
      const e = props.employeeOptions.find((o) => o.username === username)
      if (!e) return username
      return e.employeeName ? `${e.employeeName}（${username}）` : username
    }
    const resolveRoleLabel = (code) => {
      if (!code) return ''
      const r = props.roleOptions.find((o) => o.roleCode === code)
      if (!r) return code
      return r.roleName ? `${r.roleName}（${code}）` : code
    }

    // ===== 处理人类型变化：把当前类型写到 bpmn 元素 =====
    const onHandlerTypeChange = () => {
      const t = props.userTaskForm.handlerType
      let assignee = ''
      let candidateUsers = ''
      let candidateGroups = ''

      switch (t) {
        case 'ASSIGNEE':
          assignee = props.userTaskForm.assigneeValue || ''
          break
        case 'CANDIDATE_USERS':
          candidateUsers = (props.userTaskForm.candidateUsersValue || []).join(',')
          break
        case 'CANDIDATE_GROUPS':
          candidateGroups = (props.userTaskForm.candidateGroupsValue || []).join(',')
          break
        case 'INITIATOR':
          assignee = '${initiator}'
          break
        case 'FORM_FIELD':
          assignee = '${' + (props.userTaskForm.formFieldName || 'managerId') + '}'
          break
        case 'VARIABLE':
          assignee = '${' + (props.userTaskForm.varName || 'nextHandler') + '}'
          break
        case 'PREV_ASSIGNEE':
          assignee = '${prevAssignee}'
          break
        case 'DEPT_LEADER':
        case 'ROLE_LEADER':
          assignee = '${nextHandler}'
          break
        default:
          break
      }

      // 通过父级一次写入三项
      props.updateElementProp('assignee', assignee)
      props.updateElementProp('candidateUsers', candidateUsers)
      props.updateElementProp('candidateGroups', candidateGroups)
      emit('handler-type-change', t)
    }

    // ===== 多实例变更 =====
    const emitMultiInstance = () => {
      emit('multi-instance-change', props.userTaskForm.multiInstanceType)
    }

    // ===== 弹窗 confirm 回调 =====
    const onAssigneeConfirm = (username) => {
      props.userTaskForm.assigneeValue = username || ''
      props.userTaskForm.assigneeLabel = resolveEmployeeLabel(username)
      onHandlerTypeChange()
    }
    const clearAssignee = () => {
      props.userTaskForm.assigneeValue = ''
      props.userTaskForm.assigneeLabel = ''
      onHandlerTypeChange()
    }

    const onCandidateUsersConfirm = (usernames) => {
      const list = Array.isArray(usernames) ? usernames : []
      props.userTaskForm.candidateUsersValue = list
      props.userTaskForm.candidateUsersLabel = list.map((u) => resolveEmployeeLabel(u)).filter(Boolean).join('、')
      onHandlerTypeChange()
    }
    const clearCandidateUsers = () => {
      props.userTaskForm.candidateUsersValue = []
      props.userTaskForm.candidateUsersLabel = ''
      onHandlerTypeChange()
    }

    const onCandidateGroupsConfirm = (codes) => {
      const list = Array.isArray(codes) ? codes : []
      props.userTaskForm.candidateGroupsValue = list
      props.userTaskForm.candidateGroupsLabel = list.map((g) => resolveRoleLabel(g)).filter(Boolean).join('、')
      onHandlerTypeChange()
    }
    const clearCandidateGroups = () => {
      props.userTaskForm.candidateGroupsValue = []
      props.userTaskForm.candidateGroupsLabel = ''
      onHandlerTypeChange()
    }

    const onRoleLeaderConfirm = (code) => {
      props.userTaskForm.roleLeaderCode = code || ''
      props.userTaskForm.roleLeaderLabel = resolveRoleLabel(code)
      onHandlerTypeChange()
    }
    const clearRoleLeader = () => {
      props.userTaskForm.roleLeaderCode = ''
      props.userTaskForm.roleLeaderLabel = ''
      onHandlerTypeChange()
    }

    const onCcUsersConfirm = (usernames) => {
      const list = Array.isArray(usernames) ? usernames : []
      props.userTaskForm.ccUserNames = list
      props.userTaskForm.ccUsersLabel = list.map((u) => resolveEmployeeLabel(u)).filter(Boolean).join('、')
    }
    const clearCcUsers = () => {
      props.userTaskForm.ccUserNames = []
      props.userTaskForm.ccUsersLabel = ''
    }

    return {
      dialogs,
      openDialog,
      resolveEmployeeLabel,
      resolveRoleLabel,
      onHandlerTypeChange,
      emitMultiInstance,
      onAssigneeConfirm,
      clearAssignee,
      onCandidateUsersConfirm,
      clearCandidateUsers,
      onCandidateGroupsConfirm,
      clearCandidateGroups,
      onRoleLeaderConfirm,
      clearRoleLeader,
      onCcUsersConfirm,
      clearCcUsers
    }
  }
}
</script>

<style scoped>
.picker-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.picker-row .el-input {
  flex: 1;
  min-width: 0;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
:deep(.el-divider) {
  margin: 18px 0 12px;
}
:deep(.el-divider .el-divider__text) {
  font-weight: 600;
  color: #1f2937;
}
</style>
