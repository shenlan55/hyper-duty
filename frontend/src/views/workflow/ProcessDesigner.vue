<template>
  <MobileGuard>
  <div class="process-designer">
    <el-page-header @back="goBack" :title="isEditing ? '编辑流程' : '流程设计器'">
      <template #extra>
        <el-button @click="goTemplateMarket">模板市场</el-button>
        <el-button type="primary" @click="handleDeployProcess">{{ isEditing ? '更新部署' : '部署流程' }}</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4" style="height: calc(100vh - 160px)">
      <template #header>
        <div class="card-header">
          <span>流程设计</span>
          <el-form inline size="small">
            <el-form-item label="流程名称">
              <el-input v-model="processName" placeholder="请输入流程名称" :disabled="isEditing" />
            </el-form-item>
          </el-form>
        </div>
      </template>
      <!-- 始终挂载 BpmnDesigner，避免先卸载后重挂时旧实例的 importXML 残留访问已销毁 canvas
           loading 时显示加载层覆盖在画布之上 -->
      <div style="height: 100%; position: relative;">
        <BpmnDesigner ref="designerRef" :xml="initialXml" @save="handleSave" @change="handleXmlChange" @process-key-change="handleProcessKeyChange" @context-pad-action="handleContextPadAction" />
        <div v-if="loading" style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.7); z-index: 10;">
          <el-icon class="is-loading" style="font-size: 40px;"><loading /></el-icon>
          <span style="margin-left: 10px;">加载中...</span>
        </div>
      </div>
    </el-card>

    <!-- ====================== P2-1 ContextPad 业务对话框 ====================== -->
    <el-dialog
      v-if="contextPadDialog.visible"
      :model-value="true"
      @update:model-value="(v) => !v && (contextPadDialog.visible = false)"
      :title="`${contextPadDialog.type} - ${contextPadDialog.nodeName}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="contextPadDialog.type === 'handler'">
        <el-alert type="info" :closable="false" show-icon
          title="此处可配置节点处理人类型：发起人 / 部门负责人 / 角色负责人 / 表单字段 / 流程变量 等。保存到 wf_node_handler 表，部署后由运行时 TaskListener 解析。"
        />
        <p style="margin-top: 12px; color: #909399; font-size: 12px;">
          当前节点：{{ contextPadDialog.nodeId }} ｜ 打开"右侧属性面板"可在 BPMN 属性中配置基础信息。
          完整处理人配置面板待 P2-2 接入。
        </p>
      </div>
      <div v-else-if="contextPadDialog.type === 'multiInstance'">
        <el-alert type="info" :closable="false" show-icon
          title="多实例配置：会签（parallel）/ 或签（sequential）/ 比例通过。完整配置面板待 P2-2 接入。" />
      </div>
      <div v-else-if="contextPadDialog.type === 'cc'">
        <el-alert type="info" :closable="false" show-icon
          title="抄送配置：人员 / 角色 / 触发时机。完整配置面板待 P2-2 接入。" />
      </div>
      <div v-else-if="contextPadDialog.type === 'condition'">
        <el-alert type="info" :closable="false" show-icon
          title="流转条件：${变量 == '值'} / JUEL / SpEL。完整配置面板待 P2-2 接入。" />
      </div>
      <div v-else-if="contextPadDialog.type === 'initiator'">
        <el-alert type="info" :closable="false" show-icon
          title="发起人变量：指定使用哪个流程变量作为发起人。" />
      </div>
      <div v-else-if="contextPadDialog.type === 'eventDef'">
        <el-form label-width="100px" size="default">
          <el-form-item label="事件类型">
            <el-select v-model="eventForm.eventDefType" placeholder="请选择">
              <el-option label="消息 Message" value="message" />
              <el-option label="定时 Timer" value="timer" />
              <el-option label="信号 Signal" value="signal" />
              <el-option label="条件 Conditional" value="conditional" />
              <el-option label="错误 Error" value="error" />
              <el-option label="取消 Cancel" value="cancel" />
              <el-option label="补偿 Compensation" value="compensation" />
              <el-option label="终止 Terminate" value="terminate" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="eventForm.eventDefType === 'timer'" label="Timer 表达式">
            <el-radio-group v-model="eventForm.timerType">
              <el-radio label="timeDate">timeDate（具体时间）</el-radio>
              <el-radio label="timeDuration">timeDuration（持续时长）</el-radio>
              <el-radio label="timeCycle">timeCycle（Cron 周期）</el-radio>
            </el-radio-group>
            <el-input v-model="eventForm.timerValue" placeholder="如 2026-12-31T23:59:59 / PT1H / 0 0 12 * * ?" style="margin-top: 8px;" />
          </el-form-item>
          <el-form-item v-if="eventForm.eventDefType === 'message'" label="消息引用">
            <el-input v-model="eventForm.messageRef" placeholder="如 orderCreated / leaveMsg" />
          </el-form-item>
          <el-form-item v-if="eventForm.eventDefType === 'signal'" label="信号引用">
            <el-input v-model="eventForm.signalRef" placeholder="如 globalSignal" />
          </el-form-item>
          <el-form-item v-if="eventForm.eventDefType === 'error'" label="错误码">
            <el-input v-model="eventForm.errorCode" placeholder="如 biz-error-001" />
          </el-form-item>
          <el-form-item v-if="contextPadDialog.type === 'eventDef' && isBoundary" label="cancelActivity">
            <el-switch v-model="eventForm.cancelActivity" active-text="触发后中断原活动" inactive-text="触发后继续原活动" />
          </el-form-item>
        </el-form>
      </div>
      <div v-else-if="contextPadDialog.type === 'callActivity'">
        <el-form label-width="100px" size="default">
          <el-form-item label="被调流程 Key">
            <el-input v-model="callForm.calledElement" placeholder="如 subProcess_v1" />
          </el-form-item>
          <el-form-item label="入参映射 (in)">
            <el-input v-model="callForm.inMapping" type="textarea" :rows="2" placeholder="source=target,一行一条" />
          </el-form-item>
          <el-form-item label="回参映射 (out)">
            <el-input v-model="callForm.outMapping" type="textarea" :rows="2" placeholder="source=target,一行一条" />
          </el-form-item>
          <el-form-item label="业务键">
            <el-input v-model="callForm.businessKey" placeholder="可选,作为被调流程业务键" />
          </el-form-item>
        </el-form>
      </div>
      <div v-else-if="contextPadDialog.type === 'compensation'">
        <el-alert type="info" :closable="false" show-icon
          title="关联补偿处理：选择补偿 Boundary Event（throw compensation）" />
      </div>
      <div v-else>
        <el-empty :description="`未知 action: ${contextPadDialog.type}`" />
      </div>
      <template #footer>
        <el-button @click="contextPadDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</MobileGuard>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import BpmnDesigner from '@/components/BpmnDesigner.vue'
import MobileGuard from '@/components/MobileGuard.vue'
import { deployProcess as deployProcessApi, getProcessBpmnXml, getLatestProcessDefinition } from '@/api/workflow/process'

export default {
  name: 'ProcessDesigner',
  components: { BpmnDesigner, MobileGuard },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const designerRef = ref(null)
    // 流程名称（部署时使用的显示名，编辑模式下禁用）
    const processName = ref('')
    // 流程 Key（来自 BPMN XML 中 <bpmn:process id="...">，由 BpmnDesigner 通过 @process-key-change 事件透出）
    // Flowable 部署时强依赖该字段，部署前必须校验非空
    const processKey = ref('')
    const xmlContent = ref('')
    const initialXml = ref('')
    const isEditing = ref(false)
    const loading = ref(false)

    const goBack = () => {
      router.back()
    }

    // P3-1：跳到模板市场
    const goTemplateMarket = () => {
      router.push('/workflow/template-market')
    }

    const loadProcess = async (processDefinitionId) => {
      loading.value = true
      try {
        // request.js 响应拦截器已自动解包 ResponseResult.data
        const xml = await getProcessBpmnXml(processDefinitionId)
        if (!xml) {
          console.warn('流程XML为空，使用默认模板')
          ElMessage.warning('流程数据为空，将显示默认模板')
        }
        initialXml.value = xml || ''
        xmlContent.value = xml || ''
        isEditing.value = true
      } catch (error) {
        console.error('加载流程失败:', error)
        ElMessage.error(error.message || '加载流程失败')
      } finally {
        loading.value = false
      }
    }

    const handleSave = (xml) => {
      xmlContent.value = xml
      ElMessage.success('保存成功')
    }

    const handleXmlChange = (xml) => {
      xmlContent.value = xml
    }

    /**
     * 接收 BpmnDesigner 透出的 processKey
     * 触发时机：用户在设计器内编辑（commandStack.changed）或加载 XML 完成时
     * @param {string} key BPMN XML 中 <bpmn:process> 节点的 id 属性（即 processKey）
     */
    const handleProcessKeyChange = (key) => {
      processKey.value = key || ''
      // 用 processKey 兜底 processName：覆盖新建（忘记填名称）+ 编辑（URL 未带 processName 场景）
      // 仅当 processName 仍为空时才覆盖，避免覆盖用户已填写/URL 透传的名称
      if (!processName.value && key) {
        processName.value = key
      }
    }

    const handleDeployProcess = async () => {
      if (!processName.value) {
        ElMessage.warning('请输入流程名称')
        return
      }
      // 校验 processKey：BPMN XML 中 <bpmn:process> 节点必须包含 id（Flowable 部署时强依赖）
      if (!processKey.value) {
        ElMessage.warning('请在设计器中配置流程（流程 Key 不能为空）')
        return
      }
      if (!xmlContent.value) {
        ElMessage.warning('请设计流程')
        return
      }

      try {
        await deployProcessApi(processName.value, xmlContent.value)
        ElMessage.success('部署成功')
        router.back()
      } catch (error) {
        ElMessage.error(error.message || '部署失败')
      }
    }

    onMounted(() => {
      // P3-1：模板市场选中的模板，sessionStorage 透传
      const pendingXml = sessionStorage.getItem('__workflow_pending_xml__')
      if (pendingXml) {
        initialXml.value = pendingXml
        // 加载完清空，避免下次再误用
        sessionStorage.removeItem('__workflow_pending_xml__')
        // 模板新建不属"编辑"
        isEditing.value = false
        loading.value = false
        return
      }

      const processDefinitionId = route.query.processDefinitionId
      const processNameFromQuery = route.query.processName
      if (processNameFromQuery) {
        processName.value = processNameFromQuery
      }
      if (processDefinitionId) {
        // 如果有 processDefinitionId，则加载已有流程
        loadProcess(processDefinitionId)
      } else {
        // 没有流程 ID，直接渲染空设计器
        loading.value = false
      }
    })

    // ====================== P2-1 ContextPad 自定义事件处理 ======================
    // 接收 BpmnDesigner 透传的 bpmn.contextPad.action 事件
    const contextPadAction = ref(null) // { action, element, businessObject }
    const contextPadDialog = reactive({ visible: false, type: '', nodeId: '', nodeName: '' })
    const isBoundary = computed(() => contextPadAction.value?.businessObject?.$type === 'bpmn:BoundaryEvent')
    const eventForm = reactive({
      eventDefType: '',
      timerType: 'timeDuration',
      timerValue: 'PT1H',
      messageRef: '',
      signalRef: '',
      errorCode: '',
      cancelActivity: true
    })
    const callForm = reactive({ calledElement: '', inMapping: '', outMapping: '', businessKey: '' })
    const handleContextPadAction = (e) => {
      // 弹出对应业务对话框（由"action 名"分发）
      const action = e?.action
      if (!action) return
      const bo = e.businessObject || {}
      contextPadAction.value = e
      contextPadDialog.type = action
      contextPadDialog.nodeId = bo.id || ''
      contextPadDialog.nodeName = bo.name || bo.id || ''
      // 重置表单
      Object.assign(eventForm, {
        eventDefType: '',
        timerType: 'timeDuration',
        timerValue: 'PT1H',
        messageRef: '',
        signalRef: '',
        errorCode: '',
        cancelActivity: true
      })
      Object.assign(callForm, { calledElement: '', inMapping: '', outMapping: '', businessKey: '' })
      // 从 businessObject 回填已有配置
      try {
        if (bo.eventDefinitions && bo.eventDefinitions[0]) {
          const def = bo.eventDefinitions[0]
          if (def.$type === 'bpmn:TimerEventDefinition') {
            eventForm.eventDefType = 'timer'
            if (def.timeDate) eventForm.timerType = 'timeDate'
            else if (def.timeCycle) eventForm.timerType = 'timeCycle'
            else eventForm.timerType = 'timeDuration'
            const body = def[eventForm.timerType] && def[eventForm.timerType].body
            if (body) eventForm.timerValue = body
          } else if (def.$type === 'bpmn:MessageEventDefinition') {
            eventForm.eventDefType = 'message'
            eventForm.messageRef = def.messageRef && def.messageRef.name
          } else if (def.$type === 'bpmn:SignalEventDefinition') {
            eventForm.eventDefType = 'signal'
            eventForm.signalRef = def.signalRef && def.signalRef.name
          } else if (def.$type === 'bpmn:ErrorEventDefinition') {
            eventForm.eventDefType = 'error'
            eventForm.errorCode = def.errorRef && def.errorRef.errorCode
          }
        }
        if (bo.cancelActivity !== undefined) eventForm.cancelActivity = bo.cancelActivity
        if (bo.calledElement) callForm.calledElement = bo.calledElement
      } catch (e) { /* ignore parse error */ }
      contextPadDialog.visible = true
    }

    /**
     * 把弹窗表单写回 BPMN businessObject（通过 moddle 更新）
     * 走 bpmn-modeling 的 updateProperties 即可触发重绘并入 commandStack
     */
    const applyContextPadConfig = () => {
      const e = contextPadAction.value
      if (!e || !e.element) return
      const modeler = window.__bpmnModeler
      if (!modeler) {
        ElMessage.error('找不到 modeler 实例')
        return
      }
      const modeling = modeler.get('modeling')
      const moddle = modeler.get('moddle')
      const bpmnReplace = modeler.get('bpmnReplace')
      const element = e.element
      const bo = element.businessObject
      try {
        if (contextPadDialog.type === 'eventDef') {
          // 1. 构造新的 eventDefinition
          let eventDef = null
          if (eventForm.eventDefType === 'timer') {
            const time = moddle.create('bpmn:FormalExpression', { body: eventForm.timerValue || 'PT1H' })
            eventDef = moddle.create('bpmn:TimerEventDefinition', { [eventForm.timerType]: time })
          } else if (eventForm.eventDefType === 'message') {
            const msg = moddle.create('bpmn:Message', { id: eventForm.messageRef || 'msg', name: eventForm.messageRef })
            eventDef = moddle.create('bpmn:MessageEventDefinition', { messageRef: msg })
          } else if (eventForm.eventDefType === 'signal') {
            const sig = moddle.create('bpmn:Signal', { id: eventForm.signalRef || 'sig', name: eventForm.signalRef })
            eventDef = moddle.create('bpmn:SignalEventDefinition', { signalRef: sig })
          } else if (eventForm.eventDefType === 'error') {
            const err = moddle.create('bpmn:Error', { id: eventForm.errorCode || 'err', errorCode: eventForm.errorCode })
            eventDef = moddle.create('bpmn:ErrorEventDefinition', { errorRef: err })
          }
          const props = { eventDefinitions: eventDef ? [eventDef] : [] }
          if (isBoundary.value) props.cancelActivity = eventForm.cancelActivity
          modeling.updateProperties(element, props)
        } else if (contextPadDialog.type === 'callActivity') {
          modeling.updateProperties(element, { calledElement: callForm.calledElement })
          // 注：变量映射 in/out 在后续版本通过 camunda:In/Out 写入
        }
        ElMessage.success('已应用，BPMN XML 已更新')
        contextPadDialog.visible = false
      } catch (err) {
        console.error('[P2-2] 应用配置失败', err)
        ElMessage.error(`应用失败：${err.message || err}`)
      }
    }

    return {
      goBack,
      designerRef,
      processName,
      processKey,
      initialXml,
      isEditing,
      loading,
      handleSave,
      handleXmlChange,
      handleProcessKeyChange,
      handleDeployProcess,
      // P3-1 模板市场跳转
      goTemplateMarket,
      // P2-1 ContextPad 自定义事件
      handleContextPadAction,
      contextPadAction,
      contextPadDialog,
      // P2-2
      isBoundary,
      eventForm,
      callForm,
      applyContextPadConfig
    }
  }
}
</script>

<style scoped>
.process-designer {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
