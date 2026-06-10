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

          <!-- UserTask 专属：处理人 / 表单 -->
          <template v-if="isUserTask">
            <el-divider>环节处理人</el-divider>
            <el-form label-position="top" size="small">
              <el-form-item label="Assignee（单人）">
                <div class="picker-row">
                  <el-input
                    v-model="userTaskForm.assigneeLabel"
                    placeholder="点击右侧按钮选择"
                    readonly
                  />
                  <el-button type="primary" size="small" @click="openAssigneeDialog">
                    选择
                  </el-button>
                  <el-button
                    v-if="userTaskForm.assigneeLabel"
                    size="small"
                    @click="clearAssignee"
                  >
                    清空
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="Candidate Users（多人）">
                <div class="picker-row">
                  <el-input
                    v-model="userTaskForm.candidateUsersLabel"
                    placeholder="点击右侧按钮选择"
                    readonly
                  />
                  <el-button type="primary" size="small" @click="openCandidateUsersDialog">
                    选择
                  </el-button>
                  <el-button
                    v-if="userTaskForm.candidateUsersLabel"
                    size="small"
                    @click="clearCandidateUsers"
                  >
                    清空
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="Candidate Groups（候选组）">
                <div class="picker-row">
                  <el-input
                    v-model="userTaskForm.candidateGroupsLabel"
                    placeholder="点击右侧按钮选择"
                    readonly
                  />
                  <el-button type="primary" size="small" @click="openCandidateGroupsDialog">
                    选择
                  </el-button>
                  <el-button
                    v-if="userTaskForm.candidateGroupsLabel"
                    size="small"
                    @click="clearCandidateGroups"
                  >
                    清空
                  </el-button>
                </div>
              </el-form-item>

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
      title="选择 Candidate Users（多选）"
      :options="employeeOptions"
      :columns="employeeColumns"
      value-key="username"
      :multiple="true"
      :selected="userTaskForm.candidateUsersValue"
      @confirm="onCandidateUsersConfirm"
    />

    <!-- 选组弹窗 -->
    <SelectorDialog
      v-model="dialogs.candidateGroups.visible"
      title="选择 Candidate Groups（多选）"
      :options="roleOptions"
      :columns="roleColumns"
      value-key="roleCode"
      :multiple="true"
      :selected="userTaskForm.candidateGroupsValue"
      @confirm="onCandidateGroupsConfirm"
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
// 注册 camunda moddle，让 bpmn-js 识别 camunda:assignee / camunda:candidateUsers 等
// Flowable 7 完全兼容 camunda 命名空间，运行时按 camunda 属性解析处理人
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json'
import SelectorDialog from './SelectorDialog.vue'
import { listAllEmployees } from '@/api/system/employee'
import { listAllRoles } from '@/api/system/role'

export default {
  name: 'BpmnDesigner',
  components: { SelectorDialog },
  props: {
    xml: {
      type: String,
      default: ''
    }
  },
  emits: ['save', 'change'],
  setup(props, { emit }) {
    const canvas = ref(null)
    const fileInput = ref(null)
    const modeler = ref(null)

    // 当前选中的 bpmn 元素
    const selectedElement = ref(null)
    // 节点基础表单
    const baseForm = ref(null)
    // UserTask 专属表单
    const userTaskForm = reactive({
      // 显示用
      assigneeLabel: '',
      candidateUsersLabel: '',
      candidateGroupsLabel: '',
      // 实际值（username 字符串 / username 数组 / roleCode 数组）
      assigneeValue: '',
      candidateUsersValue: [],
      candidateGroupsValue: [],
      // 其它字段
      formKey: '',
      dueDate: '',
      priority: 50
    })

    // 弹窗可见性
    const dialogs = reactive({
      assignee: { visible: false },
      candidateUsers: { visible: false },
      candidateGroups: { visible: false }
    })

    // 选项数据
    const employeeOptions = ref([])
    const roleOptions = ref([])

    // 列配置
    const employeeColumns = [
      { prop: 'employeeName', label: '姓名', width: 100 },
      { prop: 'username', label: '登录账号', width: 120 },
      { prop: 'employeeCode', label: '工号', width: 120 }
    ]
    const roleColumns = [
      { prop: 'roleName', label: '角色名称', width: 140 },
      { prop: 'roleCode', label: '角色编码', width: 160 }
    ]

    // 是否显示侧栏
    const showPanel = computed(() => !!selectedElement.value)
    // 面板标题
    const panelTitle = computed(() => {
      if (!selectedElement.value) return '属性'
      const bo = selectedElement.value.businessObject
      const type = bo.$type || 'Element'
      // bpmn 命名空间前缀统一去掉
      return `${type} 属性`
    })
    // 当前选中是否为 UserTask
    const isUserTask = computed(() => {
      if (!selectedElement.value) return false
      return selectedElement.value.type === 'bpmn:UserTask'
    })

    // 自定义中文模块
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

    // 导入中锁：避免 onMounted 的 importBpmn 与 props.xml 触发的 watch importBpmn 并发执行
    // 并发时 bpmn-js 内部按 rootElement id 索引的缓存会被破坏，抛 "root-0" 错误
    let importing = false
    let pendingXml = null

    // 初始化 BpmnModeler
    const initModeler = () => {
      modeler.value = new BpmnModeler({
        container: canvas.value,
        keyboard: { bindTo: window },
        additionalModules: [customTranslate],
        // 注册 camunda moddle 后，bo.assignee / bo.candidateUsers 等即可正常读写
        moddleExtensions: { camunda: camundaModdleDescriptor }
      })

      // 监听 commandStack，序列化变更并向父组件发送最新 XML
      modeler.value.on('commandStack.changed', () => {
        modeler.value.saveXML({ format: true }).then((result) => {
          emit('change', result.xml)
        })
      })

      // 监听选中变化
      const eventBus = modeler.value.get('eventBus')
      eventBus.on('selection.changed', (e) => {
        const sel = (e.newSelection && e.newSelection[0]) || null
        handleElementSelected(sel)
      })
      // 监听元素属性变化（外部修改后回填表单）
      eventBus.on('element.changed', (e) => {
        if (selectedElement.value && e.element && e.element.id === selectedElement.value.id) {
          fillFormFromElement(e.element)
        }
      })

      importBpmn(props.xml)
    }

    /**
     * 选中节点后填充表单
     * @param {Object} element bpmn-js 元素
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
        const assignee = bo.assignee || ''
        const candidateUsersStr = bo.candidateUsers || ''
        const candidateGroupsStr = bo.candidateGroups || ''

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

        // 反查 label
        userTaskForm.assigneeLabel = resolveEmployeeLabel(assignee)
        userTaskForm.candidateUsersLabel = userTaskForm.candidateUsersValue
          .map((u) => resolveEmployeeLabel(u))
          .filter(Boolean)
          .join('、')
        userTaskForm.candidateGroupsLabel = userTaskForm.candidateGroupsValue
          .map((g) => resolveRoleLabel(g))
          .filter(Boolean)
          .join('、')
      }
    }

    /** 通过 username 反查员工姓名 */
    const resolveEmployeeLabel = (username) => {
      if (!username) return ''
      const e = employeeOptions.value.find((o) => o.username === username)
      if (!e) return username
      return e.employeeName ? `${e.employeeName}（${username}）` : username
    }
    /** 通过 roleCode 反查角色名 */
    const resolveRoleLabel = (code) => {
      if (!code) return ''
      const r = roleOptions.value.find((o) => o.roleCode === code)
      if (!r) return code
      return r.roleName ? `${r.roleName}（${code}）` : code
    }

    // 通用：通过 modeling 服务更新属性
    const updateElementProperty = (key, value) => {
      if (!selectedElement.value || !modeler.value) return
      const modeling = modeler.value.get('modeling')
      modeling.updateProperties(selectedElement.value, { [key]: value })
    }

    // 基础属性变更（name）
    const onBasePropChange = (key, value) => {
      updateElementProperty(key, value)
    }

    // UserTask 字段变更（formKey / dueDate / priority）
    const onUserTaskPropChange = (key, value) => {
      updateElementProperty(key, value)
    }

    /** 打开选 Assignee 弹窗（单选） */
    const openAssigneeDialog = () => {
      dialogs.assignee.visible = true
    }
    const onAssigneeConfirm = (username) => {
      // 写入 bo.assignee（moddle 序列化时会自动加上 camunda: 前缀）
      updateElementProperty('assignee', username || '')
      userTaskForm.assigneeValue = username || ''
      userTaskForm.assigneeLabel = resolveEmployeeLabel(username)
    }
    const clearAssignee = () => {
      updateElementProperty('assignee', '')
      userTaskForm.assigneeValue = ''
      userTaskForm.assigneeLabel = ''
    }

    /** 打开选 Candidate Users 弹窗（多选） */
    const openCandidateUsersDialog = () => {
      dialogs.candidateUsers.visible = true
    }
    const onCandidateUsersConfirm = (usernames) => {
      const list = Array.isArray(usernames) ? usernames : []
      // camunda moddle 中 candidateUsers 是 String（逗号分隔）
      updateElementProperty('candidateUsers', list.join(','))
      userTaskForm.candidateUsersValue = list
      userTaskForm.candidateUsersLabel = list.map((u) => resolveEmployeeLabel(u)).filter(Boolean).join('、')
    }
    const clearCandidateUsers = () => {
      updateElementProperty('candidateUsers', '')
      userTaskForm.candidateUsersValue = []
      userTaskForm.candidateUsersLabel = ''
    }

    /** 打开选 Candidate Groups 弹窗（多选） */
    const openCandidateGroupsDialog = () => {
      dialogs.candidateGroups.visible = true
    }
    const onCandidateGroupsConfirm = (codes) => {
      const list = Array.isArray(codes) ? codes : []
      updateElementProperty('candidateGroups', list.join(','))
      userTaskForm.candidateGroupsValue = list
      userTaskForm.candidateGroupsLabel = list.map((g) => resolveRoleLabel(g)).filter(Boolean).join('、')
    }
    const clearCandidateGroups = () => {
      updateElementProperty('candidateGroups', '')
      userTaskForm.candidateGroupsValue = []
      userTaskForm.candidateGroupsLabel = ''
    }

    /** 清空当前选择 */
    const clearSelection = () => {
      if (!modeler.value) return
      const selection = modeler.value.get('selection')
      selection.deselect()
      selectedElement.value = null
      baseForm.value = null
    }

    /** 加载后端全量数据 */
    const loadOptions = async () => {
      try {
        // 并行拉取，失败不阻塞画布渲染
        const [emp, role] = await Promise.all([
          listAllEmployees().catch((e) => {
            console.error('加载员工列表失败', e)
            ElMessage.warning('加载员工列表失败，请稍后重试')
            return []
          }),
          listAllRoles().catch((e) => {
            console.error('加载角色列表失败', e)
            ElMessage.warning('加载角色列表失败，请稍后重试')
            return []
          })
        ])
        employeeOptions.value = Array.isArray(emp) ? emp : []
        roleOptions.value = Array.isArray(role) ? role : []
      } catch (e) {
        console.error('加载选项数据失败', e)
      }
    }

    // 导入BPMN（带锁，避免并发 importXML 破坏 bpmn-js 内部状态）
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

    // 保存
    const save = async () => {
      try {
        const result = await modeler.value.saveXML({ format: true })
        emit('save', result.xml)
      } catch (error) {
        console.error('保存失败', error)
      }
    }

    // 导出XML
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

    // 导入XML
    const importXml = () => {
      fileInput.value.click()
    }
    const handleFileImport = (event) => {
      const file = event.target.files[0]
      if (!file) return
      const reader = new FileReader()
      reader.onload = async (e) => {
        await importBpmn(e.target.result)
      }
      reader.readAsText(file)
    }

    // 缩放
    const zoomIn = () => {
      const cv = modeler.value.get('canvas')
      cv.zoom(cv.zoom() * 1.1)
    }
    const zoomOut = () => {
      const cv = modeler.value.get('canvas')
      cv.zoom(cv.zoom() * 0.9)
    }
    const zoomReset = () => {
      const cv = modeler.value.get('canvas')
      cv.zoom('fit-viewport')
    }

    // 监听 xml prop 变化
    watch(() => props.xml, (newXml) => {
      if (newXml) {
        importBpmn(newXml)
      }
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
      // 状态
      baseForm,
      userTaskForm,
      dialogs,
      employeeOptions,
      roleOptions,
      employeeColumns,
      roleColumns,
      showPanel,
      panelTitle,
      isUserTask,
      // 方法
      save,
      exportXml,
      importXml,
      handleFileImport,
      zoomIn,
      zoomOut,
      zoomReset,
      onBasePropChange,
      onUserTaskPropChange,
      openAssigneeDialog,
      onAssigneeConfirm,
      clearAssignee,
      openCandidateUsersDialog,
      onCandidateUsersConfirm,
      clearCandidateUsers,
      openCandidateGroupsDialog,
      onCandidateGroupsConfirm,
      clearCandidateGroups,
      clearSelection
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
  width: 340px;
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

:deep(.el-divider) {
  margin: 18px 0 12px;
}

:deep(.el-divider .el-divider__text) {
  font-weight: 600;
  color: #1f2937;
}
</style>
