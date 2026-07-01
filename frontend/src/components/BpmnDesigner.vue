<template>
  <div class="bpmn-designer">
    <div class="toolbar">
      <el-button size="small" @click="save">保存</el-button>
      <el-button size="small" @click="exportXml">导出XML</el-button>
      <el-button size="small" @click="exportSvg">导出SVG</el-button>
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
      <el-button size="small" type="primary" plain @click="aiGenerate">AI 辅助生成</el-button>
    </div>
    <div class="content">
      <div class="canvas" ref="canvas"></div>

      <!-- 属性面板：分发到 UserTaskPanel / SequenceFlowPanel / 后续扩展 -->
      <PropertyPanel
        :selected-element="selectedElement"
        :base-form="baseForm"
        :user-task-form="userTaskForm"
        :handler-type-options="handlerTypeOptions"
        :employee-options="employeeOptions"
        :role-options="roleOptions"
        :employee-columns="employeeColumns"
        :role-columns="roleColumns"
        @clear-selection="clearSelection"
        @update-base-prop="onBasePropChange"
        @update-element-prop="updateElementProp"
        @multi-instance-change="applyMultiInstance"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import chineseTranslation from 'bpmn-js-i18n/translations/zn'
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json'
import CustomPalette from './bpmn/CustomPalette' // 自定义 Palette：补齐 UserTask / 各网关 / 各事件 / 文本注释
import CustomContextPad from './bpmn/CustomContextPad' // 自定义 ContextPad：节点上加 业务按钮（处理人/事件/CallActivity/边界）
import PropertyPanel from './bpmn/PropertyPanel.vue' // 右侧属性面板分发器
import { listAllEmployees } from '@/api/system/employee'
import { listAllRoles } from '@/api/system/role'
import { getDictDataByCodes } from '@/api/dict'
import { saveBatchNodeHandler } from '@/api/workflow/nodeHandler'

/**
 * 流程设计器容器
 * - 负责 BpmnModeler 生命周期（init / import / save / export / zoom）
 * - 负责元素选中事件（selection.changed / element.changed）
 * - 把元素属性写入和回读委托给 PropertyPanel + 子面板
 * - 部署前由父组件通过 ref 调用 saveAllNodeHandlers 持久化节点处理人配置
 */
export default {
  name: 'BpmnDesigner',
  components: { PropertyPanel },
  props: {
    xml: { type: String, default: '' },
    /** 流程定义 ID（编辑已有流程时传入） */
    processDefinitionId: { type: String, default: '' },
    /** 流程定义 KEY（部署后从 BPMN 解析得到） */
    processDefinitionKey: { type: String, default: '' }
  },
  emits: ['save', 'change', 'process-key-change', 'context-pad-action'],
  setup(props, { emit }) {
    const canvas = ref(null)
    const fileInput = ref(null)
    const modeler = ref(null)

    const selectedElement = ref(null)
    const baseForm = ref(null)

    // UserTask 表单状态：与 UserTaskPanel 共享（reactive 对象）
    const userTaskForm = reactive({
      handlerType: 'ASSIGNEE',
      assigneeValue: '',
      assigneeLabel: '',
      candidateUsersValue: [],
      candidateUsersLabel: '',
      candidateGroupsValue: [],
      candidateGroupsLabel: '',
      deptLeaderScope: 'current',
      roleLeaderCode: '',
      roleLeaderLabel: '',
      formFieldName: '',
      varName: '',
      multiInstanceType: 'none',
      multiInstanceRatio: 60,
      ccTriggerOn: 'node_end',
      ccUserNames: [],
      ccUsersLabel: '',
      ccNames: '',
      formKey: '',
      dueDate: '',
      priority: 50
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

    // 【踩坑】bpmn-js/didi 期望 additionalModules 每一项是对象
    const customPaletteModule = {
      __init__: ['customPaletteProvider'],
      customPaletteProvider: ['type', CustomPalette]
    }
    const customContextPadModule = {
      __init__: ['customContextPadProvider'],
      customContextPadProvider: ['type', CustomContextPad]
    }

    const initModeler = () => {
      modeler.value = new BpmnModeler({
        container: canvas.value,
        keyboard: { bindTo: window },
        additionalModules: [customTranslate, customPaletteModule],
        moddleExtensions: { camunda: camundaModdleDescriptor }
      })

      if (import.meta.env.DEV) {
        window.__bpmnModeler = modeler.value
        console.log('[BpmnDesigner] modeler initialized, __bpmnModeler exposed (DEV only)')
      }

      // P2-1：监听 ContextPad 自定义事件
      modeler.value.on('bpmn.contextPad.action', (e) => {
        emit('context-pad-action', e)
      })

      modeler.value.on('commandStack.changed', () => {
        modeler.value.saveXML({ format: true }).then((result) => {
          emit('change', result.xml)
          try {
            const processMatch = result.xml.match(/<bpmn:process[^>]*\bid="([^"]+)"/)
            if (processMatch) {
              emit('process-key-change', processMatch[1])
            }
          } catch (e) { /* ignore */ }
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

    /** 选中元素后填充基础表单（UserTask 字段由 UserTaskPanel 自行处理） */
    const handleElementSelected = (element) => {
      selectedElement.value = element
      if (!element) {
        baseForm.value = null
        return
      }
      fillFormFromElement(element)
    }

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

        const loop = bo.loopCharacteristics
        if (loop) {
          userTaskForm.multiInstanceType = loop.isSequential ? 'sequential' : 'countersign'
        } else {
          userTaskForm.multiInstanceType = 'none'
        }

        // 重新解析 label（从 employeeOptions / roleOptions）
        const resolveEmp = (u) => {
          if (!u) return ''
          const e = employeeOptions.value.find((o) => o.username === u)
          return e ? (e.employeeName ? `${e.employeeName}（${u}）` : u) : u
        }
        const resolveRole = (c) => {
          if (!c) return ''
          const r = roleOptions.value.find((o) => o.roleCode === c)
          return r ? (r.roleName ? `${r.roleName}（${c}）` : c) : c
        }
        userTaskForm.assigneeLabel = resolveEmp(assignee)
        userTaskForm.candidateUsersLabel = userTaskForm.candidateUsersValue.map(resolveEmp).filter(Boolean).join('、')
        userTaskForm.candidateGroupsLabel = userTaskForm.candidateGroupsValue.map(resolveRole).filter(Boolean).join('、')
      }
    }

    /**
     * 统一写入 bpmn 元素属性的入口
     * - 普通属性：直接透传 modeling.updateProperties
     * - conditionExpression（流转线条件）：构造 moddle 表达式
     * - loopCharacteristics（多实例）：构造 MultiInstanceLoopCharacteristics
     */
    const updateElementProp = (key, value) => {
      if (!selectedElement.value || !modeler.value) return
      const modeling = modeler.value.get('modeling')
      const moddle = modeler.value.get('moddle')

      // 处理 SequenceFlowPanel 传入的特殊 key
      if (key === '__conditionExpressionBody__') {
        if (!value) {
          modeling.updateProperties(selectedElement.value, { conditionExpression: undefined })
          return
        }
        const expr = moddle.create('bpmn:FormalExpression', { body: value })
        modeling.updateProperties(selectedElement.value, { conditionExpression: expr })
        return
      }
      if (key === '__conditionScript__') {
        if (!value || !value.body) {
          modeling.updateProperties(selectedElement.value, { conditionExpression: undefined })
          return
        }
        const expr = moddle.create('bpmn:FormalExpression', {
          body: value.body,
          language: value.language || 'groovy'
        })
        modeling.updateProperties(selectedElement.value, { conditionExpression: expr })
        return
      }

      modeling.updateProperties(selectedElement.value, { [key]: value })
    }

    const onBasePropChange = (key, value) => updateElementProp(key, value)

    /**
     * 多实例会签 / 或签 / 顺序 / 比例
     * 根据 userTaskForm.multiInstanceType 生成 loopCharacteristics
     */
    const applyMultiInstance = () => {
      if (!selectedElement.value || selectedElement.value.type !== 'bpmn:UserTask' || !modeler.value) return
      const modeling = modeler.value.get('modeling')
      const moddle = modeler.value.get('moddle')
      const bo = selectedElement.value.businessObject
      const t = userTaskForm.multiInstanceType

      if (t === 'none') {
        if (bo.loopCharacteristics) {
          modeling.updateProperties(selectedElement.value, { loopCharacteristics: undefined })
        }
        return
      }

      const isSequential = t === 'sequential'
      const props = {
        isSequential,
        'camunda:collection': '${multiInstanceUsers}',
        'camunda:elementVariable': 'assignee'
      }
      if (t === 'ratio') {
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

    const clearSelection = () => {
      if (!modeler.value) return
      modeler.value.get('selection').deselect()
      selectedElement.value = null
      baseForm.value = null
    }

    /** 加载员工 / 角色 / 字典选项 */
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
     * （部署时持久化到 wf_node_handler）
     */
    const collectNodeHandlers = () => {
      if (!modeler.value) return []
      const elementRegistry = modeler.value.get('elementRegistry')
      const result = []
      elementRegistry.filter((el) => el.type === 'bpmn:UserTask').forEach((el) => {
        const bo = el.businessObject
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
        const multiInstanceConfig = userTaskForm.multiInstanceType === 'ratio'
          ? { type: 'ratio', ratio: userTaskForm.multiInstanceRatio }
          : { type: userTaskForm.multiInstanceType }
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

    /** 暴露给父组件：部署前调用 */
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
        modeler.value.get('canvas').zoom('fit-viewport')
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

    // P3-1 SVG 导出
    const exportSvg = async () => {
      try {
        const { svg } = await modeler.value.saveSVG({ format: true })
        const blob = new Blob([svg], { type: 'image/svg+xml' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = 'process.svg'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('导出 SVG 失败', error)
      }
    }

    // P3-1 AI 辅助生成（P3-1 占位）
    const aiGenerate = () => {
      ElMessageBox.prompt(
        '请用自然语言描述要生成的流程，例如"员工请假申请：直属上级审批 → HR 备案"。\n（功能开发中，本期先记录需求，后续接入 LLM 一次性生成 BPMN XML。）',
        'AI 辅助生成流程',
        {
          confirmButtonText: '生成',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入流程描述...',
          inputType: 'textarea'
        }
      ).then(({ value }) => {
        if (!value || !value.trim()) return
        ElMessage.warning('AI 流程生成功能开发中，需求已记录：' + value.slice(0, 30) + (value.length > 30 ? '...' : ''))
        // TODO: 接入 LLM 后此处调用 aiGenerateProcess(value) 并把返回的 XML 加载到 modeler
      }).catch(() => {})
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
      selectedElement,
      baseForm,
      userTaskForm,
      employeeOptions,
      roleOptions,
      employeeColumns,
      roleColumns,
      handlerTypeOptions,
      save,
      exportXml,
      exportSvg,
      aiGenerate,
      importXml,
      handleFileImport,
      zoomIn,
      zoomOut,
      zoomReset,
      onBasePropChange,
      updateElementProp,
      applyMultiInstance,
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
</style>
