<template>
  <div class="bpmn-designer">
    <div class="toolbar">
      <el-button size="small" @click="save">保存</el-button>
      <el-button size="small" @click="exportXml">导出XML</el-button>
      <el-button size="small" @click="importXml">导入XML</el-button>
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        accept=".bpmn,.xml"
        @change="handleFileImport"
      />
      <el-button size="small" @click="zoomIn">放大</el-button>
      <el-button size="small" @click="zoomOut">缩小</el-button>
      <el-button size="small" @click="zoomReset">重置</el-button>
    </div>
    <div class="content">
      <div class="canvas" ref="canvas"></div>

      <!-- 节点属性侧栏 -->
      <div class="property-panel" v-show="showPanel">
        <div class="panel-header">
          <span class="panel-title">{{ panelTitle }}</span>
          <el-button text type="primary" size="small" @click="clearSelection">清空选择</el-button>
        </div>
        <div class="panel-body">
          <!-- 节点基础信息：所有可命名节点都展示 -->
          <el-form v-if="baseForm" label-position="top" size="small">
            <el-form-item label="节点ID">
              <el-input v-model="baseForm.id" disabled />
            </el-form-item>
            <el-form-item label="节点名称">
              <el-input
                v-model="baseForm.name"
                placeholder="请输入节点名称"
                @change="onBasePropChange('name', baseForm.name)"
              />
            </el-form-item>
          </el-form>

          <!-- UserTask 专属：处理人 / 表单 / 多实例 / 抄送 -->
          <template v-if="isUserTask">
            <!-- 1. 处理人类型 -->
            <el-divider>环节处理人</el-divider>
            <el-form label-position="top" size="small">
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
                    <el-input
                      v-model="userTaskForm.assigneeLabel"
                      placeholder="点击右侧按钮选择"
                      readonly
                    />
                    <el-button type="primary" size="small" @click="openAssigneeDialog">选择</el-button>
                    <el-button v-if="userTaskForm.assigneeLabel" size="small" @click="clearAssignee">清空</el-button>
                  </div>
                </el-form-item>
              </template>

              <!-- CANDIDATE_USERS 候选人 -->
              <template v-if="userTaskForm.handlerType === 'CANDIDATE_USERS'">
                <el-form-item label="候选人（多选）">
                  <div class="picker-row">
                    <el-input
                      v-model="userTaskForm.candidateUsersLabel"
                      placeholder="点击右侧按钮选择"
                      readonly
                    />
                    <el-button type="primary" size="small" @click="openCandidateUsersDialog">选择</el-button>
                    <el-button v-if="userTaskForm.candidateUsersLabel" size="small" @click="clearCandidateUsers">清空</el-button>
                  </div>
                </el-form-item>
              </template>

              <!-- CANDIDATE_GROUPS 候选角色 -->
              <template v-if="userTaskForm.handlerType === 'CANDIDATE_GROUPS'">
                <el-form-item label="候选角色（多选）">
                  <div class="picker-row">
                    <el-input
                      v-model="userTaskForm.candidateGroupsLabel"
                      placeholder="点击右侧按钮选择"
                      readonly
                    />
                    <el-button type="primary" size="small" @click="openCandidateGroupsDialog">选择</el-button>
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
                    <el-button type="primary" size="small" @click="openRoleLeaderDialog">选择</el-button>
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
                  <el-input
                    v-model="userTaskForm.formFieldName"
                    placeholder="例如：managerId"
                  />
                  <div class="hint">运行时取业务表单中该字段值作为处理人</div>
                </el-form-item>
              </template>

              <!-- VARIABLE 流程变量 -->
              <template v-if="userTaskForm.handlerType === 'VARIABLE'">
                <el-form-item label="流程变量名">
                  <el-input
                    v-model="userTaskForm.varName"
                    placeholder="例如：nextHandler"
                  />
                  <div class="hint">运行时取流程变量值作为处理人（变量值为用户名）</div>
                </el-form-item>
              </template>

              <!-- 2. 多实例会签 / 或签 -->
              <el-divider>多人审批方式</el-divider>
              <el-form-item label="审批方式">
                <el-select v-model="userTaskForm.multiInstanceType" style="width: 100%" @change="applyMultiInstance">
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
                  <el-button type="primary" size="small" @click="openCcUsersDialog">选择</el-button>
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
                  @change="onUserTaskPropChange('formKey', userTaskForm.formKey)"
                />
              </el-form-item>

              <el-form-item label="Due Date（到期时间表达式）">
                <el-input
                  v-model="userTaskForm.dueDate"
                  placeholder="例如：${date} 或 P3D"
                  @change="onUserTaskPropChange('dueDate', userTaskForm.dueDate)"
                />
              </el-form-item>

              <el-form-item label="Priority（优先级）">
                <el-input-number
                  v-model="userTaskForm.priority"
                  :min="0"
                  :max="100"
                  controls-position="right"
                  @change="onUserTaskPropChange('priority', userTaskForm.priority)"
                />
              </el-form-item>
            </el-form>
          </template>

          <el-empty
            v-if="!isUserTask && !baseForm"
            description="请在画布中选择一个节点"
            :image-size="80"
          />
        </div>
      </div>
    </div>

    <!-- 选人弹窗 -->
    <SelectorDialog
      v-model="dialogs.assignee.visible"
      title="选择 Assignee（单人）"
      :options="employeeOptions"
      :columns="employeeColumns"
      value-key="username"
      :selected="userTaskForm.assigneeValue"
      @confirm="onAssigneeConfirm"
    />

    <!-- 选多人弹窗 -->
    <SelectorDialog
      v-model="dialogs.candidateUsers.visible"
      title="选择候选人（多选）"
      :options="employeeOptions"
      :columns="employeeColumns"
      value-key="username"
      :multiple="true"
      :selected="userTaskForm.candidateUsersValue"
      @confirm="onCandidateUsersConfirm"
    />

    <!-- 选角色弹窗 -->
    <SelectorDialog
      v-model="dialogs.candidateGroups.visible"
      title="选择候选角色（多选）"
      :options="roleOptions"
      :columns="roleColumns"
      value-key="roleCode"
      :multiple="true"
      :selected="userTaskForm.candidateGroupsValue"
      @confirm="onCandidateGroupsConfirm"
    />

    <!-- 角色负责人选角色 -->
    <SelectorDialog
      v-model="dialogs.roleLeader.visible"
      title="选择目标角色"
      :options="roleOptions"
      :columns="roleColumns"
      value-key="roleCode"
      :selected="userTaskForm.roleLeaderCode"
      @confirm="onRoleLeaderConfirm"
    />

    <!-- 抄送人选人 -->
    <SelectorDialog
      v-model="dialogs.ccUsers.visible"
      title="选择抄送人（多选）"
      :options="employeeOptions"
      :columns="employeeColumns"
      value-key="username"
      :multiple="true"
      :selected="userTaskForm.ccUserNames"
      @confirm="onCcUsersConfirm"
    />
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import chineseTranslation from 'bpmn-js-i18n/translations/zn'
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json'
import SelectorDialog from './SelectorDialog.vue'
import { listAllEmployees } from '@/api/system/employee'
import { listAllRoles } from '@/api/system/role'
import { getDictDataByCodes } from '@/api/dict'
import { saveBatchNodeHandler } from '@/api/workflow/nodeHandler'

export default {
  name: 'BpmnDesigner',
  components: { SelectorDialog },
  props: {
    xml: {
      type: String,
      default: ''
    },
    /**
     * 流程定义 ID（编辑已有流程时传入），用于按 processDefinitionId 加载/保存节点处理人配置
     */
    processDefinitionId: {
      type: String,
      default: ''
    },
    /**
     * 流程定义 KEY（部署后从 BPMN 解析得到，可通过 ref 暴露给父组件）
     */
    processDefinitionKey: {
      type: String,
      default: ''
    }
  },
  emits: ['save', 'change', 'process-key-change'],
  setup(props, { emit }) {
    const canvas = ref(null)
    const fileInput = ref(null)
    const modeler = ref(null)

    const selectedElement = ref(null)
    const baseForm = ref(null)

    // UserTask 专属表单
    const userTaskForm = reactive({
      handlerType: 'ASSIGNEE',
      // 指定人员
      assigneeValue: '',
      assigneeLabel: '',
      // 候选人
      candidateUsersValue: [],
      candidateUsersLabel: '',
      // 候选角色
      candidateGroupsValue: [],
      candidateGroupsLabel: '',
      // 部门负责人
      deptLeaderScope: 'current',
      // 角色负责人
      roleLeaderCode: '',
      roleLeaderLabel: '',
      // 表单字段
      formFieldName: '',
      // 流程变量
      varName: '',
      // 多实例
      multiInstanceType: 'none',
      multiInstanceRatio: 60,
      // 抄送
      ccTriggerOn: 'node_end',
      ccUserNames: [],
      ccUsersLabel: '',
      ccNames: '',
      // 其它
      formKey: '',
      dueDate: '',
      priority: 50
    })

    const dialogs = reactive({
      assignee: { visible: false },
      candidateUsers: { visible: false },
      candidateGroups: { visible: false },
      roleLeader: { visible: false },
      ccUsers: { visible: false }
    })

    const employeeOptions = ref([])
    const roleOptions = ref([])
    const handlerTypeOptions = ref([
      { label: '指定人员', value: 'ASSIGNEE' },
      { label: '候选人', value: 'CANDIDATE_USERS' },
      { label: '候选角色', value: 'CANDIDATE_GROUPS' },
      { label: '发起人', value: 'INITIATOR' },
      { label: '发起人部门负责人', value: 'DEPT_LEADER' },
      { label: '角色负责人', value: 'ROLE_LEADER' },
      { label: '上一步处理人', value: 'PREV_ASSIGNEE' },
      { label: '表单内人员字段', value: 'FORM_FIELD' },
      { label: '流程变量', value: 'VARIABLE' }
    ])

    const employeeColumns = [
      { prop: 'employeeName', label: '姓名', width: 100 },
      { prop: 'username', label: '登录账号', width: 120 },
      { prop: 'employeeCode', label: '工号', width: 120 }
    ]
    const roleColumns = [
      { prop: 'roleName', label: '角色名称', width: 140 },
      { prop: 'roleCode', label: '角色编码', width: 160 }
    ]

    const showPanel = computed(() => !!selectedElement.value)
    const panelTitle = computed(() => {
      if (!selectedElement.value) return '属性'
      const bo = selectedElement.value.businessObject
      return `${bo.$type || 'Element'} 属性`
    })
    const isUserTask = computed(() => selectedElement.value?.type === 'bpmn:UserTask')

    const customTranslate = {
      translate: ['value', function (template, replacements) {
        replacements = replacements || {}
        let translation = chineseTranslation[template]
        if (!translation) translation = template
        return translation.replace(/{([^}]+)}/g, function (_, key) {
          return replacements[key] || '{' + key + '}'
        })
      }]
    }

    let importing = false
    let pendingXml = null

    /**
     * 初始化 BpmnModeler
     */
    const initModeler = () => {
      modeler.value = new BpmnModeler({
        container: canvas.value,
        keyboard: { bindTo: window },
        additionalModules: [customTranslate],
        moddleExtensions: { camunda: camundaModdleDescriptor }
      })

      modeler.value.on('commandStack.changed', () => {
        modeler.value.saveXML({ format: true }).then((result) => {
          emit('change', result.xml)
          // 解析并通知 processDefinitionKey
          try {
            const processMatch = result.xml.match(/<bpmn:process[^>]*\bid="([^"]+)"/)
            if (processMatch) {
              emit('process-key-change', processMatch[1])
            }
          } catch (e) {
            /* ignore */
          }
        })
      })

      const eventBus = modeler.value.get('eventBus')
      eventBus.on('selection.changed', (e) => {
        const sel = (e.newSelection && e.newSelection[0]) || null
        handleElementSelected(sel)
      })
      eventBus.on('element.changed', (e) => {
        if (selectedElement.value && e.element && e.element.id === selectedElement.value.id) {
          fillFormFromElement(e.element)
        }
      })

      importBpmn(props.xml)
    }

    /**
     * 选中节点后填充表单
     */
    const handleElementSelected = (element) => {
      selectedElement.value = element
      if (!element) {
        baseForm.value = null
        return
      }
      fillFormFromElement(element)
    }

    /**
     * 从 bpmn 元素读取属性并填充表单
     */
    const fillFormFromElement = (element) => {
      const bo = element.businessObject
      baseForm.value = {
        id: bo.id || '',
        name: bo.name || ''
      }
      if (element.type === 'bpmn:UserTask') {
        // 解析 handlerConfig (若元素上存在 camunda:assignee 等，写回表单)
        const assignee = bo.assignee || ''
        const candidateUsersStr = bo.candidateUsers || ''
        const candidateGroupsStr = bo.candidateGroups || ''

        // 默认根据已有信息推断 handlerType
        if (assignee && assignee.startsWith('${') && assignee.includes('initiator')) {
          userTaskForm.handlerType = 'INITIATOR'
        } else if (assignee) {
          userTaskForm.handlerType = 'ASSIGNEE'
        } else if (candidateUsersStr) {
          userTaskForm.handlerType = 'CANDIDATE_USERS'
        } else if (candidateGroupsStr) {
          userTaskForm.handlerType = 'CANDIDATE_GROUPS'
        } else {
          userTaskForm.handlerType = 'ASSIGNEE'
        }

        userTaskForm.assigneeValue = assignee
        userTaskForm.candidateUsersValue = candidateUsersStr
          ? candidateUsersStr.split(',').map((s) => s.trim()).filter(Boolean)
          : []
        userTaskForm.candidateGroupsValue = candidateGroupsStr
          ? candidateGroupsStr.split(',').map((s) => s.trim()).filter(Boolean)
          : []

        userTaskForm.formKey = bo.formKey || ''
        userTaskForm.dueDate = bo.dueDate || ''
        userTaskForm.priority = bo.priority != null ? Number(bo.priority) : 50

        // 解析多实例：bpmn 元素的 loopCharacteristics
        const loop = bo.loopCharacteristics
        if (loop) {
          userTaskForm.multiInstanceType = loop.isSequential ? 'sequential' : 'countersign'
        } else {
          userTaskForm.multiInstanceType = 'none'
        }

        userTaskForm.assigneeLabel = resolveEmployeeLabel(assignee)
        userTaskForm.candidateUsersLabel = userTaskForm.candidateUsersValue
          .map((u) => resolveEmployeeLabel(u))
          .filter(Boolean).join('、')
        userTaskForm.candidateGroupsLabel = userTaskForm.candidateGroupsValue
          .map((g) => resolveRoleLabel(g))
          .filter(Boolean).join('、')
      }
    }

    const resolveEmployeeLabel = (username) => {
      if (!username) return ''
      const e = employeeOptions.value.find((o) => o.username === username)
      if (!e) return username
      return e.employeeName ? `${e.employeeName}（${username}）` : username
    }
    const resolveRoleLabel = (code) => {
      if (!code) return ''
      const r = roleOptions.value.find((o) => o.roleCode === code)
      if (!r) return code
      return r.roleName ? `${r.roleName}（${code}）` : code
    }

    const updateElementProperty = (key, value) => {
      if (!selectedElement.value || !modeler.value) return
      const modeling = modeler.value.get('modeling')
      modeling.updateProperties(selectedElement.value, { [key]: value })
    }

    const onBasePropChange = (key, value) => updateElementProperty(key, value)
    const onUserTaskPropChange = (key, value) => updateElementProperty(key, value)

    /**
     * 处理人类型变化：把当前类型写到 bpmn 元素的 camunda 命名空间
     * 注意：bpmn-js 的 camunda 扩展支持的属性：
     *   - assignee（单值）
     *   - candidateUsers（字符串，逗号分隔）
     *   - candidateGroups（字符串，逗号分隔）
     * 对 DEPT_LEADER / PREV_ASSIGNEE / FORM_FIELD / VARIABLE 这类需要运行时解析的类型，
     * 这里统一以表达式占位，真实解析在任务监听器中完成。
     */
    const onHandlerTypeChange = () => {
      if (!selectedElement.value || selectedElement.value.type !== 'bpmn:UserTask') return
      const modeling = modeler.value.get('modeling')
      const bo = selectedElement.value.businessObject
      const t = userTaskForm.handlerType

      let assignee = ''
      let candidateUsers = ''
      let candidateGroups = ''

      switch (t) {
        case 'ASSIGNEE':
          assignee = userTaskForm.assigneeValue || ''
          break
        case 'CANDIDATE_USERS':
          candidateUsers = (userTaskForm.candidateUsersValue || []).join(',')
          break
        case 'CANDIDATE_GROUPS':
          candidateGroups = (userTaskForm.candidateGroupsValue || []).join(',')
          break
        case 'INITIATOR':
          assignee = '${initiator}'
          break
        case 'FORM_FIELD':
          assignee = '${' + (userTaskForm.formFieldName || 'managerId') + '}'
          break
        case 'VARIABLE':
          assignee = '${' + (userTaskForm.varName || 'nextHandler') + '}'
          break
        case 'PREV_ASSIGNEE':
          assignee = '${prevAssignee}'
          break
        case 'DEPT_LEADER':
        case 'ROLE_LEADER':
          // 复杂类型：使用占位表达式，由 TaskListener 注入
          assignee = '${nextHandler}'
          break
        default:
          break
      }

      modeling.updateProperties(selectedElement.value, {
        assignee,
        candidateUsers,
        candidateGroups
      })
    }

    /**
     * 多实例会签 / 或签：将 loopCharacteristics 写到 bpmn 元素
     */
    const applyMultiInstance = () => {
      if (!selectedElement.value || selectedElement.value.type !== 'bpmn:UserTask' || !modeler.value) return
      const modeling = modeler.value.get('modeling')
      const bo = selectedElement.value.businessObject
      const moddle = modeler.value.get('moddle')
      const bpmnReplace = modeler.value.get('bpmnReplace', false)

      const t = userTaskForm.multiInstanceType
      if (t === 'none') {
        if (bo.loopCharacteristics) {
          modeling.updateProperties(selectedElement.value, { loopCharacteristics: undefined })
        }
        return
      }

      const isSequential = t === 'sequential'
      const isRatio = t === 'ratio'
      const props = {
        isSequential,
        // 集合默认走 candidateUsers/candidateGroups 或者 assignee；用 camunda:Collection 表达式
        'camunda:collection': '${multiInstanceUsers}',
        'camunda:elementVariable': 'assignee'
      }
      if (isRatio) {
        props['camunda:completionCondition'] =
          `nrOfCompletedInstances/nrOfInstances >= ${userTaskForm.multiInstanceRatio / 100}`
      } else if (t === 'countersign') {
        props['camunda:completionCondition'] = 'nrOfCompletedInstances/nrOfInstances >= 1'
      } else if (t === 'or_sign') {
        props['camunda:completionCondition'] = 'nrOfCompletedInstances >= 1'
      } else if (t === 'sequential') {
        props['camunda:completionCondition'] = 'nrOfCompletedInstances/nrOfInstances >= 1'
      }

      const loopCharacteristics = moddle.create('bpmn:MultiInstanceLoopCharacteristics', props)
      modeling.updateProperties(selectedElement.value, { loopCharacteristics })
    }

    /**
     * 选 Assignee 弹窗
     */
    const openAssigneeDialog = () => { dialogs.assignee.visible = true }
    const onAssigneeConfirm = (username) => {
      userTaskForm.assigneeValue = username || ''
      userTaskForm.assigneeLabel = resolveEmployeeLabel(username)
      onHandlerTypeChange()
    }
    const clearAssignee = () => {
      userTaskForm.assigneeValue = ''
      userTaskForm.assigneeLabel = ''
      onHandlerTypeChange()
    }

    const openCandidateUsersDialog = () => { dialogs.candidateUsers.visible = true }
    const onCandidateUsersConfirm = (usernames) => {
      const list = Array.isArray(usernames) ? usernames : []
      userTaskForm.candidateUsersValue = list
      userTaskForm.candidateUsersLabel = list.map((u) => resolveEmployeeLabel(u)).filter(Boolean).join('、')
      onHandlerTypeChange()
    }
    const clearCandidateUsers = () => {
      userTaskForm.candidateUsersValue = []
      userTaskForm.candidateUsersLabel = ''
      onHandlerTypeChange()
    }

    const openCandidateGroupsDialog = () => { dialogs.candidateGroups.visible = true }
    const onCandidateGroupsConfirm = (codes) => {
      const list = Array.isArray(codes) ? codes : []
      userTaskForm.candidateGroupsValue = list
      userTaskForm.candidateGroupsLabel = list.map((g) => resolveRoleLabel(g)).filter(Boolean).join('、')
      onHandlerTypeChange()
    }
    const clearCandidateGroups = () => {
      userTaskForm.candidateGroupsValue = []
      userTaskForm.candidateGroupsLabel = ''
      onHandlerTypeChange()
    }

    const openRoleLeaderDialog = () => { dialogs.roleLeader.visible = true }
    const onRoleLeaderConfirm = (code) => {
      userTaskForm.roleLeaderCode = code || ''
      userTaskForm.roleLeaderLabel = resolveRoleLabel(code)
      onHandlerTypeChange()
    }
    const clearRoleLeader = () => {
      userTaskForm.roleLeaderCode = ''
      userTaskForm.roleLeaderLabel = ''
      onHandlerTypeChange()
    }

    const openCcUsersDialog = () => { dialogs.ccUsers.visible = true }
    const onCcUsersConfirm = (usernames) => {
      const list = Array.isArray(usernames) ? usernames : []
      userTaskForm.ccUserNames = list
      userTaskForm.ccUsersLabel = list.map((u) => resolveEmployeeLabel(u)).filter(Boolean).join('、')
    }
    const clearCcUsers = () => {
      userTaskForm.ccUserNames = []
      userTaskForm.ccUsersLabel = ''
    }

    const clearSelection = () => {
      if (!modeler.value) return
      const selection = modeler.value.get('selection')
      selection.deselect()
      selectedElement.value = null
      baseForm.value = null
    }

    /**
     * 加载员工 / 角色列表 + 字典
     */
    const loadOptions = async () => {
      try {
        const [emp, role] = await Promise.all([
          listAllEmployees().catch(() => []),
          listAllRoles().catch(() => [])
        ])
        employeeOptions.value = Array.isArray(emp) ? emp : []
        roleOptions.value = Array.isArray(role) ? role : []
      } catch (e) {
        console.error('加载选项数据失败', e)
      }
      // 字典：handlerType 从后端拉取（用户希望遵循业务字典）
      try {
        const dictMap = await getDictDataByCodes('wf_handler_type')
        const records = dictMap?.wf_handler_type || []
        if (Array.isArray(records) && records.length > 0) {
          handlerTypeOptions.value = records.map((r) => ({
            label: r.dictLabel,
            value: r.dictValue
          }))
        }
      } catch (e) {
        console.warn('加载处理人类型字典失败，使用本地默认', e)
      }
    }

    /**
     * 提取 bpmn 中所有 UserTask 节点的处理人配置
     * （用于部署时持久化到 wf_node_handler）
     */
    const collectNodeHandlers = () => {
      if (!modeler.value) return []
      const elementRegistry = modeler.value.get('elementRegistry')
      const result = []
      elementRegistry.filter((el) => el.type === 'bpmn:UserTask').forEach((el) => {
        const bo = el.businessObject
        // 构造 handlerConfig JSON
        let handlerConfig = {}
        switch (userTaskForm.handlerType) {
          case 'ASSIGNEE':
            handlerConfig = { username: userTaskForm.assigneeValue }
            break
          case 'CANDIDATE_USERS':
            handlerConfig = { usernames: userTaskForm.candidateUsersValue }
            break
          case 'CANDIDATE_GROUPS':
            handlerConfig = { roleCodes: userTaskForm.candidateGroupsValue }
            break
          case 'FORM_FIELD':
            handlerConfig = { fieldName: userTaskForm.formFieldName }
            break
          case 'VARIABLE':
            handlerConfig = { varName: userTaskForm.varName }
            break
          case 'DEPT_LEADER':
            handlerConfig = { scope: userTaskForm.deptLeaderScope }
            break
          case 'ROLE_LEADER':
            handlerConfig = { roleCode: userTaskForm.roleLeaderCode }
            break
          case 'INITIATOR':
          case 'PREV_ASSIGNEE':
            handlerConfig = {}
            break
        }
        // 多实例配置
        const multiInstanceConfig = userTaskForm.multiInstanceType === 'ratio'
          ? { type: 'ratio', ratio: userTaskForm.multiInstanceRatio }
          : { type: userTaskForm.multiInstanceType }
        // 抄送配置
        const ccConfig = {
          triggerOn: userTaskForm.ccTriggerOn,
          usernames: userTaskForm.ccUserNames,
          names: userTaskForm.ccNames
        }
        result.push({
          nodeId: el.id,
          nodeName: bo.name || '',
          handlerType: userTaskForm.handlerType,
          handlerConfig: JSON.stringify(handlerConfig),
          multiInstanceType: userTaskForm.multiInstanceType,
          multiInstanceConfig: JSON.stringify(multiInstanceConfig),
          ccConfig: JSON.stringify(ccConfig)
        })
      })
      return result
    }

    /**
     * 暴露给父组件：部署前调用，持久化节点处理人配置
     */
    const saveAllNodeHandlers = async (processDefinitionId, processDefinitionKey) => {
      const handlers = collectNodeHandlers()
      if (!handlers.length) return
      await saveBatchNodeHandler(processDefinitionId, processDefinitionKey, handlers)
    }

    const importBpmn = async (xml) => {
      if (!modeler.value) return
      if (importing) {
        pendingXml = xml
        return
      }
      importing = true

      let bpmnXml = xml
      if (!bpmnXml) {
        bpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             id="Definitions_1"
             targetNamespace="http://bpmn.io/schema/bpmn">
  <process id="Process_1" isExecutable="true">
    <startEvent id="StartEvent_1" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`
      }

      try {
        await modeler.value.importXML(bpmnXml)
        const cv = modeler.value.get('canvas')
        cv.zoom('fit-viewport')
        // 解析 process key
        const processMatch = bpmnXml.match(/<bpmn:process[^>]*\bid="([^"]+)"/)
        if (processMatch) {
          emit('process-key-change', processMatch[1])
        }
      } catch (error) {
        console.error('导入BPMN失败', error)
      } finally {
        importing = false
        if (pendingXml !== null && pendingXml !== xml) {
          const next = pendingXml
          pendingXml = null
          importBpmn(next)
        } else {
          pendingXml = null
        }
      }
    }

    const save = async () => {
      try {
        const result = await modeler.value.saveXML({ format: true })
        emit('save', result.xml)
      } catch (error) {
        console.error('保存失败', error)
      }
    }

    const exportXml = async () => {
      try {
        const result = await modeler.value.saveXML({ format: true })
        const blob = new Blob([result.xml], { type: 'text/xml' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = 'process.bpmn'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('导出失败', error)
      }
    }

    const importXml = () => { fileInput.value.click() }
    const handleFileImport = (event) => {
      const file = event.target.files[0]
      if (!file) return
      const reader = new FileReader()
      reader.onload = async (e) => {
        await importBpmn(e.target.result)
      }
      reader.readAsText(file)
    }

    const zoomIn = () => { modeler.value.get('canvas').zoom(modeler.value.get('canvas').zoom() * 1.1) }
    const zoomOut = () => { modeler.value.get('canvas').zoom(modeler.value.get('canvas').zoom() * 0.9) }
    const zoomReset = () => { modeler.value.get('canvas').zoom('fit-viewport') }

    watch(() => props.xml, (newXml) => {
      if (newXml) importBpmn(newXml)
    })

    onMounted(() => {
      initModeler()
      loadOptions()
    })

    onBeforeUnmount(() => {
      if (modeler.value) {
        modeler.value.destroy()
        modeler.value = null
      }
    })

    return {
      canvas,
      fileInput,
      modeler,
      baseForm,
      userTaskForm,
      dialogs,
      employeeOptions,
      roleOptions,
      employeeColumns,
      roleColumns,
      handlerTypeOptions,
      showPanel,
      panelTitle,
      isUserTask,
      save,
      exportXml,
      importXml,
      handleFileImport,
      zoomIn,
      zoomOut,
      zoomReset,
      onBasePropChange,
      onUserTaskPropChange,
      onHandlerTypeChange,
      applyMultiInstance,
      openAssigneeDialog,
      onAssigneeConfirm,
      clearAssignee,
      openCandidateUsersDialog,
      onCandidateUsersConfirm,
      clearCandidateUsers,
      openCandidateGroupsDialog,
      onCandidateGroupsConfirm,
      clearCandidateGroups,
      openRoleLeaderDialog,
      onRoleLeaderConfirm,
      clearRoleLeader,
      openCcUsersDialog,
      onCcUsersConfirm,
      clearCcUsers,
      clearSelection,
      saveAllNodeHandlers
    }
  }
}
</script>

<style scoped>
.bpmn-designer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.content {
  flex: 1;
  display: flex;
  min-height: 0;
}

.canvas {
  flex: 1;
  min-width: 0;
  background: #f5f5f5;
}

.property-panel {
  width: 360px;
  border-left: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

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
